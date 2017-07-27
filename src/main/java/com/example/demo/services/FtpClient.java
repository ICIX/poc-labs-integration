package com.example.demo.services;

import com.example.demo.model.FtpHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;

@Slf4j
@Component
public class FtpClient {
    private final String remoteRoot;
    private final String hostname;
    private final int ftpPort;
    private final String username;
    private final String password;

    private final ObjectMapper objectMapper;

    @Autowired
    public FtpClient(@Value("${ftp.root}") String remoteRoot,
                     @Value("${ftp.host.name}") String hostname,
                     @Value("${ftp.port}") int ftpPort,
                     @Value("${ftp.user.name}") String username,
                     @Value("${ftp.user.password}") String password,
                     ObjectMapper objectMapper) {
        this.remoteRoot = remoteRoot;
        this.hostname = hostname;
        this.ftpPort = ftpPort;
        this.username = username;
        this.password = password;
        this.objectMapper = objectMapper;
    }
    public void apply(FtpHandler handler) throws SftpException, JSchException, IOException {

        log.info("[x]Creating sFTP session");
        JSch jsch = new JSch();
        Session session = null;
        Channel channel = null;
        ChannelSftp c = null;

        //Now connect and SFTP to the SFTP Server
        try {
            //Create a session sending through our username and password
            session = jsch.getSession(username, hostname, ftpPort);
            log.info("[x]sFTP Session created");
            session.setPassword(password);

            //Security.addProvider(new com.sun.crypto.provider.SunJCE());
            //Setup Strict HostKeyChecking to no so we dont get the
            //unknown host key exception
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            log.info("[x]Session connected");

            //Open the SFTP channel
            log.info("[x]Opening sFTP Channel");
            channel = session.openChannel("sftp");
            channel.connect();
            c = (ChannelSftp) channel;

            //Change to the remote directory
            log.info("[x]Changing to sFTP remote dir: " + remoteRoot + handler.remoteDirectory());
            c.cd(remoteRoot + handler.remoteDirectory());

            //Send the file we generated
            try {

               handler.doWork(c);

            } catch (Exception e) {
                log.error("[x]Processing remote file failed. " + e.toString());
                throw e;
            }
        } catch (Exception e) {
            log.error("[x]Unable to connect to sFTP server. " + e.toString());
            throw e;
        } finally {
            //
            //Disconnect from the FTP server
            //
            try {
                if (session != null)
                    session.disconnect();

                if (channel != null)
                    channel.disconnect();

                if (c != null)
                    c.quit();
            } catch (Exception exc) {
                log.error("[x]Unable to disconnect from sFTP server. " + exc.toString());
            }

            log.info("[x]sFTP Process Complete");
        }
    }
}
