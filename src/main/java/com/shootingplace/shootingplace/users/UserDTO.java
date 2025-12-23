package com.shootingplace.shootingplace.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private String uuid;

    private String firstName;

    private String secondName;

    private String userPermissionsList;

    public List<String> getUserPermissionsList() {
        List<String> vals = new ArrayList<>();
        if (userPermissionsList != null) {
            vals.addAll(Arrays.asList(userPermissionsList.split(";")));
        }
        return vals;
    }

    public void setUserPermissionsList(List<String> userPermissionsList) {
        String value = "";
        for (String f : userPermissionsList) {
            value = value.concat(f + ";");
        }
        this.userPermissionsList = value;
    }
}
