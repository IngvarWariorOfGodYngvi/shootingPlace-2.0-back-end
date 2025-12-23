package com.shootingplace.shootingplace.history;

import com.shootingplace.shootingplace.users.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeHistoryEntity {

    @Id
    @UuidGenerator
    private String uuid;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_uuid", nullable = false)
    private UserEntity userEntity;
    private String classNamePlusMethod;
    private String belongsTo;
    private LocalDate dayNow;
    private String timeNow;

}
