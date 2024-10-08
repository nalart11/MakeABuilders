package org.MakeACakeStudios.storage;

import java.util.*;

public class MailStorage {
    // Хранит сообщения в формате: получатель -> список сообщений (каждое сообщение - массив из [префикс_отправителя, отправитель, текст сообщения])
    private final Map<String, List<String[]>> messages = new HashMap<>();

    // Метод для добавления сообщения
    public void addMessage(String recipient, String senderPrefix, String sender, String senderSuffix , String message) {
        messages.putIfAbsent(recipient, new ArrayList<>());
        messages.get(recipient).add(new String[]{senderPrefix, sender, senderSuffix, message}); // Сохраняем префикс отправителя, имя отправителя и сообщение
    }

    // Метод для получения сообщений
    public List<String[]> getMessages(String recipient) {
        return messages.getOrDefault(recipient, new ArrayList<>());
    }

    // Метод для очистки сообщений
    public void clearMessages(String recipient) {
        messages.remove(recipient);
    }
}