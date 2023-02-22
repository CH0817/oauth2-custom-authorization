package tw.com.rex.oca.authorizationserver.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 自訂 grant type
 */
@SuppressWarnings("deprecation")
@Slf4j
public class CustomTokenGranter extends AbstractTokenGranter {

    public static final String GRANT_TYPE = "custom";
    private final AuthenticationManager authenticationManager;

    protected CustomTokenGranter(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, AuthenticationManager authenticationManager) {
        super(tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        // 檢查必要參數
        Map<String, String> parameters = new LinkedHashMap<>(tokenRequest.getRequestParameters());
        String username = parameters.get("username");

        log.info("username: [{}]", username);

        if (StringUtils.isBlank(username)) {
            throw new InvalidGrantException("缺少必要的參數");
        }
        // 驗證交給 AuthenticationManager(call CustomAuthenticationProvider.authenticate)
        CustomAuthenticationToken authenticationToken = new CustomAuthenticationToken(username);
        Authentication authenticate = this.authenticationManager.authenticate(authenticationToken);
        if (Objects.isNull(authenticate)) {
            throw new InvalidGrantException("驗證失敗");
        }

        OAuth2Request auth2Request = this.getRequestFactory().createOAuth2Request(client, tokenRequest);
        return new OAuth2Authentication(auth2Request, authenticate);
    }
}
