package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlan;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientAuthenticator;
import org.apereo.cas.support.oauth.authenticator.OAuth20UserAuthenticator;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfileDataCreator;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfileDataCreator;
import org.apereo.cas.support.oauth.services.OAuth20ServiceRegistry;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20ClientCredentialsGrantTypeAuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20IdTokenResponseTypeAuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20PasswordGrantTypeAuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20RefreshTokenGrantTypeAuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20TokenResponseTypeAuthorizationRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20AuthorizationCodeGrantTypeTokenRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20ClientCredentialsGrantTypeTokenRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20DeviceCodeResponseTypeRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20PasswordGrantTypeTokenRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20RefreshTokenGrantTypeTokenRequestValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20TokenRequestValidator;
import org.apereo.cas.support.oauth.web.OAuth20CasCallbackUrlResolver;
import org.apereo.cas.support.oauth.web.audit.AccessTokenGrantRequestAuditResourceResolver;
import org.apereo.cas.support.oauth.web.audit.AccessTokenResponseAuditResourceResolver;
import org.apereo.cas.support.oauth.web.audit.OAuth20UserProfileDataAuditResourceResolver;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AccessTokenEndpointController;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20AuthorizeEndpointController;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20CallbackAuthorizeEndpointController;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20DeviceUserCodeApprovalEndpointController;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20IntrospectionEndpointController;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20UserProfileEndpointController;
import org.apereo.cas.support.oauth.web.response.OAuth20CasClientRedirectActionBuilder;
import org.apereo.cas.support.oauth.web.response.OAuth20DefaultCasClientRedirectActionBuilder;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20DefaultTokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenAuthorizationCodeGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenClientCredentialsGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenDeviceCodeResponseRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantAuditableRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenPasswordGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRefreshTokenGrantRequestExtractor;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20DefaultAccessTokenResponseGenerator;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationCodeAuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ClientCredentialsResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResourceOwnerCredentialsResponseBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20TokenAuthorizationResponseBuilder;
import org.apereo.cas.support.oauth.web.views.ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20ConsentApprovalViewResolver;
import org.apereo.cas.support.oauth.web.views.OAuth20DefaultUserProfileViewRenderer;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.accesstoken.DefaultAccessTokenFactory;
import org.apereo.cas.ticket.accesstoken.OAuthAccessTokenExpirationPolicy;
import org.apereo.cas.ticket.code.DefaultOAuthCodeFactory;
import org.apereo.cas.ticket.code.OAuthCodeExpirationPolicy;
import org.apereo.cas.ticket.code.OAuthCodeFactory;
import org.apereo.cas.ticket.device.DefaultDeviceTokenFactory;
import org.apereo.cas.ticket.device.DeviceTokenFactory;
import org.apereo.cas.ticket.refreshtoken.DefaultRefreshTokenFactory;
import org.apereo.cas.ticket.refreshtoken.OAuthRefreshTokenExpirationPolicy;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.support.HardTimeoutExpirationPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.inspektr.audit.spi.support.DefaultAuditActionResolver;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.config.Config;
import org.pac4j.core.context.session.J2ESessionStore;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.http.url.UrlResolver;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.direct.DirectFormClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.apereo.cas.support.oauth.OAuth20Constants.BASE_OAUTH20_URL;
import static org.apereo.cas.support.oauth.OAuth20Constants.CALLBACK_AUTHORIZE_URL_DEFINITION;
import static org.apereo.cas.support.oauth.OAuth20Constants.CLIENT_ID;
import static org.apereo.cas.support.oauth.OAuth20Constants.CLIENT_SECRET;

/**
 * This this {@link CasOAuthConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("oauthConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasOAuthConfiguration implements AuditTrailRecordResolutionPlanConfigurer, ServiceRegistryExecutionPlanConfigurer {

    @Autowired
    @Qualifier("registeredServiceAccessStrategyEnforcer")
    private AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory webApplicationServiceFactory;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("ticketRegistry")
    private ObjectProvider<TicketRegistry> ticketRegistry;

    @Autowired
    @Qualifier("ticketGrantingTicketCookieGenerator")
    private ObjectProvider<CookieRetrievingCookieGenerator> ticketGrantingTicketCookieGenerator;

    @ConditionalOnMissingBean(name = "accessTokenResponseGenerator")
    @Bean
    public OAuth20AccessTokenResponseGenerator accessTokenResponseGenerator() {
        return new OAuth20DefaultAccessTokenResponseGenerator();
    }

    @ConditionalOnMissingBean(name = "oauthCasClientRedirectActionBuilder")
    @Bean
    public OAuth20CasClientRedirectActionBuilder oauthCasClientRedirectActionBuilder() {
        return new OAuth20DefaultCasClientRedirectActionBuilder();
    }

    @Bean
    public UrlResolver casCallbackUrlResolver() {
        return new OAuth20CasCallbackUrlResolver(OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()));
    }

    @Bean
    public Config oauthSecConfig() {
        val cfg = new CasConfiguration(casProperties.getServer().getLoginUrl());
        val oauthCasClient = new CasClient(cfg);
        oauthCasClient.setRedirectActionBuilder(webContext -> oauthCasClientRedirectActionBuilder().build(oauthCasClient, webContext));
        oauthCasClient.setName(Authenticators.CAS_OAUTH_CLIENT);
        oauthCasClient.setUrlResolver(casCallbackUrlResolver());

        val authenticator = oAuthClientAuthenticator();
        val basicAuthClient = new DirectBasicAuthClient(authenticator);
        basicAuthClient.setName(Authenticators.CAS_OAUTH_CLIENT_BASIC_AUTHN);

        val directFormClient = new DirectFormClient(authenticator);
        directFormClient.setName(Authenticators.CAS_OAUTH_CLIENT_DIRECT_FORM);
        directFormClient.setUsernameParameter(CLIENT_ID);
        directFormClient.setPasswordParameter(CLIENT_SECRET);

        val userFormClient = new DirectFormClient(oAuthUserAuthenticator());
        userFormClient.setName(Authenticators.CAS_OAUTH_CLIENT_USER_FORM);

        val config = new Config(OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()),
            oauthCasClient, basicAuthClient, directFormClient, userFormClient);
        config.setSessionStore(new J2ESessionStore());
        return config;
    }


    @ConditionalOnMissingBean(name = "consentApprovalViewResolver")
    @Bean
    public ConsentApprovalViewResolver consentApprovalViewResolver() {
        return new OAuth20ConsentApprovalViewResolver(casProperties);
    }

    @ConditionalOnMissingBean(name = "callbackAuthorizeViewResolver")
    @Bean
    public OAuth20CallbackAuthorizeViewResolver callbackAuthorizeViewResolver() {
        return new OAuth20CallbackAuthorizeViewResolver() {
        };
    }

    @Bean
    public OAuth20CasClientRedirectActionBuilder defaultOAuthCasClientRedirectActionBuilder() {
        return new OAuth20DefaultCasClientRedirectActionBuilder();
    }

    @ConditionalOnMissingBean(name = "oAuthClientAuthenticator")
    @Bean
    public Authenticator<UsernamePasswordCredentials> oAuthClientAuthenticator() {
        return new OAuth20ClientAuthenticator(this.servicesManager.getIfAvailable(), webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer);
    }

    @ConditionalOnMissingBean(name = "oAuthUserAuthenticator")
    @Bean
    public Authenticator<UsernamePasswordCredentials> oAuthUserAuthenticator() {
        return new OAuth20UserAuthenticator(authenticationSystemSupport, servicesManager.getIfAvailable(), webApplicationServiceFactory);
    }

    @ConditionalOnMissingBean(name = "oauthAccessTokenResponseGenerator")
    @Bean
    public OAuth20AccessTokenResponseGenerator oauthAccessTokenResponseGenerator() {
        return new OAuth20DefaultAccessTokenResponseGenerator();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "defaultAccessTokenFactory")
    public AccessTokenFactory defaultAccessTokenFactory() {
        return new DefaultAccessTokenFactory(accessTokenIdGenerator(), accessTokenExpirationPolicy());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "defaultDeviceTokenFactory")
    public DeviceTokenFactory defaultDeviceTokenFactory() {
        return new DefaultDeviceTokenFactory(deviceTokenIdGenerator(), deviceTokenExpirationPolicy(),
            casProperties.getAuthn().getOauth().getDeviceToken().getUserCodeLength());
    }

    @Bean
    @ConditionalOnMissingBean(name = "accessTokenExpirationPolicy")
    public ExpirationPolicy accessTokenExpirationPolicy() {
        val oauth = casProperties.getAuthn().getOauth().getAccessToken();
        if (casProperties.getLogout().isRemoveDescendantTickets()) {
            return new OAuthAccessTokenExpirationPolicy(
                Beans.newDuration(oauth.getMaxTimeToLiveInSeconds()).getSeconds(),
                Beans.newDuration(oauth.getTimeToKillInSeconds()).getSeconds()
            );
        }
        return new OAuthAccessTokenExpirationPolicy.OAuthAccessTokenSovereignExpirationPolicy(
            Beans.newDuration(oauth.getMaxTimeToLiveInSeconds()).getSeconds(),
            Beans.newDuration(oauth.getTimeToKillInSeconds()).getSeconds()
        );
    }

    @Bean
    @ConditionalOnMissingBean(name = "deviceTokenExpirationPolicy")
    public ExpirationPolicy deviceTokenExpirationPolicy() {
        val oauth = casProperties.getAuthn().getOauth().getDeviceToken();
        return new HardTimeoutExpirationPolicy(Beans.newDuration(oauth.getMaxTimeToLiveInSeconds()).getSeconds());
    }

    private ExpirationPolicy oAuthCodeExpirationPolicy() {
        val oauth = casProperties.getAuthn().getOauth();
        return new OAuthCodeExpirationPolicy(oauth.getCode().getNumberOfUses(),
            oauth.getCode().getTimeToKillInSeconds());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "oAuthCodeIdGenerator")
    public UniqueTicketIdGenerator oAuthCodeIdGenerator() {
        return new DefaultUniqueTicketIdGenerator();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "refreshTokenIdGenerator")
    public UniqueTicketIdGenerator refreshTokenIdGenerator() {
        return new DefaultUniqueTicketIdGenerator();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "defaultOAuthCodeFactory")
    public OAuthCodeFactory defaultOAuthCodeFactory() {
        return new DefaultOAuthCodeFactory(oAuthCodeIdGenerator(), oAuthCodeExpirationPolicy());
    }

    @ConditionalOnMissingBean(name = "profileScopeToAttributesFilter")
    @Bean
    public OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter() {
        return new DefaultOAuth20ProfileScopeToAttributesFilter();
    }

    @Bean
    @ConditionalOnMissingBean(name = "callbackAuthorizeController")
    @RefreshScope
    public OAuth20CallbackAuthorizeEndpointController callbackAuthorizeController() {
        return new OAuth20CallbackAuthorizeEndpointController(servicesManager.getIfAvailable(),
            ticketRegistry.getIfAvailable(),
            defaultAccessTokenFactory(),
            oauthPrincipalFactory(),
            webApplicationServiceFactory,
            oauthSecConfig(),
            callbackAuthorizeViewResolver(),
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable());
    }

    @ConditionalOnMissingBean(name = "oauthTokenGenerator")
    @Bean
    @RefreshScope
    public OAuth20TokenGenerator oauthTokenGenerator() {
        return new OAuth20DefaultTokenGenerator(defaultAccessTokenFactory(),
            defaultDeviceTokenFactory(),
            defaultRefreshTokenFactory(),
            ticketRegistry.getIfAvailable(),
            casProperties);
    }

    @Bean
    public Collection<AccessTokenGrantRequestExtractor> accessTokenGrantRequestExtractors() {
        final AccessTokenGrantRequestExtractor authzCodeExt =
            new AccessTokenAuthorizationCodeGrantRequestExtractor(this.servicesManager.getIfAvailable(), this.ticketRegistry.getIfAvailable(),
                centralAuthenticationService, casProperties.getAuthn().getOauth(),
                webApplicationServiceFactory);

        final AccessTokenGrantRequestExtractor refreshTokenExt =
            new AccessTokenRefreshTokenGrantRequestExtractor(this.servicesManager.getIfAvailable(), this.ticketRegistry.getIfAvailable(),
                centralAuthenticationService, casProperties.getAuthn().getOauth(),
                webApplicationServiceFactory);

        val authenticationBuilder = oauthCasAuthenticationBuilder();
        final AccessTokenGrantRequestExtractor pswExt =
            new AccessTokenPasswordGrantRequestExtractor(this.servicesManager.getIfAvailable(), this.ticketRegistry.getIfAvailable(),
                authenticationBuilder, centralAuthenticationService,
                casProperties.getAuthn().getOauth(), registeredServiceAccessStrategyEnforcer);

        final AccessTokenGrantRequestExtractor credsExt =
            new AccessTokenClientCredentialsGrantRequestExtractor(this.servicesManager.getIfAvailable(), this.ticketRegistry.getIfAvailable(),
                authenticationBuilder, centralAuthenticationService,
                casProperties.getAuthn().getOauth(), registeredServiceAccessStrategyEnforcer);

        final AccessTokenGrantRequestExtractor deviceCodeExt =
            new AccessTokenDeviceCodeResponseRequestExtractor(this.servicesManager.getIfAvailable(), this.ticketRegistry.getIfAvailable(),
                centralAuthenticationService, casProperties.getAuthn().getOauth(),
                authenticationBuilder, registeredServiceAccessStrategyEnforcer);

        return CollectionUtils.wrapList(authzCodeExt, refreshTokenExt, deviceCodeExt, pswExt, credsExt);
    }

    @ConditionalOnMissingBean(name = "introspectionEndpointController")
    @Bean
    public OAuth20IntrospectionEndpointController introspectionEndpointController() {
        return new OAuth20IntrospectionEndpointController(
            servicesManager.getIfAvailable(),
            ticketRegistry.getIfAvailable(),
            defaultAccessTokenFactory(),
            oauthPrincipalFactory(),
            webApplicationServiceFactory,
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            centralAuthenticationService,
            registeredServiceAccessStrategyEnforcer);
    }


    @ConditionalOnMissingBean(name = "accessTokenController")
    @Bean
    public OAuth20AccessTokenEndpointController accessTokenController() {
        return new OAuth20AccessTokenEndpointController(
            servicesManager.getIfAvailable(),
            ticketRegistry.getIfAvailable(),
            defaultAccessTokenFactory(),
            oauthPrincipalFactory(),
            webApplicationServiceFactory,
            oauthTokenGenerator(),
            accessTokenResponseGenerator(),
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            accessTokenExpirationPolicy(),
            deviceTokenExpirationPolicy(),
            oauthTokenRequestValidators(),
            accessTokenGrantAuditableRequestExtractor()
        );
    }

    @ConditionalOnMissingBean(name = "accessTokenGrantAuditableRequestExtractor")
    @Bean
    public AuditableExecution accessTokenGrantAuditableRequestExtractor() {
        return new AccessTokenGrantAuditableRequestExtractor(accessTokenGrantRequestExtractors());
    }

    @ConditionalOnMissingBean(name = "deviceUserCodeApprovalEndpointController")
    @Bean
    public OAuth20DeviceUserCodeApprovalEndpointController deviceUserCodeApprovalEndpointController() {
        return new OAuth20DeviceUserCodeApprovalEndpointController(
            servicesManager.getIfAvailable(),
            ticketRegistry.getIfAvailable(),
            defaultAccessTokenFactory(),
            oauthPrincipalFactory(),
            webApplicationServiceFactory,
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            defaultDeviceTokenFactory()
        );
    }

    @ConditionalOnMissingBean(name = "oauthUserProfileViewRenderer")
    @Bean
    @RefreshScope
    public OAuth20UserProfileViewRenderer oauthUserProfileViewRenderer() {
        return new OAuth20DefaultUserProfileViewRenderer(casProperties.getAuthn().getOauth());
    }

    @ConditionalOnMissingBean(name = "oAuth2UserProfileDataCreator")
    @Bean
    public OAuth20UserProfileDataCreator oAuth2UserProfileDataCreator() {
        return new DefaultOAuth20UserProfileDataCreator(servicesManager.getIfAvailable(), profileScopeToAttributesFilter());
    }

    @ConditionalOnMissingBean(name = "profileController")
    @Bean
    public OAuth20UserProfileEndpointController profileController() {
        return new OAuth20UserProfileEndpointController(
            servicesManager.getIfAvailable(),
            ticketRegistry.getIfAvailable(),
            defaultAccessTokenFactory(),
            oauthPrincipalFactory(),
            webApplicationServiceFactory,
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            oauthUserProfileViewRenderer(),
            oAuth2UserProfileDataCreator());
    }

    @ConditionalOnMissingBean(name = "oauthAuthorizationResponseBuilders")
    @Bean
    @RefreshScope
    public Set<OAuth20AuthorizationResponseBuilder> oauthAuthorizationResponseBuilders() {
        val builders =
            this.applicationContext.getBeansOfType(OAuth20AuthorizationResponseBuilder.class, false, true);
        return new HashSet<>(builders.values());
    }

    @ConditionalOnMissingBean(name = "oauthAuthorizationRequestValidators")
    @Bean
    @RefreshScope
    public Set<OAuth20AuthorizationRequestValidator> oauthAuthorizationRequestValidators() {
        val builders =
            this.applicationContext.getBeansOfType(OAuth20AuthorizationRequestValidator.class, false, true);
        return new HashSet<>(builders.values());
    }

    @ConditionalOnMissingBean(name = "oauthTokenRequestValidators")
    @Bean
    @RefreshScope
    public Collection<OAuth20TokenRequestValidator> oauthTokenRequestValidators() {
        val validators = new ArrayList<OAuth20TokenRequestValidator>();
        validators.add(new OAuth20AuthorizationCodeGrantTypeTokenRequestValidator(servicesManager.getIfAvailable(),
            ticketRegistry.getIfAvailable(), registeredServiceAccessStrategyEnforcer));
        validators.add(new OAuth20DeviceCodeResponseTypeRequestValidator(servicesManager.getIfAvailable(), webApplicationServiceFactory));
        validators.add(new OAuth20RefreshTokenGrantTypeTokenRequestValidator(registeredServiceAccessStrategyEnforcer, ticketRegistry.getIfAvailable()));
        validators.add(new OAuth20PasswordGrantTypeTokenRequestValidator(registeredServiceAccessStrategyEnforcer, servicesManager.getIfAvailable(), webApplicationServiceFactory));
        validators.add(new OAuth20ClientCredentialsGrantTypeTokenRequestValidator(servicesManager.getIfAvailable(), registeredServiceAccessStrategyEnforcer, webApplicationServiceFactory));
        return validators;
    }

    @ConditionalOnMissingBean(name = "oauthClientCredentialsGrantTypeRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationRequestValidator oauthClientCredentialsGrantTypeRequestValidator() {
        return new OAuth20ClientCredentialsGrantTypeAuthorizationRequestValidator(servicesManager.getIfAvailable(),
            webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer);
    }

    @ConditionalOnMissingBean(name = "oauthAuthorizationCodeResponseTypeRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationRequestValidator oauthAuthorizationCodeResponseTypeRequestValidator() {
        return new OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator(servicesManager.getIfAvailable(),
            webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer);
    }

    @ConditionalOnMissingBean(name = "oauthTokenResponseTypeRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationRequestValidator oauthTokenResponseTypeRequestValidator() {
        return new OAuth20TokenResponseTypeAuthorizationRequestValidator(servicesManager.getIfAvailable(),
            webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer);
    }

    @ConditionalOnMissingBean(name = "oauthIdTokenResponseTypeRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationRequestValidator oauthIdTokenResponseTypeRequestValidator() {
        return new OAuth20IdTokenResponseTypeAuthorizationRequestValidator(servicesManager.getIfAvailable(),
            webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer);
    }

    @ConditionalOnMissingBean(name = "oauthPasswordGrantTypeRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationRequestValidator oauthPasswordGrantTypeRequestValidator() {
        return new OAuth20PasswordGrantTypeAuthorizationRequestValidator(servicesManager.getIfAvailable(),
            webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer);
    }

    @ConditionalOnMissingBean(name = "oauthRefreshTokenGrantTypeRequestValidator")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationRequestValidator oauthRefreshTokenGrantTypeRequestValidator() {
        return new OAuth20RefreshTokenGrantTypeAuthorizationRequestValidator(servicesManager.getIfAvailable(),
            webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer);
    }

    @ConditionalOnMissingBean(name = "oauthResourceOwnerCredentialsResponseBuilder")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationResponseBuilder oauthResourceOwnerCredentialsResponseBuilder() {
        return new OAuth20ResourceOwnerCredentialsResponseBuilder(accessTokenResponseGenerator(), oauthTokenGenerator(),
            accessTokenExpirationPolicy(), casProperties);
    }

    @ConditionalOnMissingBean(name = "oauthClientCredentialsResponseBuilder")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationResponseBuilder oauthClientCredentialsResponseBuilder() {
        return new OAuth20ClientCredentialsResponseBuilder(accessTokenResponseGenerator(),
            oauthTokenGenerator(), accessTokenExpirationPolicy(), casProperties);
    }

    @ConditionalOnMissingBean(name = "oauthTokenResponseBuilder")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationResponseBuilder oauthTokenResponseBuilder() {
        return new OAuth20TokenAuthorizationResponseBuilder(oauthTokenGenerator(), accessTokenExpirationPolicy());
    }

    @ConditionalOnMissingBean(name = "oauthAuthorizationCodeResponseBuilder")
    @Bean
    @RefreshScope
    public OAuth20AuthorizationResponseBuilder oauthAuthorizationCodeResponseBuilder() {
        return new OAuth20AuthorizationCodeAuthorizationResponseBuilder(ticketRegistry.getIfAvailable(), defaultOAuthCodeFactory());
    }

    @ConditionalOnMissingBean(name = "authorizeController")
    @Bean
    @RefreshScope
    public OAuth20AuthorizeEndpointController authorizeController() {
        return new OAuth20AuthorizeEndpointController(
            servicesManager.getIfAvailable(),
            ticketRegistry.getIfAvailable(),
            defaultAccessTokenFactory(),
            oauthPrincipalFactory(),
            webApplicationServiceFactory,
            defaultOAuthCodeFactory(),
            consentApprovalViewResolver(),
            profileScopeToAttributesFilter(),
            casProperties,
            ticketGrantingTicketCookieGenerator.getIfAvailable(),
            oauthCasAuthenticationBuilder(),
            oauthAuthorizationResponseBuilders(),
            oauthAuthorizationRequestValidators(),
            registeredServiceAccessStrategyEnforcer
        );
    }

    @ConditionalOnMissingBean(name = "oauthPrincipalFactory")
    @Bean
    @RefreshScope
    public PrincipalFactory oauthPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "defaultRefreshTokenFactory")
    public RefreshTokenFactory defaultRefreshTokenFactory() {
        return new DefaultRefreshTokenFactory(refreshTokenIdGenerator(), refreshTokenExpirationPolicy());
    }

    private ExpirationPolicy refreshTokenExpirationPolicy() {
        val rtProps = casProperties.getAuthn().getOauth().getRefreshToken();
        val timeout = Beans.newDuration(rtProps.getTimeToKillInSeconds()).getSeconds();
        if (casProperties.getLogout().isRemoveDescendantTickets()) {
            return new OAuthRefreshTokenExpirationPolicy(timeout);
        }
        return new OAuthRefreshTokenExpirationPolicy.OAuthRefreshTokenSovereignExpirationPolicy(timeout);
    }

    @ConditionalOnMissingBean(name = "oauthCasAuthenticationBuilder")
    @Bean
    @RefreshScope
    public OAuth20CasAuthenticationBuilder oauthCasAuthenticationBuilder() {
        return new OAuth20CasAuthenticationBuilder(oauthPrincipalFactory(), webApplicationServiceFactory,
            profileScopeToAttributesFilter(), casProperties);
    }

    @ConditionalOnMissingBean(name = "accessTokenIdGenerator")
    @Bean
    @RefreshScope
    public UniqueTicketIdGenerator accessTokenIdGenerator() {
        return new DefaultUniqueTicketIdGenerator();
    }

    @ConditionalOnMissingBean(name = "deviceTokenIdGenerator")
    @Bean
    @RefreshScope
    public UniqueTicketIdGenerator deviceTokenIdGenerator() {
        return new DefaultUniqueTicketIdGenerator();
    }

    @Override
    public void configureAuditTrailRecordResolutionPlan(final AuditTrailRecordResolutionPlan plan) {
        plan.registerAuditActionResolver("OAUTH2_USER_PROFILE_DATA_ACTION_RESOLVER",
            new DefaultAuditActionResolver("_CREATED", "_FAILED"));
        plan.registerAuditResourceResolver("OAUTH2_USER_PROFILE_DATA_RESOURCE_RESOLVER",
            new OAuth20UserProfileDataAuditResourceResolver());

        plan.registerAuditActionResolver("OAUTH2_ACCESS_TOKEN_REQUEST_ACTION_RESOLVER",
            new DefaultAuditActionResolver("_CREATED", "_FAILED"));
        plan.registerAuditResourceResolver("OAUTH2_ACCESS_TOKEN_REQUEST_RESOURCE_RESOLVER",
            new AccessTokenGrantRequestAuditResourceResolver());

        plan.registerAuditActionResolver("OAUTH2_ACCESS_TOKEN_RESPONSE_ACTION_RESOLVER",
            new DefaultAuditActionResolver("_CREATED", "_FAILED"));
        plan.registerAuditResourceResolver("OAUTH2_ACCESS_TOKEN_RESPONSE_RESOURCE_RESOLVER",
            new AccessTokenResponseAuditResourceResolver());
    }

    @Bean
    public Service oauthCallbackService() {
        val oAuthCallbackUrl = casProperties.getServer().getPrefix()
            + BASE_OAUTH20_URL + '/' + CALLBACK_AUTHORIZE_URL_DEFINITION;
        return this.webApplicationServiceFactory.createService(oAuthCallbackUrl);
    }

    @Override
    public void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
        val service = new RegexRegisteredService();
        service.setId(Math.abs(RandomUtils.getNativeInstance().nextLong()));
        service.setEvaluationOrder(0);
        service.setName(service.getClass().getSimpleName());
        service.setDescription("OAuth Authentication Callback Request URL");
        service.setServiceId(oauthCallbackService().getId());
        service.setAttributeReleasePolicy(new DenyAllAttributeReleasePolicy());
        plan.registerServiceRegistry(new OAuth20ServiceRegistry(service));
    }
}
