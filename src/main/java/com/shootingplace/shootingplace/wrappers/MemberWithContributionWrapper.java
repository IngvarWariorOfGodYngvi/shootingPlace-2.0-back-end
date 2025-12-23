package com.shootingplace.shootingplace.wrappers;

import com.shootingplace.shootingplace.contributions.Contribution;
import com.shootingplace.shootingplace.member.IMemberDTO;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MemberWithContributionWrapper {
    IMemberDTO member;
    Contribution contribution;
}
