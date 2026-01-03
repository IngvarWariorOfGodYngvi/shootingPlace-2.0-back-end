package com.shootingplace.shootingplace.users;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Builder
@Data
public class UserCreateDTO {

    private String pinCode;
    private String memberUUID;
    private Integer otherID;

    private String firstName;

    private String secondName;

    private List<String> userPermissionsList;

}
