package com.example.demo.services;

import com.example.demo.model.LabIntegrationMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class Processor {
    public byte[] convert(LabIntegrationMessage message){
        StringBuilder content = new StringBuilder();
        content.append(StringUtils.collectionToDelimitedString(message.getHeaders(), message.getDelimiter())).append('\n');
        for (List<String> row : message.getValues()) {
                content.append(StringUtils.collectionToDelimitedString(row, message.getDelimiter())).append('\n');
        }

        return content.toString().getBytes();
    }
}
