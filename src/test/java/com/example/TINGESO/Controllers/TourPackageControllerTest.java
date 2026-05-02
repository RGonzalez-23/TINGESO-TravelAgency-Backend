package com.example.TINGESO.Controllers;

import com.example.TINGESO.Entities.TourPackageEntity;
import com.example.TINGESO.Services.TourPackageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Collections;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TourPackageController.class)
@ImportAutoConfiguration(exclude = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
})
public class TourPackageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TourPackageService tourPackageService;

    @Test
    public void getAllPackages_ShouldReturnPackages() throws Exception {
        TourPackageEntity pkg = new TourPackageEntity();
        pkg.setName("Aventura");
        given(tourPackageService.getAllPackages()).willReturn(Collections.singletonList(pkg));

        mockMvc.perform(get("/api/packages")) // Public endpoint usually
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Aventura"));
    }

    @Test
    public void deletePackage_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/packages/1")
                .with(jwt().jwt(builder -> builder.claim("realm_access", java.util.Map.of("roles", java.util.List.of("ADMIN"))))))
                .andExpect(status().isOk());
    }
}
