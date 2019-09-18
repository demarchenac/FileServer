/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.services;

import com.google.gson.Gson;
import com.mycompany.models.Response;
import com.mycompany.models.ServerAddress;
//import com.mycompany.models.Response;
//import com.mycompany.models.ServerAddress;
//import com.mycompany.models.ServerPool;
//import com.mycompany.models.SinglePropQuery;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author user
 */
@Path("utils")
public class UtilController {
    private final Gson gson;
    
    public UtilController() {
        this.gson = new Gson();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getIp() throws UnknownHostException{
        ServerAddress serverAddress = new ServerAddress(InetAddress.getLocalHost().getHostAddress());
        return gson.toJson(new Response(true, "", this.gson.toJson(serverAddress)));
    }
}
