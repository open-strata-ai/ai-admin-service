package com.openstrata.admin.domain.model;

/** Per-tenant Milvus collection prefix enforcing data isolation (RULE-03). */
public record CollectionPrefix(String value) {}
