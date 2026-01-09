package com.shootingplace.shootingplace.security;

import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.exceptions.pinExceptions.InvalidPinException;
import com.shootingplace.shootingplace.exceptions.pinExceptions.PinTooShortException;
import com.shootingplace.shootingplace.exceptions.userStateExceptions.MissingPermissionException;
import com.shootingplace.shootingplace.exceptions.userStateExceptions.UserNotAtWorkException;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserRepository userRepository;
    private final WorkingTimeEvidenceRepository workingTimeEvidenceRepository;

    public UserEntity authenticate(String pinCode) {

        if (pinCode == null || pinCode.length() < 4) {
            throw new PinTooShortException();
        }

        String hash = Hashing.sha256()
                .hashString(pinCode, StandardCharsets.UTF_8)
                .toString();

        return userRepository.findByPinCode(hash)
                .orElseThrow(InvalidPinException::new);
    }

    public void hasAnyPermission(
            UserEntity user,
            UserSubType[] required,
            boolean requireWork
    ) {

        boolean hasPermission = Arrays.stream(required)
                .map(UserSubType::getName)
                .anyMatch(user.getUserPermissionsList()::contains);

        if (!hasPermission) {
            throw new MissingPermissionException();
        }

        boolean isAdmin = user.getUserPermissionsList()
                .contains(UserSubType.ADMIN.getName());

        if (!requireWork || isAdmin) {
            return;
        }

        boolean inWork = workingTimeEvidenceRepository
                .existsByUserAndIsCloseFalse(user);

        if (!inWork) {
            throw new UserNotAtWorkException();
        }
    }

    public UserEntity getAuthenticatedUser(String pinCode) {
        return authenticate(pinCode);
    }
}
