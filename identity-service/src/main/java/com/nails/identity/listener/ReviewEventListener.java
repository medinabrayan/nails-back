package com.nails.identity.listener;

import com.nails.identity.config.RabbitConfig;
import com.nails.identity.event.ReviewCreatedEvent;
import com.nails.identity.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewEventListener {

    private final ProfileService profileService;

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void handleReviewCreated(ReviewCreatedEvent event) {
        System.out.println("Received ReviewCreatedEvent: " + event);
        profileService.updateRating(event.getSpecialistId(), event.getRating());
    }
}
