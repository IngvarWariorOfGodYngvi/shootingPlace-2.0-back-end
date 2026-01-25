package com.shootingplace.shootingplace.portal;

import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.enums.CountingMethod;
import com.shootingplace.shootingplace.score.ScoreEntity;
import com.shootingplace.shootingplace.tournament.CompetitionMembersListEntity;
import com.shootingplace.shootingplace.tournament.TournamentEntity;

import java.util.*;

public class Mappers {

    public static ResultExportDto map(ScoreEntity e, CompetitionMembersListEntity comp) {

        String firstName = e.getMember() != null ? e.getMember().getFirstName() : e.getOtherPersonEntity().getFirstName();
        String secondName = e.getMember() != null ? e.getMember().getSecondName() : e.getOtherPersonEntity().getSecondName();
        String club = e.getMember() != null ? e.getMember().getClub().getShortName() : e.getOtherPersonEntity().getClub().getShortName();
        String license = e.getMember() != null ? e.getMember().getLicense().getNumber() : e.getOtherPersonEntity().getLicenseNumber();

        ResultExportDto build = ResultExportDto.builder()
                .id(e.getUuid())
                .firstName(firstName)
                .lastName(secondName)
                .club(club)
                .license(license)
                .place(1)
                .details("notatka")
                .extra(null)
                .build();


        if (comp.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {

            boolean afterComa = comp.getScoreList().stream().anyMatch(f -> {
                String s = String.format("%.1f", f.getScore());
                return Integer.parseInt(s.substring(s.indexOf(",") + 1)) > 0;
            });
            String result;
            float score = e.getScore();
            if (afterComa) {
                result = String.format("%.1f", score);
            } else {
                result = String.valueOf(Math.round(score));
            }
            build.setScore(result);

            Map<String, String> map = new HashMap<>();
            map.put("10 /", String.valueOf((int)e.getOuterTen()));
            map.put("10 X", String.valueOf((int)e.getInnerTen()));
            build.setExtra(map);
        }
        if (comp.getCountingMethod().equals(CountingMethod.COMSTOCK.getName())) {
            build.setScore(String.format("%.4f", e.getScore()));
            Map<String, String> map = new HashMap<>();
            map.put("czas", String.format("%.4f", e.getInnerTen()));
            map.put("hf", String.format("%.4f", e.getHf()));
            build.setExtra(map);
        }
        if (comp.getCountingMethod().equals(CountingMethod.IPSC.getName())) {
            build.setScore(String.format("%.4f", e.getScore()));
            Map<String, String> map = new HashMap<>();
            map.put("czas", String.format("%.4f", e.getInnerTen()));
            map.put("hf", String.format("%.4f", e.getHf()));
            build.setExtra(map);
        }
        if (comp.getCountingMethod().equals(CountingMethod.DYNAMIKADZIESIATKA.getName())) {
            build.setScore(String.format("%.4f", e.getScore()));
            Map<String, String> map = new HashMap<>();
            map.put("czas", String.format("%.4f", e.getInnerTen()));
            map.put("hf", String.format("%.4f", e.getHf()));
            build.setExtra(map);
        }
        if (comp.getCountingMethod().equals(CountingMethod.TIME.getName())) {
            build.setScore(String.format("%.4f", e.getScore()));
        }
        return build;

    }

    public static CompetitionExportDto map(CompetitionMembersListEntity e) {

        Comparator<ScoreEntity> comparator;

        if (e.getCountingMethod().equals(CountingMethod.NORMAL.getName())) {
            comparator = Comparator
                    .comparing(ScoreEntity::getScore).reversed()
                    .thenComparing(ScoreEntity::getInnerTen).reversed()
                    .thenComparing(ScoreEntity::getOuterTen).reversed();
        } else {
            comparator = Comparator
                    .comparing(ScoreEntity::getScore).reversed();
        }

        List<ScoreEntity> sorted = e.getScoreList().stream()
                .filter(Mappers::hasLicense)
                .sorted(comparator)
                .toList();
        List<ResultExportDto> results = new ArrayList<>();

        int place = 1;
        int index = 1;
        ScoreEntity prev = null;

        for (ScoreEntity score : sorted) {

            if (prev != null && comparator.compare(prev, score) != 0) {
                place = index;
            }

            ResultExportDto dto = map(score, e);
            dto.setPlace(place);

            results.add(dto);

            prev = score;
            index++;
        }

        return CompetitionExportDto.builder()
                .id(e.getUuid())
                .name(e.getName())
                .disciplines(e.getDisciplineList())
                .results(results)
                .build();

    }

    public static TournamentExportDto map(TournamentEntity e, ClubEntity club) {
        String mainArbiter = e.getMainArbiter() != null ? e.getMainArbiter().getFullName() : e.getOtherMainArbiter().getFullName();
        String RTSArbiter = e.getCommissionRTSArbiter() != null ? e.getCommissionRTSArbiter().getFullName() : e.getOtherCommissionRTSArbiter().getFullName();
        String location = e.getLocation() != null ? e.getLocation() : club.getCity();

        return TournamentExportDto.builder()
                .id(e.getUuid())
                .name(e.getName())
                .date(e.getDate().toString())
                .club(club.getShortName())
                .location(location)
                .mainArbiter(mainArbiter)
                .rtsArbiter(RTSArbiter)
                .competitions(e.getCompetitionsList().stream().filter(f->!f.getScoreList().isEmpty()).map(Mappers::map).toList())
                .build();

    }

    private static boolean hasLicense(ScoreEntity e) {
        if (e.getMember() != null) {
            return e.getMember().getLicense().getNumber() != null;
        }
        return e.getOtherPersonEntity() != null
                && e.getOtherPersonEntity().getLicenseNumber() != null
                && !e.getOtherPersonEntity().getLicenseNumber().isBlank();
    }

}
