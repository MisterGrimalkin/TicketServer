package net.amarantha.ticketserver.webservice;

import net.sf.json.JSONObject;
import org.javalite.http.Http;
import org.javalite.http.Post;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;

import static net.amarantha.ticketserver.webservice.RegistrationResource.lightboardIps;

@Path("shower")
public class ShowerResource {

    private static final String filename = "ticketnumbers.json";

    private static int nextFemaleTicket = 1;
    private static int nextMaleTicket = 2;

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
        nextFemaleTicket = number;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                saveTicketNumbers();
                pushTicketsToAllLightBoards();
            }
        }, 2000);
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity("Updated")
                .build();
    }

    @GET
    @Path("male")
    public static Response getMaleTicket() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity(nextMaleTicket)
                .build();
    }

    @POST
    @Path("male")
    public static Response setMaleTicket(@QueryParam("number") int number) {
        nextMaleTicket = number;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                saveTicketNumbers();
                pushTicketsToAllLightBoards();
            }
        }, 2000);
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity("Updated")
                .build();
    }

    public static void pushTicketsToAllLightBoards() {
        for ( String ip : lightboardIps ) {
            try {
                Post post = Http.post("http://" + ip + ":8001/lightboard/ticket?female=" + nextFemaleTicket + "&male=" + nextMaleTicket, "");
                if (post.responseCode() != 200) {
                    System.out.println("ERROR Posting Shower Ticket Numbers to " + ip);
                }
            } catch ( Exception e ) {
                System.out.println("ERROR Posting Shower Ticket Numbers to " + ip);
            }
        }
    }

}

