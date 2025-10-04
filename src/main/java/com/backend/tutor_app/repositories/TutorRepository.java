package com.backend.tutor_app.repositories;

import com.backend.tutor_app.model.Tutor;
import com.backend.tutor_app.model.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, Long> {
    List<Tutor> findByVerificationStatus(VerificationStatus status);
    List<Tutor> findByIsAvailableTrue();
}
