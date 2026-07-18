package cc.openstrata.admin.domain.model;

/** Lifecycle of a provisioning plan (DESIGN §8 / §13.3). */
public enum ProvisioningStatus {
    PENDING,
    APPLYING,
    DONE,
    FAILED;

    public boolean isTerminal() {
        return this == DONE || this == FAILED;
    }
}
