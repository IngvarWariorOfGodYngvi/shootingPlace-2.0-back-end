package com.shootingplace.shootingplace.member;


import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
import com.shootingplace.shootingplace.wrappers.MemberWithAddressWrapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;


@RestController
@RequestMapping("/member" )
@CrossOrigin
public class MemberController {

    private final MemberService memberService;
    private final ChangeHistoryService changeHistoryService;

    public MemberController(MemberService memberService, ChangeHistoryService changeHistoryService) {
        this.memberService = memberService;
        this.changeHistoryService = changeHistoryService;
    }

    @GetMapping("/{number}" )
    public ResponseEntity<?> getMember(@PathVariable int number) {
        return memberService.getMember(number);
    }

    @GetMapping("/ID/{number}" )
    public ResponseEntity<?> getMemberUUIDByLegitimationNumber(@PathVariable int number) {
        return memberService.getMemberUUIDByLegitimationNumber(number);
    }

    @GetMapping("/PESEL/{PESELNumber}" )
    public ResponseEntity<?> getMemberByPESELNumber(@PathVariable String PESELNumber) {
        return memberService.getMemberByPESELNumber(PESELNumber);
    }

    @GetMapping("/uuid/{uuid}" )
    public ResponseEntity<MemberEntity> getMemberByUUID(@PathVariable String uuid) {
        return ResponseEntity.ok(memberService.getMemberByUUID(uuid));
    }

    @GetMapping("/erased" )
    public ResponseEntity<?> getErasedMembers(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        return ResponseEntity.ok(memberService.getMembersErased(parseFirstDate, parseSecondDate));
    }
    @GetMapping("/reportView" )
    public ResponseEntity<?> getMembersToReportToPoliceView(@RequestParam String firstDate, @RequestParam String secondDate) {
        LocalDate parseFirstDate = LocalDate.parse(firstDate);
        LocalDate parseSecondDate = LocalDate.parse(secondDate);
        return ResponseEntity.ok(memberService.getMembersToReportToPoliceView(parseFirstDate, parseSecondDate));
    }

    @GetMapping("/getAllNames" )
    public List<MemberInfo> getAllNames() {
        return memberService.getAllNames();
    }
    @GetMapping("/getAdvancedSearch")
    public List<MemberDTO> getAdvancedSearch(@RequestParam boolean isErased, @RequestParam int searchType, @RequestParam String inputText) {
        return memberService.getAdvancedSearch(isErased,searchType,inputText);
    }
    @GetMapping("/getAllNamesErased" )
    public List<MemberInfo> getAllNamesErased() {
        return memberService.getAllNamesErased();
    }

    @GetMapping("/getAllMemberDTO" )
    public ResponseEntity<List<MemberDTO>> getAllMemberDTO() {
        return ResponseEntity.ok(memberService.getAllMemberDTO());
    }

    @GetMapping("/getAllMemberDTOWithArgs" )
    public ResponseEntity<List<MemberDTO>> getAllMemberDTO(@RequestParam @Nullable String adult, @Nullable @RequestParam String active, @RequestParam String erase) {
        Boolean adult1;
        Boolean active1;
        Boolean erase1;
        if (adult == null || adult.equals("null" )) {
            adult1 = null;
        } else {
            adult1 = Boolean.valueOf(adult);
        }
        if (active == null || active.equals("null" )) {
            active1 = null;
        } else {
            active1 = Boolean.valueOf(active);
        }
        if (erase == null || erase.equals("null" )) {
            erase1 = null;
        } else {
            erase1 = Boolean.valueOf(erase);
        }
        return ResponseEntity.ok(memberService.getAllMemberDTO(adult1, active1, erase1));
    }

    @GetMapping("/getArbiters" )
    public List<MemberInfo> getArbiters() {
        return memberService.getArbiters();
    }

    @GetMapping("/pesel" )
    public ResponseEntity<?> getMemberPeselIsPresent(@RequestParam String pesel) {
        return ResponseEntity.ok(memberService.getMemberPeselIsPresent(pesel));
    }

    @GetMapping("/IDCard" )
    public ResponseEntity<?> getMemberIDCardPresent(@RequestParam String IDCard) {
        return ResponseEntity.ok(memberService.getMemberIDCardPresent(IDCard));
    }

    @GetMapping("/email" )
    public ResponseEntity<?> getMemberEmailPresent(@RequestParam String email) {
        return ResponseEntity.ok(memberService.getMemberEmailPresent(email));
    }

    @GetMapping("/erasedType" )
    public ResponseEntity<?> getErasedType() {
        return ResponseEntity.ok(memberService.getErasedType());
    }

    @Transactional
    @PostMapping("/" )
    public ResponseEntity<?> addMember(@RequestBody @Valid MemberWithAddressWrapper memberWithAddressWrapper, @RequestParam boolean returningToClub, @RequestParam String pinCode) throws NoUserPermissionException {
        List<String> acceptedPermissions = Arrays.asList(UserSubType.MANAGEMENT.getName(), UserSubType.WORKER.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode,acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            ResponseEntity<?> result;
            Member member = memberWithAddressWrapper.getMember();
            Address address = memberWithAddressWrapper.getAddress();
            if (member.getPesel().isEmpty() || member.getPhoneNumber().isEmpty() || member.getFirstName().isEmpty() || member.getSecondName().isEmpty() || member.getIDCard().isEmpty()) {
                result = ResponseEntity.status(406).body("Uwaga! Nie podano wszystkich lub żadnej informacji" );
            } else {
                try {
                    result = memberService.addNewMember(member, address, returningToClub, pinCode);
                } catch (IllegalArgumentException e) {
                    result = ResponseEntity.status(HttpStatus.CONFLICT).build();
                }
            }
            return result;
        } else {
            return code;
        }
    }
    @PostMapping("/note")
    @Transactional
    public ResponseEntity<?> addNote(@RequestParam String uuid, @RequestParam String note) {
        return memberService.addNote(uuid, note);
    }
    @PutMapping("/{uuid}" )
    public ResponseEntity<?> updateMember(@PathVariable String uuid, @RequestBody @Valid Member member, @RequestParam String pinCode) throws NoUserPermissionException {
        List<String> acceptedPermissions = Arrays.asList(UserSubType.MANAGEMENT.getName(), UserSubType.WORKER.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode,acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return memberService.updateMember(uuid, member, pinCode);
        } else {
            return code;
        }
    }
    @PutMapping("/group/{uuid}" )
    public ResponseEntity<?> assignMemberToGroup(@PathVariable String uuid,@RequestParam Long groupId, @RequestParam String pinCode) throws NoUserPermissionException {
        List<String> acceptedPermissions = Arrays.asList(UserSubType.MANAGEMENT.getName(), UserSubType.WORKER.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode,acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return memberService.assignMemberToGroup(uuid, groupId, pinCode);
        } else {
            return code;
        }
    }

    @GetMapping("/getMembersToReportToThePolice" )
    public ResponseEntity<?> getMembersToReportToThePolice() {
        return ResponseEntity.ok(memberService.getMembersToReportToThePolice());
    }

    @GetMapping("/getMembersToErase" )
    public ResponseEntity<?> getMembersToErase() {
        return ResponseEntity.ok(memberService.getMembersToErase());
    }

    @GetMapping("/findByBarCode" )
    public ResponseEntity<?> findMemberByBarCode(@RequestParam String barcode) {
        return memberService.findMemberByBarCode(barcode);
    }

    @Transactional
    @PatchMapping("/adult/{uuid}" )
    public ResponseEntity<?> changeAdult(@PathVariable String uuid, @RequestParam String pinCode) throws NoUserPermissionException {
        List<String> acceptedPermissions = Arrays.asList(UserSubType.MANAGEMENT.getName(), UserSubType.WORKER.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode,acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return memberService.changeAdult(uuid, pinCode);
        } else {
            return code;
        }
    }

    @PatchMapping("/togglePzss/{uuid}" )
    public ResponseEntity<?> togglePzss(@PathVariable String uuid, @RequestParam boolean isSignedTo) {
        return memberService.togglePzss(uuid, isSignedTo);
    }

    @PatchMapping("/{uuid}" )
    public ResponseEntity<?> activateOrDeactivateMember(@PathVariable String uuid, @RequestParam String pinCode) throws NoUserPermissionException {
        List<String> acceptedPermissions = Arrays.asList(UserSubType.MANAGEMENT.getName(), UserSubType.WORKER.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode,acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            return memberService.activateOrDeactivateMember(uuid, pinCode);
        } else {
            return code;
        }
    }

    @PatchMapping("/erase/{uuid}" )
    public ResponseEntity<?> eraseMember(@PathVariable String uuid, @RequestParam String additionalDescription, @RequestParam String erasedDate, @RequestParam String erasedType, @RequestParam String pinCode) throws NoUserPermissionException {
        List<String> acceptedPermissions = Arrays.asList(UserSubType.MANAGEMENT.getName(), UserSubType.WORKER.getName());
        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode,acceptedPermissions);
        if (code.getStatusCode().equals(HttpStatus.OK)) {
            if (additionalDescription.trim().isEmpty() || additionalDescription.trim().isBlank() || additionalDescription.trim().equals("null" )) {
                additionalDescription = null;
            }
            if (erasedDate.trim().isEmpty() || erasedDate.trim().isBlank() || erasedDate.trim().equals("null" )) {
                erasedDate = String.valueOf(LocalDate.now());
            }
            LocalDate parsedDate = LocalDate.parse(erasedDate);
            return memberService.eraseMember(uuid, erasedType, parsedDate, additionalDescription, pinCode);
        } else {
            return code;
        }
    }

    @PatchMapping("/changeClub/{uuid}" )
    public ResponseEntity<?> changeClub(@PathVariable String uuid, @RequestParam int clubID) {
        return memberService.changeClub(uuid, clubID);
    }
    @PatchMapping("/toggleDeclaration/{uuid}" )
    public ResponseEntity<?> toggleDeclaration(@PathVariable String uuid, @RequestParam boolean isSigned) {
        return memberService.toggleDeclaration(uuid, isSigned);
    }

}
