package com.openstrata.admin.web;

/**
 * Business error codes (SPECS §1.5). Each maps to a single HTTP status so the
 * {@link GlobalExceptionHandler} can render a consistent error envelope.
 */
public enum ErrorCode {
    ENTITLEMENT_DEP_VIOLATION(422),
    TENANT_NOT_FOUND(404),
    QUOTA_CONFLICT(409),
    MODEL_RESTRICTED(403),
    PROVISIONING_IN_PROGRESS(409),
    GPU_POOL_NOT_AVAILABLE(404),
    ADMIN_MFA_REQUIRED(403),
    SCOPE_VIOLATION(403),
    GOVERNANCE_WRITE_VIOLATION(400),
    ORCHESTRATION_TIMEOUT(409),
    ORCHESTRATION_PLAN_VIOLATION(422),
    BOM_DRIFT(409),
    WHITELIST_CONFLICT(409),
    ISOLATION_VIOLATION(422),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    INTERNAL(500);

    private final int httpStatus;

    ErrorCode(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public int httpStatus() {
        return httpStatus;
    }
}
