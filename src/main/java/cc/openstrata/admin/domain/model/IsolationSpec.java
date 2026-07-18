package cc.openstrata.admin.domain.model;

/**
 * Isolation carrier materialized atomically when governance is applied (ADR-5):
 * Namespace + NetworkPolicy + KueueQueue (full) + CollectionPrefix + Bucket.
 */
public record IsolationSpec(String namespace, ResourceQuota quota,
                            NetworkPolicy networkPolicy, KueueQueue gpuQueue,
                            CollectionPrefix collectionPrefix, Bucket bucket) {}
