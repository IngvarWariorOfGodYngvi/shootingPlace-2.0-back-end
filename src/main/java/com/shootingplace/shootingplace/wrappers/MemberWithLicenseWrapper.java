package com.shootingplace.shootingplace.wrappers;

import com.shootingplace.shootingplace.license.LicenseEntity;
import com.shootingplace.shootingplace.member.IMemberDTO;
import lombok.Builder;

@Builder
public class MemberWithLicenseWrapper {
    IMemberDTO imemberDTO;
    LicenseEntity licenseEntity;
}
