package com.example.demo.model;

import lombok.Data;

@Data
public class ResponseData {
    private String id;
    private String scope;
    private String instance_url;
    private String token_type;
    private String access_token;
}
