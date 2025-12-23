package com.shootingplace.shootingplace.bookOfRegistrationOfStayAtTheShootingPlace;

import com.shootingplace.shootingplace.file.FilesService;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonService;
import com.shootingplace.shootingplace.wrappers.ImageOtherPersonWrapper;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/evidence")
@CrossOrigin
public class RegistrationRecordsController {

    private final RegistrationRecordsService recordsServ;
    private final FilesService filesService;
    private final OtherPersonService otherPersonService;

    public RegistrationRecordsController(RegistrationRecordsService recordsServ, FilesService filesService, OtherPersonService otherPersonService) {
        this.recordsServ = recordsServ;
        this.filesService = filesService;
        this.otherPersonService = otherPersonService;
    }

    @GetMapping("/")
    public ResponseEntity<?> getRecordsFromDate(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate firstDateParse = LocalDate.parse(firstDate);
        LocalDate secondDateParse = LocalDate.parse(secondDate);
        return recordsServ.getRecordsBetweenDate(firstDateParse, secondDateParse);
    }

    @Transactional
    @PostMapping("/")
    public ResponseEntity<?> saveToEvidenceBook(@RequestBody String imageString, @RequestParam String pesel) {
        String imageUUID = filesService.storeImageEvidenceBookMember(imageString,pesel);
        return recordsServ.createRecordInBook(pesel, imageUUID);
    }
    @Transactional
    @PostMapping("/other")
    public ResponseEntity<?> saveToEvidenceBookNonMember(@Nullable @RequestParam String phone, @NotNull @RequestBody ImageOtherPersonWrapper other, @RequestParam Boolean rememberMe) {
        // podpis
        String imageUUID = filesService.storeImageEvidenceBook(other,other.getImageString() );
        // tworzenie osoby spoza Klubu, bo wyrazi zgodę
        if (rememberMe) {
            OtherPersonEntity otherPerson = otherPersonService.addPerson(other.getOther().getClub().getShortName(), other.getOther());
            return recordsServ.createRecordInBook(imageUUID,otherPerson);
        }
        // bez tworzenia osoby - wpis bez zapisu człowieka do bazy
        return recordsServ.createRecordInBook(imageUUID, other);
    }

}
