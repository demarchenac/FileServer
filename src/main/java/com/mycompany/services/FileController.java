/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.services;

import com.google.gson.Gson;
import com.mycompany.listeners.ServletContextManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
}
