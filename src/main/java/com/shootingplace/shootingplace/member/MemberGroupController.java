package com.shootingplace.shootingplace.member;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member-groups")
public class MemberGroupController {

    private final MemberGroupRepository memberGroupRepository;

    public MemberGroupController(MemberGroupRepository memberGroupRepository) {
        this.memberGroupRepository = memberGroupRepository;
    }

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody MemberGroupEntity request) {
        if (memberGroupRepository.existsByName(request.getName())) {
            return ResponseEntity.badRequest().body("Grupa o takiej nazwie już istnieje");
        }
        MemberGroupEntity group = MemberGroupEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .build();

        return ResponseEntity.ok(memberGroupRepository.save(group));
    }

    @GetMapping
    public List<MemberGroupEntity> getAllGroups() {
        return memberGroupRepository.findAll();
    }
}
