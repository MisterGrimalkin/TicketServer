package net.amarantha.ticketserver.webservice;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.javalite.http.Get;
import org.javalite.http.Http;
import org.javalite.http.Post;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static net.amarantha.ticketserver.webservice.MessageResource.pushMessagesToAllLightBoards;
import static net.amarantha.ticketserver.webservice.ShowerResource.pushTicketsToAllLightBoards;

@Path("")
public class RegistrationResource {

    static Set<String> lightboardIps = new HashSet<>();

    @POST
    @Path("clear-registrations")
    @Produces(MediaType.TEXT_PLAIN)
    public static Response clearRegistrations() {
        lightboardIps = new HashSet<>();
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity("You are now registered")
                .build();
    }

    @POST
    @Path("register")
    @Produces(MediaType.TEXT_PLAIN)
    public static Response registerLightBoard(final String ip) {
        lightboardIps.add(ip);
        System.out.println("Registered " + ip);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    pushTicketsToAllLightBoards();
                    pushMessagesToAllLightBoards();
                } catch ( Exception e ) {
                    System.out.println(e.getMessage());
                }
            }
        }, 2000);
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity("You are now registered")
                .build();
    }

    @POST
    @Path("deregister")
    public static Response deregisterLightBoard(final String ip) {
        boolean removed = lightboardIps.remove(ip);
        System.out.println("De-registered " + ip);
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity(removed ? "Board De-registered" : "Unknown Board")
                .build();
    }

    @POST
    @Path("shutdown")
    @Produces(MediaType.TEXT_PLAIN)
    public static Response shutdown() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.exit(0);
            }
        }, 1000);
        for ( String ip : lightboardIps ) {
            Post post = Http.post("http://" + ip + ":8001/lightboard/system/shutdown", "");
            if (post.responseCode() != 200) {
                String message = "Board " + ip + " could not be shut down.";
            }
        }
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity("Shutting down")
                .build();
    }

    @GET
    @Path("lightboards")
    @Produces(MediaType.APPLICATION_JSON)
    public static Response getLightBoards() {
        String message = "";
        JSONObject json = new JSONObject();
        JSONArray ja = new JSONArray();
        for ( String s : lightboardIps) {
            JSONObject innerJson = new JSONObject();
            innerJson.put("ip", s);
            try {
                Get get = Http.get("http://" + s + ":8001/lightboard/system/name");
                if (get.responseCode() == 200) {
                    innerJson.put("status", "OK");
                    innerJson.put("message", "'"+get.text()+"' is Alive");
                } else {
                    innerJson.put("status", "ERROR");
                    innerJson.put("message", "ERROR (code "+get.responseCode());
                }
            } catch (Exception e) {
                innerJson.put("status", "ERROR");
                innerJson.put("message", "ERROR");
            }
            ja.add(innerJson);
        }
        json.put("boards", ja);
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity(json.toString())
                .build();
    }

    @POST
    @Path("update-all")
    @Produces(MediaType.TEXT_PLAIN)
    public static Response updateAllLightBoards() {
        try {
            pushMessagesToAllLightBoards();
            pushTicketsToAllLightBoards();
            return Response.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("All Boards Updated")
                    .build();
        } catch (Exception e) {
            return Response.serverError()
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("hello")
    public static Response sayHello() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity("Hello there!")
                .build();
    }

}
