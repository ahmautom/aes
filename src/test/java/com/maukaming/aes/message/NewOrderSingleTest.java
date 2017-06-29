package com.maukaming.aes.message;

import com.maukaming.aes.order.OrderSide;
import com.maukaming.aes.order.OrderType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NewOrderSingleTest {
    @Test
    public void testBuilder() {
        NewOrderSingle message = (new NewOrderSingle.Builder())
                .from("FROM")
                .clOrdID("CL_ORD_ID")
                .orderType(OrderType.LIMIT)
                .orderSide(OrderSide.BUY)
                .symbol("0001.HK")
                .price(100.0)
                .quantity(1000)
                .build();

        assertEquals("FROM", message.getFrom());
        assertEquals("CL_ORD_ID", message.getClOrdID());
        assertEquals(OrderType.LIMIT, message.getOrderType());
        assertEquals(OrderSide.BUY, message.getOrderSide());
        assertEquals("0001.HK", message.getSymbol());
        assertEquals(100.0, message.getPrice());
        assertEquals(1000, message.getQuantity());
    }
}
