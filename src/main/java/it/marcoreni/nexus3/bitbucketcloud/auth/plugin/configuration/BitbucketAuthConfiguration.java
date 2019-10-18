package it.marcoreni.nexus3.bitbucketcloud.auth.plugin.configuration;

import java.time.Duration;

import javax.inject.Named;

import com.google.inject.Singleton;

@Singleton
@Named
public class BitbucketAuthConfiguration {
    private static final Duration DEFAULT_PRINCIPAL_CACHE_TTL = Duration.ofMinutes(1);

    private static final String BITBUCKET_API_URL = "https://api.bitbucket.org/2.0";

    private static final String BITBUCKET_API_USER_URL = "/user";

    private static final String BITBUCKET_API_TEAMS_URL = "/user/permissions/teams";

    public String getBitbucketApiUrl() {
        return BITBUCKET_API_URL;
    }

    public String getBitbucketApiUserUrl() {
        return getBitbucketApiUrl() + BITBUCKET_API_USER_URL;
    }

    public String getBitbucketUserTeamsUrl() {
        return getBitbucketApiUrl() + BITBUCKET_API_TEAMS_URL;
    }

    public Duration getPrincipalCacheTtl() {
        return DEFAULT_PRINCIPAL_CACHE_TTL;
    }
}
