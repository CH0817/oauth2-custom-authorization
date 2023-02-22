package tw.com.rex.oca;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class PasswordEncoderTest {

    @Test
    void passwordEncoderTest() {
        System.out.println(new Argon2PasswordEncoder().encode("111111"));
        System.out.println("$argon2id$v=19$m=4096,t=3,p=1$YE9siItcoEaIb9LdHWnP5g$MbdFYyV1sd+5ZhlhDWGWBOCuGFhUN31XrcQQp8r622s".length());
    }

}
