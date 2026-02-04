package com.nails.identity.controller;

import com.nails.identity.dto.ProfileResponse;
import com.nails.identity.entity.User;
import com.nails.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ProfileResponse> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var profile = user.getProfile();

        // Map to DTO (ignoring password)
        ProfileResponse response = ProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .fullName(profile != null ? profile.getFullName() : null)
                .bio(profile != null ? profile.getBio() : null)
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .phoneNumber(profile != null ? profile.getPhoneNumber() : null)
                .build();

        return ResponseEntity.ok(response);
    }
}
