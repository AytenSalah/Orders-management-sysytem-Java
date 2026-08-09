package com.ordersystem.service;

import com.ordersystem.exception.CustomerNotFoundException;
import com.ordersystem.exception.DuplicateEmailException;
import com.ordersystem.model.Customer;
import com.ordersystem.repository.CustomerRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Business Logic Layer for Customer operations.
 */
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("No customer to be created");
        }

        String email = customer.getEmail();

        if (email != null && customerRepository.findByEmail(email).isPresent()) {
            throw new DuplicateEmailException(email);
        }

        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Customer customer) {
        if (customer == null || customer.getId() <= 0) {
            throw new IllegalArgumentException("Customer id must be provided for update");
        }
        if (customerRepository.findById(customer.getId()).isEmpty()) {
            throw new CustomerNotFoundException(customer.getId());
        }
        customerRepository.update(customer);
        return customer;
    }

    public List<Customer> searchCustomers(String keyword) {

        List<Customer> customersList = customerRepository.findAll();

        if (customersList.isEmpty()) {
            throw new IllegalArgumentException("There are no customers found");
        }

        if (keyword == null) {
            throw new IllegalArgumentException("Search keyword must not be empty");
        }

        List<Customer> matchedCustomers = new ArrayList<>();

        for (Customer customer : customersList) {
            String name = customer.getName();
            String email = customer.getEmail();

            boolean nameMatches = name != null && name.toLowerCase().contains(keyword.toLowerCase());
            boolean emailMatches = email != null && email.toLowerCase().contains(keyword.toLowerCase());

            if (nameMatches || emailMatches) {
                matchedCustomers.add(customer);
            }
        }

        return matchedCustomers;
    }

    public List<Customer> listCustomers() {
        return customerRepository.findAll();
    }

    public void deleteCustomer(long id) {
        if (customerRepository.findById(id).isEmpty()) {
            throw new CustomerNotFoundException(id);
        }
        customerRepository.delete(id);
    }
}
