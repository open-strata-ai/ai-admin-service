package cc.openstrata.admin.web;

import cc.openstrata.admin.domain.DomainException;
import java.util.UUID;

/**
 * Error envelope returned to clients (DESIGN §7 / SPECS §1.4). The `doc` link
 * points at the canonical error catalogue.
 */
public record ApiError(String code, String message, String traceId, String doc) {

    public static ApiError of(ErrorCode code, String message) {
        String trace = UUID.randomUUID().toString().substring(0, 8);
        return new ApiError(code.name(), message, trace,
            "https://docs.openstrata.cc/errors/" + code.name());
    }

    public static ApiError of(DomainException e) {
        return of(e.code(), e.getMessage());
    }
}
