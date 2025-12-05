package com.shootingplace.shootingplace.workingTimeEvidence;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserWithWorkingTimeList {

    private String uuid;

    private String firstName;

    private String secondName;

    private String subType;

    private String workTime;

    private List<WorkingTimeEvidenceDTO> WTEdtoList;


}
