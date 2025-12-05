package com.shootingplace.shootingplace.barCodeCards;

import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.domain.Person;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.HistoryService;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import com.shootingplace.shootingplace.utils.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class BarCodeCardService {

    private final BarCodeCardRepository barCodeCardRepo;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final HistoryService historyService;

    private static final Logger log = LoggerFactory.getLogger(BarCodeCardService.class);

    public BarCodeCardService(BarCodeCardRepository barCodeCardRepo, UserRepository userRepository, MemberRepository memberRepository, HistoryService historyService) {
        this.barCodeCardRepo = barCodeCardRepo;
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.historyService = historyService;
    }

    public ResponseEntity<?> createNewCard(String barCode, String uuid, String pinCode) throws NoUserPermissionException {
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity userEntity = userRepository.findByPinCode(pin);
        List<String> acceptedPermissions = Arrays.asList(UserSubType.ADMIN.getName(), UserSubType.SUPER_USER.getName(), UserSubType.MANAGEMENT.getName(), UserSubType.WORKER.getName());
        if (userEntity == null) {
            return ResponseEntity.badRequest().body("brak Użytkownika");
        }
        if (barCodeCardRepo.existsByBarCode(barCode)) {
            return ResponseEntity.badRequest().body("Taki numer jest już do kogoś przypisany - użyj innej karty");
        }
        if (barCode.isEmpty() || barCode.isBlank()) {
            return ResponseEntity.badRequest().body("Nie podano numeru Karty");
        }
        if (userEntity.getUserPermissionsList().stream().noneMatch(acceptedPermissions::contains)) {
            throw new NoUserPermissionException();
        }
        Person person = null;
        if (userRepository.existsById(uuid)) {
            userEntity = userRepository.getOne(uuid);
            List<BarCodeCardEntity> barCodeCardList;
            List<BarCodeCardEntity> allByBelongsTo = barCodeCardRepo.findAllByBelongsTo(userEntity.getUuid());
            int size = (int) allByBelongsTo.stream().filter(f -> f.getActivatedDay().getMonthValue() == LocalDate.now().getMonthValue()).count();
            if (size > 2) {
                return ResponseEntity.badRequest().body("Nie można dodać więcej kart do " + userEntity.getFirstName());
            }
            BarCodeCardEntity build = BarCodeCardEntity.builder()
                    .barCode(barCode)
                    .belongsTo(userEntity.getUuid())
                    .isActive(true)
                    .isMaster(true)
                    .activatedDay(LocalDate.now())
                    .type("User")
                    .build();
            BarCodeCardEntity save = barCodeCardRepo.save(build);
            barCodeCardList = userEntity.getBarCodeCardList();
            barCodeCardList.add(save);
            userEntity.setBarCodeCardList(barCodeCardList);
            userRepository.save(userEntity);
            person = userEntity;
            return ResponseEntity.ok("Zapisano numer i przypisano do: " + person.getFirstName() + " " + person.getSecondName());
        }
        if (memberRepository.existsById(uuid)) {
            MemberEntity memberEntity = memberRepository.getOne(uuid);
            List<BarCodeCardEntity> barCodeCardList;
            List<BarCodeCardEntity> allByBelongsTo = barCodeCardRepo.findAllByBelongsTo(userEntity.getUuid());
            int size = (int) allByBelongsTo.stream().filter(f -> f.getActivatedDay().getMonthValue() == LocalDate.now().getMonthValue()).count();
            if (size > 2) {
                return ResponseEntity.badRequest().body("Nie można dodać więcej kart do " + userEntity.getFirstName());
            }
            BarCodeCardEntity build = BarCodeCardEntity.builder()
                    .barCode(barCode)
                    .belongsTo(memberEntity.getUuid())
                    .isActive(true)
                    .isMaster(true)
                    .activatedDay(LocalDate.now())
                    .type("Member")
                    .build();
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
        // szukam u userów
        UserEntity userEntity = userRepository.getOne(belongsTo);
        if (userEntity != null) {
            member = userEntity.getMember();
        } else {
            // szukam u memberów
            member = memberRepository.getOne(belongsTo);
        }
        if (member != null) {
            return ResponseEntity.ok(Mapping.map1(member));
        } else {
            return ResponseEntity.badRequest().body(null);
        }

    }

    public ResponseEntity<?> deactivateCard(String barCode, String pinCode) throws NoUserPermissionException {

        BarCodeCardEntity card = barCodeCardRepo.findByBarCode(barCode);
        if (card != null) {
            card.setActive(false);
            ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, card, HttpStatus.OK, "Dezaktywacja karty", "Dezaktywowano Kartę Klubowicza");
            if (response.getStatusCode().equals(HttpStatus.OK)) {
                barCodeCardRepo.save(card);
            }
            return response;
        }
        return ResponseEntity.badRequest().body("Brak numeru Karty");
    }
}
