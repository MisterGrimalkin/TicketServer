package net.amarantha.ticketserver;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import static net.amarantha.ticketserver.webservice.RegistrationResource.*;
import static net.amarantha.ticketserver.webservice.ShowerResource.*;

public class Main {

    public static void main(String[] args) {

        loadConfig();

        startWebService(ip);

        loadMessages();
        loadTicketNumbers();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    pushMessagesToAllLightBoards();
                    pushTicketsToAllLightBoards();
                } catch ( Exception e ) {
                    System.out.println(e.getMessage());
                }
            }
        }, 0, 60000);

        while ( true ) {}

    }

    private static HttpServer server;

    public static HttpServer startWebService(String ip) {
        server = null;
        if ( ip!=null ) {
            System.out.println("Starting Web Service....");
            String fullUri = "http://"+ip+":8002/ticketserver";
            final ResourceConfig rc = new ResourceConfig().packages("net.amarantha.ticketserver.webservice");
//            rc.register(LoggingFilter.class);
            server = GrizzlyHttpServerFactory.createHttpServer(URI.create(fullUri), rc);
            System.out.println("Web Service Online @ " + fullUri);
        }
        return server;
    }

    public static void stopWebService() {
        if ( server!=null ) {
            server.shutdown();
        }
    }

    private static String ip = null;

    public static void loadConfig() {
        try {
            String message = "TicketServer configuration: ";
            Properties prop = new Properties();
            InputStream is = new FileInputStream("ticketserver.properties");
            prop.load(is);
            if ( prop.getProperty("ip")!=null ) {
                ip = prop.getProperty("ip");
                message += " Serving on " + ip;
            }
            System.out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
