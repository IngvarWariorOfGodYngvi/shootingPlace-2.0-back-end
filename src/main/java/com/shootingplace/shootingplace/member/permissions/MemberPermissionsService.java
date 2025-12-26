package com.shootingplace.shootingplace.member.permissions;

import com.shootingplace.shootingplace.enums.ArbiterClass;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberPermissionsService {

    private final MemberPermissionsRepository memberPermissionsRepository;
    private final MemberRepository memberRepository;
    private final Logger LOG = LogManager.getLogger(getClass());

    public ResponseEntity<?> updateMemberPermissions(String memberUUID, MemberPermissions memberPermissions, String ordinal) {
        if (!memberRepository.existsById(memberUUID)) {
            return ResponseEntity.badRequest().body("\"Nie znaleziono Klubowicza\"");
        }
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        MemberPermissionsEntity memberPermissionsEntity = memberEntity.getMemberPermissions();
        List<MemberEntity> collect = memberRepository.findAll()
                .stream()
                .filter(f -> !f.isErased())
                .filter(f -> f.getMemberPermissions().getInstructorNumber() != null)
                .filter(f -> f.getMemberPermissions().getShootingLeaderNumber() != null)
                .filter(f -> f.getMemberPermissions().getArbiterStaticNumber() != null).toList();
//        Instruktor
        String instructor = "";
        if (memberPermissions.getInstructorNumber() != null) {
            if (!memberPermissions.getInstructorNumber().isEmpty()) {
                if (collect.stream().noneMatch(e -> e.getMemberPermissions().getInstructorNumber().equals(memberPermissions.getInstructorNumber()))) {
                    memberPermissionsEntity.setInstructorNumber(memberPermissions.getInstructorNumber());
                    LOG.info("Nadano uprawnienia Instruktora");
                    instructor = "instruktora";
                } else {
                    LOG.info("Nie można nadać uprawnień");
                    return ResponseEntity.badRequest().body("\"Nie można nadać uprawnień Instruktora\"");
                }
            }
        }
//        Prowadzący Strzelanie
        String leader = "";
        if (memberPermissions.getShootingLeaderNumber() != null) {
            if (!memberPermissions.getShootingLeaderNumber().isEmpty()) {
                if (collect.stream().noneMatch(e -> e.getMemberPermissions().getShootingLeaderNumber().equals(memberPermissions.getShootingLeaderNumber()))) {
                    memberPermissionsEntity.setShootingLeaderNumber(memberPermissions.getShootingLeaderNumber());
                    LOG.info("Nadano uprawnienia Prowadzącego Strzelanie");
                    leader = "prowadzącego strzelanie";
                } else {
                    LOG.info("Nie można nadać uprawnień");
                    return ResponseEntity.badRequest().body("\"Nie można nadać uprawnień Prowadzącego Strzelanie\"");
                }

            }
        }
//        Sędzia
        String arbiter = "";
        if (memberPermissions.getArbiterStaticNumber() != null) {
            if (!memberPermissions.getArbiterStaticNumber().isEmpty()) {
                if (collect.stream().noneMatch(e -> e.getMemberPermissions().getArbiterStaticNumber().equals(memberPermissions.getArbiterStaticNumber()))) {
                    memberPermissionsEntity.setArbiterStaticNumber(memberPermissions.getArbiterStaticNumber());
                    LOG.info("Zmieniono numer sędziego");
                    arbiter = "sędziego";
                } else {
                    LOG.info("Nie można nadać uprawnień");
                    return ResponseEntity.badRequest().body("\"Nie można nadać uprawnień Sędziego\"");
                }
            }
            if (ordinal != null && !ordinal.isEmpty()) {
                if (ordinal.equals("1")) {
                    memberPermissionsEntity.setArbiterStaticClass(ArbiterClass.CLASS_3.getName());
                }
                if (ordinal.equals("2")) {
                    memberPermissionsEntity.setArbiterStaticClass(ArbiterClass.CLASS_2.getName());
                }
                if (ordinal.equals("3")) {
                    memberPermissionsEntity.setArbiterStaticClass(ArbiterClass.CLASS_1.getName());
                }
                if (ordinal.equals("4")) {
                    memberPermissionsEntity.setArbiterStaticClass(ArbiterClass.CLASS_STATE.getName());
                }
                if (ordinal.equals("5")) {
                    memberPermissionsEntity.setArbiterStaticClass(ArbiterClass.CLASS_INTERNATIONAL.getName());
                }
                LOG.info("Klasa sędziego ustawiona na pole nr {}", ordinal);
            }
            if (memberPermissions.getArbiterStaticPermissionValidThru() != null) {
                LocalDate date = LocalDate.of(memberPermissions.getArbiterStaticPermissionValidThru().getYear(), 12, 31);
                memberPermissionsEntity.setArbiterStaticPermissionValidThru(date);
                LOG.info("Zmieniono datę ważności licencji sędziowskiej");
            }
        }
        memberPermissionsRepository.save(memberPermissionsEntity);

        return ResponseEntity.ok("\"Przyznano uprawnienia " + instructor + " " + leader + " " + arbiter + "\"");
    }


    public ResponseEntity<?> updateMemberArbiterClass(String memberUUID) {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        MemberPermissionsEntity memberPermissions = memberEntity.getMemberPermissions();
        String arbiterClass = memberPermissions.getArbiterStaticClass();

        if (memberPermissions.getArbiterStaticNumber() == null || memberPermissions.getArbiterStaticNumber().isEmpty()) {
            LOG.info("nie można zaktualizować");
            return ResponseEntity.badRequest().body("\"nie można zaktualizować\"");
        }

        String finalArbiterClass = arbiterClass;
        ArbiterClass arbiterClass1 = Arrays.stream(ArbiterClass.values()).filter(f -> f.getName().equals(finalArbiterClass)).findFirst().orElseThrow(EntityNotFoundException::new);
        if (arbiterClass1.equals(ArbiterClass.CLASS_3)) {
            arbiterClass = ArbiterClass.CLASS_2.getName();
        }
        if (arbiterClass1.equals(ArbiterClass.CLASS_2)) {
            arbiterClass = ArbiterClass.CLASS_1.getName();
        }
        if (arbiterClass1.equals(ArbiterClass.CLASS_1)) {
            arbiterClass = ArbiterClass.CLASS_STATE.getName();
        }
        if (arbiterClass1.equals(ArbiterClass.CLASS_STATE)) {
            arbiterClass = ArbiterClass.CLASS_INTERNATIONAL.getName();
        }
        memberPermissions.setArbiterStaticClass(arbiterClass);
        memberPermissionsRepository.save(memberPermissions);
        return ResponseEntity.ok("\"Podniesiono klasę sędziego na " + arbiterClass + "\"");


    }

    public MemberPermissions getMemberPermissions() {
        return MemberPermissions.builder()
                .instructorNumber(null)
                .shootingLeaderNumber(null)
                .arbiterStaticClass(null)
                .arbiterStaticNumber(null)
                .arbiterStaticPermissionValidThru(null)
                .build();
    }

    public List<String> getArbiterClasses() {
        return Arrays.stream(ArbiterClass.values()).map(ArbiterClass::getName).collect(Collectors.toList());
    }
}
