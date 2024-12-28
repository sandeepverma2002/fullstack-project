package com.ecomerce.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomerce.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByUserId(Long userId);

    


}
