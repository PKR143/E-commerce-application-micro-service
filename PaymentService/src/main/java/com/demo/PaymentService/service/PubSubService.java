package com.demo.PaymentService.service;

import com.demo.PaymentService.dto.TransactionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PubSubService {

    @Value("${pubsub.topic}")
    private String TOPIC;

    @Autowired
    PubSubTemplate pubSubTemplate;

    public void publishMessage(TransactionEvent event)throws Exception{

        try {
            String msg = new ObjectMapper().writeValueAsString(event);
            pubSubTemplate.publish(TOPIC, msg);
            log.info("message published: {}", msg);
        } catch (Exception e) {
            log.info("Something went wrong while publishing message due to: {}",e.getMessage());
        }
    }

}

