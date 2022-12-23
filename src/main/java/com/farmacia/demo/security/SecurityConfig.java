package com.farmacia.demo.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
    public DefaultSecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests()
                .requestMatchers("/").hasAnyAuthority("ADMIN", "USER")
                .requestMatchers( "/usuario/cadastro").permitAll()
                .requestMatchers(HttpMethod.GET, "/get-file/**").hasAnyAuthority("ADMIN", "USER")
                .requestMatchers( "/usuario/conta").hasAnyAuthority("ADMIN", "USER")
                .requestMatchers(HttpMethod.POST, "/usuario/update").hasAnyAuthority("ADMIN", "USER")
                .requestMatchers(HttpMethod.GET, "/usuario/delete").hasAnyAuthority("ADMIN", "USER")
                .requestMatchers(HttpMethod.GET, "/usuario/listar-usuarios").hasAnyAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/usuario/buscar/**").hasAnyAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/usuario/deletar-um-usuario/{cpf}").hasAnyAuthority("ADMIN")
                .anyRequest().authenticated()
                .and().formLogin().loginPage("/usuario/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/usuario/login?error=true")
                .permitAll()
                .and().logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")).and()
                .csrf().disable()
                .headers().cacheControl().disable();

       return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/css/**",
                "/img/**",
                "/js/**"
        );
    }

}
