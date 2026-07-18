package com.openstrata.admin.domain.model;

/** Audit scope — platform-level or tenant-level (DESIGN §8 / §4.7.4). */
public enum AuditScope {
    PLATFORM,
    TENANT
}
