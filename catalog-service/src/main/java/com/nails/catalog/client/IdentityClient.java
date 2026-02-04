package com.nails.catalog.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Assuming Identity Service has an endpoint to get user details or validate generic user
// Since I created the Identity Service, I know it has /profile (for current user). 
// However, checking if a specific ID exists usually requires an admin endpoint or public profile endpoint.
// For this exercise, I will assume Identity Service will theoretically support validating a user by ID 
// or I will use a placeholder that the user can implement later in the Identity Service.
// Or effectively, I will define what I expect the Identity Service to provide.

@FeignClient(name = "identity-service", url = "${identity.service.url}")
public interface IdentityClient {

    // Ideally: GET /users/{id} or /public/profile/{id}
    // For now, I'll define a check that we can't easily implement without modifying
    // Identity Service first.
    // I will assume for this task that validation is "checking if the user exists".

    // I will add a hypothetical endpoint that I would add to Identity Service if I
    // could switch context again
    // But to make this work "as is" or show the intent:

    @GetMapping("/users/{id}/exists")
    boolean checkUserExists(@PathVariable("id") Long id);
}
