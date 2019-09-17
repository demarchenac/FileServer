/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.listeners;

import com.google.gson.Gson;
import com.mycompany.models.SinglePropQuery;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * @author user
 */
public class ServletContextManager implements ServletContextListener{

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            System.out.println("--- Loading props ---");
            Properties prop = new Properties();
            InputStream is = ServletContextManager.class.getClassLoader().getResourceAsStream("config.properties");
            prop.load(is);
            
            System.out.println("--- Attempting to connect to server pool ---");
            String uri = "http://" +prop.getProperty("WebPool") +":8080/WebPool/api/utils";
            
            System.out.println("Uri: " +uri);
            
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            
            Gson gson = new Gson();
            String body = gson.toJson(new SinglePropQuery(InetAddress.getLocalHost().getHostAddress()));
            
            System.out.println("Body: " +body);
            
            try(OutputStream os = con.getOutputStream()) {
                byte[] input = body.getBytes("utf-8");
                os.write(input, 0, input.length);   
                
                try(
                    BufferedReader br 
                        = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), "utf-8")
                        )
                    ){
                      StringBuffer sb = new StringBuffer();
                      String line = null;
                      while ((line = br.readLine()) != null) {
                          sb.append(line.trim());
                      }
                      System.out.println(sb.toString());
                }
            }catch(IOException ioex){
                ioex.printStackTrace();
            }
        } catch (IOException ex) {
            Logger.getLogger(ServletContextManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Bye Bye!");
    }
    
}
