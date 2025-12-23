package com.shootingplace.shootingplace.file;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.core.annotation.Order;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
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

    public void setBelongToMemberUUID(String belongToMemberUUID) {
        this.belongToMemberUUID = belongToMemberUUID;
    }

    public void setVersion(int version) {
        this.version = version;
    }
    public void incrementVersion() {
        this.version += 1;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
