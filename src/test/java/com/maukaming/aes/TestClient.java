package com.maukaming.aes;

import com.maukaming.aes.exchange.*;
import com.maukaming.aes.message.AbstractMessage;

import java.util.LinkedList;
import java.util.List;

public class TestClient implements MessageListener {
    private final List<AbstractMessage> messages = new LinkedList<>();

    @Override
    public void onMessage(AbstractMessage message) {
        System.out.println("Received " + message.getClass().getSimpleName());
        this.messages.add(message);
    }

    public List<AbstractMessage> getMessages() {
        return messages;
    }
}
