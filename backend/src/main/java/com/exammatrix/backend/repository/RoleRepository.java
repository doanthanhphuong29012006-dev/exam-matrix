package com.exammatrix.backend.repository;

import com.exammatrix.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    // findAll(), findById(), save(), deleteById()
}