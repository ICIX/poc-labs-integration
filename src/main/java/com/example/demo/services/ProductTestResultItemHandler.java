package com.example.demo.services;

import com.example.demo.model.FtpHandler;
import com.example.demo.model.LabIntegrationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.ChannelSftp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Vector;

@Slf4j
@Component
public class ProductTestResultItemHandler implements FtpHandler {

    @Autowired
    private Processor processor;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private final RecipientsService recipientsService;

    public ProductTestResultItemHandler(RecipientsService recipientsService) {
        this.recipientsService = recipientsService;
    }

    @Override
    public void doWork(ChannelSftp channel) throws Exception {
        readProductTestResults(channel);
    }

    private void readProductTestResults(ChannelSftp channel) throws Exception {
        Vector<ChannelSftp.LsEntry> list = channel.ls("*.csv");
        for(ChannelSftp.LsEntry entry : list) {
            BufferedReader br = new BufferedReader(new InputStreamReader(channel.get(entry.getFilename())));

            try {
                LabIntegrationMessage m = processor.convert(br);
                m.setFilename(entry.getFilename());
                recipientsService.sendToRecipient(m);

                String message = objectMapper.writeValueAsString(m);
                log.info("[x]Sent test results to recipient: {}", message);

                channel.rm(entry.getFilename());
            }catch (IOException e){
                log.error(e.toString());
            } finally {
                br.close();
            }
        }
    }

    @Override
    public String remoteDirectory() {
        return "/test_results";
    }
}

