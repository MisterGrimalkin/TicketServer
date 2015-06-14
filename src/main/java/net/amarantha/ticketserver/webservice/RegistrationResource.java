package net.amarantha.ticketserver.webservice;

import org.javalite.http.Http;
import org.javalite.http.Post;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Path("")
public class RegistrationResource {

    private static List<String> lightboardIps = new ArrayList<>();

    private static Timer broadcastTimer;

    @POST
    @Path("broadcast")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public static Response broadcastMessage(final String message, @QueryParam("interval") Integer interval) {
        String resp;
        if ( interval==null ) {
            resp = "Broadcast Once\n" + broadcastToAllLightBoards(message);
        } else {
            resp = "Broadcast Every " + interval + " seconds\n";
            if ( broadcastTimer!=null ) {
                broadcastTimer.cancel();
            }
            broadcastTimer = new Timer();
            broadcastTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    broadcastToAllLightBoards(message);
                }
            }, 0, interval*1000);
        }
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity(resp)
                .build();
    }

    @POST
    @Path("cancel-broadcast")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public static Response cancelBroadcast() {
        if ( broadcastTimer!=null ) {
            broadcastTimer.cancel();
            broadcastTimer = null;
            return Response.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("Broadcast Message Cancelled")
                    .build();
        } else {
            return Response.serverError()
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("No Broadcast Message To Cancel")
                    .build();
        }
    }

    private static String broadcastToAllLightBoards(String message) {
        String resp = "";
        for ( String ip : lightboardIps ) {
            try {
                Post post = Http.post("http://" + ip + ":8001/lightboard/message", message);
                if ( post.responseCode()==200 ) {
                    resp += "LightBoard @ " + ip + " OK\n";
                } else {
                    resp += "LightBoard @ " + ip + " FAILED!\n";
                }
            } catch ( Exception e ) {
                resp += "LightBoard @ " + ip + " DID NOT RESPOND!\n";
            }
        }
        return resp;
    }

//    @POST
//    public static Response setMessages(String messages) {
//
//    }

    @POST
    @Path("register")
    @Produces(MediaType.TEXT_PLAIN)
    public static Response registerLightBoard(String ip) {
        System.out.println("Registered " + ip);
        lightboardIps.add(ip);
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity("You are now registered")
                .build();
    }

    @GET
    @Path("lightboards")
    @Produces(MediaType.TEXT_PLAIN)
    public static Response getLightBoards() {
        String message = "";
        for ( String s : lightboardIps) {
            message += s + "\n";
        }
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity(message)
                .build();
    }


}
