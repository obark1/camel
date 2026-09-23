package com.prep.camel.repositories;

import com.prep.camel.entities.ImportEventLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImportEventLogRepository extends JpaRepository<ImportEventLogEntity, UUID> {
}
