package it.marcoreni.nexus3.bitbucketcloud.auth.plugin.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import it.marcoreni.nexus3.bitbucketcloud.auth.plugin.BitbucketAuthenticationException;
import it.marcoreni.nexus3.bitbucketcloud.auth.plugin.BitbucketCloudPrincipal;
import it.marcoreni.nexus3.bitbucketcloud.auth.plugin.configuration.BitbucketAuthConfiguration;
import java.util.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
@Named("BitbucketApiClient")
public class BitbucketApiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(BitbucketApiClient.class);

    private HttpClient client;
    private BitbucketAuthConfiguration configuration;
    // Cache token lookups to reduce the load on Bitbucket User API to prevent hitting the rate limit.
    private Cache<String, BitbucketCloudPrincipal> tokenToPrincipalCache;

    public BitbucketApiClient(HttpClient client, BitbucketAuthConfiguration configuration) {
        this.client = client;
        this.configuration = configuration;
        initPrincipalCache();
    }

    @Inject
    public BitbucketApiClient(BitbucketAuthConfiguration configuration) {
        this.configuration = configuration;
        init();
    }

    public void init() {
        LOGGER.info("Initializing BitBucket API client");
        client = HttpClientBuilder.create().build();
        initPrincipalCache();
    }

    private void initPrincipalCache() {
        tokenToPrincipalCache = CacheBuilder.newBuilder()
            .expireAfterWrite(configuration.getPrincipalCacheTtl().toMillis(), TimeUnit.MILLISECONDS)
            .build();
    }

    public BitbucketCloudPrincipal authz(String login, char[] password) throws BitbucketAuthenticationException {
        // Combine the login and the password as the cache key since they are both used to generate the principal.
        // If either changes we should obtain a new principal.
        String cacheKey = login + "|" + new String(password);
        BitbucketCloudPrincipal cached = (tokenToPrincipalCache != null) ? tokenToPrincipalCache.getIfPresent(cacheKey) : null;
        if (cached != null) {
            LOGGER.info("Using cached principal for login: '{}'", login);
            return cached;
        } else {
            LOGGER.info("Doing BitBucket API authentication for login: '{}'", login);
            BitbucketCloudPrincipal principal = doAuthz(login, password);
            tokenToPrincipalCache.put(cacheKey, principal);
            return principal;
        }
    }

    private BitbucketCloudPrincipal doAuthz(String loginName, char[] password) throws BitbucketAuthenticationException {
        BitbucketCloudPrincipal principal = new BitbucketCloudPrincipal();
        principal.setUsername(retrieveBitbucketUser(loginName, password).getUsername());
        principal.setTeams(retrieveBitbucketTeams(loginName, password));
        return principal;
    }

    private BitbucketUser retrieveBitbucketUser(String loginName, char[] password) throws BitbucketAuthenticationException {
        return getAndSerializeObject(configuration.getBitbucketApiUserUrl(), loginName, password, BitbucketUser.class);
    }

    private Set<String> retrieveBitbucketTeams(String loginName, char[] password) throws BitbucketAuthenticationException  {
        try {
            BitbucketTeamsResponse response = getAndSerializeObject(configuration.getBitbucketUserTeamsUrl(), loginName, password, BitbucketTeamsResponse.class);
            return response.getValues().stream().map(team -> team.getTeam().getUsername() + ":" + team.getPermission()).collect(Collectors.toSet());
        } catch (Exception e) {
            throw new BitbucketAuthenticationException(e);
        }
    }

    private BasicHeader constructBitbucketAuthorizationHeader(String username, char[] password) {
        String encoding = Base64.getEncoder().encodeToString((username + ":" + new String(password)).getBytes());
        return new BasicHeader("Authorization", "Basic " + encoding);
    }

    private String executeGet(String uri, String username, char[] password) throws BitbucketAuthenticationException {
        HttpGet request = new HttpGet(uri);
        request.addHeader(constructBitbucketAuthorizationHeader(username, password));
        try {
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                LOGGER.warn("Authentication failed, status code was: {}", response.getStatusLine().getStatusCode());
                request.releaseConnection();
                throw new BitbucketAuthenticationException("Authentication failed.");
            }
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (IOException e) {
            request.releaseConnection();
            throw new BitbucketAuthenticationException(e);
        }
    }

    private <T> T getAndSerializeObject(String uri, String username, char[] password, Class<T> clazz) throws BitbucketAuthenticationException {
        try {
            String reader = executeGet(uri, username, password);
            LOGGER.debug("getAndSerializeObject: {} => {}", uri, reader);
            return new Gson().fromJson(reader, clazz);
        } catch (Exception e) {
            throw new BitbucketAuthenticationException(e);
        }
    }
}