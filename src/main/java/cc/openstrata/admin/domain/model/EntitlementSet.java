package cc.openstrata.admin.domain.model;

import java.util.Set;

/** Component whitelist (enabled components) governed per tenant (DESIGN §3). */
public record EntitlementSet(Set<String> enabledComponents) {}
