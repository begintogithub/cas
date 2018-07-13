package org.apereo.cas.config;

import org.apereo.cas.services.ServicesManager;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.*;

/**
 * This is {@link BaseSurrogateAuthenticationTestsConfiguration}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@TestConfiguration("casSurrogateAuthenticationServiceTestConfiguration")
public class BaseSurrogateAuthenticationTestsConfiguration {

    @Bean
    @RefreshScope
    public ServicesManager servicesManager() {
        return mock(ServicesManager.class);
    }
}
