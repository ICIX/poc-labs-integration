package com.example.demo.services;

import com.example.demo.model.FtpHandler;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;

@Slf4j
public class ProductTestRequestItemHandler implements FtpHandler {

    private final byte[] content;
    private final String fileName;

    public ProductTestRequestItemHandler(byte[] content, String fileName) {
        this.content = content;
        this.fileName = fileName;
    }

    public void doWork(ChannelSftp channel) throws SftpException {

        log.info("[x]Storing file as remote filename: {} ", fileName);

        ByteArrayInputStream bis = new ByteArrayInputStream(content);
        channel.put(bis, fileName);
    }

    @Override
    public String remoteDirectory() {
        return "/test_requests";
    }
}
