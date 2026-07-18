package cc.openstrata.admin.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BomAlignmentServiceTest {

    private final BomAlignmentService service = new BomAlignmentService();

    @Test
    void pinnedVersionsAreAlignedWithRunningAdapters() {
        BomAlignmentService.BomReport report = service.check();
        assertTrue(report.aligned(), "expected no drift against bom-alignment.yaml");
        assertEquals(7, report.drifts().size());
    }
}
