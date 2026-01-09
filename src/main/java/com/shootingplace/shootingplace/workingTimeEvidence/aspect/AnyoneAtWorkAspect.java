package com.shootingplace.shootingplace.workingTimeEvidence.aspect;

import com.shootingplace.shootingplace.exceptions.workingTimeExceptions.NoOneAtWorkException;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AnyoneAtWorkAspect {

    private final WorkingTimeEvidenceRepository workingTimeEvidenceRepository;

    @Before("@annotation(com.shootingplace.shootingplace.workingTimeEvidence.aspect.RequireAnyoneAtWork)")
    public void checkAnyoneAtWork() {

        boolean anyoneAtWork =
                workingTimeEvidenceRepository.existsByIsCloseFalse();

        if (!anyoneAtWork) {
            throw new NoOneAtWorkException();
        }
    }
}

