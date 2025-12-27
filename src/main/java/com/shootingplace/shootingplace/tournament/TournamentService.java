package com.shootingplace.shootingplace.tournament;

import com.shootingplace.shootingplace.armory.CaliberRepository;
import com.shootingplace.shootingplace.competition.CompetitionEntity;
import com.shootingplace.shootingplace.competition.CompetitionRepository;
import com.shootingplace.shootingplace.enums.ArbiterWorkClass;
import com.shootingplace.shootingplace.history.*;
import com.shootingplace.shootingplace.history.changeHistory.RecordHistory;
import com.shootingplace.shootingplace.member.MemberDTO;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
import com.shootingplace.shootingplace.score.ScoreDTO;
import com.shootingplace.shootingplace.score.ScoreEntity;
import com.shootingplace.shootingplace.utils.Mapping;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final MemberRepository memberRepository;
    private final OtherPersonRepository otherPersonRepository;
    private final CompetitionMembersListRepository competitionMembersListRepository;
    private final CompetitionRepository competitionRepository;
    private final CaliberRepository caliberRepository;
    private final HistoryService historyService;
    private final UsedHistoryRepository usedHistoryRepository;
    private final JudgingHistoryRepository judgingHistoryRepository;
    private final Logger LOG = LogManager.getLogger(getClass());


    public ResponseEntity<String> createNewTournament(Tournament tournament) {
        if (tournamentRepository.findAll().stream().anyMatch(TournamentEntity::isOpen)) {
            return ResponseEntity.badRequest().body("Nie można otworzyć kolejnych zawodów bo inne są otwarte");
        }
        TournamentEntity tournamentEntity = TournamentEntity.builder()
                .name(tournament.getName())
                .date(tournament.getDate())
                .open(tournament.isOpen())
                .WZSS(tournament.isWzss())
                .build();

        if (tournament.getDate() == null) {
            tournamentEntity.setDate(LocalDate.now());
        }


        tournamentRepository.save(tournamentEntity);
        LOG.info("Stworzono nowe zawody {}", tournamentEntity.getName());

        return ResponseEntity.status(201).body("Otworzono nowe zawody: " + tournamentEntity.getName());
    }

    public ResponseEntity<?> updateTournament(String tournamentUUID, Tournament tournament) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID)
                .orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {
            if (tournament.getName() != null) {
                if (!tournament.getName().isEmpty()) {
                    String[] s1 = tournament.getName().split(" ");
                    StringBuilder name = new StringBuilder();
                    for (String value : s1) {
                        String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
                        name.append(splinted);
                    }
                    tournamentEntity.setName(name.toString());
                    LOG.info("Zmieniono nazwę zawodów");
                }
            }

            if (tournament.getDate() != null) {
                tournamentEntity.setDate(tournament.getDate());
                LOG.info("Zmieniono datę zawodów");
            }
            tournamentRepository.save(tournamentEntity);
            historyService.updateTournamentEntityInCompetitionHistory(tournamentUUID);
            historyService.updateTournamentInJudgingHistory(tournamentUUID);
            return ResponseEntity.ok("Zaktualizowano Zawody");
        }

        LOG.warn("Zawody są już zamknięte i nie można już nic zrobić");
        return ResponseEntity.badRequest().body("Zawody są już zamknięte i nie można już nic zrobić");
    }


    public ResponseEntity<?> getOpenTournament() {

        List<TournamentEntity> collect = tournamentRepository.findAll().stream().filter(TournamentEntity::isOpen).toList();
        if (collect.size() > 1) {
            return ResponseEntity.status(409).body("Pojawił się jakiś konflikt i nie można wyświetlić zawodów");
        }
        if (collect.isEmpty()) {
            return ResponseEntity.status(418).body("Nie ma nic do wyświetlenia");
        }
        TournamentEntity tournamentEntity = collect.getFirst();
        MemberDTO mainArbiterDTO;
        MemberDTO commissionRTSArbiter;
        if (tournamentEntity.getMainArbiter() != null) {
            mainArbiterDTO = Mapping.map2DTO(tournamentEntity.getMainArbiter());
        } else {
            mainArbiterDTO = null;
        }
        if (tournamentEntity.getCommissionRTSArbiter() != null) {
            commissionRTSArbiter = Mapping.map2DTO(tournamentEntity.getCommissionRTSArbiter());
        } else {
            commissionRTSArbiter = null;
        }
// DO NOT TOUCH THIS PART
        tournamentEntity.getCompetitionsList()
                .forEach(e ->
                {
                    if (e.getOrdering() == null) {
                        CompetitionEntity competitionEntity = competitionRepository.findById(e.getCompetitionUUID()).orElseThrow(EntityNotFoundException::new);
                        e.setOrdering(competitionEntity.getOrdering());
                        competitionMembersListRepository.save(e);
                    }
                });

        List<CompetitionMembersList> collect1 = tournamentEntity.getCompetitionsList()
                .stream()
                .map(Mapping::map1)
                .sorted(Comparator.comparing(CompetitionMembersList::getOrdering))
                .collect(Collectors.toList());

        Tournament tournament = Tournament.builder()
                .uuid(tournamentEntity.getUuid())
                .date(tournamentEntity.getDate())
                .name(tournamentEntity.getName())
                .open(tournamentEntity.isOpen())
                .wzss(tournamentEntity.isWZSS())
                .mainArbiter(mainArbiterDTO)
                .commissionRTSArbiter(commissionRTSArbiter)
                .otherMainArbiter(tournamentEntity.getOtherMainArbiter())
                .otherCommissionRTSArbiter(tournamentEntity.getOtherCommissionRTSArbiter())
                .arbitersList(tournamentEntity.getArbitersList().stream().map(Mapping::map2DTO).collect(Collectors.toList()))
                .otherArbitersList(tournamentEntity.getOtherArbitersList())
                .arbitersRTSList(tournamentEntity.getArbitersRTSList().stream().map(Mapping::map2DTO).collect(Collectors.toList()))
                .otherArbitersRTSList(tournamentEntity.getOtherArbitersRTSList())
                .competitionsList(collect1)
                .build();

        return ResponseEntity.ok(tournament);
    }

    public ResponseEntity<?> closeTournament(String tournamentUUID) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {
            LOG.info("Zawody {} zostały zamknięte", tournamentEntity.getName());
            tournamentEntity.setOpen(false);
            tournamentRepository.save(tournamentEntity);
            return ResponseEntity.ok("Zawody zostały zamknięte");
        } else {
            return ResponseEntity.badRequest().body("Nie można zamknąć zawodów");
        }
    }

    public ResponseEntity<?> removeArbiterFromTournament(String tournamentUUID, String memberUUID) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {

            MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);

            List<MemberEntity> list = tournamentEntity.getArbitersList();
            if (!list.contains(memberEntity)) {
                return ResponseEntity.badRequest().body("Brak sędziego na Liście sędziów");
            }
            historyService.removeJudgingRecord(memberEntity.getUuid(), tournamentUUID);
            list.remove(memberEntity);

            tournamentRepository.save(tournamentEntity);

            return ResponseEntity.ok("Usunięto Sędziego");
        }
        return ResponseEntity.status(418).body("I'm a teapot");
    }

    public ResponseEntity<String> removeOtherArbiterFromTournament(String tournamentUUID, int id) {

        if (!tournamentRepository.existsById(tournamentUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono zawodów z tym identyfikatorem");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);

        if (tournamentEntity.isOpen()) {

            OtherPersonEntity otherPersonEntity = otherPersonRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getId()
                            .equals(id))
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            List<OtherPersonEntity> list = tournamentEntity.getOtherArbitersList();
            if (!list.contains(otherPersonEntity)) {
                return ResponseEntity.badRequest().body("Brak sędziego na Liście sędziów");
            }
            list.remove(otherPersonEntity);

            tournamentRepository.save(tournamentEntity);

            return ResponseEntity.ok("Usunięto Sędziego");
        }
        return ResponseEntity.status(418).body("I'm a teapot");
    }

    public ResponseEntity<String> addMainArbiter(String tournamentUUID, String memberUUID) {
        if (!tournamentRepository.existsById(tournamentUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono zawodów z tym identyfikatorem");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {

            if (tournamentEntity.getOtherMainArbiter() != null) {
                tournamentEntity.setOtherMainArbiter(null);
            }
            String function = ArbiterWorkClass.MAIN_ARBITER.getName();
            MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
            if (tournamentEntity.getCommissionRTSArbiter() != null) {
                if (tournamentEntity.getCommissionRTSArbiter().equals(memberEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (tournamentEntity.getArbitersList() != null) {
                if (tournamentEntity.getArbitersList().stream().anyMatch(a -> a.equals(memberEntity))) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }

            if (!memberEntity.getMemberPermissions().getArbiterStaticNumber().isEmpty()) {
                if (tournamentEntity.getMainArbiter() == null || tournamentEntity.getMainArbiter() != memberEntity) {
                    if (tournamentEntity.getMainArbiter() == null) {
                        tournamentEntity.setMainArbiter(memberEntity);

                    } else {
                        historyService.removeJudgingRecord(tournamentEntity.getMainArbiter().getUuid(), tournamentUUID);
                        tournamentEntity.setMainArbiter(memberEntity);
                        tournamentEntity.setOtherMainArbiter(null);

                    }
                    tournamentRepository.save(tournamentEntity);
                    LOG.info("Ustawiono sędziego głównego zawodów");
                    historyService.addJudgingRecord(memberEntity.getUuid(), tournamentUUID, function);
                    return ResponseEntity.ok("Ustawiono sędziego głównego zawodów");
                } else {
                    return ResponseEntity.badRequest().body("Nie udało się przypisać sędziego");
                }
            }
        }
        return ResponseEntity.badRequest().

                body("Nie udało się przypisać sędziego");

    }

    public ResponseEntity<String> addOtherMainArbiter(String tournamentUUID, int id) {
        if (!tournamentRepository.existsById(tournamentUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono zawodów z tym identyfikatorem");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {

            OtherPersonEntity otherPersonEntity = otherPersonRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getId()
                            .equals(id))
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            if (tournamentEntity.getOtherCommissionRTSArbiter() != null) {
                if (tournamentEntity.getOtherCommissionRTSArbiter().equals(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (tournamentEntity.getOtherArbitersList() != null) {
                if (tournamentEntity.getOtherArbitersList().stream().anyMatch(a -> a.equals(otherPersonEntity))) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }

            if (!otherPersonEntity.getPermissionsEntity().getArbiterStaticNumber().isEmpty()) {
                if (tournamentEntity.getOtherMainArbiter() == null || tournamentEntity.getOtherMainArbiter() != otherPersonEntity) {
                    if (tournamentEntity.getOtherMainArbiter() == null) {
                        if (tournamentEntity.getMainArbiter() != null) {

                            historyService.removeJudgingRecord(tournamentEntity.getMainArbiter().getUuid(), tournamentUUID);
                        }
                        tournamentEntity.setOtherMainArbiter(otherPersonEntity);
                        tournamentEntity.setMainArbiter(null);

                    } else {
                        historyService.removeJudgingRecord(tournamentEntity.getMainArbiter().getUuid(), tournamentUUID);
                        tournamentEntity.setMainArbiter(null);
                        tournamentEntity.setOtherMainArbiter(otherPersonEntity);
                    }
                    tournamentRepository.save(tournamentEntity);
                    LOG.info("Ustawiono sędziego głównego zawodów");
                    return ResponseEntity.ok("Ustawiono sędziego głównego zawodów");
                } else {
                    return ResponseEntity.badRequest().body("Nie udało się przypisać sędziego");
                }
            }
        }
        return ResponseEntity.badRequest().body("Nie udało się przypisać sędziego");
    }

    public ResponseEntity<?> addRTSArbiter(String tournamentUUID, String memberUUID) {
        if (!tournamentRepository.existsById(tournamentUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono zawodów z tym identyfikatorem");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {

            if (tournamentEntity.getOtherCommissionRTSArbiter() != null) {
                tournamentEntity.setOtherCommissionRTSArbiter(null);
            }
            String function = ArbiterWorkClass.RTS_ARBITER.getName();
            MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);

            if (tournamentEntity.getMainArbiter() != null) {
                if (tournamentEntity.getMainArbiter().equals(memberEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (tournamentEntity.getArbitersList() != null) {
                if (tournamentEntity.getArbitersList().stream().anyMatch(a -> a.equals(memberEntity))) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }

            if (!memberEntity.getMemberPermissions().getArbiterStaticNumber().isEmpty()) {
                if (tournamentEntity.getCommissionRTSArbiter() == null || tournamentEntity.getCommissionRTSArbiter() != memberEntity) {
                    if (tournamentEntity.getCommissionRTSArbiter() != null) {
                        historyService.removeJudgingRecord(tournamentEntity.getCommissionRTSArbiter().getUuid(), tournamentUUID);
                    }
                    tournamentEntity.setCommissionRTSArbiter(memberEntity);
                    tournamentRepository.save(tournamentEntity);
                    LOG.info("Ustawiono sędziego RTS");
                    historyService.addJudgingRecord(memberEntity.getUuid(), tournamentUUID, function);
                    return ResponseEntity.ok("Ustawiono sędziego RTS");
                } else {
                    return ResponseEntity.badRequest().body("Nie udało się przypisać sędziego");
                }
            }
        }
        return ResponseEntity.badRequest().body("Nie udało się przypisać sędziego");
    }

    public ResponseEntity<?> addOtherRTSArbiter(String tournamentUUID, int id) {
        if (!tournamentRepository.existsById(tournamentUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono zawodów z tym identyfikatorem");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {

            OtherPersonEntity otherPersonEntity = otherPersonRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getId()
                            .equals(id))
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            if (tournamentEntity.getOtherMainArbiter() != null) {
                if (tournamentEntity.getOtherMainArbiter().equals(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (tournamentEntity.getOtherArbitersList() != null) {
                if (tournamentEntity.getOtherArbitersList().stream().anyMatch(a -> a.equals(otherPersonEntity))) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }

            if (!otherPersonEntity.getPermissionsEntity().getArbiterStaticNumber().isEmpty()) {
                if (tournamentEntity.getOtherCommissionRTSArbiter() == null || tournamentEntity.getOtherCommissionRTSArbiter() != otherPersonEntity) {
                    if (tournamentEntity.getOtherCommissionRTSArbiter() == null) {
                        if (tournamentEntity.getCommissionRTSArbiter() != null) {
                            historyService.removeJudgingRecord(tournamentEntity.getCommissionRTSArbiter().getUuid(), tournamentUUID);
                        }

                    } else {
                        historyService.removeJudgingRecord(tournamentEntity.getCommissionRTSArbiter().getUuid(), tournamentUUID);
                    }
                    tournamentEntity.setOtherCommissionRTSArbiter(otherPersonEntity);
                    tournamentEntity.setCommissionRTSArbiter(null);
                    tournamentRepository.save(tournamentEntity);
                    LOG.info("Ustawiono sędziego biura obliczeń");
                    return ResponseEntity.ok("Ustawiono sędziego RTS");
                } else {
                    return ResponseEntity.badRequest().body("Nie udało się przypisać sędziego");
                }
            }
        }
        return ResponseEntity.badRequest().body("Nie udało się przypisać sędziego");
    }

    public ResponseEntity<?> addOthersArbiters(String tournamentUUID, String memberUUID) {
        if (!tournamentRepository.existsById(tournamentUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono zawodów z tym identyfikatorem");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {
            String function = ArbiterWorkClass.HELP.getName();
            MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);

            if (tournamentEntity.getMainArbiter() != null) {
                if (tournamentEntity.getMainArbiter().equals(memberEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (tournamentEntity.getCommissionRTSArbiter() != null) {
                if (tournamentEntity.getCommissionRTSArbiter().equals(memberEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (tournamentEntity.getArbitersList() != null) {
                if (tournamentEntity.getArbitersList().contains(memberEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (tournamentEntity.getArbitersRTSList() != null) {
                if (tournamentEntity.getArbitersRTSList().contains(memberEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (!memberEntity.getMemberPermissions().getArbiterStaticNumber().isEmpty()) {
                List<MemberEntity> list = tournamentEntity.getArbitersList();
                list.add(memberEntity);
                list.sort(Comparator.comparing(MemberEntity::getSecondName));

                tournamentEntity.setArbitersList(list);
                tournamentRepository.save(tournamentEntity);
                LOG.info("Dodano sędziego pomocniczego");
                historyService.addJudgingRecord(memberEntity.getUuid(), tournamentUUID, function);
                return ResponseEntity.ok("Ustawiono sędziego pomocniczego");
            }
        }
        return ResponseEntity.badRequest().body("Nie udało się przypisać sędziego");
    }

    public ResponseEntity<?> addPersonOthersArbiters(String tournamentUUID, int id) {
        if (!tournamentRepository.existsById(tournamentUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono zawodów z tym identyfikatorem");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {

            OtherPersonEntity otherPersonEntity = otherPersonRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getId()
                            .equals(id))
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            if (tournamentEntity.getOtherMainArbiter() != null) {
                if (tournamentEntity.getOtherMainArbiter().equals(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (tournamentEntity.getOtherCommissionRTSArbiter() != null) {
                if (tournamentEntity.getOtherCommissionRTSArbiter().equals(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (tournamentEntity.getOtherArbitersList() != null) {
                if (tournamentEntity.getOtherArbitersList().contains(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (tournamentEntity.getOtherArbitersRTSList() != null) {
                if (tournamentEntity.getOtherArbitersRTSList().contains(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (!otherPersonEntity.getPermissionsEntity().getArbiterStaticNumber().isEmpty()) {
                List<OtherPersonEntity> list = tournamentEntity.getOtherArbitersList();
                list.add(otherPersonEntity);
                list.sort(Comparator.comparing(OtherPersonEntity::getSecondName));

                tournamentEntity.setOtherArbitersList(list);
                tournamentRepository.save(tournamentEntity);
                LOG.info("Dodano sędziego pomocniczego");
                return ResponseEntity.ok("Ustawiono sędziego pomocniczego");
            }
        }
        return ResponseEntity.badRequest().body("Nie udało się przypisać sędziego");
    }

    public String addNewCompetitionListToTournament(String tournamentUUID, String competitionUUID) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);

        if (!tournamentEntity.isOpen() || competitionUUID == null) {
            return "Nie udało się dodać konkurencji";
        }

        CompetitionEntity competition = competitionRepository.findById(competitionUUID).orElseThrow(EntityNotFoundException::new);
        if (!tournamentEntity.getCompetitionsList().isEmpty() && tournamentEntity.getCompetitionsList().stream().anyMatch(e -> e.getName().equals(competition.getName()))) {
            LOG.info("konkurencja {} jest już dodana", competition.getName());
            return "konkurencja " + competition.getName() + " jest już dodana";
        }

        CompetitionMembersListEntity competitionMembersList = CompetitionMembersListEntity.builder()
                .name(competition.getName())
                .attachedToTournament(tournamentEntity.getUuid())
                .date(tournamentEntity.getDate())
                .countingMethod(competition.getCountingMethod())
                .competitionUUID(competition.getUuid())
                .type(competition.getType())
                .numberOfShots(competition.getNumberOfShots())
                .WZSS(tournamentEntity.isWZSS())
                .ordering(competition.getOrdering())
                .caliberUUID(competition.getCaliberUUID())
                .practiceShots(competition.getPracticeShots())
                .build();
        competitionMembersList.setNumberOfManyShotsList(competition.getNumberOfManyShotsList());
        competitionMembersList.setDisciplineList(competition.getDisciplineList());
        competitionMembersListRepository.save(competitionMembersList);
        List<CompetitionMembersListEntity> competitionsList = tournamentEntity.getCompetitionsList();
        competitionsList.add(competitionMembersList);
        competitionsList.sort(Comparator.comparing(CompetitionMembersListEntity::getOrdering));
        tournamentRepository.save(tournamentEntity);
        LOG.info("Dodano konkurencję {} do zawodów", competition.getName());
        return "Dodano konkurencję " + competition.getName() + " do zawodów";


    }

    public List<TournamentDTO> getClosedTournaments(Pageable page) {
        page = PageRequest.of(page.getPageNumber(), page.getPageSize(), Sort.by("date").descending());
        return tournamentRepository.findAllByOpenIsFalse(page)
                .stream()
                .map(Mapping::map1)
                .sorted(Comparator.comparing(TournamentDTO::getDate).reversed())
                .collect(Collectors.toList());
    }
    @Transactional
    @RecordHistory(action = "Tournament.delete", entity = HistoryEntityType.TOURNAMENT, entityArgIndex = 0)
    public ResponseEntity<?> deleteTournament(String tournamentUUID) {
        if (!tournamentRepository.existsById(tournamentUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono zawodów z tym identyfikatorem");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (!tournamentEntity.isOpen()) {
            return ResponseEntity.badRequest().body("Nie udało się usunąć zawodów");
        }
        if (!tournamentEntity.getCompetitionsList().isEmpty()) {
            tournamentEntity.getCompetitionsList()
                    .forEach(e -> e.getScoreList()
                            .stream().filter(f -> f.getMember() != null)
                            .forEach(a -> historyService.removeCompetitionRecord(a.getMember().getUuid(), competitionMembersListRepository.findById(a.getCompetitionMembersListEntityUUID()).orElseThrow(EntityNotFoundException::new))));
        }
        if (tournamentEntity.getMainArbiter() != null) {
            historyService.removeJudgingRecord(tournamentEntity.getMainArbiter().getUuid(), tournamentEntity.getUuid());
        }
        if (tournamentEntity.getCommissionRTSArbiter() != null) {
            historyService.removeJudgingRecord(tournamentEntity.getCommissionRTSArbiter().getUuid(), tournamentEntity.getUuid());
        }
        if (!tournamentEntity.getArbitersList().isEmpty()) {
            tournamentEntity.getArbitersList().forEach(e -> historyService.removeJudgingRecord(e.getUuid(), tournamentEntity.getUuid()));
        }
         tournamentRepository.delete(tournamentEntity);

        return ResponseEntity.ok().build();

    }

    public List<CompetitionMembersListEntity> getCompetitionsListInTournament(String tournamentUUID) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);

        List<CompetitionMembersListEntity> competitionsList = tournamentEntity.getCompetitionsList();
        competitionsList.sort(Comparator.comparing(CompetitionMembersListEntity::getOrdering));
        competitionsList.forEach(e ->
        {
            if (e.getOrdering() == null) {
                CompetitionEntity competitionEntity = competitionRepository.findAll().stream().filter(f -> f.getName().equals(e.getName())).findFirst().orElseThrow(EntityNotFoundException::new);
                e.setOrdering(competitionEntity.getOrdering());
                competitionMembersListRepository.save(e);
            }
        });
        return competitionsList.stream().map(m -> CompetitionMembersListEntity.builder()
                .uuid(m.getUuid())
                .name(m.getName())
                .numberOfShots(m.getNumberOfShots())
                .practiceShots(m.getPracticeShots())
                .caliberUUID(m.getCaliberUUID() != null ? caliberRepository.findById(m.getCaliberUUID()).orElseThrow(EntityNotFoundException::new).getUuid() : null)
                .build()).collect(Collectors.toList());
    }

    public ResponseEntity<?> addOthersRTSArbiters(String tournamentUUID, String memberUUID) {
        if (!tournamentRepository.existsById(tournamentUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono zawodów z tym identyfikatorem");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {
            String function = ArbiterWorkClass.RTS_HELP.getName();
            MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);

            if (tournamentEntity.getMainArbiter() != null) {
                if (tournamentEntity.getMainArbiter().equals(memberEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (tournamentEntity.getCommissionRTSArbiter() != null) {
                if (tournamentEntity.getCommissionRTSArbiter().equals(memberEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (tournamentEntity.getArbitersList() != null) {
                if (tournamentEntity.getArbitersList().contains(memberEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (tournamentEntity.getArbitersRTSList() != null) {
                if (tournamentEntity.getArbitersRTSList().contains(memberEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (!memberEntity.getMemberPermissions().getArbiterStaticNumber().isEmpty()) {
                List<MemberEntity> list = tournamentEntity.getArbitersRTSList();
                list.add(memberEntity);
                list.sort(Comparator.comparing(MemberEntity::getSecondName));

                tournamentEntity.setArbitersRTSList(list);
                tournamentRepository.save(tournamentEntity);
                LOG.info("Dodano sędziego biura obliczeń");
                historyService.addJudgingRecord(memberEntity.getUuid(), tournamentUUID, function);
                return ResponseEntity.ok("Ustawiono sędziego biura obliczeń");
            }
        }
        return ResponseEntity.badRequest().body("Nie udało się przypisać sędziego");
    }

    public ResponseEntity<?> addPersonOthersRTSArbiters(String tournamentUUID, int id) {
        if (!tournamentRepository.existsById(tournamentUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono zawodów z tym identyfikatorem");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {

            OtherPersonEntity otherPersonEntity = otherPersonRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getId()
                            .equals(id))
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            if (tournamentEntity.getOtherMainArbiter() != null) {
                if (tournamentEntity.getOtherMainArbiter().equals(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (tournamentEntity.getOtherCommissionRTSArbiter() != null) {
                if (tournamentEntity.getOtherCommissionRTSArbiter().equals(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (tournamentEntity.getOtherArbitersList() != null) {
                if (tournamentEntity.getOtherArbitersList().contains(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (tournamentEntity.getOtherArbitersRTSList() != null) {
                if (tournamentEntity.getOtherArbitersRTSList().contains(otherPersonEntity)) {
                    return ResponseEntity.badRequest().body("Sędzia już jest przypisany");
                }
            }
            if (!otherPersonEntity.getPermissionsEntity().getArbiterStaticNumber().isEmpty()) {
                List<OtherPersonEntity> list = tournamentEntity.getOtherArbitersRTSList();
                list.add(otherPersonEntity);
                list.sort(Comparator.comparing(OtherPersonEntity::getSecondName));

                tournamentEntity.setOtherArbitersRTSList(list);
                tournamentRepository.save(tournamentEntity);
                LOG.info("Dodano sędziego biura obliczeń");
                return ResponseEntity.ok("Ustawiono sędziego biura obliczeń");
            }
        }
        return ResponseEntity.badRequest().body("Nie udało się przypisać sędziego");
    }

    public ResponseEntity<?> removeRTSArbiterFromTournament(String tournamentUUID, String memberUUID) {
        if (!tournamentRepository.existsById(tournamentUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono zawodów z tym identyfikatorem");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {
            MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);

            List<MemberEntity> list = tournamentEntity.getArbitersRTSList();
            if (!list.contains(memberEntity)) {
                return ResponseEntity.badRequest().body("Brak sędziego na Liście sędziów");
            }
            historyService.removeJudgingRecord(memberEntity.getUuid(), tournamentUUID);
            list.remove(memberEntity);

            tournamentRepository.save(tournamentEntity);

            return ResponseEntity.ok("Usunięto sędziego");
        }
        return ResponseEntity.badRequest().body("Nie udało się usunąć sędziego");
    }

    public ResponseEntity<?> removeRTSOtherArbiterFromTournament(String tournamentUUID, int id) {
        if (!tournamentRepository.existsById(tournamentUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono zawodów z tym identyfikatorem");
        }
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.isOpen()) {

            OtherPersonEntity otherPersonEntity = otherPersonRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getId()
                            .equals(id))
                    .findFirst().orElseThrow(EntityNotFoundException::new);

            List<OtherPersonEntity> list = tournamentEntity.getOtherArbitersRTSList();
            if (!list.contains(otherPersonEntity)) {
                return ResponseEntity.badRequest().body("Brak sędziego na Liście sędziów");
            }
            list.remove(otherPersonEntity);

            tournamentRepository.save(tournamentEntity);

            return ResponseEntity.ok("Usunięto sędziego");
        }
        return ResponseEntity.badRequest().body("Nie udało się usunąć sędziego");
    }

    public List<String> getStatistics(String tournamentUUID) {

        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);

        List<String> list = new ArrayList<>();
        List<String> list1 = new ArrayList<>();
        List<Integer> list2 = new ArrayList<>();
        tournamentEntity.getCompetitionsList().forEach(e -> e.getScoreList().forEach(g -> {
            if (g.getMember() != null) {
                if (!list1.contains(g.getMember().getUuid())) {
                    list1.add(g.getMember().getUuid());
                }
            }
            if (g.getOtherPersonEntity() != null) {
                if (!list1.contains(String.valueOf(g.getOtherPersonEntity().getId()))) {
                    list1.add(String.valueOf(g.getOtherPersonEntity().getId()));
                }
            }
        }));
        tournamentEntity.getCompetitionsList().forEach(e -> e.getScoreList().forEach(g -> list2.add(g.getMetricNumber())));

        List<ScoreEntity> list3 = new ArrayList<>();
        tournamentEntity.getCompetitionsList().forEach(e -> e.getScoreList().stream().filter(ScoreEntity::isAmmunition).forEach(list3::add));

        List<ScoreEntity> list4 = new ArrayList<>();
        tournamentEntity.getCompetitionsList().forEach(e -> e.getScoreList().stream().filter(ScoreEntity::isGun).forEach(list4::add));


        list.add(String.valueOf(tournamentEntity.getCompetitionsList().size()));
        list.add(String.valueOf(list1.size()));
        list.add(String.valueOf(list2.size()));
        list.add(String.valueOf(list3.size()));
        list.add(String.valueOf(list4.size()));
        return list;
    }
    @Transactional
    @RecordHistory(action = "Tournament.openTournament", entity = HistoryEntityType.TOURNAMENT, entityArgIndex = 0)
    public ResponseEntity<?> openTournament(String tournamentUUID) {
        if (tournamentRepository.findAll().stream().anyMatch(TournamentEntity::isOpen)) {
            return ResponseEntity.badRequest().body("Nie można otworzyć zawodów gdy inne są otwarte");
        } else {
            TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
                tournamentEntity.setOpen(true);
                tournamentRepository.save(tournamentEntity);
            LOG.info("Zawody {} zostały otwarte", tournamentEntity.getName());

            return ResponseEntity.ok().build();

        }
    }

    public Boolean checkAnyOpenTournament() {
        return tournamentRepository.findAll().stream().anyMatch(TournamentEntity::isOpen);
    }

    public List<UsedHistoryEntity> getListOfGunsOnTournament(String tournamentUUID) {

        return usedHistoryRepository.findAll().stream().filter(f -> f.getEvidenceUUID().equals(tournamentUUID)).collect(Collectors.toList());
    }

    public ResponseEntity<?> getJudgingList(String firstDate, String secondDate) {
        LocalDate firstDate1 = LocalDate.parse(firstDate);
        LocalDate secondDate1 = LocalDate.parse(secondDate);
        List<JudgingHistoryEntity> judgingHistoryEntityList = judgingHistoryRepository.findAllByDateBetween(firstDate1, secondDate1);
        List<MemberEntity> collect = memberRepository.findAll().stream().filter(f -> f.getMemberPermissions().getArbiterStaticNumber() != null).toList();
        List<JudgingHistoryDTO> list = new ArrayList<>();
        for (MemberEntity memberEntity : collect) {
            List<JudgingHistoryEntity> judgingHistory = memberEntity.getHistory().getJudgingHistory();
            for (JudgingHistoryEntity judgingHistoryEntity : judgingHistory) {
                for (JudgingHistoryEntity historyEntity : judgingHistoryEntityList) {
                    if (judgingHistoryEntity.getUuid().equals(historyEntity.getUuid())) {
                        list.add(JudgingHistoryDTO.builder()
                                .firstName(memberEntity.getFirstName())
                                .secondName(memberEntity.getSecondName())
                                .tournamentUUID(judgingHistoryEntity.getTournamentUUID())
                                .tournamentName(judgingHistoryEntity.getName())
                                .uuid(judgingHistoryEntity.getUuid())
                                .judgingFunction(judgingHistoryEntity.getJudgingFunction())
                                .date(judgingHistoryEntity.getDate())
                                .time(judgingHistoryEntity.getTime()).build());
                    }
                }
            }
        }
        list.sort(Comparator.comparing(JudgingHistoryDTO::getDate).reversed());
        return ResponseEntity.ok(list);
    }

    public List<ScoreDTO> getShootersNamesList(String tournamentUUID) {
        List<ScoreDTO> list = new ArrayList<>();
        tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new)
                .getCompetitionsList()
                .forEach(e -> e.getScoreList()
                        .forEach(el -> {
                            if (list.stream().noneMatch(r -> el.getMetricNumber() == (r.getMetricNumber()))) {
                                list.add(Mapping.mapScoreDTO(el));
                            }
                        }));

        return list.stream().distinct().sorted(Comparator.comparing(ScoreDTO::getMetricNumber)).collect(Collectors.toList());

    }
}
