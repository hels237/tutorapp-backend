package com.backend.tutor_app.repositories;

import com.backend.tutor_app.model.Tutor;
import com.backend.tutor_app.model.User;
import com.backend.tutor_app.model.enums.Role;
import com.backend.tutor_app.model.enums.UserStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByStatus(UserStatus status);

    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :date")
    List<User> findUnverifiedUsersOlderThan(@Param("date") LocalDateTime date);
}
