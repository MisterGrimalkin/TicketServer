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
import java.util.*;

import static net.amarantha.ticketserver.webservice.ShowerResource.pushTicketsToAllLightBoards;

@Path("")
public class RegistrationResource {

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
        MessageBundle bundle = wrapper.getBundle(set);
        if ( bundle!=null ) {
            bundle.addMessage(UUID.randomUUID().toString(), text);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    saveMessages();
                    pushMessagesToAllLightBoards();
                }
            }, 2000);
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
        MessageBundle bundle = wrapper.getBundle(set);
        if ( bundle!=null ) {
            String message = bundle.getMessages().get(msg);
            if ( message!=null ) {
                bundle.getMessages().remove(msg);
                bundle.getMessages().put(msg, text);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        saveMessages();
                        pushMessagesToAllLightBoards();
                    }
                }, 2000);
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
        MessageBundle bundle = wrapper.getBundle(set);
        if ( bundle!=null ) {
            if ( bundle.getMessages().remove(msg)!=null ) {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        saveMessages();
                        pushMessagesToAllLightBoards();
                    }
                }, 2000);
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

//    @POST
//    @Path("message")
//    public static Response setupMessages() {
//        wrapper = new MessageBundle.Wrapper();
//
//        wrapper
//                .addBundle(
//                        new MessageBundle(1, "Admin", 1, "red")
//                                .addMessage("1", "Please Collect A Ticket From The Shower Steward")
//                                .addMessage("2", "If Your Number Has Already Been Called You Can Still Use It")
//                                .addMessage("3", "Another Shower Is Possible")
//                )
//                .addBundle(
//                        new MessageBundle(2, "User", 2, "green")
//                                .addMessage("1", "Awesome bands on tonight")
//                                .addMessage("2", "Isn't this great?")
//                                .addMessage("3", "Some Other Stuff")
//                                .addMessage("4", "And We Have Things to Tell you")
//                                .addMessage("5", "I need some breakfast")
//                );
//
//        return Response.ok()
//                .header("Access-Control-Allow-Origin", "*")
//                .entity("Messages Added")
//                .build();
//    }

    public static void pushMessagesToAllLightBoards() {
        JSONObject json = JSONObject.fromObject(wrapper);
        for ( String ip : lightboardIps ) {
            try {
                Post post = Http.post("http://" + ip + ":8001/lightboard/messages", json.toString());
                if ( post.responseCode()==200 ) {
                    System.out.println("LightBoard @ " + ip + " OK\n");
                } else {
                    System.out.println("LightBoard @ " + ip + " FAILED!\n");
                }
            } catch ( Exception e ) {
                System.out.println("LightBoard @ " + ip + " DID NOT RESPOND!\n");
            }
        }
    }

    static Set<String> lightboardIps = new HashSet<>();

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
    public static Response registerLightBoard(final String ip) {
        System.out.println("Registered " + ip);
        lightboardIps.add(ip);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                pushTicketsToAllLightBoards();
                pushMessagesToAllLightBoards();
            }
        }, 2000);
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

    @GET
    @Path("hello")
    public static Response sayHello() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity("Hello there!")
                .build();
    }


}
