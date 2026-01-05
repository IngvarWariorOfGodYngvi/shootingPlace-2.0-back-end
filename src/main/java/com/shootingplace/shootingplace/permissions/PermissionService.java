package com.shootingplace.shootingplace.permissions;

import com.shootingplace.shootingplace.enums.ArbiterDynamicClass;
import com.shootingplace.shootingplace.enums.ArbiterStaticClass;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberInfo;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
import com.shootingplace.shootingplace.utils.Mapping;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private static final Collator PL_COLLATOR = Collator.getInstance(Locale.forLanguageTag("pl"));
    private final PermissionsRepository permissionsRepository;
    private final MemberRepository memberRepository;
    private final OtherPersonRepository otherPersonRepository;
    private final Logger LOG = LogManager.getLogger(getClass());

    public List<MemberInfo> getArbiters() {
        return memberRepository.findAllByErasedFalseAndMemberPermissions_ArbiterStaticNumberIsNotNull().stream().map(Mapping::map2).sorted(Comparator.comparing(MemberInfo::getSecondName, PL_COLLATOR).thenComparing(MemberInfo::getFirstName, PL_COLLATOR)).toList();
    }

    public List<MemberInfo> getAllOthersArbiters() {
        return otherPersonRepository.findAll().stream().filter(f -> f.getPermissionsEntity() != null && f.getPermissionsEntity().getArbiterStaticNumber() != null).map(m -> MemberInfo.builder().id(m.getId()).arbiterStaticClass(m.getPermissionsEntity().getArbiterStaticClass()).firstName(m.getFirstName()).secondName(m.getSecondName()).name(m.getFullName()).arbiterDynamicClass(m.getPermissionsEntity().getArbiterDynamicClass()).build()).collect(Collectors.toList());
    }

    @Transactional
    public ResponseEntity<?> updatePermissions(MemberPermissionsEntity entity, MemberPermissions dto) {

        List<MemberEntity> activeMembers = memberRepository.findAll().stream().filter(m -> !m.isErased()).toList();

        try {
            // Instruktor
            assertUnique(dto.getInstructorNumber(), MemberPermissionsEntity::getInstructorNumber, activeMembers, "Nie można nadać uprawnień Instruktora");

            if (isProvided(dto.getInstructorNumber())) {
                entity.setInstructorNumber(dto.getInstructorNumber());
                LOG.info("Nadano uprawnienia Instruktora");
            }

            // Prowadzący strzelanie
            assertUnique(dto.getShootingLeaderNumber(), MemberPermissionsEntity::getShootingLeaderNumber, activeMembers, "Nie można nadać uprawnień Prowadzącego Strzelanie");

            if (isProvided(dto.getShootingLeaderNumber())) {
                entity.setShootingLeaderNumber(dto.getShootingLeaderNumber());
                LOG.info("Nadano uprawnienia Prowadzącego Strzelanie");
            }

            // Sędzia statyczny
            handleArbiter(dto.getArbiterStaticNumber(), dto.getArbiterStaticClass(), dto.getArbiterStaticPermissionIssuedAt(), dto.getArbiterStaticPermissionValidThru(), "LS", ArbiterStaticClass::fromName, entity::setArbiterStaticNumber, entity::setArbiterStaticClass, entity::setArbiterStaticPermissionIssuedAt, entity::setArbiterStaticPermissionValidThru, MemberPermissionsEntity::getArbiterStaticNumber, activeMembers, "Nie można nadać uprawnień Sędziego Statycznego");

            // Sędzia dynamiczny
            handleArbiter(dto.getArbiterDynamicNumber(), dto.getArbiterDynamicClass(), dto.getArbiterDynamicPermissionIssuedAt(), dto.getArbiterDynamicPermissionValidThru(), "LD", ArbiterDynamicClass::fromName, entity::setArbiterDynamicNumber, entity::setArbiterDynamicClass, entity::setArbiterDynamicPermissionIssuedAt, entity::setArbiterDynamicPermissionValidThru, MemberPermissionsEntity::getArbiterDynamicNumber, activeMembers, "Nie można nadać uprawnień Sędziego Dynamicznego");

            permissionsRepository.save(entity);
            return ResponseEntity.ok("Zaktualizowano uprawnienia");

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    private <T extends Enum<T>> void handleArbiter(String number, String clazz, LocalDate issuedAt, LocalDate validThru, String prefix, Function<String, T> classResolver, Consumer<String> numberSetter, Consumer<String> classSetter, Consumer<LocalDate> issuedSetter, Consumer<LocalDate> validSetter, Function<MemberPermissionsEntity, String> extractor, List<MemberEntity> members, String errorMessage) {

        assertUnique(number, extractor, members, errorMessage);

        // numer licencji – enum tylko do składania numeru
        if (isProvided(number) && issuedAt != null && isProvided(clazz)) {
            String code = prefix + "-" + number + "/" + classResolver.apply(clazz) + "/" + (issuedAt.getYear() % 100);

            numberSetter.accept(code);
            LOG.info("Zmieniono numer sędziego");
        }

        // zapis klasy – dokładnie to, co przyszło z frontu
        if (isProvided(clazz)) {
            classSetter.accept(clazz);
        }

        if (issuedAt != null) {
            issuedSetter.accept(issuedAt);
        }

        if (validThru != null) {
            validSetter.accept(LocalDate.of(validThru.getYear(), 12, 31));
        }
    }

    private String extractBaseNumber(String fullNumber) {
        // LS-123/II/23 -> 123
        int dash = fullNumber.indexOf('-');
        int slash = fullNumber.indexOf('/');

        if (dash < 0 || slash < 0 || dash > slash) {
            throw new IllegalStateException("Nieprawidłowy numer sędziego: " + fullNumber);
        }
        return fullNumber.substring(dash + 1, slash);
    }
    private boolean isProvided(String value) {
        return value != null && !value.isBlank();
    }

    private void assertUnique(String value, Function<MemberPermissionsEntity, String> extractor, List<MemberEntity> members, String errorMessage) {
        if (value == null || value.isBlank()) return;
        boolean exists = members.stream().map(MemberEntity::getMemberPermissions).map(extractor).filter(Objects::nonNull).anyMatch(value::equals);
        if (exists) {
            throw new IllegalStateException(errorMessage);
        }
    }

    public ResponseEntity<?> updateMemberArbiterStaticClass(MemberPermissions permissions, String memberUUID) {

        MemberPermissionsEntity entity = memberRepository.findById(memberUUID)
                .orElseThrow(EntityNotFoundException::new)
                .getMemberPermissions();

        if (!isProvided(entity.getArbiterStaticNumber())) {
            LOG.info("nie można zaktualizować");
            return ResponseEntity.badRequest().body("nie można zaktualizować");
        }

        ArbiterStaticClass current = ArbiterStaticClass.fromName(entity.getArbiterStaticClass());

        String nextClass = switch (current) {
            case III -> ArbiterStaticClass.II.getName();
            case II  -> ArbiterStaticClass.I.getName();
            case I   -> ArbiterStaticClass.P.getName();
            default  -> entity.getArbiterStaticClass();
        };

        String baseNumber = extractBaseNumber(entity.getArbiterStaticNumber());

        List<MemberEntity> activeMembers =
                memberRepository.findAll().stream()
                        .filter(m -> !m.isErased())
                        .toList();

        handleArbiter(
                baseNumber,
                nextClass,
                permissions.getArbiterStaticPermissionIssuedAt(),
                permissions.getArbiterStaticPermissionValidThru(),
                "LS",
                ArbiterStaticClass::fromName,
                entity::setArbiterStaticNumber,
                entity::setArbiterStaticClass,
                entity::setArbiterStaticPermissionIssuedAt,
                entity::setArbiterStaticPermissionValidThru,
                MemberPermissionsEntity::getArbiterStaticNumber,
                activeMembers,
                "Nie można zaktualizować numeru sędziego"
        );

        permissionsRepository.save(entity);

        return ResponseEntity.ok("Podniesiono klasę sędziego na " + nextClass);
    }
    public ResponseEntity<?> updateMemberArbiterDynamicClass(MemberPermissions permissions, String memberUUID) {

        MemberPermissionsEntity entity = memberRepository.findById(memberUUID)
                .orElseThrow(EntityNotFoundException::new)
                .getMemberPermissions();

        if (!isProvided(entity.getArbiterDynamicNumber())) {
            LOG.info("nie można zaktualizować");
            return ResponseEntity.badRequest().body("nie można zaktualizować");
        }

        ArbiterDynamicClass current = ArbiterDynamicClass.fromName(entity.getArbiterDynamicClass());

        String nextClass = switch (current) {
            case ARO -> ArbiterDynamicClass.RO.getName();
            case RO  -> ArbiterDynamicClass.CRO.getName();
            case CRO   -> ArbiterDynamicClass.RM.getName();
            default  -> entity.getArbiterDynamicClass();
        };

        String baseNumber = extractBaseNumber(entity.getArbiterDynamicNumber());

        List<MemberEntity> activeMembers =
                memberRepository.findAll().stream()
                        .filter(m -> !m.isErased())
                        .toList();

        handleArbiter(
                baseNumber,
                nextClass,
                permissions.getArbiterDynamicPermissionIssuedAt(),
                permissions.getArbiterDynamicPermissionValidThru(),
                "LD",
                ArbiterDynamicClass::fromName,
                entity::setArbiterDynamicNumber,
                entity::setArbiterDynamicClass,
                entity::setArbiterDynamicPermissionIssuedAt,
                entity::setArbiterDynamicPermissionValidThru,
                MemberPermissionsEntity::getArbiterDynamicNumber,
                activeMembers,
                "Nie można zaktualizować numeru sędziego"
        );

        permissionsRepository.save(entity);

        return ResponseEntity.ok("Podniesiono klasę sędziego na " + nextClass);
    }


    public MemberPermissions getMemberPermissions() {
        return MemberPermissions.builder().instructorNumber(null).shootingLeaderNumber(null).arbiterStaticClass(null).arbiterStaticNumber(null).arbiterStaticPermissionValidThru(null).build();
    }

    public List<String> getArbiterStaticClasses() {
        return Arrays.stream(ArbiterStaticClass.values()).map(ArbiterStaticClass::getName).collect(Collectors.toList());
    }

    public List<String> getArbiterDynamicClasses() {
        return Arrays.stream(ArbiterDynamicClass.values()).map(ArbiterDynamicClass::getName).collect(Collectors.toList());
    }
}
