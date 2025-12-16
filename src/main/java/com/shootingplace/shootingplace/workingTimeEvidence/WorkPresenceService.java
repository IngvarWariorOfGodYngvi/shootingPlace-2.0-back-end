package com.shootingplace.shootingplace.workingTimeEvidence;

import com.shootingplace.shootingplace.users.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkPresenceService {

    private final WorkingTimeEvidenceService workServ;

    public void requireInWork(UserEntity user) {
        if (!workServ.isInWork(user)) {
            throw new IllegalStateException("Najpierw zarejestruj pobyt w Klubie");
        }
    }
}
