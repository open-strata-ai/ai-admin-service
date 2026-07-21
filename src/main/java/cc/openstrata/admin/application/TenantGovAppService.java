package cc.openstrata.admin.application;

import cc.openstrata.admin.application.dto.CreateTenantRequest;
import cc.openstrata.admin.config.OpenstrataProperties;
import cc.openstrata.admin.domain.model.IsolationSpec;
import cc.openstrata.admin.domain.model.TenantGovernance;
import cc.openstrata.admin.domain.model.TenantId;
import cc.openstrata.admin.domain.port.MultiTenancyPort;
import cc.openstrata.admin.domain.rule.IsolationEnforcementRule;
import java.util.List;
import org.springframework.stereotype.Service;

/** Use case: create / suspend / resume tenants (governance side). DESIGN §4. */
@Service
public class TenantGovAppService {

    private final GovernanceAuthorityService governance;
    private final IsolationEnforcementRule isolationRule;
    private final MultiTenancyPort multiTenancyPort;
    private final OpenstrataProperties props;

    public TenantGovAppService(GovernanceAuthorityService governance,
                               IsolationEnforcementRule isolationRule,
                               MultiTenancyPort multiTenancyPort,
                               OpenstrataProperties props) {
        this.governance = governance;
        this.isolationRule = isolationRule;
        this.multiTenancyPort = multiTenancyPort;
        this.props = props;
    }

    public TenantGovernance create(CreateTenantRequest req) {
        TenantId id = new TenantId(req.tenantId());
        TenantGovernance g = governance.createTenant(id, req.toTier());
        if (props.getFeatures().isMultiTenantEnabled()) {
            boolean full = props.getFeatures().isBillingViewEnabled();
            IsolationSpec spec = isolationRule.build(id, full);
            multiTenancyPort.deployQuota(id, spec);
            g.applyIsolation(spec);
            governance.save(g);
        }
        return g;
    }

    public void suspend(String tenantId) {
        governance.suspendTenant(new TenantId(tenantId));
    }

    public void resume(String tenantId) {
        governance.resumeTenant(new TenantId(tenantId));
    }

    public TenantGovernance get(String tenantId) {
        return governance.load(new TenantId(tenantId));
    }

    public List<String> list() {
        return governance.findAllIds();
    }

    /** RC-9 (minimal CRUD): full tenant governance listing for the registry view. */
    public List<TenantGovernance> listDetails() {
        return governance.findAll();
    }

    /** RC-9 (minimal CRUD): delete a tenant's governance mirror and notify the
     *  control plane. */
    public void delete(String tenantId) {
        governance.deleteTenant(new TenantId(tenantId));
    }
}
