package tw.com.rex.oca.authorizationserver.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.RedisAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 授權伺服器設定
 */
@SuppressWarnings("deprecation")
@Configuration
@EnableAuthorizationServer
@AllArgsConstructor
@Slf4j
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private RedisConnectionFactory redisConnectionFactory;
    private DataSource dataSource;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        log.info("start configure clients");
        // client 資料存在 DB
        clients.withClientDetails(new JdbcClientDetailsService(dataSource));
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security
                // 開啟 check token
                .checkTokenAccess("isAuthenticated()")
                // 指定加解密工具
                .passwordEncoder(passwordEncoder);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
                // refresh token 不可重複使用
                .reuseRefreshTokens(false)
                // 允許的 Http method
                .allowedTokenEndpointRequestMethods(HttpMethod.POST)
                // 自訂例外處理
                .exceptionTranslator(new CustomWebResponseExceptionTranslator())
                // 授權碼保存 service，不設定授權碼模式無法使用
                .authorizationCodeServices(authorizationCodeServices())
                // 從 security 設定取得
                .authenticationManager(authenticationManager)
                // access token 產生方式
                .tokenGranter(tokenGranter(endpoints))
                // access token 保存方式
                .tokenStore(redisTokenStore())
                // JWT access token 轉換
                .accessTokenConverter(jwtTokenConvert());
    }

    /**
     * access token 產生方式
     *
     * @param endpoints AuthorizationServerEndpointsConfigurer
     * @return TokenGranter
     */
    private TokenGranter tokenGranter(final AuthorizationServerEndpointsConfigurer endpoints) {
        List<TokenGranter> granters = new ArrayList<>(Collections.singletonList(endpoints.getTokenGranter()));

        AuthorizationServerTokenServices tokenServices = endpoints.getTokenServices();
        ClientDetailsService clientDetailsService = endpoints.getClientDetailsService();
        OAuth2RequestFactory requestFactory = endpoints.getOAuth2RequestFactory();

        // List<TokenGranter> granters = new ArrayList<>();

        // 授權碼模式
        // granters.add(new AuthorizationCodeTokenGranter(tokenServices, authorizationCodeServices(), clientDetailsService, requestFactory));
        // // 密碼模式
        // granters.add(new ResourceOwnerPasswordTokenGranter(authenticationManager, tokenServices, clientDetailsService, requestFactory));
        // // 簡化模式
        // granters.add(new ImplicitTokenGranter(tokenServices, clientDetailsService, requestFactory));
        // // 客戶端模式
        // granters.add(new ClientCredentialsTokenGranter(tokenServices, clientDetailsService, requestFactory));
        // // refresh token
        // granters.add(new RefreshTokenGranter(tokenServices, clientDetailsService, requestFactory));
        // 自定義驗證
        granters.add(new CustomTokenGranter(tokenServices, clientDetailsService, requestFactory, authenticationManager));

        return new CompositeTokenGranter(granters);
    }

    /**
     * 授權碼保存方式
     *
     * @return AuthorizationCodeServices
     */
    @Bean
    public AuthorizationCodeServices authorizationCodeServices() {
        return new RedisAuthorizationCodeServices(redisConnectionFactory);
    }

    /**
     * access token 保存方式
     *
     * @return TokenStore
     */
    private TokenStore redisTokenStore() {
        return new RedisTokenStore(redisConnectionFactory);
    }

    /**
     * JWT access token 轉換
     *
     * @return AccessTokenConverter
     */
    private AccessTokenConverter jwtTokenConvert() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey(getJwtKey());
        return converter;
    }

    /**
     * JWT 對稱加密鑰匙
     */
    private String getJwtKey() {
        return "JWT_KEY";
    }

    /**
     * 監聽驗證成功事件
     *
     * @param event AuthenticationSuccessEvent
     */
    @EventListener
    public void authenticationSuccessListener(AuthenticationSuccessEvent event) {
        log.info("authentication success, event object: [{}]", event);
    }

}
