package com.example.harvesthubbackend.SecurityConfig;

import com.example.harvesthubbackend.Models.User;
import com.example.harvesthubbackend.Service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    @Lazy
    private UserService userService;
    
    @Value("${frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        
        System.out.println("=== OAuth2 Success Handler CALLED ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Authentication: " + authentication);
        
        try {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            
            // Xác định provider từ request URI
            String requestURI = request.getRequestURI();
            System.out.println("Request URI: " + requestURI);
            String provider = "google";
            if (requestURI.contains("facebook")) {
                provider = "facebook";
            }
            System.out.println("Provider: " + provider);
            
            // Lấy thông tin từ OAuth2User
            String email = null;
            String name = null;
            String avatar = null;
            String providerId = null;
            
            if ("google".equals(provider)) {
                Map<String, Object> attributes = oauth2User.getAttributes();
                System.out.println("Google attributes: " + attributes);
                System.out.println("Google attributes keys: " + attributes.keySet());
                
                email = oauth2User.getAttribute("email");
                name = oauth2User.getAttribute("name");
                avatar = oauth2User.getAttribute("picture");
                
                // Google có thể trả về 'sub' hoặc 'id' làm providerId
                providerId = oauth2User.getAttribute("sub");
                if (providerId == null) {
                    providerId = oauth2User.getAttribute("id");
                }
                // Fallback: dùng name attribute từ OAuth2User
                if (providerId == null) {
                    providerId = oauth2User.getName();
                }
                
                System.out.println("Google Info - Email: " + email + ", Name: " + name + ", Avatar: " + avatar + ", ProviderId: " + providerId);
                
                // Google thường trả về avatar URL trực tiếp, nhưng kiểm tra để chắc chắn
                if (avatar == null || avatar.isEmpty()) {
                    System.out.println("Google avatar is null or empty, trying alternative methods");
                    // Thử lấy từ attributes map trực tiếp
                    Object pictureObj = attributes.get("picture");
                    if (pictureObj != null) {
                        avatar = pictureObj.toString();
                        System.out.println("Google avatar from attributes map: " + avatar);
                    }
                }
            } else if ("facebook".equals(provider)) {
                // Facebook trả về id trong attributes
                providerId = oauth2User.getName(); // Facebook sử dụng name như providerId
                Map<String, Object> attributes = oauth2User.getAttributes();
                System.out.println("Facebook attributes: " + attributes);
                System.out.println("Facebook attributes keys: " + attributes.keySet());
                email = (String) attributes.get("email");
                name = (String) attributes.get("name");
                
                // Facebook avatar URL - có thể có nhiều format
                Object pictureObj = attributes.get("picture");
                System.out.println("Facebook picture object: " + pictureObj);
                System.out.println("Facebook picture object type: " + (pictureObj != null ? pictureObj.getClass().getName() : "null"));
                
                if (pictureObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> picture = (Map<String, Object>) pictureObj;
                    System.out.println("Facebook picture map: " + picture);
                    
                    // Thử lấy từ data.url (format chuẩn)
                    Object dataObj = picture.get("data");
                    if (dataObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) dataObj;
                        avatar = (String) data.get("url");
                        System.out.println("Facebook avatar from data.url: " + avatar);
                    }
                    
                    // Nếu không có data.url, thử lấy trực tiếp từ picture
                    if (avatar == null || avatar.isEmpty()) {
                        avatar = (String) picture.get("url");
                        System.out.println("Facebook avatar from picture.url: " + avatar);
                    }
                } else if (pictureObj instanceof String) {
                    // Nếu picture là string trực tiếp
                    avatar = (String) pictureObj;
                    System.out.println("Facebook avatar as string: " + avatar);
                }
                
                // Nếu vẫn không có avatar, thử tạo URL từ providerId
                if (avatar == null || avatar.isEmpty()) {
                    avatar = "https://graph.facebook.com/" + providerId + "/picture?type=large";
                    System.out.println("Facebook avatar fallback URL: " + avatar);
                }
            }
            
            System.out.println("OAuth2 Info - Provider: " + provider + ", Email: " + email + ", Name: " + name + ", ProviderId: " + providerId);
            
            // Tạo hoặc cập nhật user
            User user = userService.createOrUpdateOAuthUser(provider, providerId, email, name, avatar);
            System.out.println("User created/updated: " + user.getUsername());
            
            // Tạo JWT token
            String token = jwtService.generateToken(user);
            System.out.println("JWT token generated");
            
        // Redirect về backend endpoint để forward về frontend
        // Endpoint này sẽ redirect về frontend localhost
        String callbackUrl = UriComponentsBuilder.fromUriString(request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/oauth2/callback")
                .queryParam("token", token)
                .queryParam("provider", provider)
                .build().toUriString();
        
        System.out.println("Redirecting to callback endpoint: " + callbackUrl);
        getRedirectStrategy().sendRedirect(request, response, callbackUrl);
        } catch (Exception e) {
            System.err.println("OAuth2 Success Handler Error: " + e.getMessage());
            e.printStackTrace();
            // Redirect về frontend với error
            String errorUrl = UriComponentsBuilder.fromUriString(frontendBaseUrl + "/auth")
                    .queryParam("error", "oauth2_error")
                    .queryParam("message", e.getMessage())
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
}

