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
                .requestMatchers( "/farmacia/cadastro").permitAll()
                .requestMatchers(HttpMethod.GET, "/get-file/**").hasAnyAuthority("ADMIN", "USER")
                .requestMatchers( "/farmacia/conta").hasAnyAuthority("ADMIN", "USER")
                .requestMatchers(HttpMethod.POST, "/farmacia/update").hasAnyAuthority("ADMIN", "USER")
                .requestMatchers(HttpMethod.GET, "/farmacia/deletar-conta").hasAnyAuthority("ADMIN", "USER")
                .requestMatchers(HttpMethod.GET, "/farmacia/listar-farmacias").hasAnyAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/farmacia/buscar/**").hasAnyAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/farmacia/deletar-uma-farmacia/{cnpj}").hasAnyAuthority("ADMIN")
                .anyRequest().authenticated()
                .and().formLogin().loginPage("/farmacia/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/farmacia/login?error=true")
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
