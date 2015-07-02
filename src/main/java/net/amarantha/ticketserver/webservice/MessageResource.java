package net.amarantha.ticketserver.webservice;

import net.amarantha.ticketserver.entity.MessageBundle;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.javalite.http.Http;
import org.javalite.http.Post;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static net.amarantha.ticketserver.webservice.RegistrationResource.lightboardIps;

@Path("")
public class MessageResource {

    private static final String filename = "messages.json";

    private static MessageBundle.Wrapper wrapper;

    public static void loadMessages() {
        try {
            String jsonString = new String(Files.readAllBytes(Paths.get(filename)));
            JSONObject json = JSONObject.fromObject(jsonString);
            JSONArray ja = json.getJSONArray("bundles");
            wrapper = new MessageBundle.Wrapper();
            Iterator<JSONObject> iter = ja.iterator();
            while ( iter.hasNext() ) {
                JSONObject obj = iter.next();
                MessageBundle bundle = (MessageBundle)JSONObject.toBean(obj, MessageBundle.class);
                wrapper.addBundle(bundle);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void saveMessages() {
        try {
            JSONObject json = JSONObject.fromObject(wrapper);
            FileWriter writer = new FileWriter(filename);
            writer.write(json.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GET
    @Path("messages")
    public static Response getMessages() {
        JSONObject json = JSONObject.fromObject(wrapper);
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity(json.toString())
                .build();
    }

    @POST
    @Path("add-message")
    public static Response addMessage(@QueryParam("setId") int set, @QueryParam("text") String text) {
        MessageBundle bundle = wrapper.loadBundle(set);
        if ( bundle!=null ) {
            bundle.addMessage(UUID.randomUUID().toString(), text);
            try {
                saveMessages();
                pushMessagesToAllLightBoards();
            } catch ( Exception e ) {
                return Response.serverError()
                        .header("Access-Control-Allow-Origin", "*")
                        .entity(e.getMessage())
                        .build();
            }
            return Response.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("Message Deleted")
                    .build();
        }
        return Response.serverError()
                .header("Access-Control-Allow-Origin", "*")
                .entity("Message Set Not Found")
                .build();
    }

    @POST
    @Path("update-message")
    public static Response updateMessage(@QueryParam("setId") int set, @QueryParam("msgId") String msg, @QueryParam("text") String text) {
        MessageBundle bundle = wrapper.loadBundle(set);
        if ( bundle!=null ) {
            String message = bundle.getMessages().get(msg);
            if ( message!=null ) {
                bundle.getMessages().remove(msg);
                bundle.getMessages().put(msg, text);
                try {
                    saveMessages();
                    pushMessagesToAllLightBoards();
                } catch ( Exception e ) {
                    return Response.serverError()
                            .header("Access-Control-Allow-Origin", "*")
                            .entity(e.getMessage())
                            .build();
                }
                return Response.ok()
                        .header("Access-Control-Allow-Origin", "*")
                        .entity("Message Deleted")
                        .build();
            }
        }
        return Response.serverError()
                .header("Access-Control-Allow-Origin", "*")
                .entity("Message Not Found")
                .build();
    }

    @POST
    @Path("remove-message")
    public static Response deleteMessage(@QueryParam("setId") int set, @QueryParam("msgId") String msg) {
        MessageBundle bundle = wrapper.loadBundle(set);
        if ( bundle!=null ) {
            if ( bundle.getMessages().remove(msg)!=null ) {
                try {
                    saveMessages();
                    pushMessagesToAllLightBoards();
                } catch ( Exception e ) {
                    return Response.serverError()
                            .header("Access-Control-Allow-Origin", "*")
                            .entity("Message Deleted")
                            .build();
                }
                return Response.ok()
                        .header("Access-Control-Allow-Origin", "*")
                        .entity("Message Deleted")
                        .build();
            }
        }
        return Response.serverError()
                .header("Access-Control-Allow-Origin", "*")
                .entity("Message Not Found")
                .build();
    }

    public static void pushMessagesToAllLightBoards() throws Exception {
        JSONObject json = JSONObject.fromObject(wrapper);
        for ( String ip : lightboardIps ) {
            try {
                Post post = Http.post("http://" + ip + ":8001/lightboard/messages", json.toString());
                if ( post.responseCode()!=200 ) {
                    throw new Exception();
                }
            } catch ( Exception e ) {
                throw new Exception("Error contacting LightBoard at " + ip);
            }
        }
    }

    private static Timer broadcastTimer;

    private static String broadcastMessage;
    private static Integer broadcastInterval;

    @POST
    @Path("broadcast")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public static Response broadcastMessage(final String message, @QueryParam("interval") Integer interval) {
        broadcastMessage = message;
        broadcastInterval = interval;
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

    @GET
    @Path("broadcast")
    @Produces(MediaType.TEXT_PLAIN)
    public static Response getBroadcastSettings() {
        JSONObject json = new JSONObject();
        json.put("text", broadcastMessage);
        json.put("interval", broadcastInterval);
        System.out.println(json.toString());
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity(json.toString())
                .build();
    }

}
