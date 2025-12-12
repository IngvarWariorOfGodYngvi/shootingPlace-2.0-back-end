package com.shootingplace.shootingplace.utils;

import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.address.AddressEntity;
import com.shootingplace.shootingplace.ammoEvidence.*;
import com.shootingplace.shootingplace.armory.*;
import com.shootingplace.shootingplace.armory.gunRepresentation.GunRepresentationEntity;
import com.shootingplace.shootingplace.club.Club;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.contributions.Contribution;
import com.shootingplace.shootingplace.contributions.ContributionEntity;
import com.shootingplace.shootingplace.email.*;
import com.shootingplace.shootingplace.file.FilesEntity;
import com.shootingplace.shootingplace.file.FilesModel;
import com.shootingplace.shootingplace.history.*;
import com.shootingplace.shootingplace.license.License;
import com.shootingplace.shootingplace.license.LicenseEntity;
import com.shootingplace.shootingplace.member.*;
import com.shootingplace.shootingplace.member.permissions.MemberPermissions;
import com.shootingplace.shootingplace.member.permissions.MemberPermissionsEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.score.Score;
import com.shootingplace.shootingplace.score.ScoreDTO;
import com.shootingplace.shootingplace.score.ScoreEntity;
import com.shootingplace.shootingplace.shootingPatent.ShootingPatent;
import com.shootingplace.shootingplace.shootingPatent.ShootingPatentEntity;
import com.shootingplace.shootingplace.statistics.MemberAmmo;
import com.shootingplace.shootingplace.tournament.CompetitionMembersList;
import com.shootingplace.shootingplace.tournament.CompetitionMembersListEntity;
import com.shootingplace.shootingplace.tournament.TournamentDTO;
import com.shootingplace.shootingplace.tournament.TournamentEntity;
import com.shootingplace.shootingplace.users.ChangeHistoryDTO;
import com.shootingplace.shootingplace.users.UserDTO;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.weaponPermission.WeaponPermission;
import com.shootingplace.shootingplace.weaponPermission.WeaponPermissionEntity;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceDTO;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceEntity;

import java.util.Optional;
import java.util.stream.Collectors;

public class Mapping {

    public static Member map(MemberEntity e) {
        return Member.builder()
                .uuid(e.getUuid())
                .joinDate(e.getJoinDate())
                .legitimationNumber(e.getLegitimationNumber())
                .firstName(e.getFirstName())
                .secondName(e.getSecondName())
                .shootingPatent(map(e.getShootingPatent()))
                .license(map(e.getLicense()))
                .email(e.getEmail())
                .pesel(e.getPesel())
                .IDCard(e.getIDCard())
                .club(e.getClub())
                .address(map(e.getAddress()))
                .phoneNumber(e.getPhoneNumber())
                .weaponPermission(map(e.getWeaponPermission()))
                .active(e.isActive())
                .adult(e.isAdult())
                .erased(e.isErased())
                .history(map(e.getHistory()))
                .memberPermissions(map(e.getMemberPermissions()))
                .personalEvidence(map(e.getPersonalEvidence()))
                .pzss(checkBool(e.isPZSS()))
                .erasedEntity(e.getErasedEntity())
                .image(e.getImageUUID())
                .note(e.getNote())
                .build();
    }

    private static Boolean checkBool(Boolean b) {
        if (b == null) {
            return false;
        } else return b;

    }


    public static MemberEntity map(Member e) {
        return MemberEntity.builder()
                .joinDate(e.getJoinDate())
                .legitimationNumber(e.getLegitimationNumber())
                .firstName(e.getFirstName())
                .secondName(e.getSecondName())
                .shootingPatent(map(e.getShootingPatent()))
                .license(map(e.getLicense()))
                .email(e.getEmail())
                .pesel(e.getPesel())
                .IDCard(e.getIDCard())
                .club(e.getClub())
                .address(map(e.getAddress()))
                .phoneNumber(e.getPhoneNumber())
                .weaponPermission(map(e.getWeaponPermission()))
                .active(e.getActive())
                .adult(e.getAdult())
                .erased(e.getErased())
                .memberPermissions(map(e.getMemberPermissions()))
                .personalEvidence(map(e.getPersonalEvidence()))
                .pzss(e.isPzss())
                .erasedEntity(e.getErasedEntity())
                .history(map(e.getHistory()))
                .note(e.getNote())
                .build();
    }

    public static MemberInfo map1(MemberEntity e) {
        return MemberInfo.builder()
                .uuid(e.getUuid())
                .firstName(e.getFirstName())
                .secondName(e.getSecondName())
                .name(e.getSecondName().replaceAll(" ", "") + " " + e.getFirstName().replaceAll(" ", "") + " " + e.getLegitimationNumber())
                .isActive(e.isActive())
                .legitimationNumber(e.getLegitimationNumber())
                .isAdult(e.isAdult())
                .image(e.getImageUUID())
                .note(e.getNote())
                .declarationLOK(e.getDeclarationLOK()).build();
    }

    public static MemberInfo map2(MemberEntity e) {
        return MemberInfo.builder()
                .uuid(e.getUuid())
                .firstName(e.getFirstName())
                .secondName(e.getSecondName())
                .name(e.getSecondName().replaceAll(" ", "") + " " + e.getFirstName().replaceAll(" ", "") + " " + e.getLegitimationNumber())
                .isActive(e.isActive())
                .legitimationNumber(e.getLegitimationNumber())
                .isAdult(e.isAdult())
                .note(e.getNote())
                .arbiterClass(e.getMemberPermissions() != null ? e.getMemberPermissions().getArbiterClass() : null)
                .declarationLOK(e.getDeclarationLOK()).build();
    }

    public static MemberDTO map2DTO(MemberEntity e) {
        return MemberDTO.builder()
                .uuid(e.getUuid())
                .firstName(e.getFirstName())
                .secondName(e.getSecondName())
                .image(e.getImageUUID())
                .adult(e.isAdult())
                .email(e.getEmail())
                .active(e.isActive())
                .erased(e.isErased())
                .erasedEntity(e.getErasedEntity() != null ? map(e.getErasedEntity()) : null)
                .pzss(e.isPZSS())
                .declarationLOK(e.getDeclarationLOK())
                .legitimationNumber(e.getLegitimationNumber())
                .license(map(e.getLicense()))
                .joinDate(e.getJoinDate())
                .memberPermissions(map(e.getMemberPermissions()))
                .club(map(e.getClub()))
                .note(e.getNote())
                .group(e.getMemberEntityGroup().getName())
                .build();
    }


    public static MemberAmmo map3(MemberEntity e) {
        return MemberAmmo.builder()
                .uuid(e.getUuid())
                .firstName(e.getFirstName())
                .secondName(e.getSecondName())
                .legitimationNumber(e.getLegitimationNumber())
                .build();
    }

    public static MemberAmmo map4(OtherPersonEntity e) {
        return MemberAmmo.builder()
                .uuid(String.valueOf(e.getId()))
                .firstName(e.getFirstName())
                .secondName(e.getSecondName())
                .legitimationNumber(e.getId())
                .build();
    }

    static Erased map(ErasedEntity a) {
        return Optional.ofNullable(a).map(e -> Erased.builder()
                .additionalDescription(a.getAdditionalDescription())
                .erasedType(a.getErasedType())
                .date(a.getDate())
                .uuid(a.getUuid())
                .build()).orElse(null);
    }

    static Address map(AddressEntity a) {
        return Optional.ofNullable(a)
                .map(e -> Address.builder()
                        .zipCode(e.getZipCode())
                        .postOfficeCity(e.getPostOfficeCity())
                        .street(e.getStreet())
                        .streetNumber(e.getStreetNumber())
                        .flatNumber(e.getFlatNumber())
                        .build()).orElse(null);

    }

    public static AddressEntity map(Address a) {
        return Optional.ofNullable(a)
                .map(e -> AddressEntity.builder()
                        .zipCode(e.getZipCode())
                        .postOfficeCity(e.getPostOfficeCity())
                        .street(e.getStreet())
                        .streetNumber(e.getStreetNumber())
                        .flatNumber(e.getFlatNumber())
                        .build()).orElse(null);
    }

    public static License map(LicenseEntity l) {
        return Optional.ofNullable(l)
                .map(e -> License.builder()
                        .number(e.getNumber())
                        .pistolPermission(e.isPistolPermission())
                        .riflePermission(e.isRiflePermission())
                        .shotgunPermission(e.isShotgunPermission())
                        .validThru(e.getValidThru())
                        .isValid(e.isValid())
                        .canProlong(e.isCanProlong())
                        .isPaid(e.isPaid())
                        .build()).orElse(null);
    }

    static LicenseEntity map(License l) {
        return Optional.ofNullable(l)
                .map(e -> LicenseEntity.builder()
                        .number(e.getNumber())
                        .pistolPermission(e.getPistolPermission())
                        .riflePermission(e.getRiflePermission())
                        .shotgunPermission(e.getShotgunPermission())
                        .validThru(e.getValidThru())
                        .valid(e.getValid())
                        .canProlong(e.getCanProlong())
                        .paid(e.getPaid())
                        .build()).orElse(null);
    }

    static ShootingPatentEntity map(ShootingPatent s) {
        return Optional.ofNullable(s)
                .map(e -> ShootingPatentEntity.builder()
                        .patentNumber(e.getPatentNumber())
                        .dateOfPosting(e.getDateOfPosting())
                        .pistolPermission(e.getPistolPermission())
                        .riflePermission(e.getRiflePermission())
                        .shotgunPermission(e.getShotgunPermission())
                        .build()).orElse(null);
    }

    static ShootingPatent map(ShootingPatentEntity s) {
        return Optional.ofNullable(s)
                .map(e -> ShootingPatent.builder()
                        .patentNumber(e.getPatentNumber())
                        .dateOfPosting(e.getDateOfPosting())
                        .pistolPermission(e.getPistolPermission())
                        .riflePermission(e.getRiflePermission())
                        .shotgunPermission(e.getShotgunPermission())
                        .build()).orElse(null);
    }

    static History map(HistoryEntity e) {
        return History.builder()
                .contributionList(e.getContributionList().stream().map(Mapping::map).collect(Collectors.toList()))
                .licenseHistory(e.getLicenseHistory())
                .competitionHistory(e.getCompetitionHistory().stream().map(Mapping::map).collect(Collectors.toList()))
                .patentDay(e.getPatentDay())
                .patentFirstRecord(e.getPatentFirstRecord())
                .licensePaymentHistory(e.getLicensePaymentHistory().stream().map(Mapping::map).collect(Collectors.toList()))
                .pistolCounter(e.getPistolCounter())
                .rifleCounter(e.getRifleCounter())
                .shotgunCounter(e.getShotgunCounter())
                .build();
    }

    private static CompetitionHistory map(CompetitionHistoryEntity e) {
        CompetitionHistory build = CompetitionHistory.builder()
                .date(e.getDate())
                .discipline(e.getDiscipline())
                .name(e.getName())
                .build();
        build.setDisciplineList(e.getDisciplineList());
        return build;
    }

    public static Contribution map(ContributionEntity e) {
        return Contribution.builder()
                .historyUUID(e.getHistoryUUID())
                .paymentDay(e.getPaymentDay())
                .validThru(e.getValidThru())
                .build();
    }

    static HistoryEntity map(History e) {

        return Optional.ofNullable(e).map(h -> HistoryEntity.builder()
                .licenseHistory(h.getLicenseHistory())
                .patentDay(h.getPatentDay())
                .patentFirstRecord(h.getPatentFirstRecord())
                .licensePaymentHistory(null)
                .pistolCounter(h.getPistolCounter())
                .rifleCounter(h.getRifleCounter())
                .shotgunCounter(h.getShotgunCounter())
                .build()).orElse(null);

    }

    static LicensePaymentHistory map(LicensePaymentHistoryEntity l) {
        return LicensePaymentHistory.builder()
                .date(l.getDate())
                .validForYear(l.getValidForYear())
                .memberUUID(l.getMemberUUID())
                .build();
    }

    static WeaponPermission map(WeaponPermissionEntity w) {
        return Optional.ofNullable(w).map(e -> WeaponPermission.builder()
                .number(e.getNumber())
                .isExist(e.getExist())
                .admissionToPossessAWeapon(e.getAdmissionToPossessAWeapon())
                .admissionToPossessAWeaponIsExist(e.getAdmissionToPossessAWeaponIsExist())
                .build()).orElse(null);
    }

    static WeaponPermissionEntity map(WeaponPermission w) {
        return Optional.ofNullable(w).map(e -> WeaponPermissionEntity.builder()
                .number(e.getNumber())
                .isExist(e.getExist())
                .admissionToPossessAWeapon(e.getAdmissionToPossessAWeapon())
                .admissionToPossessAWeaponIsExist(e.getAdmissionToPossessAWeaponIsExist())
                .build()).orElse(null);
    }

    static MemberPermissions map(MemberPermissionsEntity m) {
        return Optional.ofNullable(m).map(e -> MemberPermissions.builder()
                .instructorNumber(e.getInstructorNumber())
                .arbiterNumber(e.getArbiterNumber())
                .arbiterClass(e.getArbiterClass())
                .arbiterPermissionValidThru(e.getArbiterPermissionValidThru())
                .shootingLeaderNumber(e.getShootingLeaderNumber())
                .build()).orElse(null);
    }

    public static MemberPermissionsEntity map(MemberPermissions m) {
        return Optional.ofNullable(m).map(e -> MemberPermissionsEntity.builder()
                .instructorNumber(e.getInstructorNumber())
                .arbiterNumber(e.getArbiterNumber())
                .arbiterClass(e.getArbiterClass())
                .arbiterPermissionValidThru(e.getArbiterPermissionValidThru())
                .shootingLeaderNumber(e.getShootingLeaderNumber())
                .build()).orElse(null);
    }

    public static CompetitionMembersList map(CompetitionMembersListEntity c) {
        CompetitionMembersList build = CompetitionMembersList.builder()
                .uuid(c.getUuid())
                .name(c.getName())
                .date(c.getDate())
                .WZSS(c.isWZSS())
                .disciplineList(null)
                .attachedToTournament(c.getAttachedToTournament())
                .scoreList(c.getScoreList().stream().map(Mapping::map).collect(Collectors.toList()))
                .scoreListSize(c.getScoreList().size())
                .countingMethod(c.getCountingMethod())
                .type(c.getType())
                .numberOfShots(c.getNumberOfShots())
                .ordering(c.getOrdering())
                .caliberUUID(c.getCaliberUUID())
                .practiceShots(c.getPracticeShots())
                .build();
        build.setDisciplineList(c.getDisciplineList());
        build.setNumberOfManyShotsList(c.getNumberOfManyShotsList());
        return build;
    }

    public static CompetitionMembersList map1(CompetitionMembersListEntity c) {
        CompetitionMembersList build = CompetitionMembersList.builder()
                .uuid(c.getUuid())
                .name(c.getName())
                .date(c.getDate())
                .WZSS(c.isWZSS())
                .attachedToTournament(c.getAttachedToTournament())
                .scoreListSize(c.getScoreList().size())
                .countingMethod(c.getCountingMethod())
                .type(c.getType())
                .numberOfShots(c.getNumberOfShots())
                .ordering(c.getOrdering())
                .caliberUUID(c.getCaliberUUID())
                .practiceShots(c.getPracticeShots())
                .build();
        build.setDisciplineList(c.getDisciplineList());
        build.setNumberOfManyShotsList(c.getNumberOfManyShotsList());
        return build;
    }

    public static Score map(ScoreEntity s) {
        return Score.builder()
                .name(s.getMember() != null ? s.getMember().getFullName() : s.getOtherPersonEntity() != null ? s.getOtherPersonEntity().getFullName() : s.getName())
                .member(s.getMember() == null ? null : map2DTO(s.getMember()))
                .otherPersonEntity(s.getOtherPersonEntity() == null ? null : s.getOtherPersonEntity())
                .ammunition(s.isAmmunition())
                .gun(s.isGun())
                .metricNumber(s.getMetricNumber())
                .innerTen(s.getInnerTen())
                .outerTen(s.getOuterTen())
                .alfa(s.getAlfa())
                .charlie(s.getCharlie())
                .delta(s.getDelta())
                .dnf(s.isDnf())
                .dsq(s.isDsq())
                .pk(s.isPk())
                .hf(s.getHf())
                .edited(s.isEdited())
                .miss(s.getMiss())
                .series(s.getSeries() != null ? s.getSeries() : null)
                .procedures(s.getProcedures())
                .score(s.getScore())
                .competitionMembersListEntityUUID(s.getCompetitionMembersListEntityUUID())
                .uuid(s.getUuid())
                .build();

    }


    public static FilesEntity map(FilesModel f) {
        return Optional.ofNullable(f).map(e -> FilesEntity.builder()
                .uuid(e.getUuid())
                .belongToMemberUUID(e.getBelongToMemberUUID())
                .name(e.getName())
                .data(e.getData())
                .type(e.getType())
                .date(e.getDate())
                .time(e.getTime())
                .size(e.getSize())
                .version(e.getVersion())
                .build()).orElse(null);
    }

    public static FilesModel map(FilesEntity f) {
        return Optional.ofNullable(f).map(e -> FilesModel.builder()
                .uuid(e.getUuid())
                .belongToMemberUUID(e.getBelongToMemberUUID())
                .name(e.getName())
                .type(e.getType())
                .date(e.getDate())
                .time(e.getTime())
                .size(e.getSize())
                .version(e.getVersion())
                .build()).orElse(null);
    }

    public static Caliber map(CaliberEntity e) {
        return Caliber.builder()
                .name(e.getName())
                .uuid(e.getUuid())
                .quantity(e.getQuantity())
                .unitPrice(e.getUnitPrice())
                .unitPriceForNotMember(e.getUnitPriceForNotMember())
                .build();
    }

    public static CaliberEntity map(Caliber c) {
        return Optional.ofNullable(c).map(e -> CaliberEntity.builder()
                .name(e.getName())
                .quantity(e.getQuantity())
                .build()).orElse(null);
    }

    public static PersonalEvidenceEntity map(PersonalEvidence p) {
        return Optional.ofNullable(p).map(e -> PersonalEvidenceEntity.builder()
                .build()).orElse(null);
    }

    public static PersonalEvidence map(PersonalEvidenceEntity p) {
        return Optional.ofNullable(p).map(e -> PersonalEvidence.builder()
                .build()).orElse(null);
    }


    public static CompetitionMembersListEntity map(CompetitionMembersList c) {
        return Optional.ofNullable(c).map(e -> CompetitionMembersListEntity.builder()
                .name(e.getName())
                .build()).orElse(null);
    }

    public static AmmoUsedToEvidenceEntity map(AmmoUsedEvidence a) {
        return AmmoUsedToEvidenceEntity.builder()
                .caliberName(a.getCaliberName())
                .caliberUUID(a.getCaliberUUID())
                .counter(a.getCounter())
                .memberEntity(a.getMemberEntity())
                .otherPersonEntity(a.getOtherPersonEntity())
                .name(a.getUserName())
                .date(a.getDate())
                .time(a.getTime())
                .build();
    }

    public static AmmoUsedEntity map(AmmoUsedPersonal a) {
        return Optional.ofNullable(a).map(e -> AmmoUsedEntity.builder()
                .caliberName(a.getCaliberName())
                .counter(a.getCounter())
                .memberUUID(a.getMemberUUID())
                .caliberUUID(a.getCaliberUUID())
                .date(a.getDate())
                .userName(a.getMemberName())
                .otherPersonEntityID(null)
                .build()).orElse(null);

    }

    public static AmmoDTO map1(AmmoEvidenceEntity a) {
        return AmmoDTO.builder()
                .evidenceUUID(a.getUuid())
                .date(a.getDate())
                .number(a.getNumber())
                .build();

    }

    public static TournamentDTO map1(TournamentEntity a) {
        return TournamentDTO.builder()
                .tournamentUUID(a.getUuid())
                .date(a.getDate())
                .name(a.getName())
                .build();

    }

    static Club map(ClubEntity c) {
        return Club.builder()
                .id(c.getId())
                .shortName(c.getShortName())
                .build();
    }


    public static Gun map(GunEntity c) {
        return Gun.builder()
                .additionalEquipment(c.getAdditionalEquipment())
                .basisForPurchaseOrAssignment(c.getBasisForPurchaseOrAssignment())
                .caliber(c.getCaliber())
                .comment(c.getComment())
                .gunCertificateSerialNumber(c.getGunCertificateSerialNumber())
                .gunType(c.getGunType())
                .modelName(c.getModelName())
                .available(c.isAvailable())
                .imgUUID(c.getImgUUID())
                .numberOfMagazines(c.getNumberOfMagazines())
                .productionYear(c.getProductionYear())
                .recordInEvidenceBook(c.getRecordInEvidenceBook())
                .serialNumber(c.getSerialNumber())
                .addedDate(c.getAddedDate())
                .uuid(c.getUuid())
                .addedDate(c.getAddedDate())
                .addedBy(c.getAddedBy())
                .addedSing(c.getAddedSign())
                .addedUserUUID(c.getAddedUserUUID())
                .removedBy(c.getRemovedBy())
                .removedSing(c.getRemovedSign())
                .removedUserUUID(c.getRemovedUserUUID())
                .removedDate(c.getRemovedDate())
                .basisOfRemoved(c.getBasisOfRemoved())
                .build();
    }

    public static WorkingTimeEvidenceDTO map(WorkingTimeEvidenceEntity e) {
        return WorkingTimeEvidenceDTO.builder()
                .cardNumber(e.getCardNumber())
                .workTime(e.getWorkTime())
                .workType(e.getWorkType())
                .isAutomatedClosed(e.isAutomatedClosed())
                .start(e.getStart())
                .stop(e.getStop())
                .isAccepted(e.isAccepted())
                .toClarify(e.isToClarify())
                .user(e.getUser().getUuid())
                .uuid(e.getUuid()).build();
    }

    public static UserDTO map(UserEntity u) {
        return UserDTO.builder()
                .firstName(u.getFirstName())
                .secondName(u.getSecondName())
                .uuid(u.getUuid())
                .userPermissionsList(u.getUserPermissionsList().toString())
                .build();

    }

    public static ChangeHistoryDTO map(ChangeHistoryEntity c) {
        return ChangeHistoryDTO.builder()
                .belongsTo(c.getBelongsTo())
                .timeNow(c.getTimeNow())
                .dayNow(c.getDayNow())
                .classNamePlusMethod(c.getClassNamePlusMethod())
                .build();
    }

    public static AmmoEvidenceDTO map(AmmoEvidenceEntity a) {
        return AmmoEvidenceDTO.builder()
                .uuid(a.getUuid())
                .number(a.getNumber())
                .date(a.getDate())
                .open(a.isOpen())
                .locked(a.isLocked())
                .forceOpen(a.isForceOpen())
                .ammoInEvidenceDTOList(a.getAmmoInEvidenceEntityList().stream().map(Mapping::map).collect(Collectors.toList()))
                .build();
    }

    public static AmmoInEvidenceDTO map(AmmoInEvidenceEntity a) {
        return AmmoInEvidenceDTO.builder()
                .uuid(a.getUuid())
                .locked(a.isLocked())
                .caliberName(a.getCaliberName())
                .signedBy(a.getSignedBy())
                .imageUUID(a.getImageUUID())
                .date(a.getDateTime().toLocalDate())
                .time(a.getDateTime().toLocalTime())
                .ammoUsedToEvidenceDTOList(a.getAmmoUsedToEvidenceEntityList().stream().map(Mapping::map).collect(Collectors.toList()))
                .quantity(a.getQuantity())
                .build();
    }

    private static AmmoUsedToEvidenceDTO map(AmmoUsedToEvidenceEntity a) {
        return AmmoUsedToEvidenceDTO.builder()
                .caliberName(a.getCaliberName())
                .counter(a.getCounter())
                .date(a.getDate())
                .name(a.getName())
                .legitimationNumber(a.getMemberEntity() != null ? a.getMemberEntity().getLegitimationNumber() : null)
                .IDNumber(a.getOtherPersonEntity() != null ? a.getOtherPersonEntity().getId() : null)
                .build();
    }

    public static ShootingPacketDTO map(ShootingPacketEntity s) {
        return ShootingPacketDTO.builder()
                .name(s.getName())
                .price(s.getPrice())
                .calibers(s.getCalibers().stream().map(Mapping::map).collect(Collectors.toList())).build();
    }

    private static CaliberForShootingPacketDTO map(CaliberForShootingPacketEntity c) {
        return CaliberForShootingPacketDTO.builder()
                .caliberName(c.getCaliberName())
                .caliberUUID(c.getCaliberUUID())
                .quantity(c.getQuantity())
                .build();
    }

    public static ClubEntity map(Club c) {
        return ClubEntity.builder()
                .shortName(c.getShortName())
                .fullName(c.getFullName())
                .email(c.getEmail())
                .licenseNumber(c.getLicenseNumber())
                .phoneNumber(c.getPhoneNumber())
                .url(c.getUrl())
                .vovoidership(c.getVovoidership())
                .wzss(c.getWzss())
                .houseNumber(c.getHouseNumber())
                .appartmentNumber(c.getAppartmentNumber())
                .city(c.getCity())
                .street(c.getStreet())
                .build();
    }

    public static GunUsedDTO map(GunUsedEntity g) {
        return GunUsedDTO.builder()
                .uuid(g.getUuid())
                .acceptanceBy(g.getAcceptanceBy())
                .acceptanceDate(g.getAcceptanceDate())
                .acceptanceSign(g.getAcceptanceSign())
                .acceptanceTime(g.getAcceptanceTime())
                .issuanceBy(g.getIssuanceBy())
                .issuanceDate(g.getIssuanceDate())
                .issuanceSign(g.getIssuanceSign())
                .issuanceTime(g.getIssuanceTime())
                .gunTakerName(g.getGunTakerName())
                .gunTakerSign(g.getGunTakerSign())
                .gunReturnerName(g.getGunReturnerName())
                .gunReturnerSign(g.getGunReturnerSign())
                .adnotation(g.getAdnotation())
                .usedDate(g.getUsedDate())
                .usedTime(g.getUsedTime())
                .gunRepresentation(g.getGunRepresentationEntity())
                .build();
    }

    public static GunRepresentationEntity mapToRepresentation(GunEntity g) {
        return GunRepresentationEntity.builder()
                .gunUUID(g.getUuid())
                .additionalEquipment(g.getAdditionalEquipment())
                .basisForPurchaseOrAssignment(g.getBasisForPurchaseOrAssignment())
                .caliber(g.getCaliber())
                .comment(g.getComment())
                .gunCertificateSerialNumber(g.getGunCertificateSerialNumber())
                .gunType(g.getGunType())
                .modelName(g.getModelName())
                .available(g.isAvailable())
                .imgUUID(g.getImgUUID())
                .numberOfMagazines(g.getNumberOfMagazines())
                .productionYear(g.getProductionYear())
                .recordInEvidenceBook(g.getRecordInEvidenceBook())
                .serialNumber(g.getSerialNumber())
                .addedDate(g.getAddedDate())
                .addedDate(g.getAddedDate())
                .addedBy(g.getAddedBy())
                .addedSign(g.getAddedSign())
                .addedUserUUID(g.getAddedUserUUID())
                .removedBy(g.getRemovedBy())
                .removedSign(g.getRemovedSign())
                .removedUserUUID(g.getRemovedUserUUID())
                .removedDate(g.getRemovedDate())
                .basisOfRemoved(g.getBasisOfRemoved())
                .build();
    }

    public static ScoreDTO mapScoreDTO(ScoreEntity el) {
        return ScoreDTO.builder()
                .name(el.getMember() != null ? el.getMember().getFullName() : el.getOtherPersonEntity() != null ? el.getOtherPersonEntity().getFullName() : el.getName())
                .metricNumber(el.getMetricNumber())
                .full(el.getName() + " " + el.getMetricNumber())
                .build();
    }

    public static EmailQueue map(EmailRequest request, String mailType, MemberEntity memberEntity) {
        return EmailQueue.builder().request(request).mailType(mailType).memberEntity(memberEntity).build();
    }
    public static SentEmailDTO map(SentEmail e) {
        return SentEmailDTO.builder()
                .uuid(e.getUuid())
                .sentAt(e.getSentAt())
                .content(e.getContent())
                .errorMessage(e.getErrorMessage())
                .success(e.isSuccess())
                .subject(e.getSubject())
                .mailType(e.getMailType())
                .recipient(e.getRecipient()).build();
    }
    public static ScheduledEmailDTO map(ScheduledEmail e) {
        return ScheduledEmailDTO.builder()
                .uuid(e.getUuid())
                .recipient(e.getRecipient())
                .htmlContent(e.getHtmlContent())
                .subject(e.getSubject())
                .scheduledFor(e.getScheduledFor())
                .mailType(e.getMailType())
                .build();
    }
}
