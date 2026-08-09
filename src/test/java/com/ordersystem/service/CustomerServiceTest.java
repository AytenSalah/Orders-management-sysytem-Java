package com.ordersystem.service;

import com.ordersystem.exception.CustomerNotFoundException;
import com.ordersystem.exception.DuplicateEmailException;
import com.ordersystem.model.Customer;
import com.ordersystem.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerServiceTest {

    private CustomerService service;
    private FakeCustomerRepository repository;

    @BeforeEach
    void setUp() {
        repository = new FakeCustomerRepository();
        service = new CustomerService(repository);
    }

    // ---- createCustomer ----

    @Test
    void createCustomerSavesAndReturnsCustomer() {
        Customer c = new Customer(0, "Ali", "ali@example.com", 123, "Cairo");

        Customer saved = service.createCustomer(c);

        assertNotNull(saved);
        assertTrue(saved.getId() > 0);
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void createCustomerWithNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.createCustomer(null));
    }

    @Test
    void createCustomerWithDuplicateEmailThrows() {
        service.createCustomer(new Customer(0, "Ali", "duplicate@example.com", 123, "Cairo"));

        DuplicateEmailException ex = assertThrows(DuplicateEmailException.class,
                () -> service.createCustomer(new Customer(0, "Sara", "duplicate@example.com", 456, "Giza")));

        assertTrue(ex.getMessage().contains("duplicate@example.com"));
    }

    // ---- updateCustomer ----

    @Test
    void updateCustomerExistingUpdatesFields() {
        Customer saved = service.createCustomer(new Customer(0, "Ali", "ali@example.com", 123, "Cairo"));
        Customer updated = new Customer(saved.getId(), "Ali Updated", "ali@example.com", 999, "Giza");

        Customer result = service.updateCustomer(updated);

        assertEquals("Ali Updated", result.getName());
        assertEquals(999, result.getPhone());
        assertEquals("Ali Updated", repository.findById(saved.getId()).orElseThrow().getName());
    }

    @Test
    void updateCustomerNotFoundThrows() {
        Customer missing = new Customer(999L, "Ghost", "ghost@example.com", 1, "Nowhere");

        CustomerNotFoundException ex = assertThrows(CustomerNotFoundException.class,
                () -> service.updateCustomer(missing));

        assertTrue(ex.getMessage().contains("999"));
    }

    @Test
    void updateCustomerWithNullOrInvalidIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.updateCustomer(null));
        assertThrows(IllegalArgumentException.class,
                () -> service.updateCustomer(new Customer(0, "X", "x@example.com", 1, "A")));
    }

    // ---- deleteCustomer ----

    @Test
    void deleteCustomerExistingRemovesIt() {
        Customer saved = service.createCustomer(new Customer(0, "Ali", "ali@example.com", 123, "Cairo"));

        service.deleteCustomer(saved.getId());

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }

    @Test
    void deleteCustomerNotFoundThrows() {
        CustomerNotFoundException ex = assertThrows(CustomerNotFoundException.class,
                () -> service.deleteCustomer(12345L));

        assertTrue(ex.getMessage().contains("12345"));
    }

    // ---- searchCustomers ----

    @Test
    void searchCustomersMatchesNameCaseInsensitive() {
        service.createCustomer(new Customer(0, "Ahmed", "a@example.com", 1, "Cairo"));
        service.createCustomer(new Customer(0, "Sara", "s@example.com", 2, "Giza"));

        List<Customer> result = service.searchCustomers("ahm");

        assertEquals(1, result.size());
        assertEquals("Ahmed", result.get(0).getName());
    }

    @Test
    void searchCustomersMatchesEmail() {
        service.createCustomer(new Customer(0, "Ahmed", "ahmed@example.com", 1, "Cairo"));

        List<Customer> result = service.searchCustomers("example.com");

        assertEquals(1, result.size());
    }

    @Test
    void searchCustomersWithNoCustomersThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.searchCustomers("x"));
    }

    @Test
    void searchCustomersWithNullKeywordThrows() {
        service.createCustomer(new Customer(0, "Ahmed", "a@example.com", 1, "Cairo"));

        assertThrows(IllegalArgumentException.class, () -> service.searchCustomers(null));
    }

    // ---- listCustomers ----

    @Test
    void listCustomersReturnsAll() {
        service.createCustomer(new Customer(0, "A", "a@example.com", 1, "Cairo"));
        service.createCustomer(new Customer(0, "B", "b@example.com", 2, "Giza"));

        assertEquals(2, service.listCustomers().size());
    }

    // ---- test double ----

    private static class FakeCustomerRepository implements CustomerRepository {

        private final Map<Long, Customer> store = new HashMap<>();
        private long nextId = 1;

        @Override
        public Customer save(Customer customer) {
            if (customer.getId() == 0) {
                customer.setId(nextId++);
            }
            store.put(customer.getId(), customer);
            return customer;
        }

        @Override
        public Optional<Customer> findById(long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Customer> findByEmail(String email) {
            return store.values().stream()
                    .filter(c -> c.getEmail() != null && c.getEmail().equals(email))
                    .findFirst();
        }

        @Override
        public List<Customer> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public void update(Customer customer) {
            if (!store.containsKey(customer.getId())) {
                throw new CustomerNotFoundException(customer.getId());
            }
            store.put(customer.getId(), customer);
        }

        @Override
        public void delete(long id) {
            if (!store.containsKey(id)) {
                throw new CustomerNotFoundException(id);
            }
            store.remove(id);
        }
    }
}
