package org.example.docvanta_bcakend.repository;

import org.example.docvanta_bcakend.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {

    Optional<Clinic> findByName(String name);

    Optional<Clinic> findByNameContainingIgnoreCase(String name);
}
