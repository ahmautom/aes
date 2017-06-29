package com.maukaming.aes.exchange;

import com.maukaming.aes.message.AbstractMessage;

public interface MessageListener {
    void onMessage(AbstractMessage message);
}
