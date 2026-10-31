package com.performiq.auth.security;

import com.performiq.auth.entity.AppUser;
import com.performiq.auth.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String lookup = username;
        AppUser user = appUserRepository.findByUsername(lookup)
                .or(() -> !lookup.contains("@") ? appUserRepository.findByUsername(lookup + "@performiq.com") : java.util.Optional.empty())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new UserPrincipal(user);
    }
}
