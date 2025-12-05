package com.shootingplace.shootingplace.armory;

import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceRepository;
import com.shootingplace.shootingplace.ammoEvidence.AmmoInEvidenceEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoInEvidenceRepository;
import com.shootingplace.shootingplace.armory.gunRepresentation.GunRepresentationRepository;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.file.FilesRepository;
import com.shootingplace.shootingplace.history.HistoryService;
import com.shootingplace.shootingplace.history.UsedHistoryEntity;
import com.shootingplace.shootingplace.history.UsedHistoryRepository;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import com.shootingplace.shootingplace.utils.Mapping;
import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArmoryService {

    private final AmmoEvidenceRepository ammoEvidenceRepository;
    private final CaliberService caliberService;
    private final CaliberRepository caliberRepository;
    private final CaliberUsedRepository caliberUsedRepository;
    private final CalibersAddedRepository calibersAddedRepository;
    private final GunRepository gunRepository;
    private final GunStoreRepository gunStoreRepository;
    private final FilesRepository filesRepository;
    private final UsedHistoryRepository usedHistoryRepository;
    private final HistoryService historyService;
    private final UserRepository userRepository;
    private final AmmoInEvidenceRepository ammoInEvidenceRepository;
    private final MemberRepository memberRepository;

    private final Logger LOG = LogManager.getLogger();
    private final GunUsedRepository gunUsedRepository;
    private final GunRepresentationRepository gunRepresentationRepository;


    public ArmoryService(AmmoEvidenceRepository ammoEvidenceRepository, CaliberService caliberService, CaliberRepository caliberRepository, CaliberUsedRepository caliberUsedRepository, CalibersAddedRepository calibersAddedRepository, GunRepository gunRepository, GunStoreRepository gunStoreRepository, FilesRepository filesRepository, UsedHistoryRepository usedHistoryRepository, HistoryService historyService, UserRepository userRepository, AmmoInEvidenceRepository ammoInEvidenceRepository, MemberRepository memberRepository, GunUsedRepository gunUsedRepository, GunRepresentationRepository gunRepresentationRepository) {
        this.ammoEvidenceRepository = ammoEvidenceRepository;
        this.caliberService = caliberService;
        this.caliberRepository = caliberRepository;
        this.caliberUsedRepository = caliberUsedRepository;
        this.calibersAddedRepository = calibersAddedRepository;
        this.gunRepository = gunRepository;
        this.gunStoreRepository = gunStoreRepository;
        this.filesRepository = filesRepository;
        this.usedHistoryRepository = usedHistoryRepository;
        this.historyService = historyService;
        this.userRepository = userRepository;
        this.ammoInEvidenceRepository = ammoInEvidenceRepository;
        this.memberRepository = memberRepository;
        this.gunUsedRepository = gunUsedRepository;
        this.gunRepresentationRepository = gunRepresentationRepository;
    }

    public List<Caliber> getSumFromAllAmmoList(LocalDate firstDate, LocalDate secondDate) {
        List<Caliber> list = caliberService.getCalibersEntityList().stream().map(Mapping::map).collect(Collectors.toList());
        List<Caliber> list1 = new ArrayList<>();
        list.forEach(e -> ammoEvidenceRepository.findAll().stream()
                .filter(f -> f.getDate().isAfter(firstDate.minusDays(1)))
                .filter(f -> f.getDate().isBefore(secondDate.plusDays(1)))
                .forEach(g -> g.getAmmoInEvidenceEntityList()
                        .stream().filter(f -> f.getCaliberName().equals(e.getName()))
                        .forEach(h -> {
                            Caliber caliber = list.stream()
                                    .filter(f -> f.getName().equals(e.getName())).findFirst().orElseThrow(EntityNotFoundException::new);
                            if (caliber.getQuantity() == null) {
                                caliber.setQuantity(0);
                            }
                            if (list1.stream().anyMatch(f -> f.getName().equals(caliber.getName()))) {
                                Caliber caliber1 = list1.stream().filter(f -> f.getName().equals(caliber.getName())).findFirst().orElseThrow(EntityNotFoundException::new);
                                caliber.setQuantity(caliber1.getQuantity() + h.getQuantity());
                            } else {
                                caliber.setQuantity(h.getQuantity());
                                list1.add(caliber);
                            }
                        })
                ));
        return list1;
    }

    public ResponseEntity<?> updateAmmo(String caliberUUID, Integer count, LocalDate date, LocalTime time, String description, String imageUUID, String pinCode) throws NoUserPermissionException {
        String code = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity user = userRepository.findByPinCode(code);
        if (!user.getUserPermissionsList().contains(UserSubType.WEAPONS_WAREHOUSEMAN.getName())) {
            filesRepository.deleteById(imageUUID);
            return ResponseEntity.badRequest().body("Brak uprawnień");
        }

        CaliberEntity caliberEntity = caliberRepository.getOne(caliberUUID);
        if (caliberEntity.getQuantity() == null) {
            caliberEntity.setQuantity(0);
        }
        int caliberAmmoInStore = caliberService.getCaliberAmmoInStore(caliberUUID);
        CalibersAddedEntity calibersAddedEntity = CalibersAddedEntity.builder()
                .addedBy(user.getFullName())
                .imageUUID(imageUUID)
                .ammoAdded(count)
                .belongTo(caliberUUID)
                .caliberName(caliberEntity.getName())
                .date(date)
                .time(time)
                .description(description)
                .stateForAddedDay(caliberAmmoInStore)
                .finalStateForAddedDay(caliberAmmoInStore + count)
                .build();
        calibersAddedRepository.save(calibersAddedEntity);

        List<CalibersAddedEntity> ammoAdded = caliberEntity.getAmmoAdded();
        ammoAdded.add(calibersAddedEntity);
        if (caliberEntity.getQuantity() == null) {
            caliberEntity.setQuantity(0);
        }
        caliberEntity.setQuantity(caliberEntity.getQuantity() + calibersAddedEntity.getAmmoAdded());
        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, caliberEntity, HttpStatus.OK, "Dodano amunicję do kaibru " + caliberEntity.getName(), "Dodano amunicję do kaibru " + caliberEntity.getName());
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            caliberRepository.save(caliberEntity);
        }
        return response;
    }

    public void substratAmmo(String caliberUUID, Integer quantity) {
        CaliberEntity caliberEntity = caliberRepository.getOne(caliberUUID);
        CaliberUsedEntity caliberUsedEntity = CaliberUsedEntity.builder()
                .date(LocalDate.now())
                .time(LocalTime.now())
                .belongTo(caliberUUID)
                .ammoUsed(quantity)
                .unitPrice(caliberEntity.getUnitPrice())
                .build();
        caliberUsedRepository.save(caliberUsedEntity);
        List<CaliberUsedEntity> ammoUsed = caliberEntity.getAmmoUsed();
        ammoUsed.add(caliberUsedEntity);
        caliberEntity.setAmmoUsed(ammoUsed);
        caliberRepository.save(caliberEntity);

    }

    public List<CalibersAddedEntity> getHistoryOfCaliber(String caliberUUID) {
        return caliberRepository.getOne(caliberUUID)
                .getAmmoAdded()
                .stream()
                .sorted(Comparator.comparing(CalibersAddedEntity::getDate))
                .collect(Collectors.toList());
    }

    public ResponseEntity<?> addGunEntity(AddGunImageWrapper addGunImageWrapper, String imageUUID, String pinCode) {
        Gun gun = addGunImageWrapper.getGun();
        String code = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity user = userRepository.findByPinCode(code);
        if ((gun.getModelName().isEmpty() || gun.getModelName().equals("null")) || (gun.getCaliber().isEmpty() || gun.getCaliber().equals("null")) || (gun.getGunType().isEmpty() || gun.getGunType().equals("null")) || (gun.getSerialNumber().isEmpty() || gun.getSerialNumber().equals("null"))) {
            LOG.info("Nie podano wszystkich informacji");
            return ResponseEntity.badRequest().body("Nie podano wszystkich informacji");
        }
        if (gun.getProductionYear() == null || gun.getProductionYear().isEmpty() || gun.getProductionYear().equals("null")) {
            gun.setProductionYear(null);
        }
        if (gun.getAdditionalEquipment() == null || gun.getAdditionalEquipment().isEmpty() || gun.getAdditionalEquipment().equals("null")) {
            gun.setAdditionalEquipment(null);
        }
        if (gun.getComment() == null || gun.getComment().isEmpty() || gun.getComment().equals("null")) {
            gun.setComment(null);
        }
        if (gun.getBarcode() == null || gun.getBarcode().isEmpty() || gun.getBarcode().equals("null")) {
            gun.setBarcode(null);
        }
        if (gun.getGunCertificateSerialNumber() == null || gun.getGunCertificateSerialNumber().isEmpty() || gun.getGunCertificateSerialNumber().equals("null")) {
            gun.setGunCertificateSerialNumber(null);
        }
        if (gun.getRecordInEvidenceBook() == null || gun.getRecordInEvidenceBook().isEmpty() || gun.getRecordInEvidenceBook().equals("null")) {
            gun.setRecordInEvidenceBook(null);
        }

        List<GunEntity> all = gunRepository.findAll();

        if (gun.getGunCertificateSerialNumber() != null) {
            if (all.stream().filter(f -> f.getGunCertificateSerialNumber() != null).anyMatch(f -> f.getGunCertificateSerialNumber().equals(gun.getGunCertificateSerialNumber()))) {
                LOG.info("Nie można dodać broni. Upewnij się, że numer seryjny, numer świadectwa lub numer z książki ewidencji się nie powtarza");
                return ResponseEntity.badRequest().body("Nie można dodać broni. Upewnij się, że numer seryjny, numer świadectwa lub numer z książki ewidencji się nie powtarza");
            }
        }
        if (gun.getSerialNumber() != null) {
            if (all.stream().filter(f -> f.getSerialNumber() != null).anyMatch(e -> e.getSerialNumber().equals(gun.getSerialNumber()))) {
                LOG.info("Nie można dodać broni. Upewnij się, że numer seryjny, numer świadectwa lub numer z książki ewidencji się nie powtarza");
                return ResponseEntity.badRequest().body("Nie można dodać broni. Upewnij się, że numer seryjny, numer świadectwa lub numer z książki ewidencji się nie powtarza");
            }
        }
        if (gun.getRecordInEvidenceBook() != null) {
            if (all.stream().filter(f -> f.getRecordInEvidenceBook() != null).anyMatch(e -> e.getRecordInEvidenceBook().equals(gun.getRecordInEvidenceBook()))) {
                LOG.info("Nie można dodać broni. Upewnij się, że numer seryjny, numer świadectwa lub numer z książki ewidencji się nie powtarza");
                return ResponseEntity.badRequest().body("Nie można dodać broni. Upewnij się, że numer seryjny, numer świadectwa lub numer z książki ewidencji się nie powtarza");
            }
        }
        if (gun.getBarcode() != null) {
            if (all.stream().filter(f -> f.getBarcode() != null).anyMatch(e -> e.getBarcode().equals(gun.getBarcode()))) {
                LOG.info("Nie można Nadać kodu kreskowego gdyż jest przypisany już gdzieś indziej");
                return ResponseEntity.badRequest().body("Nie można Nadać kodu kreskowego gdyż jest przypisany już gdzieś indziej");
            }
        }

        String s = null;
        if (gun.getGunCertificateSerialNumber() != null) {
            s = gun.getGunCertificateSerialNumber().toUpperCase();
        }

        GunEntity gunEntity = GunEntity.builder()
                .modelName(gun.getModelName().toUpperCase())
                .caliber(gun.getCaliber())
                .gunType(gun.getGunType())
                .serialNumber(gun.getSerialNumber().toUpperCase())
                .productionYear(gun.getProductionYear())
                .numberOfMagazines(gun.getNumberOfMagazines())
                .gunCertificateSerialNumber(s)
                .additionalEquipment(gun.getAdditionalEquipment())
                .recordInEvidenceBook(gun.getRecordInEvidenceBook())
                .comment(gun.getComment())
                .basisForPurchaseOrAssignment(gun.getBasisForPurchaseOrAssignment())
                .barcode(gun.getBarcode())
                .addedDate(gun.getAddedDate() != null ? gun.getAddedDate() : LocalDate.now())
                .available(true)
                .inStock(true).build();

        gunEntity.setAddedSign(imageUUID);
        gunEntity.setAddedBy(user.getFullName());
        gunEntity.setAddedUserUUID(user.getUuid());
        GunEntity save = gunRepository.save(gunEntity);
        GunStoreEntity gunStoreEntity = gunStoreRepository.findByTypeName(gunEntity.getGunType());
        List<GunEntity> gunEntityList = gunStoreEntity.getGunEntityList();
        gunEntityList.add(gunEntity);

        gunStoreRepository.save(gunStoreEntity);
        System.out.println(save.getAddedBy());
        return ResponseEntity.ok("Dodano broń");


    }

    public List<GunStoreEntity> getGunTypeList() {
        return gunStoreRepository.findAll().stream().map(m -> GunStoreEntity.builder().uuid(m.getUuid()).gunEntityList(null).typeName(m.getTypeName()).build())
                .sorted(Comparator.comparing(GunStoreEntity::getTypeName)).collect(Collectors.toList());
    }

    public List<?> getAllGuns() {
        List<GunStoreEntity> all = gunStoreRepository.findAll();
        all.sort(Comparator.comparing(GunStoreEntity::getTypeName));
        List<GunStoreDTO> all1 = new ArrayList<>();
        all.forEach(e -> {
            GunStoreDTO dto = new GunStoreDTO();
            dto.setTypeName(e.getTypeName());
            List<Gun> collect = e.getGunEntityList()
                    .stream()
                    .filter(GunEntity::isInStock)
                    .map(Mapping::map)
                    .sorted(Comparator.comparing(Gun::getModelName).reversed())
                    .collect(Collectors.toList());

            dto.setGunList(collect);
            all1.add(dto);
        });
        return all1;
    }
    public void a() {
        gunStoreRepository.findAll().forEach(e->{
            e.setRemovedGunEntityList(new ArrayList<>());
            gunStoreRepository.save(e);
        });
    }
    public void b() {
        List<GunStoreEntity> all = gunStoreRepository.findAll();
        gunRepository.findAll().forEach(e -> {
            GunStoreEntity gunStoreEntity = all.stream().filter(f -> f.getTypeName().equals(e.getGunType())).findFirst().orElse(null);
            if (!e.isInStock() && gunStoreEntity != null) {
                List<GunEntity> gunEntityList = gunStoreEntity.getRemovedGunEntityList();
                gunEntityList.add(e);
                System.out.println("usuwam");
                gunStoreEntity.setRemovedGunEntityList(gunEntityList);
                gunStoreRepository.save(gunStoreEntity);
            }
        });
    }
    public List<?> getAllRemovedGuns() {
//        a();
//        b();
        List<GunStoreEntity> all = gunStoreRepository.findAll();
        all.sort(Comparator.comparing(GunStoreEntity::getTypeName));
        List<GunStoreDTO> all1 = new ArrayList<>();
        all.forEach(e -> {
            GunStoreDTO dto = new GunStoreDTO();
            dto.setTypeName(e.getTypeName());
            List<Gun> collect = e.getRemovedGunEntityList()
                    .stream()
                    .map(Mapping::map)
                    .sorted(Comparator.comparing(Gun::getModelName).reversed())
                    .collect(Collectors.toList());
            dto.setGunRemovedList(collect);
            all1.add(dto);
        });
        return all1;
    }

    public ResponseEntity<?> editGunEntity(Gun gun) {
        GunEntity gunEntity = gunRepository.getOne(gun.getUuid());
        if (gunEntity == null) {
            return ResponseEntity.badRequest().body("Nie udało się zaktualizować broni");
        }
        if (gun.getModelName() != null && !gun.getModelName().isEmpty()) {
            gunEntity.setModelName(gun.getModelName());
        }
        if (gun.getCaliber() != null && !gun.getCaliber().isEmpty()) {
            gunEntity.setCaliber(gun.getCaliber());
        }
        if (gun.getGunType() != null && !gun.getGunType().isEmpty()) {
            gunEntity.setGunType(gun.getGunType());
        }
        if (gun.getSerialNumber() != null && !gun.getSerialNumber().isEmpty()) {
            gunEntity.setSerialNumber(gun.getSerialNumber());
        }
        if (gun.getProductionYear() != null && !gun.getProductionYear().isEmpty()) {
            gunEntity.setProductionYear(gun.getProductionYear());
        }
        if (gun.getNumberOfMagazines() != null && !gun.getNumberOfMagazines().isEmpty()) {
            gunEntity.setNumberOfMagazines(gun.getNumberOfMagazines());
        }
        if (gun.getGunCertificateSerialNumber() != null && !gun.getGunCertificateSerialNumber().isEmpty()) {
            gunEntity.setGunCertificateSerialNumber(gun.getGunCertificateSerialNumber());
        }
        if (gun.getAdditionalEquipment() != null && !gun.getAdditionalEquipment().isEmpty()) {
            gunEntity.setAdditionalEquipment(gun.getAdditionalEquipment());
        }
        if (gun.getRecordInEvidenceBook() != null && !gun.getRecordInEvidenceBook().isEmpty()) {
            gunEntity.setRecordInEvidenceBook(gun.getRecordInEvidenceBook());
        }
        if (gun.getComment() != null && !gun.getComment().isEmpty()) {
            gunEntity.setComment(gun.getComment());
        }
        if (gun.getBasisForPurchaseOrAssignment() != null && !gun.getBasisForPurchaseOrAssignment().isEmpty()) {
            gunEntity.setBasisForPurchaseOrAssignment(gun.getBasisForPurchaseOrAssignment());
        }
        if (gun.getBarcode() != null && !gun.getBarcode().isEmpty()) {
            gunEntity.setBarcode(gun.getBarcode());
        }
        if (gun.getAddedDate() != null && gunEntity.getAddedDate() != gun.getAddedDate()) {
            gunEntity.setAddedDate(gun.getAddedDate());
        }
        gunRepository.save(gunEntity);
        return ResponseEntity.ok("Zaktualizowano broń");
    }

    public ResponseEntity<?> removeGun(String gunUUID,String basisOfRemoved, String pinCode, String imageUUID) throws NoUserPermissionException {

        GunEntity gunEntity = gunRepository.getOne(gunUUID);
        if (gunEntity == null) {
            return ResponseEntity.badRequest().body("Nie ma takie Broni");
        }
        String code = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity user = userRepository.findByPinCode(code);
        gunEntity.setInStock(false);
        gunEntity.setBasisOfRemoved(basisOfRemoved);
        gunEntity.setRemovedBy(user.getFullName());
        gunEntity.setRemovedSign(imageUUID);
        gunEntity.setRemovedUserUUID(user.getUuid());
        GunStoreEntity gunStoreEntity = gunStoreRepository.findAll().stream().filter(f -> f.getTypeName().equals(gunEntity.getGunType())).findFirst().orElseThrow(EntityNotFoundException::new);
        // Very Important
        changeList(gunEntity, gunStoreEntity);

        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, gunEntity, HttpStatus.OK, "removeGun", "Usunięto broń ze stanu magazynu");
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            gunStoreRepository.save(gunStoreEntity);
        }
        return response;
    }

    private void changeList(GunEntity gunEntity, GunStoreEntity gunStoreEntity) {
        List<GunEntity> gunEntityList = gunStoreEntity.getGunEntityList();
        gunEntityList.remove(gunEntity);
        List<GunEntity> removedGunEntityList = gunStoreEntity.getRemovedGunEntityList();
        removedGunEntityList.add(gunEntity);
    }

    public ResponseEntity<?> createNewGunStore(String nameType) {
        GunStoreEntity gunStoreEntity = gunStoreRepository.findAll().stream().filter(f -> f.getTypeName().equals(nameType)).findFirst().orElse(null);
        if (gunStoreEntity == null) {
            List<GunEntity> collect = gunRepository.findAll().stream().filter(f -> f.getGunType().equals(nameType)).collect(Collectors.toList());

            String[] s1 = nameType.split(" ");
            StringBuilder name = new StringBuilder();
            for (String value : s1) {
                String splinted = value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase() + " ";
                name.append(splinted);
            }

            GunStoreEntity build = GunStoreEntity.builder()
                    .typeName(name.toString().trim())
                    .gunEntityList(collect)
                    .build();
            LOG.info("dodaję nowy rodzaj broni");
            gunStoreRepository.save(build);
            return ResponseEntity.ok("dodaję nowy rodzaj broni");
        } else {
            LOG.info("nie dodaję nowego rodzaju broni");
            return ResponseEntity.badRequest().body("nie dodaję nowego rodzaju broni");
        }
    }

    public ResponseEntity<?> findGunByBarcode(String barcode) {

        if (gunRepository.findByBarcode(barcode).isEmpty()) {
            return ResponseEntity.badRequest().body("Nie znaleziono broni");
        }

        GunEntity gunEntity = gunRepository.findByBarcode(barcode).orElseThrow(EntityNotFoundException::new);

        return ResponseEntity.ok(gunEntity);
    }

    public void addUseToGun(String gunUUID, String gunUsedUUID) {
        GunEntity gunEntity = gunRepository.getOne(gunUUID);
        GunUsedEntity gunUsedEntity = gunUsedRepository.getOne(gunUsedUUID);
        if (gunEntity == null) {
            LOG.info("Nie znaleziono broni");
        }
        if (gunEntity != null && gunEntity.getGunUsedList() == null) {
            gunEntity.setGunUsedList(new ArrayList<>());
        }
        assert gunEntity != null;
        List<GunUsedEntity> gunUsedList = gunEntity.getGunUsedList();
        gunUsedList.add(gunUsedEntity);
        gunRepository.save(gunEntity);
    }

    public List<UsedHistoryEntity> getGunUsedHistory(String gunUUID) {

        return usedHistoryRepository.findAll().stream().filter(f -> f.getGunUUID().equals(gunUUID)).sorted(Comparator.comparing(UsedHistoryEntity::getDateTime).reversed()).collect(Collectors.toList());

    }

    public ResponseEntity<?> addGunToList(List<String> gunUUIDList, LocalDate date, LocalTime time) {
        List<GunEntity> entities = new ArrayList<>();
        gunUUIDList.forEach(e -> entities.add(gunRepository.getOne(e)));
        List<String> response = new ArrayList<>();
        entities.forEach(e -> {
            if (e == null) {
                response.add("Nie ma takiej broni");
            } else {
                if (gunUsedRepository.findAll().stream().anyMatch(a -> a.getGunUUID().equals(e.getUuid()) && a.getIssuanceDate().equals(date) && a.getAcceptanceSign() == null)) {
                    response.add("Broń już znajduje się na liście " + e.getModelName() + " " + e.getSerialNumber());
                } else {
                    GunUsedEntity build = GunUsedEntity.builder()
                            .gunRepresentationEntity(Mapping.mapToRepresentation(e))
                            .usedDate(LocalDate.now())
                            .usedTime(LocalTime.now())
                            .issuanceDate(date)
                            .issuanceTime(time)
                            .gunUUID(e.getUuid())
                            .build();
                    gunUsedRepository.save(build);
                    LOG.info("Dodano użycie Broni " + e.getModelName() + " " + e.getSerialNumber());
                    response.add("Dodano użycie Broni " + e.getModelName() + " " + e.getSerialNumber());
                }
            }
        });
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> getGun(String gunUUID) {
        return ResponseEntity.ok(gunRepository.getOne(gunUUID));
    }

    public ResponseEntity<?> changeCaliberUnitPrice(String caliberUUID, Float price, String pinCode) throws NoUserPermissionException {
        CaliberEntity caliberEntity = caliberRepository.getOne(caliberUUID);
        caliberEntity.setUnitPrice(price);
        return historyService.getStringResponseEntity(pinCode, caliberEntity, HttpStatus.OK, "changeCaliberUnitPrice " + caliberEntity.getName(), "zmieniono cenę amunicji");

    }

    public ResponseEntity<?> changeCaliberUnitPriceForNotMember(String caliberUUID, Float price, String pinCode) throws NoUserPermissionException {
        CaliberEntity caliberEntity = caliberRepository.getOne(caliberUUID);
        caliberEntity.setUnitPriceForNotMember(price);
        return historyService.getStringResponseEntity(pinCode, caliberEntity, HttpStatus.OK, "changeCaliberUnitPriceForNotMember " + caliberEntity.getName(), "zmieniono cenę amunicji dla pozostałych");

    }

    public ResponseEntity<?> signUpkeepAmmo(String ammoInEvidenceUUID, String imageUUID, String pinCode) throws NoUserPermissionException {
        AmmoInEvidenceEntity a = ammoInEvidenceRepository.getOne(ammoInEvidenceUUID);
        String code = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity user = userRepository.findByPinCode(code);
        if (!user.getUserPermissionsList().contains(UserSubType.WEAPONS_WAREHOUSEMAN.getName())) {
            filesRepository.deleteById(imageUUID);
            return ResponseEntity.badRequest().body("Brak uprawnień");
        }
        if (a.isLocked()) {
            return ResponseEntity.badRequest().body("Już zatwierdzono");
        }
        a.setImageUUID(imageUUID);
        a.setSignedBy(user.getFullName());
        a.setSignedDate(a.getDateTime().toLocalDate());
        a.setSignedTime(LocalTime.parse(a.getDateTime().toLocalTime().toString().split("\\.")[0]));
        a.lock();
        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, a, HttpStatus.OK, "signUpkeepAmmo", "zabokowano listę z kalibrem " + a.getCaliberName());

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            LOG.info("zapisuję");
            ammoInEvidenceRepository.save(a);
        }
        AmmoEvidenceEntity one = ammoEvidenceRepository.getOne(a.getEvidenceUUID());

        List<Boolean> b = new ArrayList<>();

        one.getAmmoInEvidenceEntityList().forEach(e -> b.add(e.isLocked()));
        if (!b.contains(false)) {
            LOG.info("blokuję całą listę");
            one.lockEvidence();
            ammoEvidenceRepository.save(one);
        }

        return response;
    }

    public List<GunUsedDTO> getAllGunUsedIssuance() {
        return gunUsedRepository.findAll().stream().filter(f -> (f.getIssuanceSign() == null || f.getGunTakerSign() == null) && (f.getAcceptanceSign() == null && f.getGunReturnerSign() == null)).map(m -> {
            GunUsedDTO dto = Mapping.map(m);
            dto.setGun(Mapping.map(gunRepository.getOne(m.getGunUUID())));
            dto.setGunRepresentation(gunRepresentationRepository.getOne(m.getGunRepresentationEntity().getUuid()));
            return dto;
        }).sorted(Comparator.comparing(GunUsedDTO::getIssuanceDate).thenComparing(GunUsedDTO::getIssuanceTime).reversed()).collect(Collectors.toList());
    }

    public List<GunUsedDTO> getAllGunUsedAcceptance() {
        return gunUsedRepository.findAll().stream().filter(f -> (f.getIssuanceSign() != null && f.getGunTakerSign() != null) && (f.getAcceptanceSign() == null || f.getGunReturnerSign() == null)).map(m -> {
            GunUsedDTO map = Mapping.map(m);
            map.setGun(Mapping.map(gunRepository.getOne(m.getGunUUID())));
            return map;
        }).sorted(Comparator.comparing(GunUsedDTO::getIssuanceDate).thenComparing(GunUsedDTO::getIssuanceTime).reversed()).collect(Collectors.toList());
    }

    public List<GunUsedDTO> getAllGunUsed(LocalDate firstDate, LocalDate secondDate) {
        return gunUsedRepository.findAllAcceptanceDayBeetween(firstDate, secondDate).stream().filter(f -> f.getAcceptanceSign() != null && f.getGunReturnerName() != null && f.getIssuanceSign() != null && f.getGunTakerSign() != null).map(m -> {
            GunUsedDTO map = Mapping.map(m);
            map.setGun(Mapping.map(gunRepository.getOne(m.getGunUUID())));
            return map;
        }).sorted(Comparator.comparing(GunUsedDTO::getAcceptanceDate).thenComparing(GunUsedDTO::getAcceptanceTime).reversed()).collect(Collectors.toList());
    }

    public List<Gun> getGunList() {

        return gunRepository.findAll().stream().filter(GunEntity::isInStock).map(Mapping::map).sorted(Comparator.comparing(Gun::getModelName)).collect(Collectors.toList());

    }

    public List<GunUsedDTO> getGunUsedListAmmoList() {
        return gunUsedRepository.findAll().stream().filter(f -> f.getAcceptanceSign() == null).sorted(Comparator.comparing(GunUsedEntity::getIssuanceDate).thenComparing(GunUsedEntity::getIssuanceTime).reversed()).map(m -> {
            GunUsedDTO map = Mapping.map(m);
            map.setGun(Mapping.map(gunRepository.getOne(m.getGunUUID())));
            return map;
        }).collect(Collectors.toList());
    }

    public ResponseEntity<?> signIssuanceGun(String gunUsedUUID, String imageUUID, LocalDate date, LocalTime time, String pinCode) {
        GunUsedEntity used = gunUsedRepository.getOne(gunUsedUUID);
        String code = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity user = userRepository.findByPinCode(code);
        if (!user.getUserPermissionsList().contains(UserSubType.WEAPONS_WAREHOUSEMAN.getName())) {
            filesRepository.deleteById(imageUUID);
            return ResponseEntity.badRequest().body("Brak uprawnień");
        }
        GunEntity one = gunRepository.getOne(used.getGunUUID());
        used.setIssuanceBy(user.getFullName());
        used.setIssuanceSign(imageUUID);
        used.setIssuanceDate(date);
        used.setIssuanceTime(time);
        gunUsedRepository.save(used);
        LOG.info("podpisano wydanie broni: " + one.getModelName() + " " + one.getSerialNumber());
        return ResponseEntity.ok("podpisano wydanie broni: " + one.getModelName() + " " + one.getSerialNumber());
    }

    public ResponseEntity<?> signTakerGun(String gunUsedUUID, String imageUUID, Integer memberLeg) {
        GunUsedEntity used = gunUsedRepository.getOne(gunUsedUUID);
        GunEntity one = gunRepository.getOne(used.getGunUUID());
        MemberEntity one1 = memberRepository.findByLegitimationNumber(memberLeg).orElseThrow(EntityNotFoundException::new);
        used.setGunTakerName(one1.getFullName());
        used.setGunTakerSign(imageUUID);
        gunUsedRepository.save(used);
        LOG.info("podpisano przyjęcie broni: " + one.getModelName() + " " + one.getSerialNumber() + " przez: " + one1.getFullName());
        return ResponseEntity.ok("podpisano przyjęcie broni: " + one.getModelName() + " " + one.getSerialNumber());
    }

    public ResponseEntity<?> signReturnerGun(String gunUsedUUID, String imageUUID, Integer memberLeg) {
        GunUsedEntity used = gunUsedRepository.getOne(gunUsedUUID);
        GunEntity one = gunRepository.getOne(used.getGunUUID());
        MemberEntity one1 = memberRepository.findByLegitimationNumber(memberLeg).orElseThrow(EntityNotFoundException::new);
        used.setGunReturnerName(one1.getFullName());
        used.setGunReturnerSign(imageUUID);
        gunUsedRepository.save(used);
        LOG.info("podpisano zdanie broni: " + one.getModelName() + " " + one.getSerialNumber() + " przez: " + one1.getFullName());
        return ResponseEntity.ok("podpisano zdanie broni: " + one.getModelName() + " " + one.getSerialNumber());
    }

    public ResponseEntity<?> signAcceptanceGun(String gunUsedUUID, String imageUUID, LocalDate date, LocalTime time, String pinCode) {
        GunUsedEntity used = gunUsedRepository.getOne(gunUsedUUID);
        String code = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity user = userRepository.findByPinCode(code);
        if (!user.getUserPermissionsList().contains(UserSubType.WEAPONS_WAREHOUSEMAN.getName())) {
            filesRepository.deleteById(imageUUID);
            return ResponseEntity.badRequest().body("Brak uprawnień");
        }
        GunEntity one = gunRepository.getOne(used.getGunUUID());
        used.setAcceptanceBy(user.getFullName());
        used.setAcceptanceSign(imageUUID);
        used.setAcceptanceDate(date);
        used.setAcceptanceTime(time);
        GunUsedEntity save = gunUsedRepository.save(used);
        addUseToGun(one.getUuid(), save.getUuid());
        LOG.info("podpisano przyjęcie broni: " + one.getModelName() + " " + one.getSerialNumber());
        return ResponseEntity.ok("podpisano przyjęcie broni: " + one.getModelName() + " " + one.getSerialNumber());
    }

    public ResponseEntity<?> getGunUsedByUUID(String gunUsedUUID) {
        GunUsedEntity one = gunUsedRepository.getOne(gunUsedUUID);
        GunUsedDTO map = Mapping.map(one);
        map.setGun(Mapping.map(gunRepository.getOne(one.getGunUUID())));
        return ResponseEntity.ok(map);
    }

    public ResponseEntity<?> addGunSign(String gunUUID, String imageUUID, String pinCode) {
        GunEntity one = gunRepository.getOne(gunUUID);
        String code = Hashing.sha256().hashString(pinCode, StandardCharsets.UTF_8).toString();
        UserEntity user = userRepository.findByPinCode(code);
        if (!user.getUserPermissionsList().contains(UserSubType.WEAPONS_WAREHOUSEMAN.getName())) {
            filesRepository.deleteById(imageUUID);
            return ResponseEntity.badRequest().body("Brak uprawnień");
        }
        one.setAddedSign(imageUUID);
        one.setAddedBy(user.getFullName());
        one.setAddedUserUUID(user.getUuid());
        gunRepository.save(one);
        LOG.info("podpisano przyjęcie na stan broni: " + one.getModelName() + " " + one.getSerialNumber());
        return ResponseEntity.ok("podpisano przyjęcie broni: " + one.getModelName() + " " + one.getSerialNumber());

    }

}
