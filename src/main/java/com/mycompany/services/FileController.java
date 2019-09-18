/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.services;

import com.google.gson.Gson;
import com.mycompany.listeners.ServletContextManager;
import com.mycompany.models.FileRegistrationQuery;
import com.mycompany.utils.Constants;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author user
 */
@Path("files")
public class FileController {
    private final Properties props;
    private final Gson gson;
    
    
    public FileController() {
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
    
    @GET
    @Path("/{filename}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile(@PathParam("filename") String filename) {
      File file = new File(props.getProperty("ROOT_FOLDER") +'/' +filename);
      return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
          .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"" ) //optional
          .build();
    }
    
    @PUT
    @Path("/{filename}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String fileAdded(@PathParam("filename") String filename) {
      
        try {
            com.mycompany.models.Response r;
            r = registerFileServer(props.getProperty("WebPool"), filename);
            if(r.isSuccess()){
                return gson.toJson(new com.mycompany.models.Response(true, "", filename +" added!"));
            }else{
                return gson.toJson(new com.mycompany.models.Response(false, "Error transfering file!", ""));
            }
        } catch (Exception ex) {
            return gson.toJson(new com.mycompany.models.Response(false, "Service unavailable", ""));
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
