package com.maukaming.aes.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CancelRejectedTest {
    @Test
    public void testBuilder() {
        CancelRejected message = (new CancelRejected.Builder())
                .from("FROM")
                .clOrdID("CL_ORD_ID")
                .origClOrdID("ORIG_CL_ORD_ID")
                .text("TEXT")
                .build();

        assertEquals("FROM", message.getFrom());
        assertEquals("CL_ORD_ID", message.getClOrdID());
        assertEquals("ORIG_CL_ORD_ID", message.getOrigClOrdID());
        assertEquals("TEXT", message.getText());
    }
}
