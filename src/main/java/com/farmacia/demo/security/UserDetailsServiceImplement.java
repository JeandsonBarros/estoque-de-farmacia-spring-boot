package com.farmacia.demo.security;

import com.farmacia.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Transactional
@Service
public class UserDetailsServiceImplement  implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        var usuario = usuarioRepository.findByEmail(username).orElseThrow(
                ()-> new UsernameNotFoundException("User Not Found with username:" + username)
        );

        SimpleGrantedAuthority authority = new  SimpleGrantedAuthority(usuario.getRole());
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(authority);

        return new User(usuario.getEmail(), usuario.getSenha(), true, true, true, true, authorities);
    }
}
