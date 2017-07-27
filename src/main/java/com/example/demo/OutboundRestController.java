package com.example.demo;

import com.example.demo.model.Acknowledgement;
import com.example.demo.model.LabIntegrationMessage;
import com.example.demo.services.FtpClient;
import com.example.demo.services.Processor;
import com.example.demo.services.ProductTestRequestItemHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/labs")
public class OutboundRestController {

    private final ObjectMapper objectMapper;
    @Autowired
    private final FtpClient client;
    @Autowired
    private final Processor processor;

    public OutboundRestController(ObjectMapper objectMapper, FtpClient client, Processor processor) {
        this.objectMapper = objectMapper;
        this.client = client;
        this.processor = processor;
    }

    @RequestMapping(value = "/test-items", method = RequestMethod.POST)
    ResponseEntity<?> addTestItem(@RequestBody LabIntegrationMessage input) throws JsonProcessingException {

        String message = objectMapper.writeValueAsString(input);
        log.info("[x]Sent test items to sFTP: {}", message);

        HttpHeaders httpHeaders = new HttpHeaders();
        try {
            client.apply(new ProductTestRequestItemHandler(processor.convert(input), input.getFilename()));
        }
        catch (Exception ex){
            return new ResponseEntity<>(null, httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/test-result-ask", method = RequestMethod.POST)
    ResponseEntity<?> addTestResultAck(@RequestBody Acknowledgement input) throws JsonProcessingException {

        String message = objectMapper.writeValueAsString(input);
        log.info("[x]Sent to test result ack queue: {}", message);

        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }
}
