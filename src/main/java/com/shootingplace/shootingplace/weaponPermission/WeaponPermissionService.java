package com.shootingplace.shootingplace.weaponPermission;

import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class WeaponPermissionService {

    private final MemberRepository memberRepository;
    private final WeaponPermissionRepository weaponPermissionRepository;
    private final Logger LOG = LogManager.getLogger(getClass());


    public WeaponPermissionService(MemberRepository memberRepository, WeaponPermissionRepository weaponPermissionRepository) {
        this.memberRepository = memberRepository;
        this.weaponPermissionRepository = weaponPermissionRepository;
    }

    public ResponseEntity<?> updateWeaponPermission(String memberUUID, WeaponPermission weaponPermission) {
        if(!memberRepository.existsById(memberUUID)){
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }

        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        WeaponPermissionEntity weaponPermissionEntity = memberEntity.getWeaponPermission();
        if (weaponPermission.getNumber() != null) {

            boolean match = memberRepository.findAll()
                    .stream()
                    .filter(f -> !f.isErased())
                    .filter(f -> f.getWeaponPermission().getNumber() != null)
                    .anyMatch(f -> f.getWeaponPermission().getNumber().equals(weaponPermission.getNumber()));
            if (match) {
                LOG.error("ktoś już ma taki numer pozwolenia");
                return ResponseEntity.badRequest().body("ktoś już ma taki numer pozwolenia");
            } else {
                weaponPermissionEntity.setNumber(weaponPermission.getNumber().toUpperCase());
                weaponPermissionEntity.setExist(true);
                LOG.info("Wprowadzono numer pozwolenia");
            }
        }
        if (weaponPermission.getAdmissionToPossessAWeapon() != null) {

            boolean match = memberRepository.findAll()
                    .stream()
                    .filter(f -> !f.isErased())
                    .filter(f -> f.getWeaponPermission().getAdmissionToPossessAWeapon() != null)
                    .filter(f -> f.getWeaponPermission().getAdmissionToPossessAWeapon().equals(weaponPermission.getAdmissionToPossessAWeapon()))
                    .anyMatch(f -> f.getWeaponPermission().getAdmissionToPossessAWeapon().equals(weaponPermission.getAdmissionToPossessAWeapon()));

            if (match) {
                LOG.error("ktoś już ma taki numer dopuszczenia");
                return ResponseEntity.badRequest().body("ktoś już ma taki numer dopuszczenia");
            } else {
                weaponPermissionEntity.setAdmissionToPossessAWeapon(weaponPermission.getAdmissionToPossessAWeapon().toUpperCase());
                weaponPermissionEntity.setAdmissionToPossessAWeaponIsExist(true);
                LOG.info("Wprowadzono numer dopuszczenia");
            }
        }
        weaponPermissionRepository.save(weaponPermissionEntity);
        memberEntity.setWeaponPermission(weaponPermissionEntity);
        memberRepository.save(memberEntity);
        LOG.info("Zaktualizowano pozwolenie na broń");
        return ResponseEntity.ok("Zaktualizowano pozwolenie/dopuszczenie na broń");
    }

    public ResponseEntity<?> removeWeaponPermission(String memberUUID, boolean admission, boolean permission) {
        if (!memberRepository.existsById(memberUUID)) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.badRequest().body("Nie znaleziono Klubowicza");
        }
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        WeaponPermissionEntity weaponPermission = memberEntity.getWeaponPermission();

        if (permission) {
            weaponPermission.setNumber(null);
            weaponPermission.setExist(false);
        }
        if (admission) {
            weaponPermission.setAdmissionToPossessAWeapon(null);
            weaponPermission.setAdmissionToPossessAWeaponIsExist(false);
        }
        weaponPermissionRepository.save(weaponPermission);
        return ResponseEntity.ok("Usunięto pozwolenie/dopuszczenie");
    }

    public WeaponPermission getWeaponPermission() {
        return WeaponPermission.builder()
                .number(null)
                .isExist(false)
                .admissionToPossessAWeapon(null)
                .admissionToPossessAWeaponIsExist(false)
                .build();
    }

}
