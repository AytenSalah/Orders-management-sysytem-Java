package com.ordersystem.repository.impl;

import com.ordersystem.exception.CustomerNotFoundException;
import com.ordersystem.model.Customer;
import com.ordersystem.repository.CustomerRepository;
import com.ordersystem.util.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite/JDBC implementation of CustomerRepository.
 */
public class CustomerRepositoryJdbc implements CustomerRepository {

    @Override
    public Customer save(Customer customer) {

        if (customer.getName() == null) {
            throw new IllegalArgumentException("Customer Name is required.");
        }
        if (customer.getEmail() == null) {
            throw new IllegalArgumentException("Customer Email is required.");
        }
        if (customer.getPhone() < 0) {
            throw new IllegalArgumentException("Customer Number must be positive.");
        }
        if (customer.getAddress() == null) {
            throw new IllegalArgumentException("Customer Address is required.");
        }

        String sql = "INSERT INTO customers (name, email, phone, address) VALUES (?, ?, ?, ?)";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getEmail());
            stmt.setInt(3, customer.getPhone());
            stmt.setString(4, customer.getAddress());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    customer.setId(keys.getLong(1));
                }
            }

            return customer;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save customer", e);
        }
    }

    @Override
    public Optional<Customer> findById(long id) {

        if (id <= 0) {
            throw new IllegalArgumentException("Customer ID must be Positive.");
        }

        String sql = "SELECT * FROM customers WHERE id = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Customer customer = new Customer();
                    customer.setId(rs.getLong("id"));
                    customer.setName(rs.getString("name"));
                    customer.setEmail(rs.getString("email"));
                    customer.setPhone(rs.getInt("phone"));
                    customer.setAddress(rs.getString("address"));

                    return Optional.of(customer);
                } else {
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find customer", e);
        }
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Customer email is required.");
        }

        String sql = "SELECT * FROM customers WHERE email = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Customer customer = new Customer();
                    customer.setId(rs.getLong("id"));
                    customer.setName(rs.getString("name"));
                    customer.setEmail(rs.getString("email"));
                    customer.setPhone(rs.getInt("phone"));
                    customer.setAddress(rs.getString("address"));

                    return Optional.of(customer);
                } else {
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find customer", e);
        }
    }

    @Override
    public List<Customer> findAll() {
        String sql = "SELECT * FROM customers";
        List<Customer> customers = new ArrayList<>();

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Customer customer = new Customer();
                customer.setId(rs.getLong("id"));
                customer.setName(rs.getString("name"));
                customer.setEmail(rs.getString("email"));
                customer.setPhone(rs.getInt("phone"));
                customer.setAddress(rs.getString("address"));

                customers.add(customer);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find customers", e);
        }

        return customers;
    }

    @Override
    public void update(Customer customer) {

        if (customer == null || customer.getId() <= 0) {
            throw new IllegalArgumentException("Valid customer with an ID is required to update.");
        }
        if (customer.getName() == null) {
            throw new IllegalArgumentException("Customer Name is required.");
        }
        if (customer.getEmail() == null) {
            throw new IllegalArgumentException("Customer Email is required.");
        }
        if (customer.getPhone() < 0) {
            throw new IllegalArgumentException("Customer Phone must be positive.");
        }
        if (customer.getAddress() == null) {
            throw new IllegalArgumentException("Customer Address is required.");
        }

        String sql = "UPDATE customers SET name = ?, email = ?, phone = ?, address = ? WHERE id = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getEmail());
            stmt.setInt(3, customer.getPhone());
            stmt.setString(4, customer.getAddress());
            stmt.setLong(5, customer.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new CustomerNotFoundException(customer.getId());
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update customer", e);
        }
    }

    @Override
    public void delete(long id) {

        if (id <= 0) {
            throw new IllegalArgumentException("Customer ID must be a positive number.");
        }

        String sql = "DELETE FROM customers WHERE id = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new CustomerNotFoundException(id);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete customer", e);
        }
    }
}
