package com.maukaming.aes.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CancelRequestTest {
    @Test
    public void testBuilder() {
        CancelRequest message = (new CancelRequest.Builder())
                .from("FROM")
                .clOrdID("CL_ORD_ID")
                .origClOrdID("ORIG_CL_ORD_ID")
                .build();

        assertEquals("FROM", message.getFrom());
        assertEquals("CL_ORD_ID", message.getClOrdID());
        assertEquals("ORIG_CL_ORD_ID", message.getOrigClOrdID());
    }
}
