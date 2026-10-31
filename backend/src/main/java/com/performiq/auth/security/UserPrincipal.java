package com.performiq.auth.security;

import com.performiq.auth.entity.AppUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class UserPrincipal implements UserDetails {

    private final AppUser appUser;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(AppUser appUser) {
        this.appUser = appUser;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return appUser.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return appUser.getUsername();
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
        return Boolean.TRUE.equals(appUser.getEnabled());
    }
}
