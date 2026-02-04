package com.nails.identity.dto;

import com.nails.identity.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    private Long userId;
    private String email;
    private Role role;
    private String fullName;
    private String bio;
    private String avatarUrl;
    private String phoneNumber;
}
