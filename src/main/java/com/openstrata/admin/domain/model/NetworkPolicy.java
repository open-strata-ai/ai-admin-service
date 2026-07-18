package com.openstrata.admin.domain.model;

/** Network isolation policy; default is DENY_ALL (RULE-03 / §14.2). */
public record NetworkPolicy(boolean denyAll) {
    public static final NetworkPolicy DENY_ALL = new NetworkPolicy(true);
}
