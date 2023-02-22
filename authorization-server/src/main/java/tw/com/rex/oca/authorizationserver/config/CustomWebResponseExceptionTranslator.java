package tw.com.rex.oca.authorizationserver.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;

/**
 * 自訂例外處理
 */
@SuppressWarnings("deprecation")
@Slf4j
public class CustomWebResponseExceptionTranslator implements WebResponseExceptionTranslator<OAuth2Exception> {

    @Override
    public ResponseEntity<OAuth2Exception> translate(Exception e) {
        log.error("授權失敗");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new OAuth2Exception(e.getMessage()));
    }

}
