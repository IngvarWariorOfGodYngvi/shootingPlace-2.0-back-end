package com.shootingplace.shootingplace.security;

import com.google.common.hash.Hashing;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.users.UserRepository;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceRepository;
import jakarta.persistence.EntityNotFoundException;
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
        String hash = Hashing.sha256()
                .hashString(pinCode, StandardCharsets.UTF_8)
                .toString();

        return userRepository.findByPinCode(hash)
                .orElseThrow(() -> new EntityNotFoundException("Błędny PIN"));
    }

    public void hasAnyPermission(
            UserEntity user,
            UserSubType[] required,
            boolean requireWork
    ) {

        boolean hasPermission = Arrays.stream(required)
                .map(UserSubType::getName)
                .anyMatch(p -> user.getUserPermissionsList().contains(p));

        if (!hasPermission) {
            throw new SecurityException("Brak uprawnień");
        }

        boolean isAdmin = user.getUserPermissionsList().contains(UserSubType.ADMIN.getName());

        if (!requireWork || isAdmin) {
            return;
        }

        boolean inWork = workingTimeEvidenceRepository.findAll().stream()
                .anyMatch(e -> e.getUser().equals(user) && !e.isClose());

        if (!inWork) {
            throw new SecurityException("Użytkownik nie jest w pracy");
        }
    }
    public UserEntity getAuthenticatedUser(String pinCode) {
        String hash = Hashing.sha256()
                .hashString(pinCode, StandardCharsets.UTF_8)
                .toString();

        return userRepository.findByPinCode(hash)
                .orElseThrow(() -> new EntityNotFoundException("Brak użytkownika"));
    }

}
