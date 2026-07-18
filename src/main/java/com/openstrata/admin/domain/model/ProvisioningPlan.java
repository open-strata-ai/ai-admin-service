package com.openstrata.admin.domain.model;

import java.time.Instant;

/** Orchestration plan tracked in `provisioning_plans` (ADR-3). */
public record ProvisioningPlan(String planId, TenantId tenantId, String manifest,
                               ProvisioningStatus status, Instant createdAt) {}
