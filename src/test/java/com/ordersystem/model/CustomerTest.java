package com.ordersystem.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CustomerTest {

    @Test
    void defaultConstructorInitializesFields() {
        Customer c = new Customer();

        assertEquals(0L, c.getId());
        assertNull(c.getName());
        assertNull(c.getEmail());
        assertEquals(0, c.getPhone());
        assertNull(c.getAddress());
    }

    @Test
    void allArgsConstructorSetsFields() {
        Customer c = new Customer(7L, "Ali", "ali@example.com", 123456, "Cairo");

        assertEquals(7L, c.getId());
        assertEquals("Ali", c.getName());
        assertEquals("ali@example.com", c.getEmail());
        assertEquals(123456, c.getPhone());
        assertEquals("Cairo", c.getAddress());
    }

    @Test
    void settersAndGettersRoundTrip() {
        Customer c = new Customer();
        c.setId(99L);
        c.setName("Sara");
        c.setEmail("sara@example.com");
        c.setPhone(111);
        c.setAddress("Giza");

        assertEquals(99L, c.getId());
        assertEquals("Sara", c.getName());
        assertEquals("sara@example.com", c.getEmail());
        assertEquals(111, c.getPhone());
        assertEquals("Giza", c.getAddress());
    }
}
