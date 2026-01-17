package com.shootingplace.shootingplace.changeHistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChangeHistoryDTO {

    private String classNamePlusMethod;
    private String belongsTo;
    private LocalDate dayNow;
    private String timeNow;
}
