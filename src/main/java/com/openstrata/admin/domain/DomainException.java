package com.openstrata.admin.domain;

import com.openstrata.admin.web.ErrorCode;

/**
 * Domain-level exception carrying a {@link ErrorCode} so the web layer can map
 * it to a consistent HTTP status + envelope.
 */
public class DomainException extends RuntimeException {

    private final ErrorCode code;

    public DomainException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
