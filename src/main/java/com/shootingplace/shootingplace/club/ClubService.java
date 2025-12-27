package com.shootingplace.shootingplace.club;

import com.shootingplace.shootingplace.history.HistoryEntityType;
import com.shootingplace.shootingplace.history.changeHistory.RecordHistory;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final MemberRepository memberRepository;
    private final OtherPersonRepository otherPersonRepository;
    private final Logger LOG = LogManager.getLogger();

    public List<ClubEntity> getAllClubs() {
        return clubRepository.findAll().stream().filter(f -> !f.getId().equals(2)).collect(Collectors.toList());
    }

    public List<String> getAllClubsToTournament() {
        List<String> list = new ArrayList<>();
        clubRepository.findAll().stream().filter(f -> f.getId() != 1).forEach(e -> list.add(e.getShortName()));
        list.sort(String::compareTo);
        return list;
    }

    public ResponseEntity<String> updateClub(int id, Club club) {
        if (!clubRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Nie znaleziono Klubu");
        }
        if (id == 2) {
            LOG.info("Forbidden");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (clubRepository.existsByShortName(club.getShortName()) && !clubRepository.findByShortName(club.getShortName()).getId().equals(id)) {
            return ResponseEntity.badRequest().body("Taki Klub już istnieje");
        }

        ClubEntity clubEntity = clubRepository.findById(1).orElseThrow(EntityNotFoundException::new);
        if (club.getShortName() != null && !club.getShortName().isEmpty()) {
            clubEntity.setShortName(club.getShortName());
        }
        if (club.getFullName() != null && !club.getFullName().isEmpty()) {
            clubEntity.setFullName(club.getFullName().toUpperCase());
        }
        if (club.getEmail() != null && !club.getEmail().isEmpty()) {
            clubEntity.setEmail(club.getEmail());
        }
        if (club.getPhoneNumber() != null && !club.getPhoneNumber().isEmpty()) {
            clubEntity.setPhoneNumber(club.getPhoneNumber());
        }
        if (club.getUrl() != null && !club.getUrl().isEmpty()) {
            clubEntity.setUrl(club.getUrl());
        }
        if (club.getVovoidership() != null && !club.getVovoidership().isEmpty()) {
            clubEntity.setVovoidership(club.getVovoidership());
        }
        if (club.getWzss() != null && !club.getWzss().isEmpty()) {
            clubEntity.setWzss(club.getWzss());
        }
        if (club.getHouseNumber() != null && !club.getHouseNumber().isEmpty()) {
            clubEntity.setHouseNumber(club.getHouseNumber());
        }
        if (club.getAppartmentNumber() != null && !club.getAppartmentNumber().isEmpty()) {
            clubEntity.setAppartmentNumber(club.getAppartmentNumber());
        }
        if (club.getStreet() != null && !club.getStreet().isEmpty()) {
            clubEntity.setStreet(club.getStreet());
        }
        if (club.getCity() != null && !club.getCity().isEmpty()) {
            clubEntity.setCity(club.getCity());
        }
        if (club.getLicenseNumber() != null && !club.getLicenseNumber().isEmpty()) {
            clubEntity.setLicenseNumber(club.getLicenseNumber());
        }
        clubRepository.save(clubEntity);
        return ResponseEntity.ok("Edytowano Klub");
    }

    public ResponseEntity<?> createMotherClub(Club club) {
        ResponseEntity<?> response;
        if (clubRepository.findById(1).isPresent()) {
            response = ResponseEntity.badRequest().body("Istnieje już Klub Macierzysty - nie można dodać kolejnego Klubu Macierzystego");
        } else {
            club.setId(1);
            ClubEntity clubEntity = buildCLub(club);
            clubRepository.save(clubEntity);
            response = ResponseEntity.ok("Utworzono Klub Macierzysty - dalej pójdzie z górki");
        }
        return response;
    }

    public ResponseEntity<?> createNewClub(Club club) {

        Integer id = clubRepository.findAll().stream().max(Comparator.comparing(ClubEntity::getId)).orElseThrow(EntityNotFoundException::new).getId() + 1;
        club.setId(id);
        ClubEntity clubEntity = buildCLub(club);
        clubRepository.save(clubEntity);
        return ResponseEntity.ok("Utworzono nowy Klub");
    }

    public List<ClubEntity> getAllClubsToMember() {
        List<ClubEntity> list = new ArrayList<>();
        list.add(clubRepository.findById(1).orElseThrow(EntityNotFoundException::new));
        List<ClubEntity> collect = clubRepository.findAll().stream().filter(f -> f.getId() != 1).sorted(Comparator.comparing(ClubEntity::getShortName)).toList();
        list.addAll(collect);
        return list;
    }

    public boolean isMotherClubExists() {
        return clubRepository.findById(1).orElseThrow(EntityNotFoundException::new).getShortName().equals("firstStart");
    }

    public ResponseEntity<?> importCLub(Club club) {
        if (clubRepository.findById(1).orElseThrow(EntityNotFoundException::new).getShortName().equals("firstStart")) {
            club.setId(1);
            ClubEntity clubEntity = buildCLub(club);
            clubRepository.save(clubEntity);
            LOG.info("dodano Klub :{}", club.getShortName());
            return ResponseEntity.ok("importowano Klub: " + club.getShortName());

        }
        boolean b = clubRepository.findAll().stream().anyMatch(a -> a.getShortName().replaceAll(" ", "").equalsIgnoreCase(club.getShortName().replaceAll(" ", "")));
        if (!b) {
            ClubEntity clubEntity = buildCLub(club);
            clubRepository.save(clubEntity);
            LOG.info("dodano Klub :{}", club.getShortName());
            return ResponseEntity.ok("importowano Klub: " + club.getShortName());
        }
        return ResponseEntity.ok("Klub " + club.getShortName() + " już istnieje w bazie");


    }

    private ClubEntity buildCLub(Club club) {
        return ClubEntity.builder().city(club.getCity()).wzss(club.getWzss()).vovoidership(club.getVovoidership()).url(club.getUrl()).phoneNumber(club.getPhoneNumber()).appartmentNumber(club.getAppartmentNumber()).street(club.getStreet()).houseNumber(club.getHouseNumber()).licenseNumber(club.getLicenseNumber()).email(club.getEmail()).fullName(club.getFullName()).shortName(club.getShortName()).id(club.getId()).build();
    }

    @RecordHistory(action = "Club.delete", entity = HistoryEntityType.CLUB)
    public ResponseEntity<?> deleteClub(Integer id) {
        if (id == 1 || id == 2) {
            return ResponseEntity.badRequest().body("Nie można usunąć tego Klubu");
        }
        ClubEntity clubToDelete = clubRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        ClubEntity fallbackClub = clubRepository.findById(2).orElseThrow(EntityNotFoundException::new);
        memberRepository.findAll().stream().filter(m -> m.getClub().getId().equals(id)).forEach(m -> {
            m.setClub(fallbackClub);
            memberRepository.save(m);
        });

        otherPersonRepository.findAll().stream().filter(p -> p.getClub().getId().equals(id)).forEach(p -> {
            p.setClub(fallbackClub);
            otherPersonRepository.save(p);
        });

        clubRepository.delete(clubToDelete);

        LOG.info("Usunięto klub: {}", clubToDelete.getShortName());
        return ResponseEntity.ok("Usunięto klub " + clubToDelete.getShortName());
    }

}
