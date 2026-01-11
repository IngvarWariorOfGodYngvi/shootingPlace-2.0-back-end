package com.shootingplace.shootingplace.configurations;

import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.armory.GunUsedRepository;
import com.shootingplace.shootingplace.armory.gunRepresentation.GunRepresentationService;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
import com.shootingplace.shootingplace.settings.SystemConfigEntity;
import com.shootingplace.shootingplace.settings.SystemConfigRepository;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class StartConfiguration {

    private final UserRepository userRepository;
    private final GunRepresentationService gunRepresentationService;
    private final GunUsedRepository gunUsedRepository;
    private final JdbcTemplate jdbcTemplate;
    private final OtherPersonRepository otherPersonRepository;
    private final SystemConfigRepository systemConfigRepository;
    @Transactional
    @PostConstruct
    public void init() {
        createAdmin();
        createEmptyClub();
        hashPinForAll();
        checkIP();
        function();
        fixOtherPersonEntity();
        createSystemConfigEntity();
    }

    private void fixOtherPersonEntity() {
        otherPersonRepository.findAllByLicenseNumber("").forEach(e -> {
            e.setLicenseNumber(null);
            otherPersonRepository.save(e);
        });
    }

    public void createAdmin() {
        if (!userRepository.existsBySecondName("Admin")) {
            UserEntity admin = UserEntity.builder()
                    .pinCode("9966")
                    .secondName("Admin")
                    .firstName("Admin")
                    .active(true)
                    .member(null)
                    .changeHistoryEntities(new ArrayList<>())
                    .build();

            List<String> permissions = new ArrayList<>();
            permissions.add(UserSubType.ADMIN.getName());
            permissions.add(UserSubType.SUPER_USER.getName());
            admin.setUserPermissionsList(permissions);

            userRepository.save(admin);
        }
    }
    public void createSystemConfigEntity() {
        SystemConfigEntity systemConfigEntity = systemConfigRepository.findById(1).orElse(null);
        if (systemConfigEntity == null) {
            systemConfigEntity = SystemConfigEntity.builder()
                    .buildTime(System.currentTimeMillis())
                    .iFrameGoogleCalendar(null)
                    .build();
        }
        else {
            systemConfigEntity.setBuildTime(System.currentTimeMillis());
        }
        systemConfigRepository.save(systemConfigEntity);
    }

    public void createEmptyClub() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM shootingplace.club_entity WHERE id = 1",
                Integer.class
        );

        if (count == 0) {
            jdbcTemplate.update("""
                        INSERT INTO shootingplace.club_entity (id,full_name,short_name, city, wzss, vovoidership,url,
                        phone_number,appartment_number,house_number,license_number, email)
                        VALUES (1, '', 'firstStart', '', '', '', '', '', '', '', '', '')
                    """);
        }
        Integer count1 = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM shootingplace.club_entity WHERE id = 2",
                Integer.class
        );

        if (count1 == 0) {
            jdbcTemplate.update("""
                        INSERT INTO shootingplace.club_entity (id,full_name,short_name, city, wzss, vovoidership,url,
                        phone_number,appartment_number,house_number,license_number, email)
                        VALUES (2, 'BRAK', 'BRAK', '', '', '', '', '', '', '', '', '')
                    """);
        }
    }

    public void hashPinForAll() {
        userRepository.findAll().forEach(u -> {
            if (u != null && u.getPinCode() != null && u.getPinCode().length() == 4) {
                String pin = Hashing.sha256()
                        .hashString(u.getPinCode(), StandardCharsets.UTF_8)
                        .toString();
                u.setPinCode(pin);
                userRepository.save(u);
            }
        });
    }

    public void checkIP() {
        try {
            System.out.println("Your current IP address : " + InetAddress.getLocalHost());
            System.out.println("Your current Hostname : " + InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void function() {
        gunUsedRepository.findAll()
                .forEach(gunRepresentationService::function);
    }
}

