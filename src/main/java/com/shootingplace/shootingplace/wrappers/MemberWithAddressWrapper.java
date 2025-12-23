package com.shootingplace.shootingplace.wrappers;

import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.member.Member;
import lombok.Data;

@Data
public class MemberWithAddressWrapper {
    Member member;
    Address address;
}
