package com.example.harvesthubbackend.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OAuth2CallbackController {
    
    @Value("${frontend.base-url:http://localhost:8082}")
    private String frontendBaseUrl;
    
    @GetMapping("/oauth2/callback")
    public String oauth2Callback(@RequestParam(required = false) String token, 
                                 @RequestParam(required = false) String provider,
                                 @RequestParam(required = false) String error) {
        // Redirect về frontend với token
        if (token != null && provider != null) {
            return "redirect:" + frontendBaseUrl + "/auth?token=" + token + "&provider=" + provider;
        } else if (error != null) {
            return "redirect:" + frontendBaseUrl + "/auth?error=" + error;
        } else {
            return "redirect:" + frontendBaseUrl + "/auth?error=oauth2_failed";
        }
    }
}

