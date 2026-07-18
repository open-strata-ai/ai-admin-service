package com.openstrata.admin.domain.model;

import java.util.Set;

/** Per-tenant model provider whitelist (DESIGN §3). */
public record ModelWhitelist(Set<String> allowedModels, boolean restrictedEnabled) {}
