package org.example.docvanta_bcakend.repository;

import org.example.docvanta_bcakend.entity.AccessRight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessRightRepository extends JpaRepository<AccessRight, Long> {
    Optional<AccessRight> findByCode(String code);
}
