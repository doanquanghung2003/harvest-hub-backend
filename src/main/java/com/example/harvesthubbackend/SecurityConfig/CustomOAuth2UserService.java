package com.example.harvesthubbackend.SecurityConfig;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        // Xử lý đặc biệt cho Google - không gọi super.loadUser() vì nó sẽ fail với sub
        if ("google".equals(registrationId)) {
            return loadGoogleUser(userRequest);
        }
        
        // Cho các provider khác, dùng logic mặc định
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
        
        // Xử lý cho Facebook
        if ("facebook".equals(registrationId)) {
            System.out.println("=== Processing Google OAuth2 User ===");
            System.out.println("Original attributes: " + attributes);
            System.out.println("Original attributes keys: " + attributes.keySet());
            
            // Google có thể trả về 'sub' hoặc 'id', đảm bảo có ít nhất một trong hai
            String nameAttributeKey = "sub";
            Object sub = attributes.get("sub");
            Object id = attributes.get("id");
            
            System.out.println("Google sub: " + sub);
            System.out.println("Google id: " + id);
            
            // Nếu không có 'sub', thử dùng 'id' hoặc tạo từ email
            if (sub == null || sub.toString().trim().isEmpty()) {
                System.out.println("Google sub is null or empty, trying alternatives");
                if (id != null && !id.toString().trim().isEmpty()) {
                    attributes.put("sub", id.toString());
                    nameAttributeKey = "id";
                    System.out.println("Using id as sub: " + id);
                } else {
                    // Fallback: dùng email làm sub nếu không có cả sub và id
                    String email = (String) attributes.get("email");
                    if (email != null && !email.trim().isEmpty()) {
                        attributes.put("sub", email);
                        nameAttributeKey = "email";
                        System.out.println("Using email as sub: " + email);
                    } else {
                        // Cuối cùng, dùng name
                        String name = (String) attributes.get("name");
                        if (name != null && !name.trim().isEmpty()) {
                            attributes.put("sub", name);
                            nameAttributeKey = "name";
                            System.out.println("Using name as sub: " + name);
                        } else {
                            // Last resort: generate a unique ID
                            String fallbackId = "google_" + System.currentTimeMillis();
                            attributes.put("sub", fallbackId);
                            nameAttributeKey = "sub";
                            System.out.println("Generated fallback sub: " + fallbackId);
                        }
                    }
                }
            }
            
            // Đảm bảo nameAttributeKey có giá trị hợp lệ trong attributes
            Object nameAttributeValue = attributes.get(nameAttributeKey);
            if (nameAttributeValue == null || nameAttributeValue.toString().trim().isEmpty()) {
                throw new OAuth2AuthenticationException("Cannot determine name attribute for Google user. Attributes: " + attributes);
            }
            
            System.out.println("Google OAuth2 User - Final name attribute key: " + nameAttributeKey);
            System.out.println("Google OAuth2 User - Final name attribute value: " + nameAttributeValue);
            System.out.println("Google OAuth2 User - Final attributes: " + attributes);
            
            return new DefaultOAuth2User(
                oauth2User.getAuthorities(),
                attributes,
                nameAttributeKey
            );
        }
        
        // Xử lý cho Facebook
        if ("facebook".equals(registrationId)) {
            // Facebook sử dụng 'id' làm name attribute
            String nameAttributeKey = "id";
            Object id = attributes.get("id");
            
            if (id == null) {
                // Fallback: dùng email hoặc name
                String email = (String) attributes.get("email");
                if (email != null) {
                    attributes.put("id", email);
                    nameAttributeKey = "email";
                } else {
                    String name = (String) attributes.get("name");
                    if (name != null) {
                        attributes.put("id", name);
                        nameAttributeKey = "name";
                    }
                }
            }
            
            System.out.println("Facebook OAuth2 User - Name attribute key: " + nameAttributeKey);
            System.out.println("Facebook OAuth2 User - Attributes: " + attributes);
            
            return new DefaultOAuth2User(
                oauth2User.getAuthorities(),
                attributes,
                nameAttributeKey
            );
        }
        
        // Mặc định, trả về user gốc
        return oauth2User;
    }
    
    private OAuth2User loadGoogleUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            System.out.println("=== Loading Google User (Custom) ===");
            
            // Lấy user info endpoint từ client registration
            String userInfoUri = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri();
            System.out.println("Google user info URI: " + userInfoUri);
            
            // Tạo request entity với access token
            RequestEntity<?> request = RequestEntity
                .get(userInfoUri)
                .header("Authorization", "Bearer " + userRequest.getAccessToken().getTokenValue())
                .build();
            
            // Gọi API để lấy user info
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> attributes = response.getBody();
            System.out.println("Google user info response: " + attributes);
            
            if (attributes == null) {
                throw new OAuth2AuthenticationException("Google user info response is null");
            }
            
            // Xác định name attribute key
            String nameAttributeKey = "sub";
            Object sub = attributes.get("sub");
            
            // Nếu không có 'sub', thử các giá trị khác
            if (sub == null || sub.toString().trim().isEmpty()) {
                System.out.println("Google sub is null, trying alternatives");
                Object id = attributes.get("id");
                if (id != null && !id.toString().trim().isEmpty()) {
                    attributes.put("sub", id.toString());
                    nameAttributeKey = "id";
                    System.out.println("Using id as sub: " + id);
                } else {
                    String email = (String) attributes.get("email");
                    if (email != null && !email.trim().isEmpty()) {
                        attributes.put("sub", email);
                        nameAttributeKey = "email";
                        System.out.println("Using email as sub: " + email);
                    } else {
                        String name = (String) attributes.get("name");
                        if (name != null && !name.trim().isEmpty()) {
                            attributes.put("sub", name);
                            nameAttributeKey = "name";
                            System.out.println("Using name as sub: " + name);
                        } else {
                            // Last resort
                            String fallbackId = "google_" + System.currentTimeMillis();
                            attributes.put("sub", fallbackId);
                            nameAttributeKey = "sub";
                            System.out.println("Generated fallback sub: " + fallbackId);
                        }
                    }
                }
            }
            
            // Đảm bảo nameAttributeKey có giá trị
            Object nameAttributeValue = attributes.get(nameAttributeKey);
            if (nameAttributeValue == null || nameAttributeValue.toString().trim().isEmpty()) {
                throw new OAuth2AuthenticationException("Cannot determine name attribute for Google user. Attributes: " + attributes);
            }
            
            // Tạo authorities (Google users thường có ROLE_USER)
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            
            System.out.println("Google OAuth2 User - Final name attribute key: " + nameAttributeKey);
            System.out.println("Google OAuth2 User - Final name attribute value: " + nameAttributeValue);
            System.out.println("Google OAuth2 User - Final attributes: " + attributes);
            
            return new DefaultOAuth2User(
                authorities,
                attributes,
                nameAttributeKey
            );
        } catch (Exception e) {
            System.err.println("Error loading Google user: " + e.getMessage());
            e.printStackTrace();
            throw new OAuth2AuthenticationException("Failed to load Google user: " + e.getMessage());
        }
    }
}


