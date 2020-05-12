package com.gardle.config;

import com.gardle.security.AuthoritiesConstants;
import com.gardle.security.jwt.JWTConfigurer;
import com.gardle.security.jwt.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.filter.CorsFilter;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Import(SecurityProblemSupport.class)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final TokenProvider tokenProvider;

    private final CorsFilter corsFilter;
    private final SecurityProblemSupport problemSupport;

    public SecurityConfiguration(TokenProvider tokenProvider, CorsFilter corsFilter, SecurityProblemSupport problemSupport) {
        this.tokenProvider = tokenProvider;
        this.corsFilter = corsFilter;
        this.problemSupport = problemSupport;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
            .antMatchers(HttpMethod.OPTIONS, "/**")
            .antMatchers("/app/**/*.{js,html}")
            .antMatchers("/i18n/**")
            .antMatchers("/content/**")
            .antMatchers("/swagger-ui/index.html")
            .antMatchers("/test/**");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .csrf()
            .disable()
            .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling()
            .authenticationEntryPoint(problemSupport)
            .accessDeniedHandler(problemSupport)
            .and()
            .headers()
            .contentSecurityPolicy("default-src 'self'; " +
                "frame-src 'self' https://js.stripe.com data:;" +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://storage.googleapis.com https://analytics.gardle.ga https://js.stripe.com; " +
                "style-src 'self' 'unsafe-inline'; img-src 'self' *.locationiq.com s3.eu-central-1.amazonaws.com https://analytics.gardle.ga https://fonts.googleapis.com/ data:; " +
                "font-src 'self' data:; " +
                "connect-src 'self' *.locationiq.com https://analytics.gardle.ga")
            .and()
            .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            .and()
            .featurePolicy("geolocation 'none'; midi 'none'; sync-xhr 'none'; microphone 'none'; camera 'none'; magnetometer 'none'; gyroscope 'none'; speaker 'none'; fullscreen 'self'; payment 'none'")
            .and()
            .frameOptions()
            .deny()
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers("/api/v1/authenticate").permitAll()
            .antMatchers("/api/v1/register").permitAll()
            .antMatchers("/api/v1/activate").permitAll()
            .antMatchers("/api/v1/account/reset-password/init").permitAll()
            .antMatchers("/api/v1/account/reset-password/finish").permitAll()
            .antMatchers("/api/v1/gardenfields").permitAll()
            .antMatchers("/api/v1/gardenfields/user").authenticated()
            .antMatchers("/api/v1/gardenfields/{\\d+}").permitAll()
            .antMatchers("/api/v1/gardenfields/{\\d+}").permitAll()
            .antMatchers("/api/v1/gardenfields/{\\d+}/downloadImage/*").permitAll()
            .antMatchers("/api/v1/gardenfields/{\\d+}/downloadThumbnail/*").permitAll()
            .antMatchers("/api/v1/gardenfields/{\\d+}/downloadImages").permitAll()
            .antMatchers("/api/v1/gardenfields/{\\d+}/coverImageName").permitAll()
            .antMatchers("/api/v1/leasings/{\\d+}/leasedDateRanges").permitAll()
            .antMatchers("/api/v1/**").authenticated()
            .antMatchers("/websocket/tracker").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/websocket/**").permitAll()
            .antMatchers("/management/health").permitAll()
            .antMatchers("/management/info").permitAll()
            .antMatchers("/management/prometheus").permitAll()
            .antMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
            .antMatchers("/stripe/**").permitAll()
            .and()
            .httpBasic()
            .and()
            .apply(securityConfigurerAdapter());
        // @formatter:on
    }

    private JWTConfigurer securityConfigurerAdapter() {
        return new JWTConfigurer(tokenProvider);
    }
}
