package com.example.image_caption_generator.config;

import com.example.image_caption_generator.entity.RegistrationSource;
import com.example.image_caption_generator.entity.User;
import com.example.image_caption_generator.entity.UserRole;
import com.example.image_caption_generator.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Value("${FRONTEND.URL.DEV}")
    private String frontendUrlDev;

    @Autowired
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {

//        the below lines are responsible for saving the user after successful authentication
        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        if("google".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())) {  // checking if the provider is google
            DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();  // converting the Principal interface into it's implemented class
            Map<String, Object> attributes = principal.getAttributes();                       // fetching all the attributes
            String email = attributes.getOrDefault("email", "").toString();    // fetching email to create a new user in DB
            String name = attributes.getOrDefault("name", "").toString();      // fetching name to create a new user in DB

            userService.findByEmail(email).ifPresentOrElse(user -> {                          // checking if the user is already present or not
                DefaultOAuth2User newUser = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(user.getRole().name())),                    // if it is present then create a new DefaultOAuth2User with already saved AUTHORItITS
                        attributes,
                        "sub"
                );
                Authentication securityAuth  = new OAuth2AuthenticationToken(newUser,         // creating new Authentication object with new user
                        List.of(new SimpleGrantedAuthority(user.getRole().name())),
                        oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());

                SecurityContextHolder.getContext().setAuthentication(securityAuth);           // adding the new Authentication in Spring Context
            }, () -> {

                User userEntity = new User();                    // creating a new user and saving it in DB
                userEntity.setRole(UserRole.ROLE_USER);
                userEntity.setEmail(email);
                userEntity.setName(name);
                userEntity.setSource(RegistrationSource.GOOGLE);
                userService.save(userEntity);

                DefaultOAuth2User newUser = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(userEntity.getRole().name())),                    // if it is present then create a new DefaultOAuth2User with already saved AUTHORItITS
                        attributes,
                        "sub"
                );
                Authentication securityAuth  = new OAuth2AuthenticationToken(newUser,         // creating new Authentication object with new user
                        List.of(new SimpleGrantedAuthority(userEntity.getRole().name())),
                        oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());

                SecurityContextHolder.getContext().setAuthentication(securityAuth);
            });

        }

//        these 3 lines are responsible to redirect to the frontend url after successful authentication
        this.setAlwaysUseDefaultTargetUrl(true);
        this.setDefaultTargetUrl(frontendUrlDev);
        super.onAuthenticationSuccess(request, response, authentication);
    }

}
