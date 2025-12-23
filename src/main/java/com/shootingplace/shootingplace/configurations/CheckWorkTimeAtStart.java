package com.shootingplace.shootingplace.configurations;

import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckWorkTimeAtStart {

    private final WorkingTimeEvidenceService workRepo;

    @EventListener(ContextRefreshedEvent.class)
    public void contextRefreshedEvent() {
        workRepo.closeAllActiveWorkTime();
    }
}
