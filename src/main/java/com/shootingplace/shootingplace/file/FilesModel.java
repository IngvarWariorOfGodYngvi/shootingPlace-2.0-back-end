package com.shootingplace.shootingplace.file;

import com.shootingplace.shootingplace.armory.Gun;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

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

    public int getVersion() {
        return version;
    }

    public void incrementVersion() {
        this.version += 1;
    }

    public Gun getGun() {
        return gun;
    }

    public void setGun(Gun gun) {
        this.gun = gun;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getBelongToMemberUUID() {
        return belongToMemberUUID;
    }

    public void setBelongToMemberUUID(String belongToMemberUUID) {
        this.belongToMemberUUID = belongToMemberUUID;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
