package com.shootingplace.shootingplace.file.xlsx.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class XlsxGenerationResult {

    String fileName;
    byte[] data;

}
