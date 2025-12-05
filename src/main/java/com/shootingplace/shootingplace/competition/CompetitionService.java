package com.shootingplace.shootingplace.competition;

import com.shootingplace.shootingplace.enums.CompetitionType;
import com.shootingplace.shootingplace.enums.CountingMethod;
import com.shootingplace.shootingplace.enums.Discipline;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.HistoryService;
import com.shootingplace.shootingplace.tournament.CompetitionMembersListRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CompetitionService {

    private final CompetitionRepository competitionRepository;
    private final CompetitionMembersListRepository competitionMembersListRepository;
    private final HistoryService historyService;

    private final Logger LOG = LogManager.getLogger(getClass());


    public CompetitionService(CompetitionRepository competitionRepository, CompetitionMembersListRepository competitionMembersListRepository, HistoryService historyService) {
        this.competitionRepository = competitionRepository;
        this.competitionMembersListRepository = competitionMembersListRepository;
        this.historyService = historyService;
    }

    public List<CompetitionEntity> getAllCompetitions() {
        List<CompetitionEntity> competitionEntityList = competitionRepository.findAll().stream().sorted(Comparator.comparing(CompetitionEntity::getOrdering)).collect(Collectors.toList());

//        if (competitionEntityList.isEmpty()) {
//            createCompetitions();
//            LOG.info("Zostały utworzone domyślne Konkurencje");
//        }
        return competitionEntityList;
    }

//    private void createCompetitions() {
//
//        competitionRepository.save(CompetitionEntity.builder()
//                .uuid(UUID.randomUUID().toString())
//                .name("25m Pistolet sportowy 10 strzałów OPEN")
//                .numberOfShots(10)
//                .type(CompetitionType.OPEN.getName())
////                .discipline(Discipline.PISTOL.getName())
//                .countingMethod(CountingMethod.NORMAL.getName())
//                .ordering(3)
//                .build());
//
//        competitionRepository.save(CompetitionEntity.builder()
//                .uuid(UUID.randomUUID().toString())
//                .name("25m Pistolet centralnego zapłonu 10 strzałów OPEN")
//                .numberOfShots(10)
//                .type(CompetitionType.OPEN.getName())
////                .discipline(Discipline.PISTOL.getName())
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
        List<String> list = competitionRepository.findAll().stream().map(CompetitionEntity::getName).collect(Collectors.toList());
        int size = competitionRepository.findAll().stream().max(Comparator.comparing(CompetitionEntity::getOrdering)).get().getOrdering() + 1;
        LOG.info(competition.getName().replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT));
        if (list.stream().anyMatch(a -> a.trim().toLowerCase(Locale.ROOT).equals(competition.getName().trim().toLowerCase(Locale.ROOT)))) {
            LOG.info("Taka konkurencja już istnieje");
            return ResponseEntity.badRequest().body("Taka konkurencja już istnieje");
        }
        List<String> disciplines = competition.getDisciplineList();
        CompetitionEntity c = CompetitionEntity.builder()
                .name(competition.getName().replaceAll("\\s+", " ").trim())
                .abbreviation(competition.getAbbreviation())
//                .discipline(competition.getDiscipline())
                .ordering(size)
                .type(competition.getType())
                .countingMethod(competition.getCountingMethod())
                .caliberUUID(!competition.getCaliberUUID().isEmpty()?competition.getCaliberUUID():null)
                .numberOfShots(competition.getNumberOfShots())
                .numberOfManyShotsList(null)
                .build();
        c.setDisciplineList(disciplines);
        competitionRepository.save(c);
        return ResponseEntity.status(201).body("utworzono konkurencję " + c.getName());
    }

    public ResponseEntity<?> updateCompetition(String uuid, Competition competition, String pinCode) throws NoUserPermissionException {

        CompetitionEntity competitionEntity = competitionRepository.getOne(uuid);
        competitionEntity.setOrdering(competition.getOrdering() != null ? competition.getOrdering() : competitionEntity.getOrdering());
        competitionEntity.setPracticeShots(competition.getPracticeShots() != null ? competition.getPracticeShots() : competitionEntity.getPracticeShots());
        competitionEntity.setCaliberUUID(competition.getCaliberUUID() != null ? competition.getCaliberUUID() : competitionEntity.getCaliberUUID());
        if (competition.getName() != null && !competition.getName().isEmpty()) {
            if (competitionRepository.existsByName(competition.getName()) && !competitionEntity.getName().equals(competition.getName())) {
                return ResponseEntity.badRequest().body("Taka nazwa już istnieje i nie można zaktualizować konkurencji");
            } else {
                if (!competitionEntity.getName().equals(competition.getName())) {
                    competitionEntity.setName(competition.getName());
                }
            }
        }
        competitionEntity.setNumberOfShots(competition.getNumberOfShots() != null ? competition.getNumberOfShots() : competitionEntity.getNumberOfShots());
        competitionEntity.setCountingMethod(competition.getCountingMethod() != null ? competition.getCountingMethod() : competitionEntity.getCountingMethod());
        competitionEntity.setType(competition.getType() != null ? competition.getType() : competitionEntity.getType());

//        if (competition.getDiscipline() != null) {
//            competition.setDiscipline(competition.getDiscipline().equals("") ? null : competition.getDiscipline());
//            competitionEntity.setDiscipline(competition.getDiscipline());
//        }
        competition.setDisciplineList(competition.getDisciplineList());
        competitionEntity.setDisciplineList(competition.getDisciplineList() != null ? competition.getDisciplineList() : competitionEntity.getDisciplineList());
        competitionRepository.save(competitionEntity);
        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, competitionEntity, HttpStatus.OK, "update Competition", "Zaktualizowano konkurencję");
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            competitionRepository.save(competitionEntity);
            competitionMembersListRepository.findAll()
                    .stream()
                    .filter(f -> f.getCompetitionUUID() != null && f.getCompetitionUUID().equals(competitionEntity.getUuid()))
                    .forEach(e -> {
                        e.setOrdering(competition.getOrdering());
                        e.setPracticeShots(competition.getPracticeShots() != null ? competition.getPracticeShots() : e.getPracticeShots());
                        e.setCaliberUUID(competition.getCaliberUUID() != null ? competition.getCaliberUUID() : e.getCaliberUUID());
                        e.setName(competition.getName() != null ? competition.getName() : e.getName());
                        e.setNumberOfShots(competition.getNumberOfShots() != null ? competition.getNumberOfShots() : e.getNumberOfShots());
                        e.setCountingMethod(competition.getCountingMethod() != null ? competition.getCountingMethod() : e.getCountingMethod());
                        e.setType(competition.getType() != null ? competition.getType() : e.getType());
//                        e.setDiscipline(competition.getDiscipline() != null ? competition.getDiscipline() : e.getDiscipline());
                        e.setDisciplineList(competition.getDisciplineList() != null ? competition.getDisciplineList() : e.getDisciplineList());
                        competitionMembersListRepository.save(e);
                    });
        }
        return response;
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

    public ResponseEntity<?> deleteCompetition(String uuid, String pinCode) throws NoUserPermissionException {
        CompetitionEntity one = competitionRepository.getOne(uuid);
        competitionRepository.delete(one);
        return historyService.getStringResponseEntity(pinCode, one, HttpStatus.OK, "Delete Competition", "Usunięto konkurencję " + one.getName());
    }
}
