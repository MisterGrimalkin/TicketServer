package net.amarantha.ticketserver.entity;

import java.util.ArrayList;
import java.util.List;

public class MessageBundle {

    private int bundleId;
    private String name;
    private int maxMessages;
    private String defaultColour;
    private List<Message> messages;

    public MessageBundle() {
        messages = new ArrayList<>();
    }

    public MessageBundle(int bundleId, String name, int maxMessages, String defaultColour) {
        this();
        this.bundleId = bundleId;
        this.name = name;
        this.maxMessages = maxMessages;
        this.defaultColour = defaultColour;
    }

    public int getBundleId() {
        return bundleId;
    }

    public void setBundleId(int bundleId) {
        this.bundleId = bundleId;
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

    public List<Message> getMessages() {
        return messages;
    }

    public List<String> getMessagesText() {
        List<String> result = new ArrayList<>();
        for ( Message m : messages ) {
            result.add(m.getMessage());
        }
        return result;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public MessageBundle addMessage(int id, String message) {
        messages.add(new Message(id, message));
        return this;
    }

    public static class Message {

        private int id;
        private String message;

        public Message() {}

        public Message(int id, String message) {
            this.id = id;
            this.message = message;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

    }


    public static class Wrapper {

        private List<MessageBundle> bundles;

        public Wrapper() {
            bundles = new ArrayList<>();
        }

        public List<MessageBundle> getBundles() {
            return bundles;
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
