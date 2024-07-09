package com.viettel.importwiz.security;

import com.viettel.importwiz.exception.FilterExceptionFilter;
import com.viettel.importwiz.security.jwt.AJwtRequestFilter;
import com.viettel.importwiz.security.jwt.JwtAuthenticationEntryPoint;
import com.viettel.importwiz.security.vsa.CustomVsaFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity(debug = false)
@Order(1)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String[] SWAGGER_WHITELIST = {
            // -- Swagger UI v2
            "/v2/api-docs", "/swagger-resources", "/swagger-resources/**",
            "/configuration/ui", "/configuration/security", "/swagger-ui.html",
            "/api-docs/**",
            "/webjars/**",
            "/api",
            // -- Swagger UI v3 (OpenAPI)
            "/v3/api-docs/**", "/swagger-ui/**",

            // fix when has SSO
            "/services/**",
            "/service-info/**",
            "/hive/table/create",
            "/hdfs/read-data-to-excel",
            "/hdfs/backup",
            "/hive/table/getColPartition",
    };
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @Autowired
    private AJwtRequestFilter jwtRequestFilter;
    @Autowired
    private FilterExceptionFilter filterExceptionFilter;
    @Value("${isOnlyUsingVsa}")
    private String isOnlyUsingVsa;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        String[] authenApiWhitelist;
        if (isOnlyUsingVsa.equals("true"))
            authenApiWhitelist = new String[]{"/auth/info",
                    "/auth/oauthVsa" + "/home", "/auth/logoutVsa",
                    "/auth/refreshToken", "/auth" + "/auth/refreshToken2"};
        else
            authenApiWhitelist = new String[]{"/auth/info",
                    "/auth/oauthVsa" + "/home", "/auth/logoutVsa",
                    "/auth/refreshToken", "/auth/refreshToken2", "/auth" +
                    "/loginSso",
                    "/auth/logoutSso"};
        // We don't need CSRF for this example
        http.cors().and().csrf().disable()
                // dont authenticate this particular request
                .authorizeRequests()
                .antMatchers(authenApiWhitelist).permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(SWAGGER_WHITELIST).permitAll()

//            permit all request
//            .antMatchers("/**").permitAll()

                // all other requests need to be authenticated
                .anyRequest().authenticated().and()

                // make sure we use stateless session; session won't be used to
                // store user's state.

                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(filterExceptionFilter,
                UsernamePasswordAuthenticationFilter.class);

        // All request will go to CustomVsaFilter prior

        http.addFilterBefore(new CustomVsaFilter(),
                UsernamePasswordAuthenticationFilter.class);

        // Add a filter to validate the tokens with every request

        http.addFilterBefore(jwtRequestFilter,
                UsernamePasswordAuthenticationFilter.class);
    }
}
