package com.openstrata.admin.application;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

/**
 * ADR-0006 — BOM alignment.
 *
 * <p>Loads the pinned {@code interface_versions} from `bom-alignment.yaml`
 * (mirror of meta `bom.yaml` §16.1) and compares them against the versions
 * actually wired into the running service. Reports drift so CI / startup can
 * fail fast on a divergence (R-004 single-SemVer spirit). Conservative default:
 * detect + report; reconcile is a follow-up.
 */
@Service
public class BomAlignmentService {

    public record BomDrift(String component, String pinned, String running, boolean aligned) {}
    public record BomReport(boolean aligned, List<BomDrift> drifts) {}

    /** Versions actually wired into this build (stand-in adapters). */
    private static final Map<String, String> RUNNING = Map.of(
        "keycloak", "25.0.0",
        "capsule", "1.9.0",
        "redis", "7.4.0",
        "valkey", "7.2.0",
        "postgresql", "16.0",
        "kueue", "0.6.0",
        "opencost", "1.106.0"
    );

    public BomReport check() {
        Map<String, String> pinned = loadPinned();
        List<BomDrift> drifts = new ArrayList<>();
        for (Map.Entry<String, String> e : pinned.entrySet()) {
            String running = RUNNING.get(e.getKey());
            boolean aligned = running != null && running.equals(e.getValue());
            drifts.add(new BomDrift(e.getKey(), e.getValue(), running, aligned));
        }
        boolean allAligned = drifts.stream().allMatch(BomDrift::aligned);
        return new BomReport(allAligned, drifts);
    }

    public boolean isAligned() {
        return check().aligned();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> loadPinned() {
        try (InputStream in = new ClassPathResource("bom-alignment.yaml").getInputStream()) {
            Map<String, Object> root = new Yaml().load(in);
            Object iv = root.get("interface_versions");
            if (iv instanceof Map<?, ?> m) {
                Map<String, String> out = new LinkedHashMap<>();
                ((Map<String, Object>) m).forEach((k, v) -> out.put(k, String.valueOf(v)));
                return out;
            }
            return Map.of();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load bom-alignment.yaml", e);
        }
    }
}
