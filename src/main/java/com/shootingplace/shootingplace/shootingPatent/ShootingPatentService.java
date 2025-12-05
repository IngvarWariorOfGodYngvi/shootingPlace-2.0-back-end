package com.shootingplace.shootingplace.shootingPatent;

import com.shootingplace.shootingplace.history.HistoryService;
import com.shootingplace.shootingplace.member.MemberDTO;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.utils.Mapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ShootingPatentService {

    private final ShootingPatentRepository shootingPatentRepository;
    private final MemberRepository memberRepository;
    private final HistoryService historyService;
    private final Logger LOG = LogManager.getLogger(getClass());


    public ShootingPatentService(ShootingPatentRepository shootingPatentRepository,
                                 MemberRepository memberRepository, HistoryService historyService) {
        this.shootingPatentRepository = shootingPatentRepository;
        this.memberRepository = memberRepository;
        this.historyService = historyService;
    }

    public ResponseEntity<?> updatePatent(String memberUUID, ShootingPatent shootingPatent) {
        if (!memberRepository.existsById(memberUUID)) {
            return ResponseEntity.badRequest().body("Nie znaleziono klubowicza");
        }
        MemberEntity memberEntity = memberRepository.getOne(memberUUID);
        ShootingPatentEntity shootingPatentEntity = memberEntity.getShootingPatent();
        if (shootingPatent.getPatentNumber() != null && !shootingPatent.getPatentNumber().isEmpty()) {

            boolean match = memberRepository.findAllByErasedFalse()
                    .stream()
                    .filter(f -> f.getShootingPatent().getPatentNumber() != null && !f.getShootingPatent().getPatentNumber().contains("null"))
                    .anyMatch(f -> f.getShootingPatent().getPatentNumber().equals(shootingPatent.getPatentNumber()));

            if (match && !shootingPatentEntity.getPatentNumber().equals(shootingPatent.getPatentNumber())) {
                LOG.error("ktoś już ma taki numer patentu");
                return ResponseEntity.badRequest().body("ktoś już ma taki numer patentu");
            } else {
                shootingPatentEntity.setPatentNumber(shootingPatent.getPatentNumber());
                LOG.info("Wprowadzono numer patentu");
            }
        }
        if (shootingPatent.getPistolPermission() != null) {
            if (shootingPatent.getPistolPermission().equals(true)) {
                shootingPatentEntity.setPistolPermission(shootingPatent.getPistolPermission());
                LOG.info("Dodano dyscyplinę : Pistolet");
            }
        }
        if (shootingPatent.getRiflePermission() != null) {
            if (shootingPatent.getRiflePermission().equals(true)) {
                shootingPatentEntity.setRiflePermission(shootingPatent.getRiflePermission());
                LOG.info("Dodano dyscyplinę : Karabin");
            }
        }
        if (shootingPatent.getShotgunPermission() != null) {
            if (shootingPatent.getShotgunPermission().equals(true)) {
                shootingPatentEntity.setShotgunPermission(shootingPatent.getShotgunPermission());
                LOG.info("Dodano dyscyplinę : Strzelba");
            }
        }
        if (shootingPatent.getDateOfPosting() != null) {
            LOG.info("ustawiono datę przyznania patentu na : " + shootingPatent.getDateOfPosting());
            shootingPatentEntity.setDateOfPosting(shootingPatent.getDateOfPosting());

        }
        shootingPatentRepository.save(shootingPatentEntity);
        LOG.info("Zaktualizowano patent");
        historyService.updateShootingPatentHistory(memberUUID, shootingPatent);
        return ResponseEntity.ok("Zaktualizowano patent " + memberEntity.getSecondName() + " " + memberEntity.getFirstName());
    }

    public ShootingPatent getShootingPatent() {
        return ShootingPatent.builder()
                .patentNumber(null)
                .dateOfPosting(null)
                .pistolPermission(false)
                .riflePermission(false)
                .shotgunPermission(false)
                .build();
    }

    public List<MemberDTO> getMembersWithNoShootingPatent() {
        return memberRepository.findAllWhereClubEquals1ErasedFalsePzssTrue().stream()
                .filter(f -> f.getShootingPatent() != null && f.getShootingPatent().getPatentNumber() == null)
                .map(Mapping::map2DTO)
                .sorted(Comparator.comparing(MemberDTO::getSecondName, pl()).thenComparing(MemberDTO::getFirstName, pl()))
                .collect(Collectors.toList());
    }

    private static Collator pl() {
        return Collator.getInstance(Locale.forLanguageTag("pl"));
    }

    public List<MemberDTO> getMembersWithShootingPatentAndNoLicense() {
        return memberRepository.findAllWhereClubEquals1ErasedFalsePzssTrue().stream()
                .filter(f -> f.getShootingPatent() != null && f.getShootingPatent().getPatentNumber() != null)
                .filter(f->f.getLicense() != null && f.getLicense().getNumber() == null)
                .map(Mapping::map2DTO)
                .sorted(Comparator.comparing(MemberDTO::getSecondName, pl()).thenComparing(MemberDTO::getFirstName, pl()))
                .collect(Collectors.toList());
    }
}