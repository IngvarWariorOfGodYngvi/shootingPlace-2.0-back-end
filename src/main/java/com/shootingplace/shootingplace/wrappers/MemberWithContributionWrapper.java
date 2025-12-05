package com.shootingplace.shootingplace.wrappers;

import com.shootingplace.shootingplace.contributions.Contribution;
import com.shootingplace.shootingplace.member.IMemberDTO;
import lombok.Builder;

@Builder
public class MemberWithContributionWrapper {
    IMemberDTO member;
    Contribution contribution;

    public IMemberDTO getMember() {
        return member;
    }

    public void setMember(IMemberDTO member) {
        this.member = member;
    }

    public Contribution getContribution() {
        return contribution;
    }

    public void setContribution(Contribution contribution) {
        this.contribution = contribution;
    }
}
