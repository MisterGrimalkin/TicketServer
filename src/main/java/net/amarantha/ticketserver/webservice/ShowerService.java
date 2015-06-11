package net.amarantha.ticketserver.webservice;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("shower")
public class ShowerService {

    @GET
    @Path("health")
    @Produces(MediaType.TEXT_PLAIN)
    public static Response heathCheck() {
        System.out.println("Health Check OK");
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity("Shower Service is Alive").build();
    }

    @GET
    @Path("next-tickets")
    @Produces(MediaType.TEXT_PLAIN)
    public static Response getNextTicketNumbers() {

        JSONObject male = new JSONObject();
        male.put("name","male");
        JSONArray maleTickets = new JSONArray();
        for ( int i=1; i<=7; i++ ) {
            JSONObject inner = new JSONObject();
            inner.put("number", i+9);
            maleTickets.add(inner);
        }
        male.put("tickets", maleTickets);

        JSONObject female = new JSONObject();
        female.put("name", "female");
        JSONArray femaleTickets = new JSONArray();
        for ( int i=1; i<=7; i++ ) {
            JSONObject inner = new JSONObject();
            inner.put("number", i+14);
            femaleTickets.add(inner);
        }
        female.put("tickets", femaleTickets);

        JSONObject jsonWrapper = new JSONObject();
        JSONArray blocks = new JSONArray();
        blocks.add(male);
        blocks.add(female);

        jsonWrapper.put("blocks", blocks);

        String result = jsonWrapper.toString();
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .entity(result)
                .build();
    }

}
