package com.example.rabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.name}")
    private String queueName;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.channel.max-available}")
    private int maxChannels;

    @Bean
    public Queue messageQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public DirectExchange messageExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Binding messageBinding() {
        return BindingBuilder
                .bind(messageQueue())
                .to(messageExchange())
                .with("message.routing.key");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        
        // Enable publisher confirms and returns
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("Message confirmed: {}", correlationData);
            } else {
                log.error("Message not confirmed: {}, cause: {}", correlationData, cause);
            }
        });
        
        template.setReturnsCallback(returned -> {
            log.error("Message returned: {}", returned);
        });
        
        return template;
    }
}