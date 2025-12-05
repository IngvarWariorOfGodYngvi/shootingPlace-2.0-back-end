package com.shootingplace.shootingplace.file;

import java.time.LocalDate;
import java.time.LocalTime;

public interface IFile {
    String getUuid();
    String getName();
    String getType();
    String getBelong_to_memberuuid();
    int getVersion();
    long getSize();
    LocalDate getDate();
    LocalTime getTime();
}
