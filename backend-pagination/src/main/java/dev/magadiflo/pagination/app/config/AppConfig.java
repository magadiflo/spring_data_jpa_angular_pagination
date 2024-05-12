package dev.magadiflo.pagination.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class AppConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
        corsConfiguration.setAllowedHeaders(
                Arrays.asList(
                        HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE,
                        HttpHeaders.ACCEPT, HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        HttpHeaders.AUTHORIZATION, HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
                        HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS
                )
        );
        corsConfiguration.setExposedHeaders(
                Arrays.asList(
                        HttpHeaders.ORIGIN, HttpHeaders.CONTENT_TYPE,
                        HttpHeaders.ACCEPT, HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        HttpHeaders.AUTHORIZATION, HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS
                )
        );
        corsConfiguration.setAllowedMethods(
                Arrays.asList(
                        HttpMethod.GET.name(), HttpMethod.POST.name(), HttpMethod.PUT.name(),
                        HttpMethod.PATCH.name(), HttpMethod.DELETE.name(), HttpMethod.OPTIONS.name()
                )
        );

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration); // "/**", representa todas las rutas del backend

        return new CorsFilter(urlBasedCorsConfigurationSource);
    }

}
