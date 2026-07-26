package cc.openstrata.admin.application;

import cc.openstrata.admin.config.TenantContext;

/** Resolves the current actor identity for audit records (PR-C). */
public final class Actors {
    private Actors() {}

    public static String current() {
        String t = TenantContext.tenantId();
        return t == null ? "system" : t;
    }
}
