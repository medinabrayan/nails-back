package com.nails.reviews.producer;

import com.nails.reviews.event.ReviewCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishReviewCreated(Long specialistId, Integer rating) {
        ReviewCreatedEvent event = new ReviewCreatedEvent(specialistId, rating);
        // publishing to "reviews.exchange" with routing key "review.created"
        rabbitTemplate.convertAndSend("reviews.exchange", "review.created", event);
        System.out.println("Published ReviewCreatedEvent: " + event);
    }
}
