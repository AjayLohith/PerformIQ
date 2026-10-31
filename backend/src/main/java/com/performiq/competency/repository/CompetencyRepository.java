package com.performiq.competency.repository;

import com.performiq.competency.entity.Competency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompetencyRepository extends JpaRepository<Competency, UUID> {
    Optional<Competency> findByBusinessId(String businessId);
    Optional<Competency> findByName(String name);
    boolean existsByBusinessId(String businessId);
    List<Competency> findByCategory(String category);
}
