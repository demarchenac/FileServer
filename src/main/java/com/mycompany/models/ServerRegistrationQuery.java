/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.models;

import java.util.ArrayList;

/**
 *
 * @author M4rchen
 */
public class ServerRegistrationQuery {
    private String serverIp;
    private ArrayList<String> fileList;

    public ServerRegistrationQuery(String serverIp, ArrayList<String> fileList) {
        this.serverIp = serverIp;
        this.fileList = fileList;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public ArrayList<String> getFileList() {
        return fileList;
    }

    public void setFileList(ArrayList<String> fileList) {
        this.fileList = fileList;
    }
    
    
}
