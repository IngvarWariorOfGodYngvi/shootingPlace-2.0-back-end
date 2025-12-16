package com.shootingplace.shootingplace.history;

import com.shootingplace.shootingplace.users.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ChangeHistoryService {

    private final ChangeHistoryRepository changeHistoryRepository;

    public void record(
            UserEntity user,
            String action,
            String belongsTo
    ) {
        ChangeHistoryEntity entry = ChangeHistoryEntity.builder()
                .userEntity(user)
                .classNamePlusMethod(action)
                .belongsTo(belongsTo)
                .dayNow(LocalDate.now())
                .timeNow(LocalTime.now().toString())
                .build();
        changeHistoryRepository.save(entry);
    }
}

