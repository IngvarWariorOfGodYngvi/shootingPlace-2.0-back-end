package com.shootingplace.shootingplace.wrappers;

import com.shootingplace.shootingplace.otherPerson.OtherPerson;
import lombok.Data;

@Data
public class ImageOtherPersonWrapper {
    private OtherPerson other;
    private String imageString;
}
