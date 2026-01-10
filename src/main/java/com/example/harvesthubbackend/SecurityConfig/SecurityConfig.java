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
import java.io.IOException;


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
    
    // CustomOAuth2UserService đã được đánh dấu @Service, Spring sẽ tự động inject vào SecurityFilterChain

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
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthFilter, 
            OAuth2SuccessHandler oauth2SuccessHandler, CustomOAuth2UserService customOAuth2UserService) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            // OAuth2 cần session, nhưng các endpoint khác dùng STATELESS
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation().migrateSession()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oauth2SuccessHandler)
                .failureHandler((request, response, exception) -> {
                    System.err.println("OAuth2 Login Failed: " + exception.getMessage());
                    exception.printStackTrace();
                    // Redirect về frontend với error
                    String frontendUrl = "https://f64055e91085.ngrok-free.app/auth?error=oauth2_failed&message=" + exception.getMessage();
                    try {
                        response.sendRedirect(frontendUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - Authentication & Registration
                .requestMatchers("/api/auth/**").permitAll()
                // Public endpoints - OAuth2 (phải permitAll trước khi authenticated)
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                // Public endpoint - OAuth2 callback handler
                .requestMatchers("/oauth2/callback").permitAll()
                
                // Public endpoints - AI Chat
                .requestMatchers("/api/chat/**").permitAll()
                
                // Public endpoints - Product browsing (read-only)
                .requestMatchers(HttpMethod.GET, "/api/products").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/{id}/json").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/check-product/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/stats").permitAll()
                
                // Public endpoints - Categories (read-only)
                .requestMatchers(HttpMethod.GET, "/api/categories").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                
                // Public endpoints - Static files and uploads
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/api/products/uploads/**").permitAll()
                .requestMatchers("/*.html", "/*.css", "/*.js", "/*.ico", "/*.png", "/*.jpg", "/*.gif").permitAll()
                
                // Public endpoints - Error pages
                .requestMatchers("/error").permitAll()
                
                // Public endpoints - WebSocket (may need authentication later)
                .requestMatchers("/ws/**").permitAll()
                
                // Public endpoints - Swagger/OpenAPI (should be restricted in production)
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
                
                // Public endpoints - Seller registration (initial registration should be public)
                .requestMatchers(HttpMethod.POST, "/api/sellers", "/api/sellers/register", "/api/sellers/register/**").permitAll()
                
                // Public endpoints - Seller info (read-only for product display)
                .requestMatchers(HttpMethod.GET, "/api/sellers/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/sellers/user/{userId}").permitAll()
                
                // Public endpoints - Reviews (read-only)
                .requestMatchers(HttpMethod.GET, "/api/reviews/**", "/api/v1/reviews/**").permitAll()
                
                // Authenticated endpoints - Product creation (requires authentication)
                .requestMatchers(HttpMethod.POST, "/api/products/create", "/api/products/create-json").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/products/{id}").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/products/{id}").authenticated()
                
                // Authenticated endpoints - Orders (all operations require authentication)
                .requestMatchers("/api/orders/**").authenticated()
                
                // Authenticated endpoints - Cart (all operations require authentication)
                .requestMatchers("/api/cart/**").authenticated()
                
                // Authenticated endpoints - User profile (users can only access their own data)
                .requestMatchers(HttpMethod.GET, "/api/user", "/api/user/{id}").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/user/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/user/**").authenticated()
                
                // Authenticated endpoints - Notifications (users can only access their own)
                // Cho phép GET notifications cho user cụ thể (có thể public nếu không có thông tin nhạy cảm)
                .requestMatchers(HttpMethod.GET, "/api/notifications/user/{userId}").permitAll()
                .requestMatchers("/api/notifications/**").authenticated()
                
                // Authenticated endpoints - Reviews (write operations)
                .requestMatchers(HttpMethod.POST, "/api/reviews/**", "/api/v1/reviews/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/reviews/**", "/api/v1/reviews/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/reviews/**", "/api/v1/reviews/**").authenticated()
                
                // Authenticated endpoints - Seller operations
                // GET seller info đã được cho phép public ở trên, chỉ PUT cần authenticated
                .requestMatchers(HttpMethod.PUT, "/api/sellers/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/sellers/**").permitAll() // Cho phép xem thông tin seller công khai
                
                // Admin endpoints - Full access required
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers("/api/products/{id}/status").hasRole("ADMIN")
                
                // Seller endpoints - Allow authenticated sellers to upload products
                .requestMatchers("/api/products/upload").authenticated()
                .requestMatchers("/api/products/create").authenticated()
                
                // Test endpoints - Allow public access for development (remove in production)
                .requestMatchers("/api/products/test-add-sample").permitAll()
                .requestMatchers("/api/products/test-add-vietnam-vegetables").permitAll()
                .requestMatchers("/api/products/test-add-vietnam-fruits").permitAll()
                .requestMatchers("/api/products/test-add-vietnam-seeds").permitAll()
                .requestMatchers("/api/products/test-add-vietnam-tools").permitAll()
                
                // Default: require authentication for all other endpoints
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}