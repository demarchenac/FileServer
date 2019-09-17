/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.services;

import com.google.gson.Gson;
import com.mycompany.models.Response;
import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author user
 */
@Path("files")
public class FileController {
    private final String FILE_FOLDER = "D://DISTRIBUIDA/P1/FILES";
    private final Gson gson;
    
    public FileController() {
        this.gson = new Gson();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String listAllFiles() throws UnknownHostException{
        ArrayList<String> fileList = new ArrayList<String>();
        File folder = new File(FILE_FOLDER);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
              fileList.add(listOfFiles[i].getName());
            }
        }
        
        return gson.toJson(new Response(true, "", this.gson.toJson(fileList)));
    }
}
