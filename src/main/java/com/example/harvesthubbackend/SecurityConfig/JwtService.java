package com.example.harvesthubbackend.SecurityConfig;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }
    
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        // Token không có thời gian hết hạn (vĩnh viễn)
        // Đặt expiration rất xa trong tương lai (100 năm) để tránh lỗi với JWT library
        // Thực tế token sẽ không bao giờ hết hạn trong thời gian sử dụng bình thường
        long hundredYearsInMs = 100L * 365 * 24 * 60 * 60 * 1000; // ~3,155,760,000,000 ms
        
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + hundredYearsInMs)) // ~100 năm (vĩnh viễn)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            // Token được coi là hợp lệ nếu username khớp
            // Token có expiration 100 năm nên được coi như vĩnh viễn
            // Không cần kiểm tra expiration vì token sẽ không bao giờ hết hạn trong thời gian sử dụng thực tế
            return username.equals(userDetails.getUsername());
        } catch (Exception e) {
            // Nếu có lỗi khi parse token (ví dụ: signature không hợp lệ), token không hợp lệ
            return false;
        }
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
