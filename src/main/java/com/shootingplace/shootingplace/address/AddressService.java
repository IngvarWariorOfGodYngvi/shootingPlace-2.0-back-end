package com.shootingplace.shootingplace.address;

import com.shootingplace.shootingplace.history.HistoryEntityType;
import com.shootingplace.shootingplace.changeHistory.RecordHistory;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final MemberRepository memberRepository;
    private final Logger LOG = LogManager.getLogger(getClass());

    @Transactional
    @RecordHistory(action = "ADDRESS.updateAddress", entity = HistoryEntityType.ADDRESS, entityArgIndex = 0)
    public ResponseEntity<?> updateAddress(String memberUUID, Address address) {
        MemberEntity member = memberRepository.findById(memberUUID).orElse(null);
        if (member == null) {
            LOG.info("Nie znaleziono Klubowicza");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono Klubowicza");
        }
        AddressEntity addressEntity = member.getAddress();
        if (address.getZipCode() != null && !address.getZipCode().isEmpty()) {
            addressEntity.setZipCode(address.getZipCode());
        }
        if (address.getPostOfficeCity() != null && !address.getPostOfficeCity().isEmpty()) {
            addressEntity.setPostOfficeCity(normalizeName(address.getPostOfficeCity()));
        }
        if (address.getStreet() != null && !address.getStreet().isEmpty()) {
            addressEntity.setStreet(normalizeName(address.getStreet()));
        }
        if (address.getStreetNumber() != null && !address.getStreetNumber().isEmpty()) {
            addressEntity.setStreetNumber(address.getStreetNumber().toUpperCase());
        }
        if (address.getFlatNumber() != null && !address.getFlatNumber().isEmpty()) {
            addressEntity.setFlatNumber(address.getFlatNumber().toUpperCase());
        }
        addressRepository.save(addressEntity);
        LOG.info("Zaktualizowano adres dla {}", member.getFullName());
        return ResponseEntity.ok("Zaktualizowano adres " + member.getFullName());
    }

    private String normalizeName(String value) {
        String[] parts = value.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            result.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase()).append(" ");
        }
        return result.toString().trim();
    }

}
