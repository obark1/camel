package com.prep.camel.repositories;

import com.prep.camel.entities.RejectedImportRowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RejectedImportRepository extends JpaRepository<RejectedImportRowEntity, Long> {
}
