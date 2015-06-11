package net.amarantha.ticketserver;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TicketInfo {

    String name;

    public TicketInfo(String name) {
        this.name = name;
    }
}
