package com.shootingplace.shootingplace.otherPerson;

import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.address.AddressEntity;
import com.shootingplace.shootingplace.address.AddressRepository;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.exceptions.domain.DomainNotFoundException;
import com.shootingplace.shootingplace.history.HistoryEntityType;
import com.shootingplace.shootingplace.changeHistory.RecordHistory;
import com.shootingplace.shootingplace.permissions.MemberPermissionsEntity;
import com.shootingplace.shootingplace.permissions.PermissionsRepository;
import com.shootingplace.shootingplace.permissions.PermissionService;
import com.shootingplace.shootingplace.utils.Mapping;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.shootingplace.shootingplace.utils.NameNormalizer.normalizeFirstName;
import static com.shootingplace.shootingplace.utils.NameNormalizer.normalizeSecondName;

@Service
@RequiredArgsConstructor
public class OtherPersonService {

    private final ClubRepository clubRepository;
    private final OtherPersonRepository otherPersonRepository;
    private final PermissionsRepository permissionsRepository;
    private final PermissionService permissionService;
    private final AddressRepository addressRepository;
    private final Logger LOG = LogManager.getLogger();

    public ResponseEntity<?> addPerson(OtherPerson person) {

        boolean a = otherPersonRepository.existsByPhoneNumber(person.getPhoneNumber().replaceAll(" ", ""));
        if (a) {
            return ResponseEntity.badRequest().body("Ktoś już ma taki numer telefonu i nie można takiego już dodać");
        }
        boolean b = otherPersonRepository.existsByLicenseNumber(person.getLicenseNumber());
        if (b) {
            return ResponseEntity.badRequest().body("Ktoś już ma taki numer licencji zawodniczej");
        }

        ClubEntity clubEntity = clubRepository.findByShortName(person.getClub().getShortName());
        if (person.getClub().getShortName().isEmpty() || clubEntity == null) {
            clubEntity = clubRepository.findById(2).orElseThrow(() -> new DomainNotFoundException("Club", "2"));
        }
        MemberPermissionsEntity memberPermissionsEntity = null;
        if (person.getMemberPermissions() != null) {
            memberPermissionsEntity = permissionsRepository.save(Mapping.map(person.getMemberPermissions()));
        }
        AddressEntity addressEntity = null;
        if (person.getAddress() != null) {
            addressEntity = addressRepository.save(Mapping.map(person.getAddress()));
        }

        OtherPersonEntity otherPersonEntity = OtherPersonEntity.builder()
                .firstName(normalizeFirstName(person.getFirstName()))
                .secondName(normalizeSecondName(person.getSecondName()))
                .phoneNumber(person.getPhoneNumber().trim().replaceAll(" ", ""))
                .active(true)
                .email(person.getEmail())
                .permissionsEntity(memberPermissionsEntity)
                .weaponPermissionNumber(person.getWeaponPermissionNumber() != null ? person.getWeaponPermissionNumber().toUpperCase(Locale.ROOT) : null)
                .club(clubEntity)
                .licenseNumber(person.getLicenseNumber().isEmpty() ? null : person.getLicenseNumber().trim().replaceAll(" ", ""))
                .address(addressEntity).build();
        otherPersonEntity.setCreationDate();
        otherPersonRepository.save(otherPersonEntity);
        LOG.info("Zapisano nową osobę {}", otherPersonEntity.getFullName());
        return ResponseEntity.status(201).body("Zapisano nową osobę " + otherPersonEntity.getFirstName() + " " + otherPersonEntity.getSecondName());

    }

    public List<String> getAllOthers() {
        return otherPersonRepository.findAllByActiveTrue().stream().map(e -> e.getSecondName().concat(" " + e.getFirstName() + " Klub: " + e.getClub().getShortName() + " ID: " + e.getId())).collect(Collectors.toList());
    }

    public List<?> getAll() {
        return otherPersonRepository.findAllByActiveTrue().stream().sorted(Comparator.comparing(OtherPersonEntity::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl"))).thenComparing(OtherPersonEntity::getFirstName)).collect(Collectors.toList());
    }

    public List<?> getAllCompetitors() {
        return otherPersonRepository.findAllByActiveTrue().stream()
                .filter(f -> f.getLicenseNumber() != null && !f.getLicenseNumber().isEmpty())
                .sorted(Comparator.comparing(OtherPersonEntity::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl"))).thenComparing(OtherPersonEntity::getFirstName)).collect(Collectors.toList());
    }

    public List<OtherPersonEntity> getOthersWithPermissions() {
        return otherPersonRepository.findAllByActiveTrue().stream().filter(f -> f.getPermissionsEntity() != null).collect(Collectors.toList());
    }

    @RecordHistory(action = "OtherPerson.deactivate", entity = HistoryEntityType.OTHER_PERSON, entityArgIndex = 0)
    public ResponseEntity<?> deactivatePerson(int id) {
        if (!otherPersonRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Nie znaleziono osoby");
        }

        OtherPersonEntity otherPerson = otherPersonRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        otherPerson.setActive(false);
        otherPersonRepository.save(otherPerson);

        LOG.info("Dezaktywowano Nie-Klubowicza: {}", otherPerson.getFullName());
        return ResponseEntity.ok("Usunięto Osobę");
    }


    @Transactional
    public ResponseEntity<?> updatePerson(String id, OtherPerson oP) {

        OtherPersonEntity one = otherPersonRepository.findById(Integer.valueOf(id)).orElseThrow(EntityNotFoundException::new);

        one.setEmail(coalesceText(oP.getEmail(), one.getEmail()));

        one.setPhoneNumber(coalesceText(oP.getPhoneNumber(), one.getPhoneNumber()).replaceAll(" ", ""));

        one.setFirstName(normalizeFirstName(coalesceText(oP.getFirstName(), one.getFirstName())));
        one.setSecondName(normalizeSecondName(coalesceText(oP.getSecondName(), one.getSecondName())));

        one.setWeaponPermissionNumber(coalesceText(oP.getWeaponPermissionNumber(), one.getWeaponPermissionNumber()));

        boolean b = otherPersonRepository.existsByLicenseNumber(oP.getLicenseNumber());
        if (!b) {
            one.setLicenseNumber(oP.getLicenseNumber());
        } else {
            LOG.info("Ktoś już ma taki numer licencji");
        }

        one.setLicenseNumber(oP.getLicenseNumber());


        Address a1 = oP.getAddress();
        AddressEntity a2 = one.getAddress() != null ? one.getAddress() : new AddressEntity();

        if (a1 != null) {
            a2.setPostOfficeCity(coalesce(a1.getPostOfficeCity(), a2.getPostOfficeCity()));
            a2.setZipCode(coalesce(a1.getZipCode(), a2.getZipCode()));
            a2.setStreet(coalesce(a1.getStreet(), a2.getStreet()));
            a2.setStreetNumber(coalesce(a1.getStreetNumber(), a2.getStreetNumber()));
            a2.setFlatNumber(coalesce(a1.getFlatNumber(), a2.getFlatNumber()));
        }

        one.setAddress(addressRepository.save(a2));

        if (oP.getClub() != null && oP.getClub().getShortName() != null) {
            if (one.getClub() == null || !oP.getClub().getShortName().equals(one.getClub().getShortName())) {
                ClubEntity club = clubRepository.findByShortName(oP.getClub().getShortName());

                if (club != null) {
                    one.setClub(club);
                }
            }
        }
        MemberPermissionsEntity permissionsEntity = one.getPermissionsEntity() != null ? one.getPermissionsEntity() : new MemberPermissionsEntity();

        ResponseEntity<?> response = permissionService.updatePermissions(permissionsEntity, oP.getMemberPermissions());

        if (!response.getStatusCode().is2xxSuccessful()) {
            return response;
        }

        one.setPermissionsEntity(permissionsEntity);
        otherPersonRepository.save(one);

        return ResponseEntity.ok("Zaktualizowano");
    }

    private <T> T coalesce(T newVal, T oldVal) {
        return newVal != null ? newVal : oldVal;
    }

    private String coalesceText(String newVal, String oldVal) {
        return (newVal != null && !newVal.isBlank()) ? newVal : oldVal;
    }

    public ResponseEntity<?> getOtherByPhone(String phone) {
        OtherPersonEntity otherPerson = otherPersonRepository.findAllByPhoneNumberAndActiveTrue(phone.replaceAll(" ", "")).stream().filter(OtherPersonEntity::isActive).findFirst().orElse(null);
        if (otherPerson != null) {
            return ResponseEntity.ok(otherPerson);
        } else {
            return ResponseEntity.badRequest().body("brak takiego numeru w bazie");
        }
    }

    public ResponseEntity<?> getOtherByLicense(String license) {
        OtherPersonEntity otherPerson = otherPersonRepository.findByLicenseNumberAndActiveTrue(license).orElse(null);
        if (otherPerson != null) {
            return ResponseEntity.ok(otherPerson);
        } else {
            return ResponseEntity.badRequest().body("brak takiego numeru licencji w bazie");
        }
    }
}
