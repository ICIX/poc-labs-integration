package com.example.demo.services;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    private final FtpClient client;
    private final ProductTestResultItemHandler handler;

    public ScheduledTasks(FtpClient client, ProductTestResultItemHandler handler) {
        this.client = client;
        this.handler = handler;
    }

    @Scheduled(fixedRate = 60000)
    public void checkTestReport() throws JSchException, SftpException, IOException {
        try {
            client.apply(handler);
        }catch (Exception ex){
            logger.error(String.format("[x]Scheduled task failed. Reason: %s", ex.getMessage()));
        }

    }
}