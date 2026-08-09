package com.ordersystem.repository.impl;

import com.ordersystem.exception.CustomerNotFoundException;
import com.ordersystem.model.Customer;
import com.ordersystem.repository.CustomerRepository;
import com.ordersystem.util.DbConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests CustomerRepositoryJdbc against a real SQLite database.
 * The DB url is overridden by surefire (-Ddb.url=jdbc:sqlite:target/test-orders.db)
 * so the real orders.db is never touched.
 */
class CustomerRepositoryJdbcTest {

    private CustomerRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        repository = new CustomerRepositoryJdbc();

        try (Connection conn = DbConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "email TEXT NOT NULL UNIQUE, " +
                    "phone INTEGER, " +
                    "address TEXT)");
            stmt.execute("DELETE FROM customers");
        }
    }

    private Customer sampleCustomer() {
        return new Customer(0, "Ali", "ali@example.com", 123456, "Cairo");
    }

    // ---- save ----

    @Test
    void savePersistsAndAssignsId() {
        Customer saved = repository.save(sampleCustomer());

        assertTrue(saved.getId() > 0);
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void saveRejectsNullName() {
        Customer c = sampleCustomer();
        c.setName(null);
        assertThrows(IllegalArgumentException.class, () -> repository.save(c));
    }

    @Test
    void saveRejectsNullEmail() {
        Customer c = sampleCustomer();
        c.setEmail(null);
        assertThrows(IllegalArgumentException.class, () -> repository.save(c));
    }

    @Test
    void saveRejectsNegativePhone() {
        Customer c = sampleCustomer();
        c.setPhone(-5);
        assertThrows(IllegalArgumentException.class, () -> repository.save(c));
    }

    @Test
    void saveRejectsNullAddress() {
        Customer c = sampleCustomer();
        c.setAddress(null);
        assertThrows(IllegalArgumentException.class, () -> repository.save(c));
    }

    // ---- findById ----

    @Test
    void findByIdReturnsEmptyForUnknownId() {
        assertEquals(Optional.empty(), repository.findById(9999L));
    }

    @Test
    void findByIdRejectsNonPositiveId() {
        assertThrows(IllegalArgumentException.class, () -> repository.findById(0L));
        assertThrows(IllegalArgumentException.class, () -> repository.findById(-1L));
    }

    // ---- findByEmail ----

    @Test
    void findByEmailFindsSavedCustomer() {
        repository.save(sampleCustomer());

        Optional<Customer> found = repository.findByEmail("ali@example.com");

        assertTrue(found.isPresent());
        assertEquals("Ali", found.get().getName());
    }

    @Test
    void findByEmailReturnsEmptyForUnknownEmail() {
        assertEquals(Optional.empty(), repository.findByEmail("nobody@example.com"));
    }

    @Test
    void findByEmailRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> repository.findByEmail(null));
    }

    // ---- findAll ----

    @Test
    void findAllReturnsSavedCustomers() {
        repository.save(sampleCustomer());
        repository.save(new Customer(0, "Sara", "sara@example.com", 111, "Giza"));

        List<Customer> all = repository.findAll();

        assertEquals(2, all.size());
    }

    // ---- update ----

    @Test
    void updateChangesExistingRow() {
        Customer saved = repository.save(sampleCustomer());
        saved.setName("Ali Updated");
        saved.setPhone(999);

        repository.update(saved);

        Customer reloaded = repository.findById(saved.getId()).orElseThrow();
        assertEquals("Ali Updated", reloaded.getName());
        assertEquals(999, reloaded.getPhone());
    }

    @Test
    void updateUnknownCustomerThrows() {
        Customer missing = new Customer(9876L, "Ghost", "ghost@example.com", 1, "Nowhere");

        assertThrows(CustomerNotFoundException.class, () -> repository.update(missing));
    }

    // ---- delete ----

    @Test
    void deleteRemovesExistingRow() {
        Customer saved = repository.save(sampleCustomer());

        repository.delete(saved.getId());

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }

    @Test
    void deleteUnknownCustomerThrows() {
        assertThrows(CustomerNotFoundException.class, () -> repository.delete(5555L));
    }

    @Test
    void deleteRejectsNonPositiveId() {
        assertThrows(IllegalArgumentException.class, () -> repository.delete(0L));
    }
}
