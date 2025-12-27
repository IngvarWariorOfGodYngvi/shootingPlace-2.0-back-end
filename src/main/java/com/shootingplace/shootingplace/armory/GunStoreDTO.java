package com.shootingplace.shootingplace.armory;

import lombok.*;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GunStoreDTO {
    private String typeName;

    private List<Gun> gunList;
    private List<Gun> gunRemovedList;

}
