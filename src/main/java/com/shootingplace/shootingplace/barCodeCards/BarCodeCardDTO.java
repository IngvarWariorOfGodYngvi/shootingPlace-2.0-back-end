package com.shootingplace.shootingplace.barCodeCards;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BarCodeCardDTO {

    private String barCode;

    private boolean isActive;
    /**
     *  If Member - enter first name and second name.
     *  If User - enter name and second name
     */
    private String belongsTo;

    private String subType;

    private boolean isMaster;

}
