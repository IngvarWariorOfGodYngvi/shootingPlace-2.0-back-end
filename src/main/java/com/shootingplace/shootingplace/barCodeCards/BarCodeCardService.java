package com.shootingplace.shootingplace.barCodeCards;

import com.shootingplace.shootingplace.domain.Person;
import com.shootingplace.shootingplace.history.HistoryEntityType;
import com.shootingplace.shootingplace.changeHistory.RecordHistory;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.security.UserAuthContext;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import com.shootingplace.shootingplace.utils.Mapping;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BarCodeCardService {

    private final BarCodeCardRepository barCodeCardRepo;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final UserAuthContext userAuthContext;
    private final Logger LOG = LogManager.getLogger();

    @RecordHistory(action = "BarcodeCard.create", entity = HistoryEntityType.BARCODE_CARD)
    public ResponseEntity<?> createNewCard(String barCode, String uuid) {
        UserEntity user = userAuthContext.get();
        if (user == null) {
            throw new IllegalStateException("Brak użytkownika w kontekście");
        }
        if (barCodeCardRepo.existsByBarCode(barCode)) {
            return ResponseEntity.badRequest().body("Taki numer jest już do kogoś przypisany - użyj innej karty");
        }
        if (barCode.isBlank()) {
            return ResponseEntity.badRequest().body("Nie podano numeru Karty");
        }
        Person person = null;
        List<BarCodeCardEntity> barCodeCardList;
        if (userRepository.existsById(uuid)) {
            user = userRepository.findById(uuid).orElseThrow(EntityNotFoundException::new);
            List<BarCodeCardEntity> allByBelongsTo = barCodeCardRepo.findAllByBelongsTo(user.getUuid());
            int size = (int) allByBelongsTo.stream().filter(f -> f.getActivatedDay().getMonthValue() == LocalDate.now().getMonthValue()).count();
            if (size > 2) {
                return ResponseEntity.badRequest().body("Nie można dodać więcej kart do " + user.getFirstName());
            }
            BarCodeCardEntity build = BarCodeCardEntity.builder().barCode(barCode).belongsTo(user.getUuid()).isActive(true).isMaster(true).activatedDay(LocalDate.now()).type("User").build();
            BarCodeCardEntity save = barCodeCardRepo.save(build);
            barCodeCardList = user.getBarCodeCardList();
            barCodeCardList.add(save);
            user.setBarCodeCardList(barCodeCardList);
            userRepository.save(user);
            person = user;
            return ResponseEntity.ok("Zapisano numer i przypisano do: " + person.getFirstName() + " " + person.getSecondName());
        }
        if (memberRepository.existsById(uuid)) {
            MemberEntity memberEntity = memberRepository.findById(uuid).orElseThrow(EntityNotFoundException::new);
            List<BarCodeCardEntity> allByBelongsTo = barCodeCardRepo.findAllByBelongsTo(user.getUuid());
            int size = (int) allByBelongsTo.stream().filter(f -> f.getActivatedDay().getMonthValue() == LocalDate.now().getMonthValue()).count();
            if (size > 2) {
                return ResponseEntity.badRequest().body("Nie można dodać więcej kart do " + user.getFirstName());
            }
            BarCodeCardEntity build = BarCodeCardEntity.builder().barCode(barCode).belongsTo(memberEntity.getUuid()).isActive(true).isMaster(true).activatedDay(LocalDate.now()).type("Member").build();
            BarCodeCardEntity save = barCodeCardRepo.save(build);
            barCodeCardList = memberEntity.getBarCodeCardList();
            barCodeCardList.add(save);
            memberEntity.setBarCodeCardList(barCodeCardList);
            memberRepository.save(memberEntity);
            person = memberEntity;
        }
        if (person != null) {
            return ResponseEntity.ok("Zapisano numer i przypisano do: " + person.getFirstName() + " " + person.getSecondName());
        } else {
            return ResponseEntity.badRequest().body("coś się nie udało");
        }
    }

    public ResponseEntity<?> findMemberByCard(String cardNumber) {
        BarCodeCardEntity barCodeCardEntity = barCodeCardRepo.findByBarCode(cardNumber);
        if (barCodeCardEntity == null) {
            return ResponseEntity.badRequest().body("brak takiego numeru karty");
        }
        String belongsTo = barCodeCardEntity.getBelongsTo();
        MemberEntity member;
        UserEntity userEntity = userRepository.findById(belongsTo).orElseThrow(EntityNotFoundException::new);
        if (userEntity != null) {
            member = userEntity.getMember();
        } else {
            member = memberRepository.findById(belongsTo).orElseThrow(EntityNotFoundException::new);
        }
        if (member != null) {
            return ResponseEntity.ok(Mapping.map1(member));
        } else {
            return ResponseEntity.badRequest().body(null);
        }

    }

    @RecordHistory(action = "BarcodeCard.deactivate", entity = HistoryEntityType.BARCODE_CARD)
    public ResponseEntity<?> deactivateCard(String barCode) {
        BarCodeCardEntity card = barCodeCardRepo.findByBarCode(barCode);
        if (card == null) {
            return ResponseEntity.badRequest().body("Brak numeru Karty");
        }
        card.setActive(false);
        barCodeCardRepo.save(card);
        LOG.info("Dezaktywowano kartę: {}", barCode);
        return ResponseEntity.ok("Dezaktywowano Kartę Klubowicza");
    }

}
