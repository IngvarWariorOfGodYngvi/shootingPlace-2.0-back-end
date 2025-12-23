package com.shootingplace.shootingplace.score;

import com.shootingplace.shootingplace.enums.CompetitionType;
import com.shootingplace.shootingplace.enums.CountingMethod;
import com.shootingplace.shootingplace.enums.ProfilesEnum;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.tournament.CompetitionMembersListEntity;
import com.shootingplace.shootingplace.tournament.CompetitionMembersListRepository;
import com.shootingplace.shootingplace.tournament.TournamentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final CompetitionMembersListRepository competitionMembersListRepository;
    private final TournamentRepository tournamentRepository;
    private final Environment environment;

    private final Logger LOG = LogManager.getLogger(getClass());

    public ScoreEntity createScore(float score, float innerTen, float outerTen, int procedures, String competitionMembersListEntityUUID, MemberEntity memberEntity, OtherPersonEntity otherPersonEntity) {
        String name = memberEntity != null ? memberEntity.getFullName() : otherPersonEntity.getFullName();

        CompetitionMembersListEntity competitionMembersListEntity = competitionMembersListRepository.findById(competitionMembersListEntityUUID)
                .orElseThrow(EntityNotFoundException::new);

        List<ScoreEntity> scoreEntityList = new ArrayList<>();

        tournamentRepository.findById(competitionMembersListEntity.getAttachedToTournament()).orElseThrow(EntityNotFoundException::new).getCompetitionsList().forEach(e -> scoreEntityList.addAll(e.getScoreList()));

        ScoreEntity scoreEntity = scoreEntityList.stream().max(Comparator.comparing(ScoreEntity::getMetricNumber)).orElse(null);

        boolean match = scoreEntityList.stream().anyMatch(a -> a.getMember() == (memberEntity));

        boolean match1 = scoreEntityList.stream().anyMatch(a -> a.getOtherPersonEntity() == (otherPersonEntity));

        int number = scoreEntity != null ? scoreEntity.getMetricNumber() + 1 : 1;

        if (memberEntity != null) {
            if (match) {
                number = scoreEntityList.stream().filter(f -> f.getMember() == (memberEntity)).findFirst().orElseThrow(EntityNotFoundException::new).getMetricNumber();
            } else {
                if (scoreEntity != null) {
                    number = scoreEntity.getMetricNumber() + 1;
                }
            }
        }
        if (otherPersonEntity != null) {
            if (match1) {
                number = scoreEntityList.stream().filter(f -> f.getOtherPersonEntity() == (otherPersonEntity)).findFirst().orElseThrow(EntityNotFoundException::new).getMetricNumber();
            } else {
                if (scoreEntity != null) {
                    number = scoreEntity.getMetricNumber() + 1;
                }
            }
        }
        String value = null;
        if (competitionMembersListEntity.getNumberOfShots() > 0) {
            Integer n = competitionMembersListEntity.getNumberOfShots();
            double s = n / 10d;
            s = Math.ceil(s);
            Float[] t = new Float[(int) s];
            Arrays.fill(t, 0f);

            value = "";
            for (Float f : t) {
                value = value.concat(f + ";");
            }

        }
        scoreEntity = scoreRepository.save(ScoreEntity.builder()
                .competitionMembersListEntityUUID(competitionMembersListEntityUUID)
                .member(memberEntity)
                .otherPersonEntity(otherPersonEntity)
                .score(score)
                .innerTen(innerTen)
                .alfa(0)
                .charlie(0)
                .delta(0)
                .outerTen(outerTen)
                .procedures(procedures)
                .ammunition(false)
                .gun(false)
                .series(value)
                .name(name)
                .metricNumber(number)
                .createDate(LocalDateTime.now())
                .build());

        LOG.info("Utworzono wynik dla {}", name);

        return scoreEntity;

    }

    public ResponseEntity<?> setScore(String scoreUUID, float score, float innerTen, float outerTen, Float alfa, Float charlie, Float delta, int procedures, float miss, List<Float> series) {
        if (!scoreRepository.existsById(scoreUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono wyniku. Sprawdź identyfikator rekordu");
        }
        ScoreEntity scoreEntity = scoreRepository.getOne(scoreUUID);
        scoreEntity.setName(scoreEntity.getOtherPersonEntity() != null ? scoreEntity.getOtherPersonEntity().getFullName() : scoreEntity.getMember().getFullName());
        String competitionMembersListEntityUUID = scoreEntity.getCompetitionMembersListEntityUUID();
        CompetitionMembersListEntity competitionMembersListEntity = competitionMembersListRepository.findById(competitionMembersListEntityUUID).orElseThrow(EntityNotFoundException::new);
        // Metoda COMSTOCK
        if (competitionMembersListEntity.getCountingMethod() != null && competitionMembersListEntity.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
            if (innerTen == -1) {
                //czas
                innerTen = scoreEntity.getInnerTen();
            }
            if (outerTen == -1) {
                //trafienia
                outerTen = scoreEntity.getOuterTen();
            }
            if (procedures == -1) {
                //procedury
                procedures = scoreEntity.getProcedures();
            }
            if (alfa == -1) {
                alfa = (float) 0;
            }
            if (charlie == -1) {
                charlie = (float) 0;
            }
            if (delta == -1) {
                delta = (float) 0;
            }
            List<ScoreEntity> scoreList = competitionMembersListEntity
                    .getScoreList()
                    .stream()
                    .filter(f -> !f.isDsq() && !f.isDnf() && !f.isPk())
                    .sorted(Comparator.comparing(ScoreEntity::getScore).reversed())
                    .collect(Collectors.toList());
            if (competitionMembersListEntity.getNumberOfShots() == null) {
                scoreRepository.save(scoreEntity);
            } else {
                int numberOfShots = competitionMembersListEntity.getNumberOfShots();
                int points;
                float penalties = numberOfShots - outerTen/*shots*/;

                if (alfa > 0 || charlie > 0 || delta > 0) {
                    outerTen = alfa + charlie + delta;
                    points = (int) ((alfa * 5) + (charlie * 3) + (delta * 1) + (penalties * -10));
                } else {
                    points = (int) ((outerTen * 5) + (penalties * -10));
                }
                if (outerTen > numberOfShots) {
                    return ResponseEntity.badRequest().body("Nie można dodać wyniku. Sprawdź ilość podanych strzałów");
                }
                scoreEntity.setOuterTen(outerTen);
                if (points < 0) {
                    points = 0;
                }
                float hf;
                if (environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName())) {
                    hf = points / (innerTen + (procedures * 3)) /*time*/;
                } else {
                    hf = points / (innerTen + (procedures * 5)) /*time*/;
                }
                scoreEntity.setInnerTen(innerTen);

                float hf1;
                if (scoreList.size() > 1) {
                    hf1 = scoreList.stream()
                            .max(Comparator.comparing(ScoreEntity::getHf))
                            .orElseThrow(EntityNotFoundException::new)
                            .getHf();
                } else {
                    hf1 = hf;
                }
                scoreEntity.setHf(hf);
                scoreEntity.setProcedures(procedures);
                scoreEntity.setAlfa(alfa);
                scoreEntity.setCharlie(charlie);
                scoreEntity.setDelta(delta);
                scoreEntity.setMiss(penalties);
                if (hf < hf1) {
                    scoreEntity.setScore((hf / hf1) * 100);
                } else {
                    scoreEntity.setScore(100);
                }
                if (innerTen <= 0) {
                    scoreEntity.setScore(0);
                    scoreEntity.setHf(0);
                }
                scoreRepository.save(scoreEntity);
                float hf2 = scoreList.stream()
                        .max(Comparator.comparing(ScoreEntity::getHf))
                        .orElseThrow(EntityNotFoundException::new)
                        .getHf();
                scoreList.forEach(e -> {
                    e.setScore((e.getHf() / hf2) * 100);
                    scoreRepository.save(e);
                });
            }
            scoreList.sort(Comparator.comparing(ScoreEntity::getScore)
                    .reversed());
            List<ScoreEntity> collect = competitionMembersListEntity
                    .getScoreList()
                    .stream()
                    .filter(f -> f.isDsq() || f.isDnf() || f.isPk())
                    .toList();
            scoreList.addAll(collect);
            competitionMembersListEntity.setScoreList(scoreList);

        }
        // Metoda NORMAL
        if (competitionMembersListEntity.getCountingMethod() != null && competitionMembersListEntity.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {
            if (innerTen == -1) {
                innerTen = scoreEntity.getInnerTen();
            }
            if (outerTen == -1) {
                outerTen = scoreEntity.getOuterTen();
            }
            List<Float> series1 = scoreEntity.getSeries();
            if (series != null) {
                for (int i = 0; i < scoreEntity.getSeries().size(); i++) {
                    series1.set(i, series.get(i) == null ? series1.get(i) : series.get(i));
                }
            }
            series = series1;
            scoreEntity.setSeries(series);
            scoreEntity.setScore((float) series.stream().mapToDouble(a -> a).sum());
            scoreEntity.setInnerTen(innerTen);
            scoreEntity.setOuterTen(outerTen);
            scoreRepository.save(scoreEntity);

            List<ScoreEntity> scoreList = competitionMembersListEntity.getScoreList().stream()
                    .filter(f -> !f.isDsq() && !f.isDnf() && !f.isPk())
                    .sorted(Comparator.comparing(ScoreEntity::getScore)
                            .thenComparing(ScoreEntity::getInnerTen)
                            .thenComparing(ScoreEntity::getOuterTen).reversed())
                    .collect(Collectors.toList());

            List<ScoreEntity> collect = competitionMembersListEntity.getScoreList().stream()
                    .filter(f -> f.isDnf() || f.isDsq() || f.isPk())
                    .sorted(Comparator.comparing(ScoreEntity::getScore)
                            .thenComparing(ScoreEntity::getInnerTen)
                            .thenComparing(ScoreEntity::getOuterTen).reversed())
                    .toList();

            scoreList.addAll(collect);

            competitionMembersListEntity.setScoreList(scoreList);
        }
        // Metoda CZAS
        if (competitionMembersListEntity.getCountingMethod() != null && competitionMembersListEntity.getCountingMethod().equals(CountingMethod.TIME.getName())) {
            if (procedures == -1) {
                //procedury
                procedures = scoreEntity.getProcedures();
            } else {
                scoreEntity.setProcedures(procedures);
            }


            scoreEntity.setScore(score);
            scoreEntity.setInnerTen(0);
            scoreEntity.setOuterTen(0);
            scoreRepository.save(scoreEntity);
            int time;
            if (environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName())) {
                time = 3;
            } else {
                time = 5;
            }
            List<ScoreEntity> scoreList = competitionMembersListEntity.getScoreList()
                    .stream()
                    .filter(f -> !f.isDsq() && !f.isDnf() && !f.isPk() && f.getScore() != 0)
                    .sorted(Comparator.comparingInt(s -> (int) s.getScore() + (s.getProcedures() * time)))
                    .collect(Collectors.toList());

            List<ScoreEntity> collect = competitionMembersListEntity
                    .getScoreList()
                    .stream()
                    .filter(f -> f.isDnf() || f.isDsq() || f.isPk() || f.getScore() == 0)
                    .sorted(Comparator.comparingInt(s -> (int) s.getScore() + (s.getProcedures() * time)))
                    .toList();
            scoreList.addAll(collect);

            competitionMembersListEntity.setScoreList(scoreList);
        }
        // Metoda IPSC
        if (competitionMembersListEntity.getCountingMethod() != null && competitionMembersListEntity.getCountingMethod().equals(CountingMethod.IPSC.getName())) {

            // czas
            innerTen = innerTen == -1 ? scoreEntity.getInnerTen() : innerTen;
            // trafienia
            outerTen = outerTen == -1 ? scoreEntity.getOuterTen() : outerTen;
            // procedury
            procedures = procedures == -1 ? scoreEntity.getProcedures() : procedures;
            // alfa
            alfa = alfa == -1 ? scoreEntity.getAlfa() : alfa;
            // charlie
            charlie = charlie == -1 ? scoreEntity.getCharlie() : charlie;
            // delta
            delta = delta == -1 ? scoreEntity.getDelta() : delta;
            // miss
            miss = miss == -1 ? scoreEntity.getMiss() : miss;

            List<ScoreEntity> scoreList = competitionMembersListEntity
                    .getScoreList()
                    .stream()
                    .filter(f -> !f.isDsq() && !f.isDnf() && !f.isPk())
                    .sorted(Comparator.comparing(ScoreEntity::getScore).reversed())
                    .collect(Collectors.toList());
            String type = competitionMembersListEntity.getType();

            int alfaPoint, charliePoint = 0, deltaPoint = 0;
            alfaPoint = (int) (alfa * 5);
            if (type.equals(CompetitionType.MINOR.getName())) {
                charliePoint = (int) (charlie * 3);
                deltaPoint = (int) (delta * 1);
            } else if (type.equals(CompetitionType.MAJOR.getName())) {
                charliePoint = (int) (charlie * 4);
                deltaPoint = (int) (delta * 2);
            }

            int points;

            points = (int) ((alfaPoint) + (charliePoint) + (deltaPoint) + (miss * -10) + (procedures * -10));

            if (points < 0) {
                points = 0;
            }
            outerTen = points;
            float hf;
            // hf punkty / czas
            hf = points / (innerTen);


            float hf1;
            if (scoreList.size() > 1) {
                hf1 = scoreList.stream()
                        .max(Comparator.comparing(ScoreEntity::getHf))
                        .orElseThrow(EntityNotFoundException::new)
                        .getHf();
            } else {
                hf1 = hf;
            }
            scoreEntity.setOuterTen(outerTen);
            scoreEntity.setInnerTen(innerTen);
            scoreEntity.setHf(hf);
            scoreEntity.setProcedures(procedures);
            scoreEntity.setAlfa(alfa);
            scoreEntity.setCharlie(charlie);
            scoreEntity.setDelta(delta);
            scoreEntity.setMiss(miss);
            if (hf < hf1) {
                scoreEntity.setScore((hf / hf1) * 100);
            } else {
                scoreEntity.setScore(100);
            }
            if (innerTen <= 0) {
                scoreEntity.setScore(0);
                scoreEntity.setHf(0);
            }
            scoreRepository.save(scoreEntity);
            scoreList.forEach(e -> {
                float hf2 = scoreList.stream()
                        .max(Comparator.comparing(ScoreEntity::getHf))
                        .orElseThrow(EntityNotFoundException::new).getHf();
                e.setScore((e.getHf() / hf2) * 100);
                scoreRepository.save(e);
            });

            scoreList.sort(Comparator.comparing(ScoreEntity::getScore)
                    .reversed());
            List<ScoreEntity> collect = competitionMembersListEntity
                    .getScoreList()
                    .stream()
                    .filter(f -> f.isDsq() || f.isDnf() || f.isPk())
                    .toList();
            scoreList.addAll(collect);
            competitionMembersListEntity.setScoreList(scoreList);

        }
        // Metoda dla Dziesiątki
        if (competitionMembersListEntity.getCountingMethod() != null && competitionMembersListEntity.getCountingMethod().equals(CountingMethod.DYNAMIKADZIESIATKA.getName())) {

            // czas
            innerTen = innerTen == -1 ? scoreEntity.getInnerTen() : innerTen;
            // trafienia
            outerTen = outerTen == -1 ? scoreEntity.getOuterTen() : outerTen;
            // procedury
            procedures = procedures == -1 ? scoreEntity.getProcedures() : procedures;
            // alfa
            alfa = alfa == -1 ? scoreEntity.getAlfa() : alfa;
            // charlie
            charlie = charlie == -1 ? scoreEntity.getCharlie() : charlie;
            // delta
            delta = delta == -1 ? scoreEntity.getDelta() : delta;
            // miss
            miss = miss == -1 ? scoreEntity.getMiss() : miss;

            List<ScoreEntity> scoreList = competitionMembersListEntity
                    .getScoreList()
                    .stream()
                    .filter(f -> !f.isDsq() && !f.isDnf() && !f.isPk())
                    .sorted(Comparator.comparing(ScoreEntity::getScore).reversed())
                    .collect(Collectors.toList());

            int alfaPoint, charliePoint, deltaPoint;
            alfaPoint = (int) (alfa * 5);
            charliePoint = (int) (charlie * 3);
            deltaPoint = (int) (delta * 1);

            int points;

            points = (alfaPoint) + (charliePoint) + (deltaPoint);

            if (points < 0) {
                points = 0;
            }
            outerTen = points;
            float hf;
            // hf punkty / czas
            hf = points / (innerTen + (procedures * 3));


            float hf1;
            if (scoreList.size() > 1) {
                hf1 = scoreList.stream()
                        .max(Comparator.comparing(ScoreEntity::getHf))
                        .orElseThrow(EntityNotFoundException::new)
                        .getHf();
            } else {
                hf1 = hf;
            }
            scoreEntity.setOuterTen(outerTen);
            scoreEntity.setInnerTen(innerTen);
            scoreEntity.setHf(hf);
            scoreEntity.setProcedures(procedures);
            scoreEntity.setAlfa(alfa);
            scoreEntity.setCharlie(charlie);
            scoreEntity.setDelta(delta);
            scoreEntity.setMiss(miss);
            if (hf < hf1) {
                scoreEntity.setScore((hf / hf1) * 100);
            } else {
                scoreEntity.setScore(100);
            }
            if (innerTen <= 0) {
                scoreEntity.setScore(0);
                scoreEntity.setHf(0);
            }
            scoreRepository.save(scoreEntity);
            scoreList.forEach(e -> {
                float hf2 = scoreList.stream()
                        .max(Comparator.comparing(ScoreEntity::getHf))
                        .orElseThrow(EntityNotFoundException::new).getHf();
                e.setScore((e.getHf() / hf2) * 100);
                scoreRepository.save(e);
            });

            scoreList.sort(Comparator.comparing(ScoreEntity::getScore)
                    .reversed());
            List<ScoreEntity> collect = competitionMembersListEntity
                    .getScoreList()
                    .stream()
                    .filter(f -> f.isDsq() || f.isDnf() || f.isPk())
                    .toList();
            scoreList.addAll(collect);
            competitionMembersListEntity.setScoreList(scoreList);
        }
        // Metoda Pojedynek
        if (competitionMembersListEntity.getCountingMethod() != null && competitionMembersListEntity.getCountingMethod().equals(CountingMethod.POJEDYNEK.getName())) {

            if (innerTen == -1) {
                innerTen = scoreEntity.getInnerTen();
            }
            if (outerTen == -1) {
                outerTen = scoreEntity.getOuterTen();
            }
            List<Float> series1 = scoreEntity.getSeries();
            if (series != null) {
                for (int i = 0; i < scoreEntity.getSeries().size(); i++) {
                    series1.set(i, series.get(i) == null ? series1.get(i) : series.get(i));
                }
            }
            series = series1;
            scoreEntity.setScore((float) series.stream().mapToDouble(a -> a).sum());
            scoreEntity.setInnerTen(innerTen);
            scoreEntity.setOuterTen(outerTen);
            scoreRepository.save(scoreEntity);

            List<ScoreEntity> scoreList = competitionMembersListEntity.getScoreList().stream()
                    .filter(f -> !f.isDsq() && !f.isDnf() && !f.isPk())
                    .sorted(Comparator.comparing(ScoreEntity::getScore)
                            .thenComparing(ScoreEntity::getInnerTen)
                            .thenComparing(ScoreEntity::getOuterTen).reversed())
                    .collect(Collectors.toList());

            List<ScoreEntity> collect = competitionMembersListEntity.getScoreList().stream()
                    .filter(f -> f.isDnf() || f.isDsq() || f.isPk())
                    .sorted(Comparator.comparing(ScoreEntity::getScore)
                            .thenComparing(ScoreEntity::getInnerTen)
                            .thenComparing(ScoreEntity::getOuterTen).reversed())
                    .toList();

            scoreList.addAll(collect);

            competitionMembersListEntity.setScoreList(scoreList);
        }
        competitionMembersListRepository.save(competitionMembersListEntity);
        return ResponseEntity.ok("Ustawiono wynik " + scoreEntity.getName());
    }

    public ResponseEntity<?> toggleAmmunitionInScore(String scoreUUID) {

        if (!scoreRepository.existsById(scoreUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono wyniku. Sprawdź identyfikator rekordu");
        }
        ScoreEntity scoreEntity = scoreRepository.findById(scoreUUID).orElseThrow(EntityNotFoundException::new);
        scoreEntity.toggleAmmunition();


        scoreRepository.save(scoreEntity);
        LOG.info("wydaję amunicję {}", scoreEntity.getName());
        return ResponseEntity.ok("Wydano amunicję " + scoreEntity.getName());
    }

    public ResponseEntity<?> toggleGunInScore(String scoreUUID) {

        if (!scoreRepository.existsById(scoreUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono wyniku. Sprawdź identyfikator rekordu");
        }
        ScoreEntity scoreEntity = scoreRepository.findById(scoreUUID).orElseThrow(EntityNotFoundException::new);
        scoreEntity.toggleGun();
        scoreRepository.save(scoreEntity);
        reorganizeCompetitionMemberList(scoreUUID);
        LOG.info("wydaję broń {}", scoreEntity.getName());
        return ResponseEntity.ok("Wydano broń " + scoreEntity.getName());
    }

    public ResponseEntity<?> toggleDnfScore(String scoreUUID) {

        if (!scoreRepository.existsById(scoreUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono wyniku. Sprawdź identyfikator rekordu");
        }
        ScoreEntity scoreEntity = scoreRepository.findById(scoreUUID).orElseThrow(EntityNotFoundException::new);
        scoreEntity.toggleDnf();
        scoreRepository.save(scoreEntity);
        reorganizeCompetitionMemberList(scoreUUID);

        LOG.info("Ustawiam DNF {}", scoreEntity.getName());
        return ResponseEntity.ok("Ustawiono DNF " + scoreEntity.getName());
    }

    public ResponseEntity<?> toggleDsqScore(String scoreUUID) {

        if (!scoreRepository.existsById(scoreUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono wyniku. Sprawdź identyfikator rekordu");
        }
        String name;

        ScoreEntity scoreEntity = scoreRepository.findById(scoreUUID).orElseThrow(EntityNotFoundException::new);
        scoreEntity.toggleDsq();
        scoreRepository.save(scoreEntity);
        reorganizeCompetitionMemberList(scoreUUID);

        if (scoreEntity.getMember() != null) {
            name = scoreEntity.getMember().getFirstName() + " " + scoreEntity.getMember().getSecondName();
        } else {
            name = scoreEntity.getOtherPersonEntity().getFirstName() + " " + scoreEntity.getOtherPersonEntity().getSecondName();
        }
        LOG.info("Ustawiam DSQ {}", name);
        return ResponseEntity.ok("Ustawiono DSQ " + name);
    }

    public ResponseEntity<?> togglePkScore(String scoreUUID) {

        if (!scoreRepository.existsById(scoreUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono wyniku. Sprawdź identyfikator rekordu");
        }
        ScoreEntity scoreEntity = scoreRepository.findById(scoreUUID).orElseThrow(EntityNotFoundException::new);
        scoreEntity.togglePk();
        scoreRepository.save(scoreEntity);
        reorganizeCompetitionMemberList(scoreUUID);

        LOG.info("Ustawiono PK {}", scoreEntity.getName());
        return ResponseEntity.ok("Ustawiono PK " + scoreEntity.getName());
    }

    public void reorganizeCompetitionMemberList(String scoreUUID) {
        ScoreEntity one = scoreRepository.getOne(scoreUUID);
        setScore(scoreUUID, one.getScore(), -1, -1, -1f, -1f, -1f, -1, -1, null);
    }

    public ResponseEntity<?> forceSetScore(String scoreUUID, float score) {
        ScoreEntity scoreEntity = scoreRepository.findById(scoreUUID).orElseThrow(EntityNotFoundException::new);

        String competitionMembersListEntityUUID = scoreEntity.getCompetitionMembersListEntityUUID();
        CompetitionMembersListEntity competitionMembersListEntity = competitionMembersListRepository.findById(competitionMembersListEntityUUID).orElseThrow(EntityNotFoundException::new);

        scoreEntity.setScore(score);
        scoreEntity.setName(scoreEntity.getOtherPersonEntity() != null ? scoreEntity.getOtherPersonEntity().getFullName() : scoreEntity.getMember().getFullName());

        scoreRepository.save(scoreEntity);
        List<ScoreEntity> scoreList = competitionMembersListEntity.getScoreList().stream().filter(f -> !f.isDsq()).filter(f -> !f.isDnf()).filter(f -> !f.isPk()).sorted(Comparator.comparing(ScoreEntity::getScore)
                .reversed()).collect(Collectors.toList());

        List<ScoreEntity> collect = competitionMembersListEntity.getScoreList().stream().filter(f -> f.isDsq() || f.isDnf() || f.isPk()).toList();
        scoreList.addAll(collect);
        competitionMembersListEntity.setScoreList(scoreList);
        competitionMembersListRepository.save(competitionMembersListEntity);
        LOG.info("Ustawiono wynik na twardo {}", scoreEntity.getName());
        return ResponseEntity.ok("Ustawiono wynik " + scoreEntity.getName());
    }
}
