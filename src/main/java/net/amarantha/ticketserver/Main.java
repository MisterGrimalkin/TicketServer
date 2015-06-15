package net.amarantha.ticketserver;

import net.amarantha.ticketserver.webservice.RegistrationResource;
import net.amarantha.ticketserver.webservice.ShowerResource;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;


import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

import static net.amarantha.ticketserver.webservice.RegistrationResource.*;
import static net.amarantha.ticketserver.webservice.ShowerResource.*;

public class Main {

    public static void main(String[] args) {

        startWebService("192.168.0.17");

        loadMessages();
        loadTicketNumbers();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                pushMessagesToAllLightBoards();
                pushTicketsToAllLightBoards();
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
            rc.register(LoggingFilter.class);
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

}
