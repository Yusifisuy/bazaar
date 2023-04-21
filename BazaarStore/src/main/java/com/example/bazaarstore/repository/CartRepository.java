package com.example.bazaarstore.repository;

import com.example.bazaarstore.model.entity.Cart;
import com.example.bazaarstore.model.entity.Product;
import com.example.bazaarstore.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart,Long> {

    Optional<Cart> findByUser(User user);


}
