/*
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package it.marcoreni.nexus3.bitbucketcloud.auth.plugin;

import it.marcoreni.nexus3.bitbucketcloud.auth.plugin.api.BitbucketApiClient;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.eclipse.sisu.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * The Class BitbucketAuthenticatingRealm.
 */
@Singleton
@Named
@Description("Bitbucket Cloud Authentication Realm")
public class BitbucketCloudAuthenticatingRealm extends AuthorizingRealm {
	private BitbucketApiClient githubClient;

	private static final Logger LOGGER = LoggerFactory.getLogger(BitbucketCloudAuthenticatingRealm.class);
	public static final String NAME = BitbucketCloudAuthenticatingRealm.class.getName();

	@Inject
	public BitbucketCloudAuthenticatingRealm(BitbucketApiClient githubClient) {
		this.githubClient = githubClient;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.shiro.realm.CachingRealm#getName()
	 */
	@Override
	public String getName() {
		return NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.shiro.realm.AuthorizingRealm#onInit()
	 */
	@Override
	protected void onInit() {
		super.onInit();
		LOGGER.info("Bitbucket Authentication Realm initialized");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.shiro.realm.AuthorizingRealm#doGetAuthorizationInfo(org.apache
	 * .shiro.subject.PrincipalCollection)
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		BitbucketCloudPrincipal user = (BitbucketCloudPrincipal) principals.getPrimaryPrincipal();
		LOGGER.info("doGetAuthorizationInfo for user {} with roles {}", user.getUsername(), String.join(",", user.getTeams()));
		return new SimpleAuthorizationInfo(user.getTeams());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.shiro.realm.AuthenticatingRealm#doGetAuthenticationInfo(org.
	 * apache.shiro.authc.AuthenticationToken)
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		if (!(token instanceof UsernamePasswordToken)) {
			throw new UnsupportedTokenException(String.format("Token of type %s  is not supported. A %s is required.",
					token.getClass().getName(), UsernamePasswordToken.class.getName()));
		}

		UsernamePasswordToken t = (UsernamePasswordToken) token;
		LOGGER.info("doGetAuthenticationInfo for {}", ((UsernamePasswordToken) token).getUsername());
		BitbucketCloudPrincipal authenticatedPrincipal;
		try {
			authenticatedPrincipal = githubClient.authz(t.getUsername(), t.getPassword());
			LOGGER.info("Successfully authenticated {}",t.getUsername());
		} catch (BitbucketAuthenticationException e) {
			LOGGER.warn("Failed authentication", e);
			return null;
		}

		return createSimpleAuthInfo(authenticatedPrincipal, t);
	}

	/**
	 * Creates the simple auth info.
	 *
	 * @param token
	 *            the token
	 * @return the simple authentication info
	 */
	private SimpleAuthenticationInfo createSimpleAuthInfo(BitbucketCloudPrincipal principal, UsernamePasswordToken token) {
		return new SimpleAuthenticationInfo(principal, token.getCredentials(), NAME);
	}

}
