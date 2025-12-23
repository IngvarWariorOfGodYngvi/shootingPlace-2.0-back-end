package com.shootingplace.shootingplace.file;

import com.shootingplace.shootingplace.armory.Gun;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilesModel {

    private String uuid;
    private String belongToMemberUUID;

    private String name;
    private String type;

    private byte[] data;
    private LocalDate date;
    private LocalTime time;
    private long size;
    private Gun gun;
    private int version;

    public void incrementVersion() {
        this.version += 1;
    }
}
