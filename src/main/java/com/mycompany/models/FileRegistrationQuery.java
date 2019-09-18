/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.models;

/**
 *
 * @author user
 */
public class FileRegistrationQuery {
    private String serverIp;
    private String filename;

    public FileRegistrationQuery(String serverIp, String filename) {
        this.serverIp = serverIp;
        this.filename = filename;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String fileList) {
        this.filename = fileList;
    }
}
