package com.ecomerce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecomerce.model.Product;
import com.ecomerce.repository.ProductRepository;
import com.ecomerce.util.FileUploadUtil;

import java.io.IOException;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
   
    
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public void updateProduct(Long id, Product product, MultipartFile file) throws IOException {
        Product existingProduct = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        existingProduct.setProductName(product.getProductName());
        existingProduct.setTitle(product.getTitle());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setQuantity(product.getQuantity());

        if (file != null && !file.isEmpty()) {
            String fileName = FileUploadUtil.saveFile(file);
            existingProduct.setProductImage(fileName);
        }

        productRepository.save(existingProduct);
    }
    //add to cart

//product added by userid
    public List<Product>getproductbyUser(Long userId){
    	return productRepository.findByUserId(userId);
    }
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }
	
    
    
}
