package com.shootingplace.shootingplace.file;

import com.lowagie.text.DocumentException;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/files")
@CrossOrigin
@RequiredArgsConstructor
public class FilesController {

    private final FilesService filesService;
    private final XLSXFilesService xlsxFiles;
    private final MemberService memberService;
    private final Environment environment;

    @GetMapping("/countPages")
    public ResponseEntity<?> countPages() {
        return filesService.countPages();
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        String message;
        try {
            filesService.store(file);

            message = "Uploaded the file successfully: " + file.getName();
            return ResponseEntity.status(HttpStatus.OK).body(message);
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + "!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
        }
    }

    @DeleteMapping("/removeImg")
    public ResponseEntity<?> removeImageFromGun(@RequestParam String gunUUID) {
        return filesService.removeImageFromGun(gunUUID);
    }

    @PostMapping("/member/{uuid}")
    public ResponseEntity<?> addImageToMember(@PathVariable String uuid, @RequestParam("file") MultipartFile file) {
        String message;
        try {
            MemberEntity member = memberService.getMember(uuid);
            filesService.store(file, member);

            message = "Uploaded the file successfully: " + file.getName();
            return ResponseEntity.status(HttpStatus.OK).body(message);
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + "!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
        }
    }

    // składka członkowska
    @GetMapping("/downloadMemberEmailCSV")
    public ResponseEntity<byte[]> getMemberEmailCSV() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getMemberEmailCSV();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    // składka członkowska
    @GetMapping("/downloadContribution/{memberUUID}")
    public ResponseEntity<byte[]> getContributionFile(@PathVariable String memberUUID, @RequestParam String contributionUUID, @Nullable @RequestParam Boolean a5rotate) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.contributionConfirm(memberUUID, contributionUUID, a5rotate);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    // karta klubowa
    @GetMapping("/getMembershipDeclaration/{memberUUID}")
    public ResponseEntity<byte[]> getMembershipDeclaration(@PathVariable String memberUUID) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getMembershipDeclaration(memberUUID);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    // legitymacja klubowa
    @GetMapping("/downloadMemberLegitimation/{memberUUID}")
    public ResponseEntity<byte[]> getMemberLegitimationPdfGenerator(@PathVariable String memberUUID) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getMemberLegitimationPdfGenerator(memberUUID);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    // csv
    @GetMapping("/downloadCSVFile/{memberUUID}")
    public ResponseEntity<byte[]> getMemberCSVFile(@PathVariable String memberUUID) throws IOException {
        FilesEntity filesEntity = filesService.getMemberCSVFile(memberUUID);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_TYPE, filesEntity.getName().replaceAll("ó", "o")).body(filesEntity.getData());
    }

    // wniosek o przedłużenie licencji
    @GetMapping("/downloadApplication/{memberUUID}")
    public ResponseEntity<byte[]> getApplicationForExtensionOfTheCompetitorsLicense(@PathVariable String memberUUID) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.createApplicationForExtensionOfTheCompetitorsLicense(memberUUID);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    // zaświadczenie o przynależności do klubu
    @GetMapping("/downloadCertificateOfClubMembership/{memberUUID}")
    public ResponseEntity<byte[]> CertificateOfClubMembership(@PathVariable String memberUUID, @RequestParam String reason, @RequestParam String city, @RequestParam boolean enlargement) throws IOException, DocumentException {
        FilesEntity filesEntity;
        if (environment.getActiveProfiles()[0].equals("rcs")) {
            filesEntity = filesService.certificateOfClubMembershipPanaszew(memberUUID, reason); // RCS Panaszew
        } else {
            filesEntity = filesService.CertificateOfClubMembership(memberUUID, reason, city, enlargement); // Dziesiątka
        }

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    // wniosek o pozwolenie na broń
    @GetMapping("/ApplicationForFirearmsLicense/{memberUUID}")
    public ResponseEntity<byte[]> ApplicationForFirearmsLicense(@PathVariable String memberUUID, @Nullable @RequestParam String thirdName, @RequestParam String birthPlace, @RequestParam String fatherName, @RequestParam String motherName, @RequestParam String motherMaidenName, @RequestParam String issuingAuthority, @RequestParam String IDDate, @RequestParam String licenseDate, @RequestParam String city) throws IOException, DocumentException {
        LocalDate parseIDDate = LocalDate.parse(IDDate);
        LocalDate parselicenseDate = LocalDate.parse(licenseDate);
        FilesEntity filesEntity = filesService.ApplicationForFirearmsLicense(memberUUID, thirdName, birthPlace, fatherName, motherName, motherMaidenName, issuingAuthority, parseIDDate, parselicenseDate, city); // Dziesiątka

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    // lista amunicyjna
    @GetMapping("/downloadAmmunitionList/{ammoEvidenceUUID}")
    public ResponseEntity<byte[]> getAmmoListFile(@PathVariable String ammoEvidenceUUID) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.createAmmunitionListDocument(ammoEvidenceUUID);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }


    // komunikat z zawodów xls
    @GetMapping("/downloadAnnouncementFromCompetitionXLSX/{tournamentUUID}")
    public ResponseEntity<byte[]> getAnnouncementFromCompetitionXSLX(@PathVariable String tournamentUUID) throws IOException {
        FilesEntity filesEntity = xlsxFiles.createAnnouncementInXLSXType(tournamentUUID);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().replaceAll(" ", "") + "\"").body(filesEntity.getData());
    }

    // lista klubowiczów
    @GetMapping("/downloadAllMembers")
    public ResponseEntity<byte[]> getAllMembersToTable() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.generateMembersListByAdult();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    @GetMapping("/downloadAllMembersXLSXFile")
    public ResponseEntity<byte[]> getAllMembersToTableXLSXFile() throws IOException {
        FilesEntity filesEntity = xlsxFiles.getAllMembersToTableXLSXFile();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    // lista klubowiczów z licencją zawodniczą
    @GetMapping("/downloadAllMembersWithLicense")
    public ResponseEntity<byte[]> getAllMembersWithLicense() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.generateMembersListWithLicense();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    // zgłoszenie do katowic
    @GetMapping("/downloadAllMembersWithLicenseXlsx")
    public ResponseEntity<byte[]> getAllMembersWithLicenseXlsx() throws IOException {
        FilesEntity filesEntity = xlsxFiles.getAllMembersWithLicenseXlsx();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    // lista obecności na wybory
    @GetMapping("/downloadAllMembersToElection")
    public ResponseEntity<byte[]> getAllMembersToElection() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.generateAllMembersList();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    // lista do zgłoszenia na policję
    @GetMapping("/generateListOfMembersToReportToPolice")
    public ResponseEntity<byte[]> generateListOfMembersToReportToPolice() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.generateListOfMembersToReportToPolice();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    // lista osób usuniętych
    @GetMapping("/downloadAllErasedMembers")
    public ResponseEntity<byte[]> getAllErasedMembers(@RequestParam String firstDate, @RequestParam String secondDate) throws DocumentException, IOException {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        FilesEntity filesEntity = filesService.getAllErasedMembers(parseFirstDate, parseSecondDate);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    @GetMapping("/downloadAllErasedMembersXlsx")
    public ResponseEntity<byte[]> getAllErasedMembersXlsx(@RequestParam String firstDate, @RequestParam String secondDate) throws IOException {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        FilesEntity filesEntity = xlsxFiles.getAllErasedMembersXlsx(parseFirstDate, parseSecondDate);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    // lista osób do skreślenia
    @GetMapping("/downloadAllMembersToErased")
    public ResponseEntity<byte[]> getAllMembersToErased() throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.generateAllMembersToErasedList();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    @GetMapping("/downloadAllMembersToErasedXlsx")
    public ResponseEntity<byte[]> getAllMembersToErasedXlsx() throws IOException {
        FilesEntity filesEntity = xlsxFiles.generateAllMembersToErasedListXlsx();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    // spis broni
    @GetMapping("/downloadGunRegistry")
    public ResponseEntity<byte[]> getGunRegistry(@RequestParam List<String> guns) throws IOException, DocumentException {
        FilesEntity filesEntity = filesService.getGunRegistry(guns);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    // spis broni
    @GetMapping("/downloadGunRegistryXlsx")
    public ResponseEntity<byte[]> getGunRegistryXlsx(@RequestParam List<String> guns) throws IOException {
        FilesEntity filesEntity = xlsxFiles.getGunRegistryXlsx(guns);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    // list przewozowy broni
    @GetMapping("/downloadGunTransportCertificate")
    public ResponseEntity<byte[]> getGunTransportCertificate(@RequestParam List<String> guns, @RequestParam String date, @RequestParam String date1) throws IOException, DocumentException {
        LocalDate parse = LocalDate.parse(date);
        LocalDate parse1 = LocalDate.parse(date1);
        FilesEntity filesEntity = filesService.getGunTransportCertificate(guns, parse, parse1);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"").body(filesEntity.getData());
    }

    // metryczka
    @GetMapping("/downloadMetric/{tournamentUUID}")
    public ResponseEntity<byte[]> getMemberMetrics(@RequestParam String memberUUID, @RequestParam String otherID, @PathVariable String tournamentUUID, @RequestParam List<String> competitions, @RequestParam String startNumber, @RequestParam Boolean a5rotate) throws IOException, DocumentException {
        if (otherID.equals("0")) {
            otherID = null;
        } else {
            memberUUID = null;
        }
        if (competitions.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        FilesEntity filesEntity = filesService.getStartsMetric(memberUUID, otherID, tournamentUUID, competitions, startNumber, a5rotate);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().replaceAll(" ", "") + "\"").body(filesEntity.getData());
    }

    // raport sędziowania ogólny
    @GetMapping("/downloadJudgingReport")
    public ResponseEntity<byte[]> getJudgingReportInChosenTime(@RequestParam String firstDate, @RequestParam String secondDate) throws DocumentException, IOException {
        LocalDate firstDateParse = LocalDate.parse(firstDate);
        LocalDate secondDateParse = LocalDate.parse(secondDate);
        FilesEntity filesEntity = filesService.getJudgingReportInChosenTime(firstDateParse, secondDateParse);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().replaceAll(" ", "") + "\"").body(filesEntity.getData());
    }

    @GetMapping("/downloadEvidenceBook")
    public ResponseEntity<byte[]> getEvidenceBookInChosenTime(@RequestParam String firstDate, @RequestParam String secondDate) throws DocumentException, IOException {
        LocalDate firstDateParse = LocalDate.parse(firstDate);
        LocalDate secondDateParse = LocalDate.parse(secondDate);
        FilesEntity filesEntity = filesService.getEvidenceBookInChosenTime(firstDateParse, secondDateParse);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().replaceAll(" ", "") + "\"").body(filesEntity.getData());
    }

    // raport pracy
    @GetMapping("/downloadWorkReport")
    public ResponseEntity<byte[]> getWorkTimeReport(@Nullable @RequestParam String year, @Nullable @RequestParam String month, @RequestParam boolean detailed) throws DocumentException, IOException {
        if (year == null || year.equals("null") || month == null || month.equals("null")) {
            ResponseEntity.badRequest().body("Musisz podać Rok i Miesiąc aby pobrać wyniki");
        }
        int year1 = Integer.parseInt(year);
        FilesEntity filesEntity = filesService.getWorkTimeReport(year1, month, detailed);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().replaceAll(" ", "") + "\"").body(filesEntity.getData());
    }

    // lista sędziów na zawodach
    @GetMapping("/downloadJudge/{tournamentUUID}")
    public ResponseEntity<byte[]> getJudge(@PathVariable String tournamentUUID) throws IOException, DocumentException {

        FilesEntity filesEntity = filesService.getJudge(tournamentUUID);

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().replaceAll(" ", "") + "\"").body(filesEntity.getData());
    }
    // lista zawodników na zawodach
    @GetMapping("/downloadCompetitors/{tournamentUUID}")
    public ResponseEntity<byte[]> getCompetitors(@PathVariable String tournamentUUID) throws IOException, DocumentException {

        FilesEntity filesEntity = filesService.getCompetitors(tournamentUUID);

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().replaceAll(" ", "") + "\"").body(filesEntity.getData());
    }

    @GetMapping("/membershipDeclarationLOK")
    public ResponseEntity<byte[]> getMembershipDeclarationLOK(@RequestParam String uuid) throws DocumentException, IOException {
        FilesEntity filesEntity = filesService.getMembershipDeclarationLOK(uuid);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().replaceAll(" ", "") + "\"").body(filesEntity.getData());
    }

    @GetMapping("/getAllMemberFiles")
    public ResponseEntity<?> getAllMemberFiles(@RequestParam String uuid) {
        return ResponseEntity.ok(filesService.getAllMemberFiles(uuid));
    }

    @GetMapping("/getAllFiles")
    public ResponseEntity<?> getAllFiles(Pageable page) {

        return ResponseEntity.ok(filesService.getAllFilesList(page));
    }

    @GetMapping("/getFile")
    public ResponseEntity<byte[]> getFile(@RequestParam String uuid) {
        FilesEntity filesEntity = filesService.getFile(uuid);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().replaceAll(" ", "") + "\"").body(filesEntity.getData());
    }

    @GetMapping("/joinDateSum")
    public ResponseEntity<?> getJoinDateSum(@RequestParam String firstDate, @RequestParam String secondDate) throws IOException {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        FilesEntity filesEntity = xlsxFiles.getJoinDateSum(parseFirstDate, parseSecondDate);

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().replaceAll(" ", "") + "\"").body(filesEntity.getData());
    }

    @GetMapping("/erasedSum")
    public ResponseEntity<?> getErasedSum(@RequestParam String firstDate, @RequestParam String secondDate) throws IOException {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        FilesEntity filesEntity = xlsxFiles.getErasedSum(parseFirstDate, parseSecondDate);

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().replaceAll(" ", "") + "\"").body(filesEntity.getData());
    }

    @GetMapping("/contributions")
    public ResponseEntity<?> getContributions(@RequestParam String firstDate, @RequestParam String secondDate) throws IOException {

        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);

        FilesEntity filesEntity = xlsxFiles.getContributions(parseFirstDate, parseSecondDate);

        String fileName = filesEntity.getName();
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        String asciiFileName = Normalizer.normalize(fileName, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(filesEntity.getType())).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + asciiFileName + "\"; filename*=UTF-8''" + encodedFileName).body(filesEntity.getData());
    }

    @DeleteMapping("/deleteFile")
    public ResponseEntity<?> deleteFile(@RequestParam String uuid) {
        return filesService.delete(uuid);
    }


}
