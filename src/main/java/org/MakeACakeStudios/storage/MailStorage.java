package org.MakeACakeStudios.storage;

import java.util.*;

public class MailStorage {
    private final Map<String, List<String[]>> messages = new HashMap<>();

    public void addMessage(String recipient, String senderPrefix, String sender, String senderSuffix , String message) {
        messages.putIfAbsent(recipient, new ArrayList<>());
        messages.get(recipient).add(new String[]{senderPrefix, sender, senderSuffix, message});
    }

    public List<String[]> getMessages(String recipient) {
        return messages.getOrDefault(recipient, new ArrayList<>());
    }

    public void clearMessages(String recipient) {
        messages.remove(recipient);
    }
}