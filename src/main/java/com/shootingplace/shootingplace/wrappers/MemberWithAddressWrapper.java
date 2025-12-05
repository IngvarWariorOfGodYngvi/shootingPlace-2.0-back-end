package com.shootingplace.shootingplace.wrappers;

import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.member.Member;

public class MemberWithAddressWrapper {
    Member member;
    Address address;

    public Member getMember() {
        return member;
    }

    public Address getAddress() {
        return address;
    }
}
