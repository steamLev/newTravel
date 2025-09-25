package com.example.rabbitmq.service;

import com.example.rabbitmq.model.ChannelStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelMonitorService {

    private final ConnectionFactory connectionFactory;
    
    @Value("${rabbitmq.channel.max-available}")
    private int maxChannels;
    
    @Value("${rabbitmq.channel.check-interval}")
    private long checkInterval;
    
    private final AtomicInteger currentChannelCount = new AtomicInteger(0);
    private final AtomicLong lastCheckTime = new AtomicLong(0);
    private volatile boolean channelsAvailable = true;

    @PostConstruct
    public void initialize() {
        log.info("Initializing Channel Monitor Service with max channels: {}", maxChannels);
        checkChannelAvailability();
    }

    @Scheduled(fixedDelayString = "${rabbitmq.channel.check-interval}")
    public void checkChannelAvailability() {
        try {
            // Get current connection info
            var connection = connectionFactory.createConnection();
            var isOpen = connection.isOpen();
            
            // Simulate channel count check - in real implementation you might track this differently
            int channelCount = currentChannelCount.get();
            lastCheckTime.set(System.currentTimeMillis());
            
            // Check if we can create more channels
            boolean canCreateMore = channelCount < maxChannels && isOpen;
            channelsAvailable = canCreateMore;
            
            log.debug("Channel availability check - Current: {}, Max: {}, Available: {}, Connection Open: {}", 
                    channelCount, maxChannels, canCreateMore, isOpen);
            
            if (!channelsAvailable) {
                log.warn("Channel limit reached or connection closed. Current: {}, Max: {}", 
                        channelCount, maxChannels);
            }
            
        } catch (Exception e) {
            log.error("Error checking channel availability", e);
            channelsAvailable = false;
        }
    }

    public ChannelStatus getChannelStatus() {
        return new ChannelStatus(
                channelsAvailable,
                currentChannelCount.get(),
                maxChannels
        );
    }

    public boolean areChannelsAvailable() {
        return channelsAvailable;
    }

    public int getCurrentChannelCount() {
        return currentChannelCount.get();
    }

    public int getMaxChannels() {
        return maxChannels;
    }

    public long getLastCheckTime() {
        return lastCheckTime.get();
    }

    /**
     * Увеличивает счетчик каналов при создании нового канала
     */
    public void incrementChannelCount() {
        int current = currentChannelCount.incrementAndGet();
        log.debug("Channel count incremented to: {}", current);
    }

    /**
     * Уменьшает счетчик каналов при закрытии канала
     */
    public void decrementChannelCount() {
        int current = currentChannelCount.decrementAndGet();
        log.debug("Channel count decremented to: {}", current);
    }
}