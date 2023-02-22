package tw.com.rex.oca.authorizationserver.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 自訂驗證 provider
 */
@AllArgsConstructor
@Slf4j
@Component("customAuthenticationProvider")
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        log.info("start authenticate");
        String username = (String) authentication.getCredentials();
        // 只是範例，所以 username 查的到就給過
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (Objects.isNull(userDetails)) {
            throw new BadCredentialsException("驗證失敗");
        }
        return new CustomAuthenticationToken(userDetails, AuthorityUtils.createAuthorityList("ADMIN"));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // authentication = CustomAuthenticationToken 才會進此 provider
        return CustomAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
