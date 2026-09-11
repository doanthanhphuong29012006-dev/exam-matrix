package com.exammatrix.backend.repository;

import com.exammatrix.backend.entity.User;
import com.exammatrix.backend.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    @Query("""
    SELECT u
    FROM User u
    JOIN u.role r
    WHERE (
        :search IS NULL
        OR LOWER(u.username) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
        OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
    )
    AND (
        :role IS NULL
        OR LOWER(r.name) = LOWER(CAST(:role AS string))
    )
    AND (
        :status IS NULL
        OR u.status = :status
    )
    """)
    Page<User> searchUsers(
        @Param("search") String search,
        @Param("role") String role,
        @Param("status") UserStatus status,
        Pageable pageable
    );
}