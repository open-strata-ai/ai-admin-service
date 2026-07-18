package com.openstrata.admin.domain.model;

/** Quota policy = package tier + quota + GPU-enabled flag (DESIGN §3). */
public record QuotaPolicy(PackageTier pkg, ResourceQuota quota, boolean gpuEnabled) {}
