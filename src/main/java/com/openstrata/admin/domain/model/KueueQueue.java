package com.openstrata.admin.domain.model;

/** GPU ClusterQueue configuration — only present in the full profile (ADR-0002). */
public record KueueQueue(String name, int gpu) {}
