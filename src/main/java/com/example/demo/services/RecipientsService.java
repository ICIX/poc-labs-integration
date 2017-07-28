package com.example.demo.services;


import com.example.demo.model.LabIntegrationMessage;
import com.example.demo.model.ResponseData;
import com.example.demo.model.TokenAcquiringException;
import com.example.demo.repository.ApplicationResourceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecipientsService {

    private final Logger logger = Logger.getLogger(RecipientsService.class);
    private final int limit = 10000;
    private final String header = "{\"alg\":\"RS256\"}";
    private final String claimTemplate = "'{'\"iss\": \"{0}\", \"prn\": \"{1}\", \"aud\": \"{2}\", \"exp\": \"{3}\"'}'";

    @Value("${recipient.issuer}")
    private String issuer;
    @Value("${recipient.subject}")
    private String subject;
    @Value("${recipient.audience}")
    private String audience;
    @Value("${recipient.namespace}")
    private String namespace;

    private final ObjectMapper objectMapper;
    private final ApplicationResourceRepository applicationResourceRepository;

    @Autowired
    public RecipientsService(ObjectMapper objectMapper, ApplicationResourceRepository applicationResourceRepository) {
        this.objectMapper = objectMapper;
        this.applicationResourceRepository = applicationResourceRepository;
    }

    public void sendToRecipient(LabIntegrationMessage result) throws Exception {

        logger.info("[x]Sending product test result.....");

        send(objectMapper.writeValueAsString(result), "/producttestresult");

    }


    private void send(String content, String endpoint) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, IOException {
        try {
            StringBuffer token = new StringBuffer();

            // Encode the JWT Header and add it to our string to sign
            token.append(Base64.encodeBase64URLSafeString(header.getBytes("UTF-8")));

            // Separate with a period
            token.append(".");

            // Create the JWT Claims Object
            String[] claimArray = new String[4];
            claimArray[0] = issuer;
            claimArray[1] = subject;
            claimArray[2] = audience;
            claimArray[3] = Long.toString((System.currentTimeMillis() / 1000) + 300);

            MessageFormat claims = new MessageFormat(claimTemplate);
            String payload = claims.format(claimArray);

            // Add the encoded claims object
            token.append(Base64.encodeBase64URLSafeString(payload.getBytes("UTF-8")));

            // Load the private key from a database
            // We can afford stupid hardcoded namespace because it is not what to be changed often if at all.
            String PRIVATE_KEY = namespace.toLowerCase().equals("icix_v1") ? "PRIVATE_KEY_V1" : "PRIVATE_KEY";
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(applicationResourceRepository.findByKey(PRIVATE_KEY).get(0).getValue());
            KeyFactory fact = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = fact.generatePrivate(keySpec);

            // Sign the JWT Header + "." + JWT Claims Object
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(token.toString().getBytes("UTF-8"));
            String signedPayload = Base64.encodeBase64URLSafeString(signature.sign());

            // Separate with a period
            token.append(".");

            // Add the encoded signature
            token.append(signedPayload);

            logger.info(String.format("[x]Token: '%s'", token.toString()));

            List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer"));
            nameValuePairs.add(new BasicNameValuePair("assertion", token.toString()));

            HttpPost httpPost = new HttpPost(String.format("%s/services/oauth2/token", audience));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse httpResponse = httpClient.execute(httpPost);

            String json = EntityUtils.toString(httpResponse.getEntity());
            logger.info(String.format("[x]Response: '%s'", json));

            // TODO: refactoring for code required, just stub for now
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                ResponseData responseData = objectMapper.readValue(json, ResponseData.class);
                HttpPost httpPost1 = new HttpPost(responseData.getInstance_url() + "/services/apexrest/" + namespace + endpoint);
                httpPost1.setHeader("Authorization", "Bearer " + responseData.getAccess_token());
                logger.info("[x]Data to be send:" + content);
                StringEntity params = new StringEntity(content, "UTF-8");
                httpPost1.addHeader("content-type", "application/json;charset=UTF-8");
                httpPost1.setEntity(params);
                HttpResponse response = httpClient.execute(httpPost1);
                logger.info("[x]Response after post request sent: " + response.getStatusLine());

                HttpEntity httpEntity = response.getEntity();
                String responseMessage= EntityUtils.toString(httpEntity);
                logger.info("[x]Response data: " + responseMessage);
                if (response.getStatusLine().getStatusCode() != 200) {
//                    FailedInboundMessage message = FailedInboundMessage.builder()
//                            .id(UUID.randomUUID())
//                            .message(content)
//                            .endPoint(endpoint)
//                            .error(responseMessage)
//                            .created(new Date())
//                            .build();
//                    failedInboundMessageRepository.save(message);
                }
            } else {
                throw new TokenAcquiringException("Can't get token from salesforce.com");
            }
        } catch (Exception e) {

            logger.error(e);
        }
    }
}
