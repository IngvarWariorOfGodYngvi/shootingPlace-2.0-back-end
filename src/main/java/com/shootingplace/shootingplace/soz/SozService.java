package com.shootingplace.shootingplace.soz;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SozService {

    private final SozConfigRepository sozConfigRepository;
    private final SozClient sozClient;

    ResponseEntity<?> getSozConfig() {
        boolean present = sozConfigRepository.findById(1L).isPresent();
        return ResponseEntity.ok(present);
    }


    public ResponseEntity<?> setSozConfig(SozConfig sozConfig) {

        if (sozConfig.getPassword() == null || sozConfig.getPassword().isEmpty() || sozConfig.getLogin() == null || sozConfig.getLogin().isEmpty()) {
            return ResponseEntity.badRequest().body("Brak Loginu lub hasła");
        }
        sozConfig.setPassword(sozConfig.getPassword().trim());
        sozConfig.setId(1L);
        sozConfigRepository.save(sozConfig);
        sozConfig.setPassword(null);
        return ResponseEntity.ok("Zapisano ustawienie");
    }

    public ResponseEntity<?> getInvitations() {
        return ResponseEntity.ok(sozClient.fetchInvitations());
    }

    public ResponseEntity<?> getMembers() {
        return ResponseEntity.ok(sozClient.fetchMembers());
    }

    public ResponseEntity<?> getLicenseAndPatents() {
        return ResponseEntity.ok(sozClient.fetchLicenseAndPatents());
    }

}
