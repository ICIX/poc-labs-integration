package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabIntegrationMessage {
    private String senderId;             //Lab UBE systemId
    private List<String> headers;
    private List<List<String>> values;
    private String delimiter;
    private String filename;
    private String type;
}
