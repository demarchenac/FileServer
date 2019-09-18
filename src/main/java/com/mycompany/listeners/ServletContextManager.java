/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.listeners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;

import com.google.gson.Gson;
import com.mycompany.models.ServerRegistrationQuery;

import com.mycompany.models.SinglePropQuery;
import com.mycompany.utils.Constants;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author user
 */
public class ServletContextManager implements ServletContextListener{

    
    private final String FILE_FOLDER = "D://DISTRIBUIDA/P1/FILES";
    private final Properties props;
    private final Gson gson;
    
    public ServletContextManager() {
        System.out.println("Started File Server -> [FS]");
        System.out.println("[FS] Listener created!");
        this.gson = new Gson();
        
        props = new Properties();
            
        try {
            System.out.println("[FS] Loading props");
            InputStream is = ServletContextManager.class.getClassLoader().getResourceAsStream("config.properties");
            props.load(is);
        } catch (IOException ex) {
            Logger.getLogger(ServletContextManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {    
        System.out.println("[FS] Attempting to connect to server pool");

        try {
            registerFileServer(props.getProperty("WebPool"));
        } catch (Exception ex) {
            System.out.println("[FS] Error with the request logic.");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[FS] Web-app suspending started! ");
        try {
            unregisterFileServer(props.getProperty("WebPool"));
        } catch (Exception exception) {
            System.out.println("[FS] Error with the request logic.");
        }
        System.out.println("[FS] Web-App stopped!");
    }
    
    private void unregisterFileServer(String prop) throws Exception {

        String ip = InetAddress.getLocalHost().getHostAddress();
        String url = "http://" +prop +":8080/WebPool/api/utils" +"/" +ip;

        HttpClient client = HttpClients.createDefault();
        HttpDelete request = new HttpDelete(url);

        // add request header
        request.addHeader("User-Agent", Constants.USER_AGENT);

        HttpResponse response = client.execute(request);

        System.out.println("[FS] Unregistering server from load balancer service!");
        System.out.println("Response Code : " + 
               response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
               new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
                result.append(line);
        }

        System.out.println("[FS] " +result.toString());
    }
    
    private void registerFileServer(String prop) throws Exception {

        String url = "http://" +prop +":8080/WebPool/api/utils";

        HttpClient client = HttpClients.createDefault();
        HttpPut request = new HttpPut(url);

        // add header
        request.setHeader("User-Agent", Constants.USER_AGENT);

        ArrayList<String> fileList = new ArrayList<String>();
        File folder = new File(FILE_FOLDER);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
              fileList.add(listOfFiles[i].getName());
            }
        }
        
        ServerRegistrationQuery srq 
                = new ServerRegistrationQuery(InetAddress.getLocalHost().getHostAddress(), fileList);
        
        StringEntity entity = new StringEntity(gson.toJson(srq));
        
        request.setEntity(entity);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");

        System.out.println("[FS] Sending register request");
        HttpResponse response = client.execute(request);
        System.out.println("[FS] Response Code : " + 
                            response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
                result.append(line);
        }

        System.out.println("[FS] "+result.toString());
    }
}
