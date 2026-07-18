package com.openstrata.admin.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantGovernanceRepository extends JpaRepository<TenantGovernanceEntity, String> {
}
