package com.ecomerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecomerce.model.ShipmentDetails;

@Repository
public interface ShipmentDetailsRepository extends JpaRepository<ShipmentDetails, Long> {
}
