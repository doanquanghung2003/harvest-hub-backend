package com.example.harvesthubbackend.SecurityConfig;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, org.springframework.security.core.userdetails.UserDetailsService userDetailsService) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
            // Cho phép tất cả origins để hỗ trợ mobile devices và các IP khác nhau
            config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://192.168.*:*",
                "http://10.*:*",
                "http://172.16.*:*",
                "http://172.17.*:*",
                "http://172.18.*:*",
                "http://172.19.*:*",
                "http://172.20.*:*",
                "http://172.21.*:*",
                "http://172.22.*:*",
                "http://172.23.*:*",
                "http://172.24.*:*",
                "http://172.25.*:*",
                "http://172.26.*:*",
                "http://172.27.*:*",
                "http://172.28.*:*",
                "http://172.29.*:*",
                "http://172.30.*:*",
                "http://172.31.*:*",
                "https://*",  // Cho phép HTTPS từ mobile hoặc các nguồn khác
                "file://",
                "null"
            ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        // Cho phép tất cả headers bao gồm Content-Type cho multipart upload
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        // Expose các headers cần thiết cho frontend
        config.setExposedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "Content-Length",
            "Content-Disposition",
            "X-Requested-With"
        ));
        // Cho phép preflight requests tồn tại lâu hơn (24 giờ)
        config.setMaxAge(86400L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(@org.springframework.lang.NonNull ResourceHandlerRegistry registry) {
                // Phục vụ static files từ thư mục static
                registry.addResourceHandler("/*.html", "/*.css", "/*.js", "/*.ico", "/*.png", "/*.jpg", "/*.gif")
                        .addResourceLocations("classpath:/static/");
                
                // Phục vụ file tĩnh từ thư mục uploads (ngoài classpath)
                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations("file:D:/harvest-hub-backend/uploads/");
                
                // Phục vụ ảnh sản phẩm
                registry.addResourceHandler("/uploads/products/**")
                        .addResourceLocations("file:D:/harvest-hub-backend/uploads/products/");
            }
        };
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/products/check-product/**").permitAll()
                .requestMatchers("/api/products/*/json").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/api/products/uploads/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/ws/**").permitAll()
                
                // Static files
                .requestMatchers("/*.html", "/*.css", "/*.js", "/*.ico", "/*.png", "/*.jpg", "/*.gif").permitAll()
                
                // Swagger/OpenAPI
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
                
                // Products endpoints - public read, admin write
                .requestMatchers("/api/products").permitAll()
                .requestMatchers("/api/products/{id}").permitAll()
                .requestMatchers("/api/products/{id}/json").permitAll()
                .requestMatchers("/api/products/stats").permitAll()
                // Allow product creation endpoints for now
                .requestMatchers("/api/products/create").permitAll()
                .requestMatchers("/api/products/create-json").permitAll()
                // Seller registration endpoints
                .requestMatchers("/api/sellers/**").permitAll()
                // Allow order seller actions for now
                .requestMatchers("/api/orders/**").permitAll()
                
                // User endpoints (temporarily open for admin UI integration)
                .requestMatchers(HttpMethod.GET, "/api/user", "/api/user/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/user/**").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/user/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/user/**").permitAll()
                
                // Notification endpoints (temporarily open for admin UI integration)
                .requestMatchers("/api/notifications/**").permitAll()
                
                // Reviews endpoints - public read, authenticated write
                .requestMatchers(HttpMethod.GET, "/api/reviews/**", "/api/v1/reviews/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/reviews/**", "/api/v1/reviews/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/reviews/**", "/api/v1/reviews/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/reviews/**", "/api/v1/reviews/**").authenticated()
                
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers("/api/products/upload").hasRole("ADMIN")
                .requestMatchers("/api/products/{id}/status").hasRole("ADMIN")
                
                // Default: cho phép các route còn lại để người khác truy cập như local
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}