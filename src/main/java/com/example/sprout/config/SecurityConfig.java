package com.example.sprout.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.sprout.security.SproutUserDetailsService;

@Configuration
public class SecurityConfig {

  private final SproutUserDetailsService userDetailsService;

  public SecurityConfig(SproutUserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

    http
        .csrf(csrf -> csrf.disable())

        .authorizeHttpRequests(auth -> auth

            .requestMatchers(
                "/login",
                "/register",
                "/css/**",
                "/js/**",
                "/img/**",
                "/webjars/**")
            .permitAll()

            .anyRequest().authenticated())

        .formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .defaultSuccessUrl("/", true)
            .failureHandler((request, response, exception) -> {
              request.getSession().setAttribute("loginError",
                  "ログインIDまたはパスワードが正しくありません");
              response.sendRedirect("/login");
            })
            .permitAll())

        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login"));

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }

}