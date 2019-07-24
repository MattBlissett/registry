package org.gbif.registry.ws.config;

import org.gbif.registry.identity.util.RegistryPasswordEncoder;
import org.gbif.registry.ws.security.jwt.JwtAuthenticator;
import org.gbif.registry.ws.security.jwt.JwtRequestFilter;
import org.gbif.registry.ws.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {

  private final ApplicationContext context;

  private UserDetailsService userDetailsService;

  public WebSecurityConfigurer(@Qualifier("registryUserDetailsService") UserDetailsService userDetailsService, ApplicationContext context) {
    this.userDetailsService = userDetailsService;
    this.context = context;
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) {
    auth.authenticationProvider(dbAuthenticationProvider());
  }

  private DaoAuthenticationProvider dbAuthenticationProvider() {
    final DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .httpBasic().and()
        .addFilterAfter(new JwtRequestFilter(context.getBean(JwtAuthenticator.class), context.getBean(JwtService.class)), BasicAuthenticationFilter.class)
        .csrf().disable().authorizeRequests()
        .antMatchers("/user/login").authenticated()
        .antMatchers("/**").permitAll();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new RegistryPasswordEncoder();
  }

}
