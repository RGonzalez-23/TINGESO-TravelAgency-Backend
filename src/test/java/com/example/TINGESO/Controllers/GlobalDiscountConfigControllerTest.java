package com.example.TINGESO.Controllers;

import com.example.TINGESO.Entities.GlobalDiscountConfigEntity;
import com.example.TINGESO.Repositories.GlobalDiscountConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Optional;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GlobalDiscountConfigController.class)
@ImportAutoConfiguration(exclude = {
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
})
public class GlobalDiscountConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GlobalDiscountConfigRepository configRepository;

    @Test
    public void getConfig_ShouldReturnConfig() throws Exception {
        GlobalDiscountConfigEntity config = new GlobalDiscountConfigEntity();
        config.setGroupMinPassengers(10);
        given(configRepository.findById(1L)).willReturn(Optional.of(config));

        mockMvc.perform(get("/api/admin/discount-config")
                .with(jwt().jwt(builder -> builder.claim("realm_access", java.util.Map.of("roles", java.util.List.of("ADMIN"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupMinPassengers").value(10));
    }
}
