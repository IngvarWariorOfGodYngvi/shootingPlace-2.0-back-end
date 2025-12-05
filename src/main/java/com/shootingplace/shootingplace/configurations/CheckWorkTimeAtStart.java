package com.shootingplace.shootingplace.configurations;

import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceService;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class CheckWorkTimeAtStart {

    private final WorkingTimeEvidenceService workRepo;

    public CheckWorkTimeAtStart(WorkingTimeEvidenceService workRepo) {
        this.workRepo = workRepo;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void contextRefreshedEvent() {
        workRepo.closeAllActiveWorkTime();
    }
}
