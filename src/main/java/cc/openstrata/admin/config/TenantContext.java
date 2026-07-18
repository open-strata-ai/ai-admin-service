package cc.openstrata.admin.config;

import java.util.Set;

/**
 * Per-request tenant/role context, populated by {@link AuthInterceptor}
 * (auth-contract.md §3 — Java `AuthInterceptor`). Never holds the tenant id from
 * a client-supplied body; it is derived from the token / X-Tenant-Id header.
 */
public final class TenantContext {

    private static final ThreadLocal<Tenant> CTX = new ThreadLocal<>();

    public record Tenant(String tenantId, Set<String> roles, boolean platformAdmin) {}

    private TenantContext() {}

    public static void set(Tenant tenant) { CTX.set(tenant); }
    public static Tenant get() { return CTX.get(); }
    public static String tenantId() {
        Tenant t = CTX.get();
        return t == null ? null : t.tenantId();
    }
    public static boolean isPlatformAdmin() {
        Tenant t = CTX.get();
        return t != null && t.platformAdmin();
    }
    public static void clear() { CTX.remove(); }
}
