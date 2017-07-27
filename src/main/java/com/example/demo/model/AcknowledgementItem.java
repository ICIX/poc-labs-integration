package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcknowledgementItem {
    private String uid;         //Same UID from ProductTestRequestItem/ProductTestResultItem
    private String status;      //"Received" or "Rejected" Only
    private String description; //If the EDI message is rejected, this field must fill in reject reason
}
