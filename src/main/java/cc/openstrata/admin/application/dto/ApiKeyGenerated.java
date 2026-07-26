package cc.openstrata.admin.application.dto;

/** Result of generating an API key (PR-C). {@code plaintext} is returned exactly
 *  once and must be shown to the caller immediately; it is never stored. */
public record ApiKeyGenerated(ApiKeyView key, String plaintext) {
}
