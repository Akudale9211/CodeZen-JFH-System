package com.marketyardbill.marketyardbill.dao;

import com.marketyardbill.marketyardbill.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface iCustomerRepository extends JpaRepository<Customer, Long> {
    // Additional custom query methods can be defined here if needed
}

