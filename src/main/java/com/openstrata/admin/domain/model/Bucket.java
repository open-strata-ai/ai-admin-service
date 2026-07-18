package com.openstrata.admin.domain.model;

/** Per-tenant MinIO bucket enforcing data isolation (RULE-03). */
public record Bucket(String value) {}
