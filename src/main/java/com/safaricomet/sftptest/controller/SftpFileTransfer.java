package com.safaricomet.sftptest.controller;

import com.jcraft.jsch.*;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Vector;

@RequestMapping(value = "sftp-file-transfer")
@RestController
public class SftpFileTransfer {
    @GetMapping("hi")
    public String sayHi() {
        return "Hello SFTP Tester!";
    }

    //@GetMapping("upload-file")
    public void uploadFile(File file, String folderName) throws JSchException, SftpException {
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();

        String absolutePath = file.getAbsolutePath();
        String localAbsolutePath = absolutePath;
        String remoteDir = "/home/dejazmach/"+folderName;

        channelSftp.put(localAbsolutePath, remoteDir + file.getName());

        channelSftp.exit();
    }

    //@GetMapping("download-file")
    public void downloadFile(File file, String folderName) throws JSchException, SftpException {
        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();

        String remoteFileName = file.getName();
        String localDir = "src/main/resources/"+folderName+"/";



        channelSftp.get(remoteFileName, localDir + remoteFileName);

        channelSftp.exit();
    }

    @GetMapping("upload-folder")
    public void uploadFolder() throws IOException {
        Path dir = Paths.get("/Users/dejazmachmolla/.ssh");
        Files.walk(dir).forEach(path -> {
            try {
                File file = path.toFile();
                if(!file.isDirectory()) {
                    uploadFile(path.toFile(), "sftptest");
                }
            } catch (JSchException e) {
                throw new RuntimeException(e);
            } catch (SftpException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @GetMapping("download-folder")
    public void downloadFolder() throws IOException, JSchException, SftpException {
        Path rootDir = Paths.get("/Users/dejazmachmolla/Documents/ims-back");
        //Files.walk(rootDir).forEach(path -> showFile(path.toFile()));

        ChannelSftp channelSftp = setupJsch();
        channelSftp.connect();
        String rootDirectory = "/home/dejazmach/sftptest";
        showRemoteFiles(channelSftp, rootDirectory);

        /*Files.walk(rootDir).forEach(path -> {
            try {
                File file = path.toFile();
                if(!file.isDirectory()) {
                    downloadFile(path.toFile(), "static");
                } else {

                }
            } catch (JSchException e) {
                throw new RuntimeException(e);
            } catch (SftpException e) {
                throw new RuntimeException(e);
            }
        });*/
    }

    public void showRemoteFiles(ChannelSftp channelSftp, String remoteDirectory) throws SftpException {
        //String remoteDirectory = directory;
        Vector<ChannelSftp.LsEntry> flLst = channelSftp.ls(remoteDirectory);
        System.out.println("flLst size : "+flLst.size());
        for(int j=0; j<flLst.size(); j++) {
            ChannelSftp.LsEntry entry = flLst.get(j);

            SftpATTRS attr = entry.getAttrs();

            if(attr.isDir()) {
                if(!(".".equals(entry.getFilename()) || "..".equals(entry.getFilename()))) {
                    String subDirectory = remoteDirectory + "/"+entry.getFilename();
                    System.out.println("Remote Directory : "+subDirectory);
                    //showRemoteFiles(channelSftp, subDirectory);
                }

            } else {
                System.out.println("Filename : "+entry.getFilename());
            }
            //System.out.println("Long Name : "+entry.getLongname());

        }
    }

    public static void showFile(File file) {
        if (file.isDirectory()) {
            System.out.println("Directory: " + file.getAbsolutePath());
        } else {
            System.out.println("File: " + file.getAbsolutePath());
        }
    }

    private ChannelSftp setupJsch() throws JSchException {
        Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");

        JSch jsch = new JSch();
        jsch.setKnownHosts("/Users/dejazmachmolla/.ssh/known_hosts");
        Session jschSession = jsch.getSession("dejazmach", "192.168.13.228");

        jschSession.setConfig(config);
        jschSession.setPassword("admin");
        jschSession.connect();
        return (ChannelSftp) jschSession.openChannel("sftp");
    }
}
