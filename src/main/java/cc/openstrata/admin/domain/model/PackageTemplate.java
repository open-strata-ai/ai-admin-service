package cc.openstrata.admin.domain.model;

import java.util.List;

/** A reusable package template (PA-04) describing a tier's components + quota. */
public class PackageTemplate {
    private String id;
    private String name;
    private String tier;
    private List<String> components;
    private String quotaPolicy;

    public PackageTemplate() {
    }

    public PackageTemplate(String id, String name, String tier, List<String> components, String quotaPolicy) {
        this.id = id;
        this.name = name;
        this.tier = tier;
        this.components = components;
        this.quotaPolicy = quotaPolicy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public List<String> getComponents() {
        return components;
    }

    public void setComponents(List<String> components) {
        this.components = components;
    }

    public String getQuotaPolicy() {
        return quotaPolicy;
    }

    public void setQuotaPolicy(String quotaPolicy) {
        this.quotaPolicy = quotaPolicy;
    }
}
