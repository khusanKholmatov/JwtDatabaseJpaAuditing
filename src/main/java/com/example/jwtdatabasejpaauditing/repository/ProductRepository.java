package com.example.jwtdatabasejpaauditing.repository;

import com.example.jwtdatabasejpaauditing.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RepositoryRestResource(path = "product")
public interface ProductRepository extends JpaRepository<Product, UUID> {
}
