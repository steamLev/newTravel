package com.example.rabbitmq.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelStatus {
    private boolean available;
    private int channelCount;
    private int maxChannels;
    private String status;
    private long lastChecked;
    
    public ChannelStatus(boolean available, int channelCount, int maxChannels) {
        this.available = available;
        this.channelCount = channelCount;
        this.maxChannels = maxChannels;
        this.status = available ? "AVAILABLE" : "UNAVAILABLE";
        this.lastChecked = System.currentTimeMillis();
    }
}