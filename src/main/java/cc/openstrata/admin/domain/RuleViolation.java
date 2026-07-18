package cc.openstrata.admin.domain;

/**
 * A single rule violation produced by a domain rule. `code` is an error-code
 * name (see {@link cc.openstrata.admin.web.ErrorCode}) and `message` is
 * human-readable (often citing the relevant design §).
 */
public record RuleViolation(String code, String message) {}
