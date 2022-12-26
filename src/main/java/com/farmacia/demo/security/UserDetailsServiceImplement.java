package com.farmacia.demo.security;

import com.farmacia.demo.repository.FarmaciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Transactional
@Service
public class UserDetailsServiceImplement  implements UserDetailsService {

    @Autowired
    private FarmaciaRepository farmaciaRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        var farmacia = farmaciaRepository.findByEmail(username).orElseThrow(
                ()-> new UsernameNotFoundException("User Not Found with username:" + username)
        );

        SimpleGrantedAuthority authority = new  SimpleGrantedAuthority(farmacia.getRole());
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(authority);

        return new User(farmacia.getEmail(), farmacia.getSenha(), true, true, true, true, authorities);
    }
}
