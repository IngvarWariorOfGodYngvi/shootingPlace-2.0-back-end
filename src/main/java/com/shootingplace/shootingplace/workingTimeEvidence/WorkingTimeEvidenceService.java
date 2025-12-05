package com.shootingplace.shootingplace.workingTimeEvidence;

import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.barCodeCards.BarCodeCardEntity;
import com.shootingplace.shootingplace.barCodeCards.BarCodeCardRepository;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.file.FilesEntity;
import com.shootingplace.shootingplace.file.FilesRepository;
import com.shootingplace.shootingplace.file.IFile;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import com.shootingplace.shootingplace.utils.Mapping;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class WorkingTimeEvidenceService {
    private final UserRepository userRepository;
    private final WorkingTimeEvidenceRepository workRepo;
    private final BarCodeCardRepository barCodeCardRepo;
    private final FilesRepository filesRepo;

    private static final Logger log = LoggerFactory.getLogger(WorkingTimeEvidenceService.class);

    public WorkingTimeEvidenceService(UserRepository userRepository, WorkingTimeEvidenceRepository workRepo, BarCodeCardRepository barCodeCardRepo, FilesRepository filesRepo) {
        this.userRepository = userRepository;
        this.workRepo = workRepo;
        this.barCodeCardRepo = barCodeCardRepo;
        this.filesRepo = filesRepo;
    }

    public ResponseEntity<?> createNewWTE(String number) {
        String msg;
        BarCodeCardEntity barCode = barCodeCardRepo.findByBarCode(number);
        if (barCode == null) {
            return ResponseEntity.badRequest().body("Brak takiego numeru");
        }
        if (!barCode.isActive()) {
            msg = "Karta jest nieaktywna i nie można jej użyć ponownie";
            log.info(msg);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(msg);
        }
        String belongsTo = barCode.getBelongsTo();

        // szukam osoby do której należy karta
        UserEntity user = userRepository.getOne(belongsTo);

        // biorę wszystkie niezamknięte wiersze z obecnego miesiąca gdzie występuje osoba
        WorkingTimeEvidenceEntity entity1 = workRepo.findAll()
                .stream()
                .filter(f -> !f.isClose())
                .filter(f -> f.getStart().getMonth().equals(LocalDateTime.now().getMonth()))
                .filter(f -> f.getUser().equals(user))
                .findFirst().orElse(null);

        if (entity1 != null) {
            return ResponseEntity.ok(closeWTE(entity1, false, barCode));
        } else {
            return ResponseEntity.ok(openWTE(barCode, number));
        }
    }

    public String countTime(LocalDateTime start, LocalDateTime stop) {

        Duration duration = Duration.between(start, stop);

        long totalSeconds = Math.abs(duration.getSeconds());

        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


    public List<String> getAllUsersInWork() {

        List<WorkingTimeEvidenceEntity> list = workRepo.findAll().stream().filter(f -> !f.isClose()).collect(Collectors.toList());
        List<String> list1 = new ArrayList<>();
        list.forEach(e -> list1.add(e.getUser().getFirstName() + " " + e.getUser().getSecondName()));

        return list1;
    }

    public void closeAllActiveWorkTime() {
        log.info("zamykam to co jest otwarte");
        List<WorkingTimeEvidenceEntity> allByCloseFalse = workRepo.findAllByIsCloseFalse();
        allByCloseFalse
                .stream()
                .filter(f -> !f.isClose())
                .collect(Collectors.toList())
                .forEach(e ->
                {
                    String s = countTime(e.getStart(), LocalDateTime.now());
                    BarCodeCardEntity barCode = barCodeCardRepo.findByBarCode(e.getCardNumber());
                    if (Integer.parseInt(s.substring(0, 2)) > 6) {
                        closeWTE(e, true, barCode);
                    }

                });

    }


    public LocalDateTime getTime(LocalDateTime time, boolean in) {
        LocalTime temp = time.toLocalTime();
        if (in) {
            if (time.getMinute() < 8) {
                temp = LocalTime.of(time.getHour(), 0);
            }
            if (time.getMinute() >= 8 && time.getMinute() <= 15) {
                temp = LocalTime.of(time.getHour(), 15);
            }
            if (time.getMinute() > 15 && time.getMinute() <= 23) {
                temp = LocalTime.of(time.getHour(), 15);
            }
            if (time.getMinute() > 23 && time.getMinute() <= 30) {
                temp = LocalTime.of(time.getHour(), 30);
            }
            if (time.getMinute() > 30 && time.getMinute() <= 38) {
                temp = LocalTime.of(time.getHour(), 30);
            }
            if (time.getMinute() > 38 && time.getMinute() <= 45) {
                temp = LocalTime.of(time.getHour(), 45);
            }
            if (time.getMinute() > 45 && time.getMinute() <= 53) {
                temp = LocalTime.of(time.getHour(), 45);
            }
            if (time.getMinute() > 53) {
                if (time.getHour() + 1 > 23) {
                    temp = LocalTime.of(22 + 1, 0);

                } else {
                    temp = LocalTime.of(time.getHour() + 1, 0);

                }
            }

        } else {
            if (time.getMinute() < 8) {
                temp = LocalTime.of(time.getHour(), 0);
            }
            if (time.getMinute() >= 8) {
                temp = LocalTime.of(time.getHour(), 15);
            }
            if (time.getMinute() >= 20 && time.getMinute() <= 30) {
                temp = LocalTime.of(time.getHour(), 30);
            }
            if (time.getMinute() > 30 && time.getMinute() <= 38) {
                temp = LocalTime.of(time.getHour(), 30);
            }
            if (time.getMinute() > 38 && time.getMinute() <= 45) {
                temp = LocalTime.of(time.getHour(), 45);
            }
            if (time.getMinute() > 45 && time.getMinute() <= 50) {
                temp = LocalTime.of(time.getHour(), 45);
            }
            if (time.getMinute() > 50) {
                temp = LocalTime.of(time.getHour() + 1, 0);
            }

        }

        return LocalDateTime.of(time.toLocalDate(), temp);
    }

    public List<UserWithWorkingTimeList> getAllWorkingTimeEvidenceInMonth(int year, String month) {

        month = (month == null || month.toLowerCase(Locale.ROOT).equals("null"))
                ? LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("LLLL", Locale.forLanguageTag("pl")))
                .toLowerCase(Locale.ROOT)
                : month.toLowerCase(Locale.ROOT);
//        workType = workType == null || workType.equals("null") ? null : workType;
        List<UserWithWorkingTimeList> userWithWorkingTimeListList = new ArrayList<>();
        String finalMonth = month;
        List<WorkingTimeEvidenceEntity> pl1;
//        if (workType != null) {
//            pl1 = workRepo.findAll()
//                    .stream()
//                    .filter(WorkingTimeEvidenceEntity::isClose)
//                    .filter(f -> f.getStart() != null && f.getStop() != null)
//                    .filter(f -> f.getStart().getYear() == year)
//                    .filter(f -> f.getStart().getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.forLanguageTag("pl")).equals(finalMonth.toLowerCase(Locale.ROOT)))
//                    .collect(Collectors.toList());
//        } else {
        pl1 = workRepo.findAll()
                .stream()
                .filter(WorkingTimeEvidenceEntity::isClose)
                .filter(f -> f.getStart() != null && f.getStop() != null)
                .filter(f -> f.getStart().getYear() == year)
                .filter(f -> {
                    String monthPl = f.getStart()
                            .format(java.time.format.DateTimeFormatter.ofPattern("LLLL", Locale.forLanguageTag("pl")))
                            .toLowerCase(Locale.ROOT);
                    return monthPl.equals(finalMonth.toLowerCase(Locale.ROOT));
                })
                .toList();

//        }
        AtomicInteger workSumHours = new AtomicInteger();
        AtomicInteger workSumMinutes = new AtomicInteger();
        AtomicReference<String> format = new AtomicReference<>("");
        Set<UserEntity> user = new HashSet<>();

        pl1.forEach(e -> user.add(e.getUser()));

        user.forEach(e -> {
            List<WorkingTimeEvidenceDTO> pl2 = pl1.stream()
                    .filter(f -> f.getUser().equals(e))
                    .map(Mapping::map)
                    .sorted(Comparator.comparing(WorkingTimeEvidenceDTO::getStart).reversed())
                    .collect(Collectors.toList());

            for (WorkingTimeEvidenceDTO g : pl2) {

                LocalDateTime start = getTime(g.getStart(), true);
                LocalDateTime stop = getTime(g.getStop(), false);
                String workTime = countTime(start, stop);

                int workTimeSumHours;
                int workTimeSumMinutes;


                workTimeSumHours = sumIntFromString(workTime, 0, 2);
                workTimeSumMinutes = sumIntFromString(workTime, 3, 5);
                workSumHours.getAndAdd(workTimeSumHours);
                workSumMinutes.getAndAdd(workTimeSumMinutes);

            }
            int acquire = workSumMinutes.getAcquire() % 60;
            int acquire1 = workSumMinutes.getAcquire() / 60;
            workSumHours.getAndAdd(acquire1);
            format.set(String.format("%02d:%02d",
                    workSumHours.getAcquire(), acquire));

            UserWithWorkingTimeList build = UserWithWorkingTimeList.builder()
                    .uuid(e.getUuid())
                    .firstName(e.getFirstName())
                    .secondName(e.getSecondName())
                    .subType(e.getUserPermissionsList().toString())
                    .WTEdtoList(pl2)
                    .workTime(String.valueOf(format))
                    .build();
            userWithWorkingTimeListList.add(build);
            workSumHours.set(0);
            workSumMinutes.set(0);
            format.set(String.format("0%2d:%02d", 0, 0));
        });
        userWithWorkingTimeListList.sort(Comparator.comparing(UserWithWorkingTimeList::getSecondName)
                .thenComparing(UserWithWorkingTimeList::getFirstName)
                .reversed());
        return userWithWorkingTimeListList;

    }

    private Integer sumIntFromString(String sequence, int substringStart, int substringEnd) {
        return Integer.parseInt(sequence.substring(substringStart, substringEnd));

    }

    public ResponseEntity<?> acceptWorkingTime(List<String> uuidList, String pinCode) {

        if (uuidList.isEmpty()) {
            return ResponseEntity.badRequest().body("Lista jest pusta - wybierz elementy do zmiany");
        }
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity userEntity = userRepository.findAll()
                .stream()
                .filter(f -> f.getUserPermissionsList().contains(UserSubType.CEO.getName()))
                .filter(f -> f.getPinCode().equals(pin))
                .findFirst()
                .orElse(null);

        if (userEntity != null && userEntity.getUserPermissionsList().contains(UserSubType.CEO.getName())) {
            List<WorkingTimeEvidenceEntity> list = new ArrayList<>();
            uuidList.forEach(e -> list.add(workRepo.findById(e).orElseThrow(EntityNotFoundException::new)));
            list.forEach(e -> e.setAccepted(true));

            list.forEach(workRepo::save);
            return ResponseEntity.ok("Zatwierdzono czas pracy");
        }
        return ResponseEntity.badRequest().body("Pin jest niezgodny - tylko Prezes");
    }

    public ResponseEntity<?> inputChangesToWorkTime(List<WorkingTimeEvidenceDTO> list, String pinCode) {

        if (list.isEmpty()) {
            return ResponseEntity.badRequest().body("Lista jest pusta - wybierz elementy do zmiany");
        }
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity userEntity = userRepository.findByPinCode(pin);

        if (userEntity != null && userEntity.getUserPermissionsList().contains(UserSubType.CEO.getName())) {
            String month = list.get(0)
                    .getStart()
                    .format(java.time.format.DateTimeFormatter.ofPattern("LLLL", Locale.forLanguageTag("pl")))
                    .toLowerCase(Locale.ROOT);
            String workType = workRepo.getOne(list.get(0).getUuid()).getWorkType();
            int yearValue = list.get(0).getStart().getYear();
            String finalMonth = month.toLowerCase();
            List<IFile> collect = filesRepo.findAllByNameContains("%" + month.toLowerCase() + "%", "%" + yearValue + "%");
            if (!collect.isEmpty()) {
                collect.forEach(e -> {
                    FilesEntity one = filesRepo.getOne(e.getUuid());
                    one.setName("raport_pracy_" + finalMonth + "_" + (e.getVersion()) + "_" + yearValue + "_" + workType + ".pdf");
                    filesRepo.save(one);
                });
            }

            list.forEach(e -> {
                String s = countTime(e.getStart(), e.getStop());
                int i = Integer.parseInt(s.substring(0, 2));

                WorkingTimeEvidenceEntity entity = workRepo.findById(e.getUuid()).orElseThrow(EntityNotFoundException::new);
                if (i > 8) {
                    entity.setToClarify(true);
                }

                entity.setStart(e.getStart());
                entity.setStop(e.getStop());
                entity.setWorkTime(countTime(getTime(e.getStart(), true), getTime(e.getStop(), false)));
                workRepo.save(entity);
            });
            log.info("Zatwierdzono zmiany w czasie pracy");
            return ResponseEntity.ok("Zatwierdzono zmiany w czasie pracy");
        }
        return ResponseEntity.badRequest().body("Pin jest niezgodny - tylko Prezes");
    }

    public ResponseEntity<?> getAllWorkingType() {


        List<String> list = new ArrayList<>();
        list.add(UserSubType.MANAGEMENT.getName());
        list.add(UserSubType.WORKER.getName());
        list.add(UserSubType.REVISION_COMMITTEE.getName());
        list.add(UserSubType.VISITOR.getName());


        return ResponseEntity.ok(list);
    }

    public boolean isInWork(UserEntity userEntity) {
        return workRepo.findAll().stream().filter(f -> !f.isClose()).anyMatch(e -> e.getUser().equals(userEntity));
    }

    public ResponseEntity<?> getAllWorkingYear() {
        return ResponseEntity.ok(workRepo.findAll().stream().map(e -> e.getStart().getYear()).distinct().sorted().collect(Collectors.toList()));
    }

    public ResponseEntity<?> getAllWorkingMonthInYear(Integer year) {
        List<Integer> collect = workRepo.findAll()
                .stream()
                .filter(f -> f.getStop() != null)
                .filter(f -> f.getStop().getYear() == year)
                .map(e -> e.getStop().getMonth().getValue())
                .distinct()
                .sorted().toList();
        return ResponseEntity.ok(
                collect.stream()
                        .map(e -> java.time.Month.of(e)
                                .getValue())
                        .map(m -> java.time.LocalDate.of(2000, m, 1)
                                .format(java.time.format.DateTimeFormatter.ofPattern("LLLL", Locale.forLanguageTag("pl"))))
                        .toList()
        );
    }

    public ResponseEntity<?> getAllWorkingTypeInMonthAndYear(String year, String month) {
        return ResponseEntity.ok(workRepo.findAllByStopQuery(Integer.parseInt(year), number(month))
                .stream().map(WorkingTimeEvidenceEntity::getWorkType).distinct().collect(Collectors.toList()));
    }

    private int number(String finalMonth) {
        int pl = 0;
        switch (finalMonth) {
            case "styczeń":
                pl = 1;
                break;
            case "luty":
                pl = 2;
                break;
            case "marzec":
                pl = 3;
                break;
            case "kwiecień":
                pl = 4;
                break;
            case "maj":
                pl = 5;
                break;
            case "czerwiec":
                pl = 6;
                break;
            case "lipiec":
                pl = 7;
                break;
            case "sierpień":
                pl = 8;
                break;
            case "wrzesień":
                pl = 9;
                break;
            case "październik":
                pl = 10;
                break;
            case "listopad":
                pl = 11;
                break;
            case "grudzień":
                pl = 12;
                break;
        }
        return pl;
    }

    public ResponseEntity<?> createNewWTEByPin(String pinCode) {
        String msg;
        String pin = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity user = userRepository.findByPinCode(pin);
        if (user == null) {
            msg = "Brak użytkownika";
            log.info(msg);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(msg);
        }

        WorkingTimeEvidenceEntity entity1 = workRepo.findAll()
                .stream()
                .filter(f -> !f.isClose())
                .filter(f -> f.getStart().getMonth().equals(LocalDateTime.now().getMonth()))
                .filter(f -> f.getUser().equals(user))
                .findFirst().orElse(null);

        if (entity1 != null) {
            return ResponseEntity.ok("użutkownik znajduje się już na liście");
        } else {
            msg = openWTEByUser(user);
            return ResponseEntity.ok(msg);
        }
    }

    String openWTE(BarCodeCardEntity barCode, String number) {
        String msg;
        String belongsTo = barCode.getBelongsTo();
        UserEntity user = userRepository.getOne(belongsTo);
        if (user == null) {
            msg = "nie znaleziono osoby o tym numerze karty";
        } else {
            LocalDateTime start = LocalDateTime.now();

            WorkingTimeEvidenceEntity entity = WorkingTimeEvidenceEntity.builder()
                    .start(start)
                    .stop(null)
                    .cardNumber(number)
                    .isClose(false)
                    .user(user).build();

            workRepo.save(entity);
            msg = user.getFirstName() + " " + user.getSecondName() + " Witaj w Pracy";
        }
        barCode.addCountUse();
        barCodeCardRepo.save(barCode);
        log.info(msg);
        return msg;
    }

    public String openWTEByUser(UserEntity user) {
        String msg;
        if (user == null) {
            msg = "nie znaleziono osoby";
        } else {
            if (workRepo.findAllByIsCloseFalse().stream().noneMatch(f -> f.getUser().equals(user))) {
                LocalDateTime start = LocalDateTime.now();
                BarCodeCardEntity barCodeCardEntity = barCodeCardRepo.findAllByBelongsTo(user.getUuid()).get(0);
                WorkingTimeEvidenceEntity entity = WorkingTimeEvidenceEntity.builder()
                        .start(start)
                        .stop(null)
                        .cardNumber(barCodeCardEntity.getBarCode())
                        .isClose(false)
                        .user(user).build();

                workRepo.save(entity);
                msg = user.getFirstName() + " " + user.getSecondName() + " Witaj w Pracy";
                barCodeCardEntity.addCountUse();
                barCodeCardRepo.save(barCodeCardEntity);
            } else {
                msg = "Użytkownik jest już w pracy";
            }
        }
        log.info(msg);
        return msg;
    }

    String closeWTE(WorkingTimeEvidenceEntity entity, boolean auto, BarCodeCardEntity barCode) {
        String msg;

        LocalDateTime stop = LocalDateTime.now();
        entity.setAutomatedClosed(false);
        if (auto) {
            if (entity.getStart().getHour() > 20) {
                stop = entity.getStart().plusMinutes(1);
            } else {
                stop = LocalDateTime.of(entity.getStart().getYear(), entity.getStart().getMonth(), entity.getStart().getDayOfMonth(), 20, 0);
            }
            entity.setAutomatedClosed(true);
        }
        if (!barCode.isMaster()) {
            barCode.setActive(false);
            barCodeCardRepo.save(barCode);
        }

        LocalDateTime temp = getTime(entity.getStart(), true);
        LocalDateTime temp1 = getTime(stop, false);

        entity.setStop(stop);
        String s = countTime(temp, temp1);
        int i = Integer.parseInt(s.substring(0, 2));

        if (i > 8) {
            entity.setToClarify(true);
        }
        if (i > 24) {
            i = i % 24;
            int j = Integer.parseInt(s.substring(3, 5));
            int k = Integer.parseInt(s.substring(6));

            s = String.format("%02d:%02d:%02d", i, j, k);
        }

        entity.setWorkTime(s);
        entity.closeWTE();
        workRepo.save(entity);
        msg = entity.getUser().getFullName() + " Opuścił Pracę";
        log.info(msg);
        return msg;
    }
}
