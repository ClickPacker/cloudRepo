package ru.nikita.cloudrepo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import ru.nikita.cloudrepo.service.AuthUserDetailsService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthUserDetailsService authUserDetailsService;

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(
                auths -> auths
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/auth/**",
                                "/home",
                                "/login",
                                "/css/**"
                        ).permitAll()
                        .requestMatchers("/user/delete", "/user/update").hasRole("ADMIN")
                        .anyRequest().authenticated()
        ).formLogin(form -> form
                        .loginPage("/auth/sign-in").permitAll()
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .loginProcessingUrl("/auth/process-sign-in")
                        .defaultSuccessUrl("/home", true)
        ).logout(form -> form.permitAll()
                        .logoutUrl("/auth/sign-out")
                        .logoutSuccessUrl("/auth/sign-in")
        ).exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        ).build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(authUserDetailsService);
        provider.setPasswordEncoder(getPasswordEncoder());
        return provider;
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
