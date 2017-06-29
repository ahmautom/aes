package com.maukaming.aes.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderRejectedTest {
    @Test
    public void testBuilder() {
        OrderRejected message = (new OrderRejected.Builder())
                .from("FROM")
                .clOrdID("CL_ORD_ID")
                .text("TEXT")
                .build();

        assertEquals("FROM", message.getFrom());
        assertEquals("CL_ORD_ID", message.getClOrdID());
        assertEquals("TEXT", message.getText());
    }
}
