package com.nails.identity.service;

import com.nails.identity.entity.Profile;
import com.nails.identity.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;

    @Transactional
    public void updateRating(Long specialistUserId, Integer newRating) {
        Profile profile = profileRepository.findByUserId(specialistUserId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user id: " + specialistUserId));

        // Logic to update average
        // (currentAvg * count + newRating) / (count + 1)
        double currentAvg = profile.getAvgRating() != null ? profile.getAvgRating() : 0.0;
        int count = profile.getRatingCount() != null ? profile.getRatingCount() : 0;

        double totalScore = currentAvg * count;
        totalScore += newRating;
        count++;

        double newAvg = totalScore / count;

        profile.setAvgRating(newAvg);
        profile.setRatingCount(count);

        profileRepository.save(profile);
        System.out.println("Updated rating for specialist " + specialistUserId + ": " + newAvg);
    }
}
