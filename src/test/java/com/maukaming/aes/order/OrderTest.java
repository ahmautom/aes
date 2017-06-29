package com.maukaming.aes.order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderTest {
    @Test
    public void testBuilder() {
        Order order = (new Order.Builder())
                .from("FROM")
                .clOrdID("CL_ORD_ID")
                .type(OrderType.LIMIT)
                .side(OrderSide.BUY)
                .symbol("0001.HK")
                .price(100.0)
                .quantity(1000)
                .build();

        assertEquals("FROM", order.getFrom());
        assertEquals(OrderType.LIMIT, order.getType());
        assertEquals("CL_ORD_ID", order.getClOrdID());
        assertEquals(100.0, order.getPrice());
        assertEquals(1000, order.getQuantity());
        assertEquals("0001.HK", order.getSymbol());
        assertEquals(OrderSide.BUY, order.getSide());
        assertEquals(1000, order.getLeaveQuantity());
        assertEquals(0, order.getCumQuantity());
        assertEquals(0, order.getAvgPrice());
        assertEquals(OrderStatus.NEW, order.getStatus());
    }

    @Test
    public void testApplyFill() {
        Order order = (new Order.Builder())
                .from("FROM")
                .clOrdID("CL_ORD_ID")
                .type(OrderType.LIMIT)
                .side(OrderSide.BUY)
                .symbol("0001.HK")
                .price(100.0)
                .quantity(1000)
                .build();
        order.applyFill(99.0, 600);
        order.applyFill(98.0, 300);

        assertEquals("FROM", order.getFrom());
        assertEquals(OrderType.LIMIT, order.getType());
        assertEquals("CL_ORD_ID", order.getClOrdID());
        assertEquals(100.0, order.getPrice());
        assertEquals(1000, order.getQuantity());
        assertEquals("0001.HK", order.getSymbol());
        assertEquals(OrderSide.BUY, order.getSide());
        assertEquals(100, order.getLeaveQuantity());
        assertEquals(900, order.getCumQuantity());
        assertEquals(98.66, order.getAvgPrice(), 0.01);
        assertEquals(OrderStatus.PARTIALLY_FILLED, order.getStatus());
    }

    @Test
    public void testIsActive() {
        Order order = (new Order.Builder())
                .build();
        assertTrue(order.isActive());
        order.setRejected();
        assertFalse(order.isActive());

        order = (new Order.Builder())
                .build();
        order.setCanceled();
        assertFalse(order.isActive());
    }
}
