package net.amarantha.ticketserver.webservice;

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

import static net.amarantha.ticketserver.webservice.RegistrationResource.lightboardIps;

@Path("shower")
public class ShowerResource {

    private static final String filename = "ticketnumbers.json";

    private static int nextFemaleTicket = 0;
    private static int nextMaleTicket = 0;

    public static void loadTicketNumbers() {
        try {
            String jsonString = new String(Files.readAllBytes(Paths.get(filename)));
            JSONObject obj = JSONObject.fromObject(jsonString);
            nextFemaleTicket = obj.getInt("female");
            nextMaleTicket = obj.getInt("male");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveTicketNumbers() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("female", nextFemaleTicket);
            obj.put("male", nextMaleTicket);
            FileWriter writer = new FileWriter(filename);
            writer.write(obj.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("female")
    public static Response getFemaleTicket() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity(nextFemaleTicket)
                .build();
    }

    @POST
    @Path("female")
    public static Response setFemaleTicket(@QueryParam("number") int number) {
        try {
            nextFemaleTicket = number;
            saveTicketNumbers();
            pushTicketsToAllLightBoards();
            return Response.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("Updated")
                    .build();
        } catch ( Exception e ) {
            return Response.serverError()
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("male")
    @Produces(MediaType.TEXT_PLAIN)
    public static Response getMaleTicket() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity(nextMaleTicket)
                .build();
    }

    @POST
    @Path("male")
    public static Response setMaleTicket(@QueryParam("number") int number) {
        try {
            nextMaleTicket = number;
            saveTicketNumbers();
            pushTicketsToAllLightBoards();
            return Response.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .entity("Updated")
                    .build();
        } catch ( Exception e ) {
            return Response.serverError()
                    .header("Access-Control-Allow-Origin", "*")
                    .entity(e.getMessage())
                    .build();
        }
    }

    public static void pushTicketsToAllLightBoards() throws Exception {
        for ( String ip : lightboardIps ) {
            Post post = Http.post("http://" + ip + ":8001/lightboard/ticket?female=" + nextFemaleTicket + "&male=" + nextMaleTicket, "");
            if (post.responseCode() != 200) {
                String message = "Board " + ip + " could not be updated.";
                System.out.println(message);
                throw new Exception(message);
            }
        }
    }

}

