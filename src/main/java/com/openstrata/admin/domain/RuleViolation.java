package com.openstrata.admin.domain;

/**
 * A single rule violation produced by a domain rule. `code` is an error-code
 * name (see {@link com.openstrata.admin.web.ErrorCode}) and `message` is
 * human-readable (often citing the relevant design §).
 */
public record RuleViolation(String code, String message) {}
