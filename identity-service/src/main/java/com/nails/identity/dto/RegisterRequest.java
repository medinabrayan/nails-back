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
public class RegisterRequest {
    private String email;
    private String password;
    private Role role;
    private String fullName;
    private String bio;
    private String avatarUrl;
    private String phoneNumber;
}
