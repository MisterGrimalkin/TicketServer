package net.amarantha.ticketserver;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;


import java.net.URI;

public class Main {

    public static void main(String[] args) {

        startWebService("192.168.0.17");
        while ( true ) {}

    }

    private static org.glassfish.grizzly.http.server.HttpServer server;

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
