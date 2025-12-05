package com.shootingplace.shootingplace.email;

import com.shootingplace.shootingplace.member.MemberEntity;
import lombok.Builder;

@Builder
public class EmailQueue {

    private EmailRequest request;
    private String mailType;
    private MemberEntity memberEntity;

    public EmailQueue(EmailRequest request, String mailType, MemberEntity memberEntity) {
        this.request = request;
        this.mailType = mailType;
        this.memberEntity = memberEntity;
    }

    public EmailRequest getRequest() {
        return request;
    }

    public String getMailType() {
        return mailType;
    }

    public MemberEntity getMemberEntity() {
        return memberEntity;
    }

}
