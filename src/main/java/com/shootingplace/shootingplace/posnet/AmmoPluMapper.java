package com.shootingplace.shootingplace.posnet;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AmmoPluMapper {

    private final AmmoPluMappingProperties props;

    public Integer mapUuidToPlu(String caliberUuid) {
        Integer plu = props.getPluMapping().get(caliberUuid);
        if (plu == null) {
            throw new IllegalStateException(
                    "Brak mapowania UUID kalibru na PLU: " + caliberUuid
            );
        }
        return plu;
    }
}
