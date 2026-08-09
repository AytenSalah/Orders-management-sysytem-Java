package com.ordersystem.exception;

/**
 * Thrown when a customer with the given id cannot be found.
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(long customerId) {
        super("Customer not found with id: " + customerId);
    }
}