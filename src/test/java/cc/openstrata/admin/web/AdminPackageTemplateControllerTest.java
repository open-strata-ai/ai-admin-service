package cc.openstrata.admin.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cc.openstrata.admin.application.PackageTemplateAppService;
import cc.openstrata.admin.application.dto.CreatePackageTemplateRequest;
import cc.openstrata.admin.domain.model.PackageTemplate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AdminPackageTemplateControllerTest {
    private final PackageTemplateAppService service = mock(PackageTemplateAppService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(
        new AdminPackageTemplateController(service)).build();

    @BeforeEach
    void setup() {
        when(service.create(any())).thenReturn(
            new PackageTemplate("pt-starter", "Starter", "basic",
                List.of("chat", "memory"), "2 vCPU"));
        when(service.list()).thenReturn(List.of(
            new PackageTemplate("pt-starter", "Starter", "basic",
                List.of("chat"), "2 vCPU")));
    }

    @Test
    void createReturns201() throws Exception {
        mvc.perform(post("/api/v1/admin/package-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Starter\",\"tier\":\"basic\",\"components\":[\"chat\"],\"quotaPolicy\":\"2 vCPU\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("pt-starter"));
    }

    @Test
    void listReturnsTemplates() throws Exception {
        mvc.perform(get("/api/v1/admin/package-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Starter"));
    }

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/v1/admin/package-templates/pt-starter"))
                .andExpect(status().isNoContent());
    }
}
