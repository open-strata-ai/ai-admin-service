package cc.openstrata.admin.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProvisioningPlanRepository extends JpaRepository<ProvisioningPlanEntity, String> {

    List<ProvisioningPlanEntity> findByTenantId(String tenantId);

    @Query("select p from ProvisioningPlanEntity p where p.tenantId = ?1 and p.status <> cc.openstrata.admin.domain.model.ProvisioningStatus.DONE and p.status <> cc.openstrata.admin.domain.model.ProvisioningStatus.FAILED")
    List<ProvisioningPlanEntity> findActiveByTenant(String tenantId);
}
