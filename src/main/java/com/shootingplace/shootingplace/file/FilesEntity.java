package com.shootingplace.shootingplace.file;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.core.annotation.Order;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilesEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private String belongToMemberUUID;

    private String name;
    private String type;
    @Lob
    private byte[] data;
    @Order
    private LocalDate date;
    private LocalTime time;
    private long size;
    private int version;

}
