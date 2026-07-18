package com.openstrata.admin.application;

import com.openstrata.admin.config.OpenstrataProperties;
import com.openstrata.admin.domain.DomainException;
import com.openstrata.admin.domain.RuleResult;
import com.openstrata.admin.domain.model.EntitlementSet;
import com.openstrata.admin.domain.rule.EntitlementConsistencyRule;
import com.openstrata.admin.web.ErrorCode;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * ADR-0005 — Whitelist conflicts with Manifest.
 *
 * <p>Validates the component whitelist against the `PlatformManifest` dependency
 * graph (§12.4). When a tenant's guide-portal selects components beyond the
 * management-portal whitelist, the conflict-resolution strategy (config-driven,
 * REJECT | ESCALATE) decides the outcome. Conservative default: REJECT (deny).
 */
@Service
public class WhitelistManifestService {

    public enum Strategy { REJECT, ESCALATE }

    public record Evaluation(boolean accepted, boolean escalated, String reason) {}

    private final EntitlementConsistencyRule rule = new EntitlementConsistencyRule();
    private final OpenstrataProperties props;
    private final AuditAggregationService audit;

    public WhitelistManifestService(OpenstrataProperties props, AuditAggregationService audit) {
        this.props = props;
        this.audit = audit;
    }

    public RuleResult validate(EntitlementSet whitelist) {
        return rule.validate(whitelist);
    }

    /**
     * Resolve a conflict between the management whitelist and the guide-portal
     * selection. REJECT → deny (WHITELIST_CONFLICT). ESCALATE → proceed but mark
     * the divergence for arraignment.
     */
    public Evaluation resolveConflict(String tenantId, EntitlementSet adminWhitelist,
                                      List<String> guideSelection) {
        List<String> beyond = guideSelection.stream()
            .filter(c -> !adminWhitelist.enabledComponents().contains(c))
            .toList();
        if (beyond.isEmpty()) {
            return new Evaluation(true, false, "no conflict");
        }
        Strategy strategy = Strategy.valueOf(
            props.getFeatures().getWhitelistManifest().getConflictStrategy());
        if (strategy == Strategy.ESCALATE) {
            audit.record("system", com.openstrata.admin.domain.model.AuditScope.PLATFORM,
                tenantId, "WHITELIST_ESCALATED",
                Map.of("beyond", beyond));
            return new Evaluation(true, true, "escalated: " + beyond);
        }
        throw new DomainException(ErrorCode.WHITELIST_CONFLICT,
            "Guide selection exceeds management whitelist (conflict strategy=REJECT): " + beyond);
    }
}
