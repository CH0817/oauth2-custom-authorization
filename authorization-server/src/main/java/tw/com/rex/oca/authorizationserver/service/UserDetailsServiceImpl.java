package tw.com.rex.oca.authorizationserver.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.com.rex.oca.authorizationserver.mapper.UserInfoMapper;
import tw.com.rex.oca.authorizationserver.mapper.model.UserInfo;

import java.util.Collection;
import java.util.Collections;

/**
 * 必須有 UserDetailsService，否則無法 refresh token
 */
@AllArgsConstructor
@Service
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

    private UserInfoMapper userInfoMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo userInfo = userInfoMapper.selectByUsername(username);
        return new UserDetailsImpl(userInfo);
    }

    public static class UserDetailsImpl implements UserDetails {
        private static final long serialVersionUID = 7220777773535122924L;
        private final String username;
        private final String password;

        public UserDetailsImpl(UserInfo userInfo) {
            this.username = userInfo.getUsername();
            this.password = userInfo.getPassword();
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.singletonList(new SimpleGrantedAuthority("USER"));
        }

        @Override
        public String getPassword() {
            return this.password;
        }

        @Override
        public String getUsername() {
            return this.username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

}
