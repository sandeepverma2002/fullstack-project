package com.ecomerce.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecomerce.model.Cart;
import com.ecomerce.model.Product;
import com.ecomerce.model.User;
import com.ecomerce.repository.CartRepository;


@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    public Cart findByProductAndUser(Long productId, Long userId) {
        return cartRepository.findByProductIdAndUserId(productId, userId);
    }

    // Add product to the cart
    public void addToCart(User user, Product product, int quantity) {
        // Check if the product is already in the user's cart
        Cart existingCartItem = cartRepository.findByProductIdAndUserId(product.getId(), user.getId());

        if (existingCartItem != null) {
            // If the product is already in the cart, update the quantity
            existingCartItem.setQuantity(existingCartItem.getQuantity() + quantity);
            cartRepository.save(existingCartItem); // Update the existing record
        } else {
            // If the product is not in the cart, create a new cart item
            Cart newCartItem = new Cart();
            newCartItem.setUser(user);
            newCartItem.setProduct(product);
            newCartItem.setQuantity(quantity);
            cartRepository.save(newCartItem); // Insert a new record
        }
    }


    // Get cart items for a user
  

    // Update cart item
    public void updateCartItem(Cart cart) {
        cartRepository.save(cart); // Updates the cart with the new quantity
    }

    // Remove product from the cart
    public void removeFromCart(User user, Product product) {
        Cart cartItem = cartRepository.findByProductIdAndUserId(product.getId(), user.getId());
        if (cartItem != null) {
            cartRepository.delete(cartItem);
        }
    }

    // Get cart items for a user
    public List<Cart> getCartItems(User user) {
        return cartRepository.findByUserId(user.getId());
    }

    // Calculate the total price of all items in the cart
    public double calculateTotalPrice(User user) {
        double total = 0.0;
        List<Cart> cartItems = getCartItems(user);
        for (Cart cartItem : cartItems) {
            total += cartItem.getQuantity() * cartItem.getProduct().getPrice();
        }
        return total;
    }

    // Get the total quantity of products in the cart
    public int getTotalProductQuantity(User user) {
        int totalQuantity = 0;
        List<Cart> cartItems = getCartItems(user);
        for (Cart cartItem : cartItems) {
            totalQuantity += cartItem.getQuantity();
        }
        return totalQuantity;
    }
    
    public int getCartItemCount(Long userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId); // This assumes you have a cart item repository that finds cart items by userId
        return cartItems.size();
    }
    
    public List<Cart> getCartItemsByUser(Long userId) {
        return cartRepository.findByUserId(userId);
    }
    
    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
    }
}
