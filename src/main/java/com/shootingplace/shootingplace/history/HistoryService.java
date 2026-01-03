package com.shootingplace.shootingplace.history;

import com.shootingplace.shootingplace.contributions.ContributionEntity;
import com.shootingplace.shootingplace.contributions.ContributionRepository;
import com.shootingplace.shootingplace.enums.Discipline;
import com.shootingplace.shootingplace.license.LicenseRepository;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.shootingPatent.ShootingPatent;
import com.shootingplace.shootingplace.shootingPatent.ShootingPatentEntity;
import com.shootingplace.shootingplace.tournament.CompetitionMembersListEntity;
import com.shootingplace.shootingplace.tournament.TournamentEntity;
import com.shootingplace.shootingplace.tournament.TournamentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final MemberRepository memberRepository;
    private final LicenseRepository licenseRepository;
    private final CompetitionHistoryRepository competitionHistoryRepository;
    private final TournamentRepository tournamentRepository;
    private final JudgingHistoryRepository judgingHistoryRepository;
    private final ContributionRepository contributionRepository;

    private final Logger LOG = LogManager.getLogger(getClass());

    //  Basic
    public History getHistory() {
        return History.builder().licenseHistory(new String[3]).patentDay(new LocalDate[3]).licensePaymentHistory(new ArrayList<>()).contributionList(new ArrayList<>()).judgingHistory(new ArrayList<>()).competitionHistory(new ArrayList<>()).pistolCounter(0).rifleCounter(0).shotgunCounter(0).patentFirstRecord(false).build();

    }

    // Contribution
    public void addContribution(String memberUUID, ContributionEntity contribution) {
        HistoryEntity historyEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new).getHistory();
        contribution.setHistoryUUID(historyEntity.getUuid());
        List<ContributionEntity> contributionList = historyEntity.getContributionList();
        if (contributionList != null) {
            contributionList.sort(Comparator.comparing(ContributionEntity::getPaymentDay).thenComparing(ContributionEntity::getValidThru));
        } else {
            contributionList = new ArrayList<>();
        }
        contributionList.add(contribution);
        contributionList.sort(Comparator.comparing(ContributionEntity::getPaymentDay).thenComparing(ContributionEntity::getValidThru).reversed());
        historyEntity.setContributionList(contributionList);

        LOG.info("Dodano rekord w historii składek");
        historyRepository.save(historyEntity);
    }

    public void removeContribution(String memberUUID, ContributionEntity contribution) {
        HistoryEntity historyEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new).getHistory();
        historyEntity.getContributionList().remove(contribution);
        contribution.setHistoryUUID(null);
        contributionRepository.save(contribution);
        LOG.info("Usunięto składkę");
        historyRepository.save(historyEntity);
    }

    // license
    public void addLicenseHistoryRecord(String memberUUID, int index) {
        HistoryEntity historyEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new).getHistory();

        String[] licenseTab = historyEntity.getLicenseHistory().clone();
        if (licenseTab == null) {
            licenseTab = new String[3];
        } else {
            if (index == 0) {
                licenseTab[0] = "Pistolet";
            }
            if (index == 1) {
                licenseTab[1] = "Karabin";
            }
            if (index == 2) {
                licenseTab[2] = "Strzelba";
            }
        }
        historyEntity.setLicenseHistory(licenseTab);
        historyRepository.save(historyEntity);
    }

    void addDateToPatentPermissions(String memberUUID, LocalDate date, int index) {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        HistoryEntity historyEntity = memberEntity.getHistory();
        LocalDate[] dateTab = historyEntity.getPatentDay().clone();
        if (index == 0) {
            if (memberEntity.getShootingPatent().getDateOfPosting() != null) {
                if (!memberEntity.getHistory().getPatentFirstRecord()) {
                    dateTab[0] = memberEntity.getShootingPatent().getDateOfPosting();
                    LOG.info("Pobrano datę patentu dla Pistoletu");
                }
                if (memberEntity.getHistory().getPatentFirstRecord() && historyEntity.getPatentDay()[0] == null) {
                    dateTab[0] = date;
                    LOG.info("Ustawiono datę patentu Karabinu na domyślną");
                }
            }
        }
        if (index == 1) {
            if (memberEntity.getShootingPatent().getDateOfPosting() != null) {
                if (!memberEntity.getHistory().getPatentFirstRecord()) {
                    dateTab[1] = memberEntity.getShootingPatent().getDateOfPosting();
                    LOG.info("Pobrano datę patentu dla Karabinu");
                }
                if (memberEntity.getHistory().getPatentFirstRecord() && historyEntity.getPatentDay()[1] == null) {
                    dateTab[1] = date;
                    LOG.info("Ustawiono datę patentu Karabinu na domyślną");
                }
            }
        }
        if (index == 2) {
            if (memberEntity.getShootingPatent().getDateOfPosting() != null) {
                if (!memberEntity.getHistory().getPatentFirstRecord()) {
                    dateTab[2] = memberEntity.getShootingPatent().getDateOfPosting();
                    LOG.info("Pobrano datę patentu dla Strzelby");
                }
                if (memberEntity.getHistory().getPatentFirstRecord() && historyEntity.getPatentDay()[2] == null) {
                    dateTab[2] = date;
                    LOG.info("Ustawiono datę patentu Strzelby na domyślną");
                }
            }
        }
        if (!historyEntity.getPatentFirstRecord()) {
            LOG.info("Już wpisano datę pierwszego nadania patentu");
        }
        historyEntity.setPatentDay(dateTab);
        historyRepository.save(historyEntity);

    }

    //  Tournament
    private CompetitionHistoryEntity createCompetitionHistoryEntity(String tournamentUUID, LocalDate date, List<String> disciplineList, String attachedTo) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        String name = tournamentEntity.getName();
        CompetitionHistoryEntity build = CompetitionHistoryEntity.builder().name(name).WZSS(tournamentEntity.isWZSS()).date(date).attachedToList(attachedTo).build();
        build.setDisciplineList(disciplineList);
        return build;
    }

    public void addCompetitionRecord(String memberUUID, CompetitionMembersListEntity list) {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);

        CompetitionHistoryEntity competitionHistoryEntity = createCompetitionHistoryEntity(list.getAttachedToTournament(), list.getDate(), list.getDisciplineList(), list.getUuid());
        competitionHistoryRepository.save(competitionHistoryEntity);

        List<CompetitionHistoryEntity> competitionHistoryEntityList = memberEntity.getHistory().getCompetitionHistory();

        competitionHistoryEntityList.add(competitionHistoryEntity);
        competitionHistoryEntityList.sort(Comparator.comparing(CompetitionHistoryEntity::getDate).reversed());

        HistoryEntity historyEntity = memberEntity.getHistory();
        historyEntity.setCompetitionHistory(competitionHistoryEntityList);
        historyRepository.save(historyEntity);
        LOG.info("Dodano wpis w historii startów.");
        checkStarts(memberUUID);
        checkProlongLicense(memberUUID);
    }

    private void checkProlongLicense(String memberUUID) {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        HistoryEntity historyEntity = memberEntity.getHistory();
        Integer[] arr = {memberEntity.getLicense().isPistolPermission() ? historyEntity.getPistolCounter() : -1, memberEntity.getLicense().isRiflePermission() ? historyEntity.getRifleCounter() : -1, memberEntity.getLicense().isShotgunPermission() ? historyEntity.getShotgunCounter() : -1};
        Arrays.sort(arr, Collections.reverseOrder());
        memberEntity.getLicense().setCanProlong(arr[0] != -1 && arr[0] >= 4 && arr[1] == -1 && arr[2] == -1 || arr[0] != -1 && arr[0] >= 4 && arr[1] != -1 && arr[1] >= 2 && arr[2] == -1 || arr[0] != -1 && arr[0] >= 4 && arr[1] != -1 && arr[1] >= 2 && arr[2] >= 2);
        licenseRepository.save(memberEntity.getLicense());
    }

    public void checkStarts() {
        List<MemberEntity> collect = memberRepository.findAllByErasedFalse();
        collect.forEach(e -> checkStarts(e.getUuid()));
    }

    public void checkStarts(String memberUUID) {
        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        HistoryEntity history = member.getHistory();
        if (history == null || history.getCompetitionHistory() == null) {
            return;
        }
        int year = member.getLicense().getNumber() != null ? member.getLicense().getValidThru().getYear() : LocalDate.now().getYear();
        int pistol = 0;
        int rifle = 0;
        int shotgun = 0;
        for (CompetitionHistoryEntity ch : history.getCompetitionHistory()) {
            if (ch.getDate() == null || ch.getDate().getYear() != year) {
                continue;
            }
            List<String> disciplines = ch.getDisciplineList();
            if (disciplines == null) {
                continue;
            }
            for (String d : disciplines) {
                if (d.contains(Discipline.PISTOL.getName())) {
                    pistol++;
                }
                if (d.contains(Discipline.RIFLE.getName())) {
                    rifle++;
                }
                if (d.contains(Discipline.SHOTGUN.getName())) {
                    shotgun++;
                }
            }
        }

        history.setPistolCounter(pistol);
        history.setRifleCounter(rifle);
        history.setShotgunCounter(shotgun);

        historyRepository.save(history);
    }

    public void removeCompetitionRecord(String memberUUID, CompetitionMembersListEntity list) {
        HistoryEntity historyEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new).getHistory();
        CompetitionHistoryEntity competitionHistoryEntity = new CompetitionHistoryEntity();
        for (CompetitionHistoryEntity e : historyEntity.getCompetitionHistory()) {
            if (e.getAttachedToList().equals(list.getUuid())) {
                competitionHistoryEntity = competitionHistoryRepository.findById(e.getUuid()).orElseThrow(EntityNotFoundException::new);
                break;
            }

        }
        historyEntity.getCompetitionHistory().remove(competitionHistoryEntity);

        LOG.info("Zaktualizowano wpis w historii startów");
        historyRepository.save(historyEntity);
        checkStarts(memberUUID);
        competitionHistoryRepository.delete(competitionHistoryEntity);
        checkProlongLicense(memberUUID);
    }

    public void addJudgingRecord(String memberUUID, String tournamentUUID, String function) {

        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);

        HistoryEntity historyEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new).getHistory();

        JudgingHistoryEntity judgingHistoryEntity = createJudgingHistoryEntity(tournamentEntity.getDate(), tournamentEntity.getName(), tournamentEntity.getUuid(), function);

        List<JudgingHistoryEntity> judgingHistory = historyEntity.getJudgingHistory();

        judgingHistory.add(judgingHistoryEntity);
        judgingHistoryRepository.save(judgingHistoryEntity);
        historyEntity.setJudgingHistory(judgingHistory);

        historyRepository.save(historyEntity);
    }

    public void removeJudgingRecord(String memberUUID, String tournamentUUID) {

        List<JudgingHistoryEntity> judgingHistoryEntityList = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new).getHistory().getJudgingHistory();

        JudgingHistoryEntity any = judgingHistoryEntityList.stream().filter(e -> e.getTournamentUUID().equals(tournamentUUID)).findFirst().orElseThrow(EntityNotFoundException::new);
        judgingHistoryEntityList.remove(any);
        HistoryEntity historyEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new).getHistory();
        historyEntity.setJudgingHistory(judgingHistoryEntityList);
        historyRepository.save(historyEntity);
        judgingHistoryRepository.delete(any);

    }

    private JudgingHistoryEntity createJudgingHistoryEntity(LocalDate date, String name, String tournamentUUID, String function) {
        return JudgingHistoryEntity.builder().date(date).time(LocalTime.now()).name(name).judgingFunction(function).tournamentUUID(tournamentUUID).build();
    }

    public void updateTournamentEntityInCompetitionHistory(String tournamentUUID) {

        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        tournamentEntity.getCompetitionsList().forEach(competitionList -> competitionList.getScoreList().stream().filter(f -> f.getMember() != null).forEach(scoreEntity -> scoreEntity.getMember().getHistory().getCompetitionHistory().stream().filter(f -> f.getAttachedToList().equals(competitionList.getUuid())).forEach(f -> {
            f.setName(tournamentEntity.getName());
            f.setDate(tournamentEntity.getDate());
            competitionHistoryRepository.save(f);
        })));
    }

    public void updateTournamentInJudgingHistory(String tournamentUUID) {
        TournamentEntity tournamentEntity = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        if (tournamentEntity.getMainArbiter() != null) {
            tournamentEntity.getMainArbiter().getHistory().getJudgingHistory().stream().filter(f -> f.getTournamentUUID().equals(tournamentUUID)).forEach(f -> {
                f.setName(tournamentEntity.getName());
                f.setDate(tournamentEntity.getDate());
                judgingHistoryRepository.save(f);
            });
        }
        if (tournamentEntity.getCommissionRTSArbiter() != null) {
            tournamentEntity.getCommissionRTSArbiter().getHistory().getJudgingHistory().stream().filter(f -> f.getTournamentUUID().equals(tournamentUUID)).forEach(f -> {
                f.setName(tournamentEntity.getName());
                f.setDate(tournamentEntity.getDate());
                judgingHistoryRepository.save(f);
            });
        }

    }

    public void updateShootingPatentHistory(String memberUUID, ShootingPatent shootingPatent) {
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        ShootingPatentEntity shootingPatentEntity = memberEntity.getShootingPatent();
        HistoryEntity historyEntity = memberEntity.getHistory();
        if (shootingPatentEntity.getDateOfPosting() != null) {
            if (shootingPatentEntity.isPistolPermission()) {
                addDateToPatentPermissions(memberUUID, shootingPatent.getDateOfPosting(), 0);
            }
            if (shootingPatentEntity.isRiflePermission()) {
                addDateToPatentPermissions(memberUUID, shootingPatent.getDateOfPosting(), 1);
            }
            if (shootingPatentEntity.isShotgunPermission()) {
                addDateToPatentPermissions(memberUUID, shootingPatent.getDateOfPosting(), 2);
            }
            if (shootingPatentEntity.getDateOfPosting() != null) {
                historyEntity.setPatentFirstRecord(true);
            }
            historyRepository.save(historyEntity);
        }
    }
}
