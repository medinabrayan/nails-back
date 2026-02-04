package com.nails.identity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String bio;
    private String avatarUrl;
    private String phoneNumber;

    @Column(columnDefinition = "DOUBLE PRECISION DEFAULT 0.0")
    @Builder.Default
    private Double avgRating = 0.0;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    @Builder.Default
    private Integer ratingCount = 0;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
