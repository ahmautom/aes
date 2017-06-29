package com.maukaming.aes.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderAcceptedTest {
    @Test
    public void testBuilder() {
        OrderAccepted message = (new OrderAccepted.Builder())
                .from("FROM")
                .clOrdID("CL_ORD_ID")
                .build();

        assertEquals("FROM", message.getFrom());
        assertEquals("CL_ORD_ID", message.getClOrdID());
    }
}
