package net.amarantha.ticketserver.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageBundle {

    private int id;
    private String name;
    private int maxMessages;
    private String defaultColour;
    private Map<String, String> messages;

    public MessageBundle() {
        this(0);
    }

    public MessageBundle(int id) {
        messages = new HashMap<>();
        this.id = id;

    }

    public MessageBundle(int id, String name, int maxMessages, String defaultColour) {
        this(id);
        this.name = name;
        this.maxMessages = maxMessages;
        this.defaultColour = defaultColour;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    public String getDefaultColour() {
        return defaultColour;
    }

    public void setDefaultColour(String defaultColour) {
        this.defaultColour = defaultColour;
    }

    public Map<String, String> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, String> messages) {
        this.messages = messages;
    }

    public MessageBundle addMessage(String id, String message) {
        messages.put(id, message);
        return this;
    }


    public static class Wrapper {

        private List<MessageBundle> bundles;

        public Wrapper() {
            bundles = new ArrayList<>();
        }

        public List<MessageBundle> getBundles() {
            return bundles;
        }

        public MessageBundle loadBundle(int id) {
            for ( MessageBundle bundle : bundles ) {
                if ( bundle.getId()==id ) {
                    return bundle;
                }
            }
            return null;
        }

        public void setBundles(List<MessageBundle> bundles) {
            this.bundles = bundles;
        }

        public Wrapper addBundle(MessageBundle bundle) {
            bundles.add(bundle);
            return this;
        }

    }
}
