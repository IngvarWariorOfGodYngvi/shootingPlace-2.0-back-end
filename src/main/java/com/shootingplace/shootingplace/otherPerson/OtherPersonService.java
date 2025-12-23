package com.shootingplace.shootingplace.otherPerson;

import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.address.AddressEntity;
import com.shootingplace.shootingplace.address.AddressRepository;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.history.HistoryEntityType;
import com.shootingplace.shootingplace.history.RecordHistory;
import com.shootingplace.shootingplace.member.MemberInfo;
import com.shootingplace.shootingplace.member.permissions.MemberPermissions;
import com.shootingplace.shootingplace.member.permissions.MemberPermissionsEntity;
import com.shootingplace.shootingplace.member.permissions.MemberPermissionsRepository;
import com.shootingplace.shootingplace.utils.Mapping;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OtherPersonService {

    private final ClubRepository clubRepository;
    private final OtherPersonRepository otherPersonRepository;
    private final MemberPermissionsRepository memberPermissionsRepository;
    private final AddressRepository addressRepository;
    private final Logger LOG = LogManager.getLogger();

    public ResponseEntity<?> addPerson(String club, OtherPerson person, MemberPermissions permissions) {
        MemberPermissionsEntity permissionsEntity = null;
        boolean match = clubRepository.findAll().stream().anyMatch(a -> a.getShortName().equals(club));

        ClubEntity clubEntity;
        if (match) {
            clubEntity = clubRepository.findAll().stream().filter(f -> f.getShortName().equals(club)).findFirst().orElseThrow(EntityNotFoundException::new);
        } else {
            List<ClubEntity> all = clubRepository.findAll();
            all.sort(Comparator.comparing(ClubEntity::getId).reversed());
            Integer id = (all.getFirst().getId()) + 1;
            clubEntity = ClubEntity.builder().id(id).shortName(club).build();
            clubRepository.save(clubEntity);
        }
        if (permissions != null) {
            permissionsEntity = Mapping.map(permissions);

            memberPermissionsRepository.save(permissionsEntity);
        }
        AddressEntity addressEntity = null;
        if (person.getAddress() != null) {
            addressEntity = addressRepository.save(AddressEntity.builder().zipCode(person.getAddress().getZipCode()).postOfficeCity(person.getAddress().getPostOfficeCity()).street(person.getAddress().getStreet()).streetNumber(person.getAddress().getStreetNumber()).flatNumber(person.getAddress().getFlatNumber()).build());
        }
        OtherPersonEntity otherPersonEntity = OtherPersonEntity.builder().firstName(person.getFirstName().substring(0, 1).toUpperCase() + person.getFirstName().substring(1).toLowerCase()).secondName(person.getSecondName().toUpperCase()).phoneNumber(person.getPhoneNumber().trim().replaceAll(" ", "")).active(true).email(person.getEmail()).permissionsEntity(permissionsEntity).weaponPermissionNumber(person.getWeaponPermissionNumber() != null ? person.getWeaponPermissionNumber().toUpperCase(Locale.ROOT) : null).club(clubEntity).address(addressEntity).build();
        otherPersonEntity.setCreationDate();
        otherPersonRepository.save(otherPersonEntity);
        LOG.info("Zapisano nową osobę {} {}", otherPersonEntity.getFirstName(), otherPersonEntity.getSecondName());
        return ResponseEntity.status(201).body("Zapisano nową osobę " + otherPersonEntity.getFirstName() + " " + otherPersonEntity.getSecondName());

    }

    public OtherPersonEntity addPerson(String club, OtherPerson person) {
        boolean match = clubRepository.findAll().stream().anyMatch(a -> a.getShortName().equals(club));
        ClubEntity clubEntity;
        if (club.isEmpty()) {
            clubEntity = clubRepository.findById(1).orElseThrow(EntityNotFoundException::new);
        } else {
            if (match) {
                clubEntity = clubRepository.findAll().stream().filter(f -> f.getShortName().equals(club)).findFirst().orElseThrow(EntityNotFoundException::new);
            } else {
                List<ClubEntity> all = clubRepository.findAll();
                all.sort(Comparator.comparing(ClubEntity::getId).reversed());
                Integer id = (all.getFirst().getId()) + 1;
                clubEntity = ClubEntity.builder().id(id).shortName(club).build();
                clubRepository.save(clubEntity);
            }
        }
        List<OtherPersonEntity> all = otherPersonRepository.findAll();
        int id;
        if (all.isEmpty()) {
            id = 1;
        } else {
            all.sort(Comparator.comparing(OtherPersonEntity::getId).reversed());
            id = all.stream().max(Comparator.comparing(OtherPersonEntity::getId)).get().getId() + 1;
        }
        OtherPersonEntity otherPersonEntity = OtherPersonEntity.builder().id(id).firstName(person.getFirstName().substring(0, 1).toUpperCase() + person.getFirstName().substring(1).toLowerCase()).secondName(person.getSecondName().toUpperCase()).phoneNumber(person.getPhoneNumber().replaceAll(" ", "").trim()).active(true).email(person.getEmail()).permissionsEntity(null).creationDate(LocalDateTime.now()).club(clubEntity).build();
        if (person.getWeaponPermissionNumber().isEmpty()) {
            AddressEntity addressEntity = addressRepository.save(AddressEntity.builder().zipCode(person.getAddress().getZipCode()).postOfficeCity(person.getAddress().getPostOfficeCity()).street(person.getAddress().getStreet()).streetNumber(person.getAddress().getStreetNumber()).flatNumber(person.getAddress().getFlatNumber()).build());
            otherPersonEntity.setAddress(addressEntity);
        } else {
            otherPersonEntity.setWeaponPermissionNumber(person.getWeaponPermissionNumber());
        }
        otherPersonEntity.setCreationDate();
        LOG.info("Zapisano nową osobę {} {}", otherPersonEntity.getFirstName(), otherPersonEntity.getSecondName());
        return otherPersonRepository.save(otherPersonEntity);

    }

    public List<String> getAllOthers() {
        return otherPersonRepository.findAllByActiveTrue().stream().map(e -> e.getSecondName().concat(" " + e.getFirstName() + " Klub: " + e.getClub().getShortName() + " ID: " + e.getId())).collect(Collectors.toList());
    }

    public List<MemberInfo> getAllOthersArbiters() {
        return otherPersonRepository.findAll().stream().filter(f -> f.getPermissionsEntity() != null && f.getPermissionsEntity().getArbiterNumber() != null).map(m -> MemberInfo.builder().id(m.getId()).arbiterClass(m.getPermissionsEntity().getArbiterClass()).firstName(m.getFirstName()).secondName(m.getSecondName()).name(m.getFullName()).build()).collect(Collectors.toList());
    }

    public List<?> getAll() {
        return otherPersonRepository.findAllByActiveTrue().stream().sorted(Comparator.comparing(OtherPersonEntity::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl"))).thenComparing(OtherPersonEntity::getFirstName)).collect(Collectors.toList());
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


    public ResponseEntity<?> updatePerson(String id, OtherPerson oP, String clubName) {
        OtherPersonEntity one = otherPersonRepository.findById(Integer.valueOf(id)).orElseThrow(EntityNotFoundException::new);
        if ((oP.getEmail() != null && !oP.getEmail().isEmpty()) && !one.getEmail().equals(oP.getEmail())) {
            LOG.info("Osobie {} Zmieniono email", one.getFullName());
            one.setEmail(oP.getEmail());
        }
        if ((oP.getPhoneNumber() != null && !oP.getPhoneNumber().isEmpty()) && !one.getPhoneNumber().equals(oP.getPhoneNumber())) {
            LOG.info("Osobie {} Zmieniono numer telefonu", one.getFullName());
            one.setPhoneNumber(oP.getPhoneNumber().replaceAll(" ", ""));
        }
        if ((oP.getFirstName() != null && !oP.getFirstName().isEmpty()) && !(one.getFirstName().substring(0, 1).toUpperCase() + one.getFirstName().substring(1).toLowerCase()).equals(oP.getFirstName())) {
            LOG.info("Osobie {} Zmieniono Imię", one.getFullName());
            one.setFirstName(oP.getFirstName());
        }
        if ((oP.getSecondName() != null && !oP.getSecondName().isEmpty()) && !one.getSecondName().toUpperCase().equals(oP.getSecondName())) {
            LOG.info("Osobie {} Zmieniono nazwisko", one.getFullName());
            one.setSecondName(oP.getSecondName());
        }
        if ((oP.getWeaponPermissionNumber() != null && !oP.getWeaponPermissionNumber().isEmpty()) && !one.getWeaponPermissionNumber().equals(oP.getWeaponPermissionNumber())) {
            LOG.info("Osobie {} Zmieniono numer Pozwolenia na broń", one.getFullName());
            one.setWeaponPermissionNumber(oP.getWeaponPermissionNumber());
        }
        Address a1 = oP.getAddress();
        AddressEntity a2 = one.getAddress() != null ? one.getAddress() : new AddressEntity();

        a2.setPostOfficeCity(a1.getPostOfficeCity() != null ? a1.getPostOfficeCity() : a2.getPostOfficeCity());
        a2.setZipCode(a1.getZipCode() != null ? a1.getZipCode() : a2.getZipCode());
        a2.setStreet(a1.getStreet() != null ? a1.getStreet() : a2.getStreet());
        a2.setStreetNumber(a1.getStreetNumber() != null ? a1.getStreetNumber() : a2.getStreetNumber());
        a2.setFlatNumber(a1.getFlatNumber() != null ? a1.getFlatNumber() : a2.getFlatNumber());
        AddressEntity save1 = addressRepository.save(a2);
        one.setAddress(save1);
        if ((clubName != null && !clubName.isEmpty()) && !one.getClub().getShortName().equals(clubName)) {
            ClubEntity clubEntity = clubRepository.findAll().stream().filter(f -> f.getShortName().equals(clubName)).findFirst().orElse(null);
            if (clubEntity == null) {
                {
                    List<ClubEntity> all = clubRepository.findAll();
                    all.sort(Comparator.comparing(ClubEntity::getId).reversed());
                    Integer clubID = (all.getFirst().getId()) + 1;
                    clubEntity = ClubEntity.builder().id(clubID).shortName(clubName).build();
                    clubRepository.save(clubEntity);
                }
            }
            LOG.info("Osobie{} Zmieniono Klub", one.getFullName());
            one.setClub(clubEntity);
        }
        MemberPermissions m1 = oP.getMemberPermissions();
        MemberPermissionsEntity m2 = one.getPermissionsEntity() != null ? one.getPermissionsEntity() : new MemberPermissionsEntity();

        m2.setArbiterClass(m1.getArbiterClass() != null ? m1.getArbiterClass() : m2.getArbiterClass() != null ? m2.getArbiterClass() : null);
        m2.setArbiterNumber(m1.getArbiterNumber() != null ? m1.getArbiterNumber() : m2.getArbiterNumber() != null ? m2.getArbiterNumber() : null);
        m2.setArbiterPermissionValidThru(m1.getArbiterPermissionValidThru() != null ? m1.getArbiterPermissionValidThru() : m2.getArbiterPermissionValidThru() != null ? m2.getArbiterPermissionValidThru() : null);
        MemberPermissionsEntity save2 = memberPermissionsRepository.save(m2);
        one.setPermissionsEntity(save2);
        otherPersonRepository.save(one);
        return ResponseEntity.ok("Zaktualizowano");
    }

    public ResponseEntity<?> getOtherByPhone(String phone) {
        OtherPersonEntity otherPerson = otherPersonRepository.findAllByPhoneNumberAndActiveTrue(phone.replaceAll(" ", "")).stream().filter(OtherPersonEntity::isActive).findFirst().orElse(null);
        if (otherPerson != null && otherPerson.isActive()) {
            return ResponseEntity.ok(otherPerson);
        } else {
            return ResponseEntity.badRequest().body("brak takiego numeru w bazie");
        }
    }
}
