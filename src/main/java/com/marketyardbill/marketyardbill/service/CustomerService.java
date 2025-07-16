package com.marketyardbill.marketyardbill.service;

import com.marketyardbill.marketyardbill.dao.iCustomerRepository;
import com.marketyardbill.marketyardbill.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private iCustomerRepository customerRepository;

    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        return customerRepository.findById(id).map(customer -> {
            customer.setFullName(updatedCustomer.getFullName());
            customer.setAddress(updatedCustomer.getAddress());
            customer.setGstNumber(updatedCustomer.getGstNumber());
            customer.setContactNumber(updatedCustomer.getContactNumber());
            return customerRepository.save(customer);
        }).orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}
