package com.maukaming.aes.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FillTest {
    @Test
    public void testBuilder() {
        Fill message = (new Fill.Builder())
                .from("FROM")
                .clOrdID("CL_ORD_ID")
                .avgPrice(96.0)
                .cumQuantity(800)
                .leaveQuantity(200)
                .lastPrice(100.0)
                .lastShare(100)
                .build();

        assertEquals("FROM", message.getFrom());
        assertEquals("CL_ORD_ID", message.getClOrdID());
        assertEquals(96.0, message.getAvgPrice());
        assertEquals(800, message.getCumQuantity());
        assertEquals(200, message.getLeaveQuantity());
        assertEquals(100.0, message.getLastPrice());
        assertEquals(100, message.getLastShare());
    }
}
