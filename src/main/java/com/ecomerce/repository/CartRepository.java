package com.ecomerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecomerce.model.Cart;
import com.ecomerce.model.Product;
import com.ecomerce.model.User;

public interface CartRepository extends JpaRepository<Cart, Long> {
	    List<Cart> findByUser(User user);
	    Cart findByUserAndProduct(User user, Product product);
	    void deleteByUserAndProduct(User user, Product product);
	    void deleteByUserId(Long userId);

	    Cart findByProductIdAndUserId(Long productId, Long userId);
	    List<Cart> findByUserId(Long userId);
	    


	

}
