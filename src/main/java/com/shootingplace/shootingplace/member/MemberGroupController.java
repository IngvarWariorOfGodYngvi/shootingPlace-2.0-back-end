package com.shootingplace.shootingplace.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/memberGroups")
@CrossOrigin
@RequiredArgsConstructor
public class MemberGroupController {

    private final MemberGroupRepository memberGroupRepository;

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody MemberGroupEntity group) {
        if (memberGroupRepository.existsByName(group.getName())) {
            return ResponseEntity.badRequest().body("Grupa o takiej nazwie już istnieje");
        }
        MemberGroupEntity memberGroup = MemberGroupEntity.builder()
                .name(group.getName())
                .description(group.getDescription())
                .active(true)
                .build();
        memberGroupRepository.save(memberGroup);
        return ResponseEntity.ok("Utworzono nową grupę");
    }

    @GetMapping
    public List<MemberGroupEntity> getAllGroups() {
        return memberGroupRepository.findAll();
    }
}
