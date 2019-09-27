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
import java.io.File;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;

import com.google.gson.Gson;
import com.mycompany.models.FileRegistrationQuery;
import com.mycompany.models.Response;

import com.mycompany.models.ServerRegistrationQuery;
import com.mycompany.utils.Constants;
import java.io.FileOutputStream;
import org.apache.http.client.methods.HttpGet;

/**
 *
 * @author user
 */
public class ServletContextManager implements ServletContextListener{

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
            
            System.out.println("[FS] Checking files that could be missing...");
            
            ArrayList<String> missingFiles 
                    = obtainFileList(props.getProperty("WebPool"));
            
            System.out.println("[FS] Files missing: ");
            System.out.println(gson.toJson(missingFiles));
            
            String response = "";
            for(String filename : missingFiles){
                System.out.println("[FS] Attempting to download File: " +filename);
                do{
                    response = donwloadFile(props.getProperty("WebPool"), filename);
                    if(response.equals("success!")){
                        registerFileServer(props.getProperty("WebPool"), filename);
                    }
                }while(response.equals("retry"));
            }
            
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

        ArrayList<String> fileList = new ArrayList<>();
        
        File folder = new File(props.getProperty("ROOT_FOLDER"));
        
        if(! folder.exists()){
            folder.mkdirs();
        }
        
        File[] listOfFiles = folder.listFiles();

        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                fileList.add(listOfFile.getName());
            }
        }
        
        ServerRegistrationQuery srq 
            = new ServerRegistrationQuery(
                InetAddress.getLocalHost().getHostAddress(), 
                fileList);
        
        StringEntity entity = new StringEntity(gson.toJson(srq));
        
        request.setEntity(entity);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");

        System.out.println("[FS] Sending register request");
        HttpResponse response = client.execute(request);
        System.out.println(
            "[FS] Response Code : " + 
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

    private ArrayList<String> obtainFileList(String serverPoolIp) throws Exception{
        ArrayList<String> filesMissing = new ArrayList<>();
        String url = "http://" +serverPoolIp +":8080/WebPool/api/files";
        
        HttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);

        // add request header
        request.addHeader("User-Agent", Constants.USER_AGENT);

        HttpResponse response = client.execute(request);

        System.out.println("[FS] Requesting file list from server pool!");
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
        
        Response noJson = gson.fromJson(result.toString(), Response.class);
        
        ArrayList<String> filesFromServer 
            = gson.fromJson(noJson.getData(), ArrayList.class);
        
        ArrayList<String> fileList = new ArrayList<>();
        
        File folder = new File(props.getProperty("ROOT_FOLDER"));
        
        if(! folder.exists()){
            folder.mkdirs();
        }
        
        File[] listOfFiles = folder.listFiles();

        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                fileList.add(listOfFile.getName());
            }
        }
        
        if(filesFromServer.size() > 0){
            for(String file : filesFromServer){
                if(!fileList.contains(file)){
                    filesMissing.add(file);
                }
            }
        }
        
        return filesMissing;
    }

    private String donwloadFile(String serverPoolIp, String filename){
        String message = "retry";
        int inByte;
        try {
            InputStream is = downloadFileFromServer(serverPoolIp, filename);
            if(is == null){
                message = "404";
            }else{
                FileOutputStream fos 
                    = new FileOutputStream(
                        new File(props.getProperty("ROOT_FOLDER") + "/" + filename));
                 while((inByte = is.read()) != -1){
                    fos.write(inByte);
                }
                is.close();
                fos.close();
                message = "success!";
            }
        } catch (Exception ex) {
            message = "retry";
        }
        return message;
    }

    public InputStream downloadFileFromServer(String server, String filename) throws Exception {
        HttpResponse response = null;
        try {
            filename.replaceAll(" ", "%20");
            String url = "http://" + server + ":8080"  + "/WebPool/api/files" + "/" + filename;

            HttpClient client = HttpClients.createDefault();
            HttpGet request = new HttpGet(url);

            // add request header
            request.addHeader("User-Agent", Constants.USER_AGENT);

            System.out.println("[FS] Requesting " + filename + " to server-pool@" + server);

            response = client.execute(request);

            return response.getEntity().getContent();
        } catch (Exception e) {
            return null;
        }
        
    }

    private com.mycompany.models.Response registerFileServer(String serverPool, String filename) throws Exception {

        String url = "http://" +serverPool +":8080/WebPool/api/files";

        HttpClient client = HttpClients.createDefault();
        HttpPut request = new HttpPut(url);

        // add header
        request.setHeader("User-Agent", Constants.USER_AGENT);
        
        FileRegistrationQuery frq 
            = new FileRegistrationQuery(InetAddress.getLocalHost().getHostAddress(), filename);
        
        StringEntity entity = new StringEntity(gson.toJson(frq));
        
        request.setEntity(entity);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");

        System.out.println("[FS] Sending file registration request");
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

        return gson.fromJson(result.toString(), com.mycompany.models.Response.class);
    }
}
