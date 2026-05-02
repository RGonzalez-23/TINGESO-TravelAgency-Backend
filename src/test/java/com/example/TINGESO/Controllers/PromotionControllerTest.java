package com.example.TINGESO.Controllers;

import com.example.TINGESO.Entities.PromotionEntity;
import com.example.TINGESO.Services.PromotionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Collections;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PromotionController.class)
@ImportAutoConfiguration(exclude = {
        OAuth2ClientAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
})
public class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PromotionService promotionService;

    @Test
    public void getAllPromotions_ShouldReturnPromotions() throws Exception {
        PromotionEntity promo = new PromotionEntity();
        promo.setName("Cyber");
        given(promotionService.getAllPromotions()).willReturn(Collections.singletonList(promo));

        mockMvc.perform(get("/api/promotions")
                .with(jwt().jwt(builder -> builder.claim("realm_access", java.util.Map.of("roles", java.util.List.of("ADMIN"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cyber"));
    }

    @Test
    public void deletePromotion_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/promotions/1")
                .with(jwt().jwt(builder -> builder.claim("realm_access", java.util.Map.of("roles", java.util.List.of("ADMIN"))))))
                .andExpect(status().isOk());
    }
}
