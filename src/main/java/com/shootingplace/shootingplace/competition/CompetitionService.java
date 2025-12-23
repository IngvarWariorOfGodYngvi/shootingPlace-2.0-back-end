package com.shootingplace.shootingplace.competition;

import com.shootingplace.shootingplace.enums.CompetitionType;
import com.shootingplace.shootingplace.enums.CountingMethod;
import com.shootingplace.shootingplace.enums.Discipline;
import com.shootingplace.shootingplace.history.HistoryEntityType;
import com.shootingplace.shootingplace.history.RecordHistory;
import com.shootingplace.shootingplace.tournament.CompetitionMembersListRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompetitionService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionMembersListRepository competitionMembersListRepository;

    private final Logger LOG = LogManager.getLogger(getClass());

    public List<CompetitionEntity> getAllCompetitions() {
        return competitionRepository.findAll().stream().sorted(Comparator.comparing(CompetitionEntity::getOrdering)).collect(Collectors.toList());
    }

//    private void createCompetitions() {
//
//        competitionRepository.save(CompetitionEntity.builder()
//                .uuid(UUID.randomUUID().toString())
//                .name("25m Pistolet sportowy 10 strzałów OPEN")
//                .numberOfShots(10)
//                .type(CompetitionType.OPEN.getName())
//                .discipline(Discipline.PISTOL.getName())
//                .countingMethod(CountingMethod.NORMAL.getName())
//                .ordering(3)
//                .build());
//
//        competitionRepository.save(CompetitionEntity.builder()
//                .uuid(UUID.randomUUID().toString())
//                .name("25m Pistolet centralnego zapłonu 10 strzałów OPEN")
//                .numberOfShots(10)
//                .type(CompetitionType.OPEN.getName())

    /// /                .discipline(Discipline.PISTOL.getName())
//                .countingMethod(CountingMethod.NORMAL.getName())
//                .ordering(4)
//                .build());
//        competitionRepository.save(CompetitionEntity.builder()
//                .uuid(UUID.randomUUID().toString())
//                .name("10m Pistolet pneumatyczny 10 strzałów OPEN")
//                .numberOfShots(10)
//                .type(CompetitionType.OPEN.getName())
//                .discipline(Discipline.PISTOL.getName())
//                .countingMethod(CountingMethod.NORMAL.getName())
//                .ordering(2)
//                .build());
//        competitionRepository.save(CompetitionEntity.builder()
//                .uuid(UUID.randomUUID().toString())
//                .name("50m Pistolet dowolny 10 strzałów OPEN")
//                .numberOfShots(10)
//                .type(CompetitionType.OPEN.getName())
//                .discipline(Discipline.PISTOL.getName())
//                .countingMethod(CountingMethod.NORMAL.getName())
//                .ordering(5)
//                .build());
//        competitionRepository.save(CompetitionEntity.builder()
//                .uuid(UUID.randomUUID().toString())
//                .name("10m Karabin pneumatyczny 10 strzałów OPEN")
//                .numberOfShots(10)
//                .type(CompetitionType.OPEN.getName())
//                .discipline(Discipline.RIFLE.getName())
//                .countingMethod(CountingMethod.NORMAL.getName())
//                .ordering(1)
//                .build());
//        LOG.info("Stworzono encje konkurencji");
//    }
    public ResponseEntity<?> createNewCompetition(Competition competition) {
        List<String> list = competitionRepository.findAll().stream().map(CompetitionEntity::getName).toList();
        int size = competitionRepository.findAll().stream().max(Comparator.comparing(CompetitionEntity::getOrdering)).get().getOrdering() + 1;
        LOG.info(competition.getName().replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT));
        if (list.stream().anyMatch(a -> a.trim().toLowerCase(Locale.ROOT).equals(competition.getName().trim().toLowerCase(Locale.ROOT)))) {
            LOG.info("Taka konkurencja już istnieje");
            return ResponseEntity.badRequest().body("Taka konkurencja już istnieje");
        }
        List<String> disciplines = competition.getDisciplineList();
        CompetitionEntity c = CompetitionEntity.builder().name(competition.getName().replaceAll("\\s+", " ").trim()).abbreviation(competition.getAbbreviation())
                .ordering(size).type(competition.getType()).countingMethod(competition.getCountingMethod()).caliberUUID(!competition.getCaliberUUID().isEmpty() ? competition.getCaliberUUID() : null).numberOfShots(competition.getNumberOfShots()).numberOfManyShotsList(null).build();
        c.setDisciplineList(disciplines);
        competitionRepository.save(c);
        return ResponseEntity.status(201).body("utworzono konkurencję " + c.getName());
    }

    @RecordHistory(action = "Competition.update", entity = HistoryEntityType.COMPETITION, entityArgIndex = 0)
    public ResponseEntity<?> updateCompetition(String uuid, Competition competition) {

        CompetitionEntity entity = competitionRepository.findById(uuid).orElseThrow(EntityNotFoundException::new);

        if (competition.getName() != null && !competition.getName().isBlank()) {
            if (competitionRepository.existsByName(competition.getName()) && !entity.getName().equals(competition.getName())) {
                return ResponseEntity.badRequest().body("Taka nazwa już istnieje i nie można zaktualizować konkurencji");
            }
            entity.setName(competition.getName());
        }

        if (competition.getOrdering() != null) {
            entity.setOrdering(competition.getOrdering());
        }
        if (competition.getPracticeShots() != null) {
            entity.setPracticeShots(competition.getPracticeShots());
        }
        if (competition.getCaliberUUID() != null) {
            entity.setCaliberUUID(competition.getCaliberUUID());
        }
        if (competition.getNumberOfShots() != null) {
            entity.setNumberOfShots(competition.getNumberOfShots());
        }
        if (competition.getCountingMethod() != null) {
            entity.setCountingMethod(competition.getCountingMethod());
        }
        if (competition.getType() != null) {
            entity.setType(competition.getType());
        }
        if (competition.getDisciplineList() != null) {
            entity.setDisciplineList(competition.getDisciplineList());
        }

        competitionRepository.save(entity);

        competitionMembersListRepository.findAll().stream().filter(e -> uuid.equals(e.getCompetitionUUID())).forEach(e -> {
            if (competition.getOrdering() != null) {
                e.setOrdering(competition.getOrdering());
            }
            if (competition.getPracticeShots() != null) {
                e.setPracticeShots(competition.getPracticeShots());
            }
            if (competition.getCaliberUUID() != null) {
                e.setCaliberUUID(competition.getCaliberUUID());
            }
            if (competition.getName() != null) {
                e.setName(competition.getName());
            }
            if (competition.getNumberOfShots() != null) {
                e.setNumberOfShots(competition.getNumberOfShots());
            }
            if (competition.getCountingMethod() != null) {
                e.setCountingMethod(competition.getCountingMethod());
            }
            if (competition.getType() != null) {
                e.setType(competition.getType());
            }
            if (competition.getDisciplineList() != null) {
                e.setDisciplineList(competition.getDisciplineList());
            }
            competitionMembersListRepository.save(e);
        });

        LOG.info("Zaktualizowano konkurencję: {}", entity.getName());
        return ResponseEntity.ok("Zaktualizowano konkurencję");
    }


    public ResponseEntity<?> getCompetitionMemberList(String competitionMembersListUUID) {
        if (competitionMembersListRepository.existsById(competitionMembersListUUID)) {
            return ResponseEntity.ok(competitionMembersListRepository.findById(competitionMembersListUUID));

        } else {
            return ResponseEntity.badRequest().body("brak takiej konkurencji");
        }
    }

    public List<String> getCountingMethods() {
        return Arrays.stream(CountingMethod.values()).map(CountingMethod::getName).collect(Collectors.toList());
    }

    public List<String> getDisciplines() {
        return Arrays.stream(Discipline.values()).map(Discipline::getName).collect(Collectors.toList());
    }

    public List<String> getCompetitionTypes() {
        return Arrays.stream(CompetitionType.values()).map(CompetitionType::getName).collect(Collectors.toList());
    }

    @RecordHistory(
            action = "Competiton.Felete",
            entity = HistoryEntityType.COMPETITION,
            entityArgIndex = 0
    )
    public ResponseEntity<?> deleteCompetition(String uuid) {

        CompetitionEntity competition = competitionRepository.findById(uuid)
                .orElseThrow(EntityNotFoundException::new);

        competitionRepository.delete(competition);

        LOG.info("Usunięto konkurencję: {}", competition.getName());
        return ResponseEntity.ok("Usunięto konkurencję " + competition.getName());
    }

}
