package com.shootingplace.shootingplace.member;


import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.file.XLSXFilesService;
import com.shootingplace.shootingplace.security.RequirePermissions;
import com.shootingplace.shootingplace.soz.SozClient;
import com.shootingplace.shootingplace.wrappers.MemberWithAddressWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/member")
@CrossOrigin
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final XLSXFilesService xlsxFilesService;
    private final SozClient sozClient;

    @GetMapping("/{number}")
    public ResponseEntity<?> getMember(@PathVariable int number) {
        return memberService.getMember(number);
    }

    @GetMapping("/ID/{number}")
    public ResponseEntity<?> getMemberUUIDByLegitimationNumber(@PathVariable int number) {
        return memberService.getMemberUUIDByLegitimationNumber(number);
    }

    @GetMapping("/PESEL/{PESELNumber}")
    public ResponseEntity<?> getMemberByPESELNumber(@PathVariable String PESELNumber) {
        return memberService.getMemberByPESELNumber(PESELNumber);
    }

    @GetMapping("/uuid/{uuid}")
    public ResponseEntity<MemberEntity> getMemberByUUID(@PathVariable String uuid) {
        return ResponseEntity.ok(memberService.getMemberByUUID(uuid));
    }

    @GetMapping("/erased")
    public ResponseEntity<?> getErasedMembers(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        return ResponseEntity.ok(memberService.getMembersErased(parseFirstDate, parseSecondDate));
    }

    @GetMapping("/reportView")
    public ResponseEntity<?> getMembersToReportToPoliceView(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        return ResponseEntity.ok(memberService.getMembersToReportToPoliceView(parseFirstDate, parseSecondDate));
    }

    @GetMapping("/getAllNames")
    public List<MemberInfo> getAllNames() {
        return memberService.getAllNames();
    }

    @GetMapping("/getAdvancedSearch")
    public List<MemberDTO> getAdvancedSearch(@RequestParam boolean isErased, @RequestParam int searchType, @RequestParam String inputText) {
        return memberService.getAdvancedSearch(isErased, searchType, inputText);
    }

    @GetMapping("/getAllNamesErased")
    public List<MemberInfo> getAllNamesErased() {
        return memberService.getAllNamesErased();
    }

    @GetMapping("/getAllMemberDTO")
    public ResponseEntity<List<MemberDTO>> getAllMemberDTO() {
        return ResponseEntity.ok(memberService.getAllMemberDTO());
    }

    @GetMapping("/getAllMemberDTOWithArgs")
    public ResponseEntity<List<MemberDTO>> getAllMemberDTO(@RequestParam @Nullable String adult, @Nullable @RequestParam String active, @RequestParam String erase) {
        Boolean adult1;
        Boolean active1;
        Boolean erase1;
        if (adult == null || adult.equals("null")) {
            adult1 = null;
        } else {
            adult1 = Boolean.valueOf(adult);
        }
        if (active == null || active.equals("null")) {
            active1 = null;
        } else {
            active1 = Boolean.valueOf(active);
        }
        if (erase == null || erase.equals("null")) {
            erase1 = null;
        } else {
            erase1 = Boolean.valueOf(erase);
        }
        return ResponseEntity.ok(memberService.getAllMemberDTO(adult1, active1, erase1));
    }

    @GetMapping("/pesel")
    public ResponseEntity<?> getMemberPeselIsPresent(@RequestParam String pesel) {
        return ResponseEntity.ok(memberService.getMemberPeselIsPresent(pesel));
    }

    @GetMapping("/IDCard")
    public ResponseEntity<?> getMemberIDCardPresent(@RequestParam String IDCard) {
        return ResponseEntity.ok(memberService.getMemberIDCardPresent(IDCard));
    }

    @GetMapping("/email")
    public ResponseEntity<?> getMemberEmailPresent(@RequestParam String email) {
        return ResponseEntity.ok(memberService.getMemberEmailPresent(email));
    }

    @GetMapping("/erasedType")
    public ResponseEntity<?> getErasedType() {
        return ResponseEntity.ok(memberService.getErasedType());
    }

    @Transactional
    @PostMapping("/exportMemberToSOZ")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<String> exportMemberToSOZ(
            @RequestParam String memberUUID
    ) throws IOException {
        byte[] xlsx = xlsxFilesService.generateMemberXlsxForSoz(memberUUID);
        sozClient.uploadAndLoad(xlsx);
        return ResponseEntity.ok(
                "Dane zawodnika zostały poprawnie przekazane do SOZ"
        );
    }


    @Transactional
    @PostMapping("/")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> addMember(@RequestBody @Valid MemberWithAddressWrapper memberWithAddressWrapper, @RequestParam boolean returningToClub) {
        Member member = memberWithAddressWrapper.getMember();
        Address address = memberWithAddressWrapper.getAddress();
        if (member.getPesel().isEmpty() || member.getPhoneNumber().isEmpty() || member.getFirstName().isEmpty() || member.getSecondName().isEmpty() || member.getIDCard().isEmpty()) {
            return ResponseEntity.status(406).body("Uwaga! Nie podano wszystkich informacji");
        }
        return memberService.addNewMember(member, address, returningToClub);
    }

    @PostMapping("/note")
    @Transactional
    public ResponseEntity<?> addNote(@RequestParam String uuid, @RequestParam String note) {
        return memberService.addNote(uuid, note);
    }

    @PutMapping("/{uuid}")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> updateMember(@PathVariable String uuid, @RequestBody @Valid Member member) {
        return memberService.updateMember(uuid, member);
    }

    @PutMapping("/group/{uuid}")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> assignMemberToGroup(@PathVariable String uuid, @RequestParam Long groupId) {
        return memberService.assignMemberToGroup(uuid, groupId);
    }

    @GetMapping("/getMembersToReportToThePolice")
    public ResponseEntity<?> getMembersToReportToThePolice() {
        return ResponseEntity.ok(memberService.getMembersToReportToThePolice());
    }

    @GetMapping("/getMembersToErase")
    public ResponseEntity<?> getMembersToErase() {
        return ResponseEntity.ok(memberService.getMembersToErase());
    }

    @GetMapping("/findByBarCode")
    public ResponseEntity<?> findMemberByBarCode(@RequestParam String barcode) {
        return memberService.findMemberByBarCode(barcode);
    }

    @Transactional
    @PatchMapping("/adult/{uuid}")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> changeAdult(@PathVariable String uuid) {
        return memberService.changeAdult(uuid);
    }

    @PatchMapping("/togglePzss/{uuid}")
    public ResponseEntity<?> togglePzss(@PathVariable String uuid, @RequestParam boolean isSignedTo) {
        return memberService.togglePzss(uuid, isSignedTo);
    }

    @PatchMapping("/{uuid}")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> activateOrDeactivateMember(@PathVariable String uuid) {
        return memberService.activateOrDeactivateMember(uuid);
    }

    @PatchMapping("/erase/{uuid}")
    @RequirePermissions(value = {UserSubType.MANAGEMENT, UserSubType.WORKER})
    public ResponseEntity<?> eraseMember(@PathVariable String uuid, @RequestParam String additionalDescription, @RequestParam String erasedDate, @RequestParam String erasedType) {
        if (additionalDescription.trim().isBlank() || additionalDescription.trim().equals("null")) {
            additionalDescription = null;
        }
        if (erasedDate.trim().isBlank() || erasedDate.trim().equals("null")) {
            erasedDate = String.valueOf(LocalDate.now());
        }
        LocalDate parsedDate = LocalDate.parse(erasedDate);
        return memberService.eraseMember(uuid, erasedType, parsedDate, additionalDescription);
    }

    @PatchMapping("/changeClub/{uuid}")
    public ResponseEntity<?> changeClub(@PathVariable String uuid, @RequestParam int clubID) {
        return memberService.changeClub(uuid, clubID);
    }

    @PatchMapping("/toggleDeclaration/{uuid}")
    public ResponseEntity<?> toggleDeclaration(@PathVariable String uuid, @RequestParam boolean isSigned) {
        return memberService.toggleDeclaration(uuid, isSigned);
    }

}
