package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Acknowledgement {
    private String senderId; //system id for SF organization
    private String testProg; //CPSIA
    private List<AcknowledgementItem> acknowledgementItems = new ArrayList<>();
}
