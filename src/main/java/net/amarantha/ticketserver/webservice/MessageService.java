package net.amarantha.ticketserver.webservice;

import net.amarantha.ticketserver.entity.MessageBundle;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

//@Path("messages")
public class MessageService {

    private static Map<Integer, String> messages = new HashMap<Integer, String>();
    private static MessageBundle.Wrapper wrapper = new MessageBundle.Wrapper();

    private static int id = 0;

//    @DELETE
//    @Produces(MediaType.TEXT_PLAIN)
    public static Response deleteMessage(@QueryParam("id") int id) {
        messages.remove(id);
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity("Message Removed: " + id)
                .build();
    }

//    @POST
//    @Consumes(MediaType.TEXT_PLAIN)
//    @Produces(MediaType.TEXT_PLAIN)
    public static Response postMessage(String message) {




        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity("New Message: " + (id - 1))
                .build();
    }

//    @GET
//    @Produces(MediaType.TEXT_PLAIN)
    public static Response getMessages() {
        JSONObject json = new JSONObject();
        JSONArray ja = new JSONArray();
        for ( Map.Entry<Integer, String> entry : messages.entrySet() ) {
            JSONObject obj = new JSONObject();
            obj.put("id", entry.getKey());
            obj.put("message", entry.getValue());
            ja.add(obj);
        }
        json.put("messages", ja);
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity(json.toString())
                .build();
    }

}
