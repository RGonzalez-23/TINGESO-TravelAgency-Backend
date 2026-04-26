package com.example.TINGESO.Controllers;

import com.example.TINGESO.Entities.GlobalDiscountConfigEntity;
import com.example.TINGESO.Repositories.GlobalDiscountConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/discount-config")
@CrossOrigin(origins = "*")
public class GlobalDiscountConfigController {

    @Autowired
    private GlobalDiscountConfigRepository configRepository;

    @GetMapping
    public ResponseEntity<?> getConfig() {
        GlobalDiscountConfigEntity config = configRepository.findById(1L).orElseGet(GlobalDiscountConfigEntity::new);
        return ResponseEntity.ok(config);
    }

    @PutMapping
    public ResponseEntity<?> updateConfig(@RequestBody GlobalDiscountConfigEntity updatedConfig) {
        updatedConfig.setId(1L); // Forzar que sea el ID 1 para que sobreescriba la tabla global
        return ResponseEntity.ok(configRepository.save(updatedConfig));
    }
}
