package com.example.demo.services;

import com.example.demo.model.LabIntegrationMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

    public LabIntegrationMessage convert(BufferedReader fileContent) throws IOException {
        String delimiter = "|";
        LabIntegrationMessage message = new LabIntegrationMessage();
        message.setDelimiter(delimiter);
        String line = fileContent.readLine();
        message.setHeaders(Arrays.asList(StringUtils.delimitedListToStringArray(line, delimiter)));
        List<List<String>> values = new ArrayList<>();
        while ((line = fileContent.readLine()) != null) {
            values.add(Arrays.asList(StringUtils.delimitedListToStringArray(line, delimiter)));
        }
        message.setValues(values);
        return message;
    }
}
