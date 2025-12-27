package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceRepository;
import com.shootingplace.shootingplace.ammoEvidence.AmmoInEvidenceEntity;
import com.shootingplace.shootingplace.ammoEvidence.AmmoInEvidenceRepository;
import com.shootingplace.shootingplace.armory.gunRepresentation.GunRepresentationRepository;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.file.FilesRepository;
import com.shootingplace.shootingplace.history.HistoryEntityType;
import com.shootingplace.shootingplace.history.changeHistory.RecordHistory;
import com.shootingplace.shootingplace.history.UsedHistoryEntity;
import com.shootingplace.shootingplace.history.UsedHistoryRepository;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.security.UserAuthContext;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.utils.Mapping;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
    private final AmmoInEvidenceRepository ammoInEvidenceRepository;
    private final MemberRepository memberRepository;
    private final UserAuthContext userAuthContext;
    private final Logger LOG = LogManager.getLogger();
    private final GunUsedRepository gunUsedRepository;
    private final GunRepresentationRepository gunRepresentationRepository;

    public List<Caliber> getSumFromAllAmmoList(LocalDate firstDate, LocalDate secondDate) {
        List<Caliber> list = caliberService.getCalibersEntityList().stream().map(Mapping::map).toList();
        List<Caliber> list1 = new ArrayList<>();
        list.forEach(e -> ammoEvidenceRepository.findAll().stream().filter(f -> f.getDate().isAfter(firstDate.minusDays(1))).filter(f -> f.getDate().isBefore(secondDate.plusDays(1))).forEach(g -> g.getAmmoInEvidenceEntityList().stream().filter(f -> f.getCaliberName().equals(e.getName())).forEach(h -> {
            Caliber caliber = list.stream().filter(f -> f.getName().equals(e.getName())).findFirst().orElseThrow(EntityNotFoundException::new);
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
        })));
        return list1;
    }

    @Transactional
    @RecordHistory(action = "AMMO.updateAmmo", entity = HistoryEntityType.AMMO, entityArgIndex = 0)
    public ResponseEntity<?> updateAmmo(String caliberUUID, Integer count, LocalDate date, LocalTime time, String description, String imageUUID) {
        UserEntity user = userAuthContext.get();
        if (user == null) {
            throw new IllegalStateException("Brak użytkownika w kontekście");
        }
        CaliberEntity caliber = caliberRepository.findById(caliberUUID).orElseThrow(EntityNotFoundException::new);
        int currentAmmo = caliberService.getCaliberAmmoInStore(caliberUUID);
        CalibersAddedEntity added = CalibersAddedEntity.builder().addedBy(user.getFullName()).imageUUID(imageUUID).ammoAdded(count).belongTo(caliberUUID).caliberName(caliber.getName()).date(date).time(time).description(description).stateForAddedDay(currentAmmo).finalStateForAddedDay(currentAmmo + count).build();
        calibersAddedRepository.save(added);
        caliber.setQuantity((caliber.getQuantity() == null ? 0 : caliber.getQuantity()) + count);
        caliberRepository.save(caliber);
        LOG.info("Dodano {} szt. amunicji do kalibru {} przez {}", count, caliber.getName(), user.getFullName());
        return ResponseEntity.ok("Dodano amunicję do kalibru " + caliber.getName());
    }


    public void substratAmmo(String caliberUUID, Integer quantity) {
        CaliberEntity caliberEntity = caliberRepository.findById(caliberUUID).orElseThrow(EntityNotFoundException::new);
        CaliberUsedEntity caliberUsedEntity = CaliberUsedEntity.builder().date(LocalDate.now()).time(LocalTime.now()).belongTo(caliberUUID).ammoUsed(quantity).unitPrice(caliberEntity.getUnitPrice()).build();
        caliberUsedRepository.save(caliberUsedEntity);
        List<CaliberUsedEntity> ammoUsed = caliberEntity.getAmmoUsed();
        ammoUsed.add(caliberUsedEntity);
        caliberEntity.setAmmoUsed(ammoUsed);
        caliberRepository.save(caliberEntity);

    }

    public List<CalibersAddedEntity> getHistoryOfCaliber(String caliberUUID) {
        return caliberRepository.findById(caliberUUID).orElseThrow(EntityNotFoundException::new).getAmmoAdded().stream().sorted(Comparator.comparing(CalibersAddedEntity::getDate)).collect(Collectors.toList());
    }

    public ResponseEntity<?> addGunEntity(AddGunImageWrapper addGunImageWrapper, String imageUUID) {
        Gun gun = addGunImageWrapper.getGun();
        UserEntity user = userAuthContext.get();
        if (user == null) {
            throw new IllegalStateException("Brak użytkownika w kontekście");
        }
        if (isBlank(gun.getModelName()) || isBlank(gun.getCaliber()) || isBlank(gun.getGunType()) || isBlank(gun.getSerialNumber())) {

            LOG.info("Nie podano wszystkich informacji");
            return ResponseEntity.badRequest().body("Nie podano wszystkich informacji");
        }
        gun.setProductionYear(normalizeNullable(gun.getProductionYear()));
        gun.setAdditionalEquipment(normalizeNullable(gun.getAdditionalEquipment()));
        gun.setComment(normalizeNullable(gun.getComment()));
        gun.setBarcode(normalizeNullable(gun.getBarcode()));
        gun.setGunCertificateSerialNumber(normalizeNullable(gun.getGunCertificateSerialNumber()));
        gun.setRecordInEvidenceBook(normalizeNullable(gun.getRecordInEvidenceBook()));

        if (gun.getGunCertificateSerialNumber() != null && gunRepository.existsByGunCertificateSerialNumber(gun.getGunCertificateSerialNumber())) {

            return ResponseEntity.badRequest().body("Nie można dodać broni – numer świadectwa już istnieje");
        }
        if (gunRepository.existsBySerialNumber(gun.getSerialNumber())) {
            return ResponseEntity.badRequest().body("Nie można dodać broni – numer seryjny już istnieje");
        }
        if (gun.getRecordInEvidenceBook() != null && gunRepository.existsByRecordInEvidenceBook(gun.getRecordInEvidenceBook())) {
            return ResponseEntity.badRequest().body("Nie można dodać broni – numer z książki ewidencji już istnieje");
        }
        if (gun.getBarcode() != null && gunRepository.existsByBarcode(gun.getBarcode())) {
            return ResponseEntity.badRequest().body("Nie można nadać kodu kreskowego – jest już przypisany");
        }
        GunEntity gunEntity = GunEntity.builder().modelName(gun.getModelName().toUpperCase()).caliber(gun.getCaliber()).gunType(gun.getGunType()).serialNumber(gun.getSerialNumber().toUpperCase()).productionYear(gun.getProductionYear()).numberOfMagazines(gun.getNumberOfMagazines()).gunCertificateSerialNumber(gun.getGunCertificateSerialNumber() != null ? gun.getGunCertificateSerialNumber().toUpperCase() : null).additionalEquipment(gun.getAdditionalEquipment()).recordInEvidenceBook(gun.getRecordInEvidenceBook()).comment(gun.getComment()).basisForPurchaseOrAssignment(gun.getBasisForPurchaseOrAssignment()).barcode(gun.getBarcode()).addedDate(gun.getAddedDate() != null ? gun.getAddedDate() : LocalDate.now()).available(true).inStock(true).build();
        gunEntity.setAddedSign(imageUUID);
        gunEntity.setAddedBy(user.getFullName());
        gunEntity.setAddedUserUUID(user.getUuid());
        GunEntity savedGun = gunRepository.save(gunEntity);
        GunStoreEntity gunStoreEntity = gunStoreRepository.findByTypeName(savedGun.getGunType());
        gunStoreEntity.getGunEntityList().add(savedGun);
        gunStoreRepository.save(gunStoreEntity);
        LOG.info("Dodano broń: {}", savedGun.getModelName());
        return ResponseEntity.ok("Dodano broń");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank() || value.equalsIgnoreCase("null");
    }

    private String normalizeNullable(String value) {
        return isBlank(value) ? null : value;
    }

    public List<GunStoreEntity> getGunTypeList() {
        return gunStoreRepository.findAll().stream().map(m -> GunStoreEntity.builder().uuid(m.getUuid()).gunEntityList(null).typeName(m.getTypeName()).build()).sorted(Comparator.comparing(GunStoreEntity::getTypeName)).collect(Collectors.toList());
    }

    public List<?> getAllGuns() {
        List<GunStoreEntity> all = gunStoreRepository.findAll();
        all.sort(Comparator.comparing(GunStoreEntity::getTypeName));
        List<GunStoreDTO> all1 = new ArrayList<>();
        all.forEach(e -> {
            GunStoreDTO dto = new GunStoreDTO();
            dto.setTypeName(e.getTypeName());
            List<Gun> collect = e.getGunEntityList().stream().filter(GunEntity::isInStock).map(Mapping::map).sorted(Comparator.comparing(Gun::getModelName).reversed()).collect(Collectors.toList());

            dto.setGunList(collect);
            all1.add(dto);
        });
        return all1;
    }

    public void a() {
        gunStoreRepository.findAll().forEach(e -> {
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
            List<Gun> collect = e.getRemovedGunEntityList().stream().map(Mapping::map).sorted(Comparator.comparing(Gun::getModelName).reversed()).collect(Collectors.toList());
            dto.setGunRemovedList(collect);
            all1.add(dto);
        });
        return all1;
    }

    public ResponseEntity<?> editGunEntity(Gun gun) {
        GunEntity gunEntity = gunRepository.findById(gun.getUuid()).orElseThrow(EntityNotFoundException::new);
        updateIfPresent(gun.getModelName(), gunEntity::setModelName);
        updateIfPresent(gun.getCaliber(), gunEntity::setCaliber);
        updateIfPresent(gun.getGunType(), gunEntity::setGunType);
        updateIfPresent(gun.getSerialNumber(), gunEntity::setSerialNumber);
        updateIfPresent(gun.getProductionYear(), gunEntity::setProductionYear);
        updateIfPresent(gun.getNumberOfMagazines(), gunEntity::setNumberOfMagazines);
        updateIfPresent(gun.getGunCertificateSerialNumber(), gunEntity::setGunCertificateSerialNumber);
        updateIfPresent(gun.getAdditionalEquipment(), gunEntity::setAdditionalEquipment);
        updateIfPresent(gun.getRecordInEvidenceBook(), gunEntity::setRecordInEvidenceBook);
        updateIfPresent(gun.getComment(), gunEntity::setComment);
        updateIfPresent(gun.getBasisForPurchaseOrAssignment(), gunEntity::setBasisForPurchaseOrAssignment);
        updateIfPresent(gun.getBarcode(), gunEntity::setBarcode);
        if (gun.getAddedDate() != null && !gun.getAddedDate().equals(gunEntity.getAddedDate())) {
            gunEntity.setAddedDate(gun.getAddedDate());
        }
        gunRepository.save(gunEntity);
        return ResponseEntity.ok("Zaktualizowano broń");
    }

    private void updateIfPresent(String value, Consumer<String> setter) {
        if (value != null && !value.isBlank()) {
            setter.accept(value);
        }
    }


    @Transactional
    @RecordHistory(action = "Gun.remove", entity = HistoryEntityType.GUN, entityArgIndex = 0)
    public ResponseEntity<?> removeGun(String gunUUID, String basisOfRemoved, String imageUUID) {

        GunEntity gun = gunRepository.findById(gunUUID).orElseThrow(() -> new EntityNotFoundException("Nie ma takiej broni"));
        UserEntity user = userAuthContext.get();
        if (user == null) {
            throw new IllegalStateException("Brak użytkownika w kontekście");
        }
        gun.setInStock(false);
        gun.setBasisOfRemoved(basisOfRemoved);
        gun.setRemovedBy(user.getFullName());
        gun.setRemovedSign(imageUUID);
        gun.setRemovedUserUUID(user.getUuid());
        GunStoreEntity gunStore = gunStoreRepository.findAll().stream().filter(s -> s.getTypeName().equals(gun.getGunType())).findFirst().orElseThrow(EntityNotFoundException::new);
        changeList(gun, gunStore);
        gunRepository.save(gun);
        gunStoreRepository.save(gunStore);
        LOG.info("Usunięto broń {} ze stanu przez {} (podstawa: {})", gun.getSerialNumber(), user.getFullName(), basisOfRemoved);
        return ResponseEntity.ok("Usunięto broń ze stanu magazynu");
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

            GunStoreEntity build = GunStoreEntity.builder().typeName(name.toString().trim()).gunEntityList(collect).build();
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
        GunEntity gunEntity = gunRepository.findById(gunUUID).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono broni"));
        GunUsedEntity gunUsedEntity = gunUsedRepository.findById(gunUsedUUID).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono użycia broni"));
        if (gunEntity.getGunUsedList() == null) {
            gunEntity.setGunUsedList(new ArrayList<>());
        }
        gunEntity.getGunUsedList().add(gunUsedEntity);
        gunRepository.save(gunEntity);
    }


    public List<UsedHistoryEntity> getGunUsedHistory(String gunUUID) {

        return usedHistoryRepository.findAll().stream().filter(f -> f.getGunUUID().equals(gunUUID)).sorted(Comparator.comparing(UsedHistoryEntity::getDateTime).reversed()).collect(Collectors.toList());

    }

    public ResponseEntity<List<String>> addGunToList(List<String> gunUUIDList, LocalDate date, LocalTime time) {

        if (gunUUIDList == null || gunUUIDList.isEmpty()) {
            return ResponseEntity.badRequest().body(List.of("Lista broni jest pusta"));
        }

        List<String> response = new ArrayList<>();

        // Pobieramy tylko potrzebne użycia z danego dnia (zamiast findAll())
        List<GunUsedEntity> usedToday = gunUsedRepository.findAllByAcceptanceDateBetween(date, date);
        for (String gunUUID : gunUUIDList) {
            GunEntity gun = gunRepository.findById(gunUUID).orElse(null);
            if (gun == null) {
                response.add("Nie ma takiej broni");
                continue;
            }
            boolean alreadyOnList = usedToday.stream().anyMatch(a -> gunUUID.equals(a.getGunUUID()) && a.getAcceptanceSign() == null);
            if (alreadyOnList) {
                response.add("Broń już znajduje się na liście: " + gun.getModelName() + " " + gun.getSerialNumber());
                continue;
            }
            GunUsedEntity build = GunUsedEntity.builder().gunRepresentationEntity(Mapping.mapToRepresentation(gun)).usedDate(LocalDate.now()).usedTime(LocalTime.now()).issuanceDate(date).issuanceTime(time).gunUUID(gun.getUuid()).build();
            gunUsedRepository.save(build);
            LOG.info("Dodano użycie Broni {} {}", gun.getModelName(), gun.getSerialNumber());
            response.add("Dodano użycie Broni " + gun.getModelName() + " " + gun.getSerialNumber());
        }
        return ResponseEntity.ok(response);
    }


    public ResponseEntity<?> getGun(String gunUUID) {
        return ResponseEntity.ok(gunRepository.findById(gunUUID).orElseThrow(EntityNotFoundException::new));
    }

    @Transactional
    @RecordHistory(action = "Caliber.changeUnitPrice", entity = HistoryEntityType.CALIBER, entityArgIndex = 0)
    public ResponseEntity<?> changeCaliberUnitPrice(String caliberUUID, Float price) {
        CaliberEntity caliber = caliberRepository.findById(caliberUUID).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono kalibru"));
        caliber.setUnitPrice(price);
        caliberRepository.save(caliber);
        LOG.info("Zmieniono cenę jednostkową kalibru {} na {}", caliber.getName(), price);
        return ResponseEntity.ok("Zmieniono cenę amunicji");
    }


    @Transactional
    @RecordHistory(action = "Caliber.changeUnitPriceForNotMember", entity = HistoryEntityType.CALIBER, entityArgIndex = 0)
    public ResponseEntity<?> changeCaliberUnitPriceForNotMember(String caliberUUID, Float price) {
        CaliberEntity caliber = caliberRepository.findById(caliberUUID).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono kalibru"));
        caliber.setUnitPriceForNotMember(price);
        caliberRepository.save(caliber);
        LOG.info("Zmieniono cenę kalibru {} dla osób spoza klubu na {}", caliber.getName(), price);
        return ResponseEntity.ok("Zmieniono cenę amunicji dla pozostałych");
    }


    @Transactional
    @RecordHistory(action = "AmmoEvidence.signUpkeep", entity = HistoryEntityType.AMMO_IN_EVIDENCE, entityArgIndex = 0)
    public ResponseEntity<?> signUpkeepAmmo(String ammoInEvidenceUUID, String imageUUID) {

        AmmoInEvidenceEntity entry = ammoInEvidenceRepository.findById(ammoInEvidenceUUID).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono wpisu amunicji"));
        UserEntity user = userAuthContext.get();
        if (user == null) {
            throw new IllegalStateException("Brak użytkownika w kontekście");
        }
        if (entry.isLocked()) {
            return ResponseEntity.badRequest().body("Już zatwierdzono");
        }

        entry.setImageUUID(imageUUID);
        entry.setSignedBy(user.getFullName());
        entry.setSignedDate(entry.getDateTime().toLocalDate());
        entry.setSignedTime(entry.getDateTime().toLocalTime().withNano(0));
        entry.lock();

        ammoInEvidenceRepository.save(entry);

        AmmoEvidenceEntity evidence = ammoEvidenceRepository.findById(entry.getEvidenceUUID()).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono listy ewidencji"));

        boolean allLocked = evidence.getAmmoInEvidenceEntityList().stream().allMatch(AmmoInEvidenceEntity::isLocked);

        if (allLocked) {
            evidence.lockEvidence();
            ammoEvidenceRepository.save(evidence);
            LOG.info("Zablokowano całą listę ewidencji {}", evidence.getUuid());
        }

        LOG.info("Podpisano i zablokowano wpis amunicji {} przez {}", entry.getUuid(), user.getFullName());

        return ResponseEntity.ok("Zablokowano listę z kalibrem " + entry.getCaliberName());
    }


    public List<GunUsedDTO> getAllGunUsedIssuance() {
        return gunUsedRepository.findAll().stream().filter(e -> (e.getIssuanceSign() == null || e.getGunTakerSign() == null) && (e.getAcceptanceSign() == null && e.getGunReturnerSign() == null)).map(e -> {
            GunUsedDTO dto = Mapping.map(e);
            gunRepository.findById(e.getGunUUID()).ifPresent(gun -> dto.setGun(Mapping.map(gun)));
            if (e.getGunRepresentationEntity() != null) {
                dto.setGunRepresentation(gunRepresentationRepository.findById(e.getGunRepresentationEntity().getUuid()).orElse(null));
            }
            return dto;
        }).sorted(Comparator.comparing(GunUsedDTO::getIssuanceDate).thenComparing(GunUsedDTO::getIssuanceTime).reversed()).toList();
    }


    public List<GunUsedDTO> getAllGunUsedAcceptance() {
        return gunUsedRepository.findAll().stream().filter(e -> e.getIssuanceSign() != null && e.getGunTakerSign() != null && (e.getAcceptanceSign() == null || e.getGunReturnerSign() == null)).map(e -> {
            GunUsedDTO dto = Mapping.map(e);
            gunRepository.findById(e.getGunUUID()).ifPresent(gun -> dto.setGun(Mapping.map(gun)));
            return dto;
        }).sorted(Comparator.comparing(GunUsedDTO::getIssuanceDate).thenComparing(GunUsedDTO::getIssuanceTime).reversed()).toList();
    }


    public List<GunUsedDTO> getAllGunUsed(LocalDate firstDate, LocalDate secondDate) {
        return gunUsedRepository.findAllByAcceptanceDateBetween(firstDate, secondDate).stream().filter(e -> e.getAcceptanceSign() != null && e.getGunReturnerName() != null && e.getIssuanceSign() != null && e.getGunTakerSign() != null).map(e -> {
            GunUsedDTO dto = Mapping.map(e);
            gunRepository.findById(e.getGunUUID()).ifPresent(gun -> dto.setGun(Mapping.map(gun)));
            return dto;
        }).sorted(Comparator.comparing(GunUsedDTO::getAcceptanceDate).thenComparing(GunUsedDTO::getAcceptanceTime).reversed()).toList();
    }


    public List<Gun> getGunList() {

        return gunRepository.findAll().stream().filter(GunEntity::isInStock).map(Mapping::map).sorted(Comparator.comparing(Gun::getModelName)).collect(Collectors.toList());

    }

    public List<GunUsedDTO> getGunUsedListAmmoList() {
        return gunUsedRepository.findAll().stream().filter(e -> e.getAcceptanceSign() == null).sorted(Comparator.comparing(GunUsedEntity::getIssuanceDate).thenComparing(GunUsedEntity::getIssuanceTime).reversed()).map(e -> {
            GunUsedDTO dto = Mapping.map(e);
            gunRepository.findById(e.getGunUUID()).ifPresent(gun -> dto.setGun(Mapping.map(gun)));
            return dto;
        }).toList();
    }


    public ResponseEntity<?> signIssuanceGun(String gunUsedUUID, String imageUUID, LocalDate date, LocalTime time) {
        GunUsedEntity used = gunUsedRepository.findById(gunUsedUUID).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono wpisu wydania broni"));
        UserEntity user = userAuthContext.get();
        if (user == null) {
            throw new IllegalStateException("Brak użytkownika w kontekście");
        }
        if (user.getUserPermissionsList() == null || !user.getUserPermissionsList().contains(UserSubType.WEAPONS_WAREHOUSEMAN.getName())) {
            filesRepository.deleteById(imageUUID);
            return ResponseEntity.badRequest().body("Brak uprawnień");
        }
        GunEntity gun = gunRepository.findById(used.getGunUUID()).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono broni"));
        used.setIssuanceBy(user.getFullName());
        used.setIssuanceSign(imageUUID);
        used.setIssuanceDate(date);
        used.setIssuanceTime(time.withNano(0));
        gunUsedRepository.save(used);
        String msg = "podpisano wydanie broni: " + gun.getModelName() + " " + gun.getSerialNumber();
        LOG.info(msg);
        return ResponseEntity.ok(msg);
    }

    public ResponseEntity<?> signTakerGun(String gunUsedUUID, String imageUUID, Integer memberLeg) {
        GunUsedEntity used = gunUsedRepository.findById(gunUsedUUID).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono wydania broni"));
        GunEntity gun = gunRepository.findById(used.getGunUUID()).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono broni"));
        MemberEntity member = memberRepository.findByLegitimationNumber(memberLeg).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono członka"));
        used.setGunTakerName(member.getFullName());
        used.setGunTakerSign(imageUUID);
        gunUsedRepository.save(used);
        LOG.info("podpisano przyjęcie broni: {} {} przez: {}", gun.getModelName(), gun.getSerialNumber(), member.getFullName());
        return ResponseEntity.ok("podpisano przyjęcie broni: " + gun.getModelName() + " " + gun.getSerialNumber());
    }


    public ResponseEntity<?> signReturnerGun(String gunUsedUUID, String imageUUID, Integer memberLeg) {
        GunUsedEntity used = gunUsedRepository.findById(gunUsedUUID).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono wydania broni"));
        GunEntity gun = gunRepository.findById(used.getGunUUID()).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono broni"));
        MemberEntity member = memberRepository.findByLegitimationNumber(memberLeg).orElseThrow(() -> new EntityNotFoundException("Nie znaleziono członka"));
        used.setGunReturnerName(member.getFullName());
        used.setGunReturnerSign(imageUUID);
        gunUsedRepository.save(used);
        LOG.info("podpisano zdanie broni: {} {} przez: {}", gun.getModelName(), gun.getSerialNumber(), member.getFullName());
        return ResponseEntity.ok("podpisano zdanie broni: " + gun.getModelName() + " " + gun.getSerialNumber());
    }

    @Transactional
    public ResponseEntity<?> signAcceptanceGun(String gunUsedUUID, String imageUUID, LocalDate date, LocalTime time) {
        UserEntity user = userAuthContext.get();
        if (user == null) {
            throw new IllegalStateException("Brak użytkownika w kontekście");
        }
        GunUsedEntity used = gunUsedRepository.findById(gunUsedUUID).orElseThrow(EntityNotFoundException::new);
        GunEntity gun = gunRepository.findById(used.getGunUUID()).orElseThrow(EntityNotFoundException::new);
        used.setAcceptanceBy(user.getFullName());
        used.setAcceptanceSign(imageUUID);
        used.setAcceptanceDate(date);
        used.setAcceptanceTime(time);
        GunUsedEntity saved = gunUsedRepository.save(used);
        addUseToGun(gun.getUuid(), saved.getUuid());
        LOG.info("Podpisano przyjęcie broni: {} {}", gun.getModelName(), gun.getSerialNumber());
        return ResponseEntity.ok("Podpisano przyjęcie broni: " + gun.getModelName() + " " + gun.getSerialNumber());
    }

    public ResponseEntity<?> getGunUsedByUUID(String gunUsedUUID) {
        GunUsedEntity gunUsed = gunUsedRepository.findById(gunUsedUUID).orElseThrow(EntityNotFoundException::new);
        GunEntity gun = gunRepository.findById(gunUsed.getGunUUID()).orElseThrow(EntityNotFoundException::new);
        GunUsedDTO dto = Mapping.map(gunUsed);
        dto.setGun(Mapping.map(gun));
        return ResponseEntity.ok(dto);
    }

    @Transactional
    public ResponseEntity<?> addGunSign(String gunUUID, String imageUUID) {
        UserEntity user = userAuthContext.get();
        if (user == null) {
            throw new IllegalStateException("Brak użytkownika w kontekście");
        }
        GunEntity gun = gunRepository.findById(gunUUID).orElseThrow(EntityNotFoundException::new);
        gun.setAddedSign(imageUUID);
        gun.setAddedBy(user.getFullName());
        gun.setAddedUserUUID(user.getUuid());
        gunRepository.save(gun);
        LOG.info("Podpisano przyjęcie na stan broni: {} {}", gun.getModelName(), gun.getSerialNumber());
        return ResponseEntity.ok("Podpisano przyjęcie broni: " + gun.getModelName() + " " + gun.getSerialNumber());
    }


}
