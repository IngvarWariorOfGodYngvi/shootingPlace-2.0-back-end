package com.shootingplace.shootingplace.member;

import java.time.LocalDate;

public interface IMemberDTO {
    String getUuid();
    String getFirst_name();
    String getSecond_name();
    String getImageuuid();
    String getNote();
    String getEmail();
    boolean getActive();
    boolean getAdult();
    boolean getErased();
    boolean getPzss();
    int getLegitimation_number();
    LocalDate getJoin_date();
}
