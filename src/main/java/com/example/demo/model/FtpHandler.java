package com.example.demo.model;


import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import java.io.IOException;

public interface FtpHandler {
    void doWork(ChannelSftp channel) throws Exception;
    String remoteDirectory();
}
