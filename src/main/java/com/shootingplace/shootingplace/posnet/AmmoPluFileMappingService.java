package com.shootingplace.shootingplace.posnet;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class AmmoPluFileMappingService {

    private final Map<String, Integer> mapping = new HashMap<>();

    public AmmoPluFileMappingService() {
        load();
    }

    private void load() {
        try (InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("ammo-plu.map")) {

            if (is == null) {
                throw new IllegalStateException(
                        "Brak pliku mapowania ammo-plu.map na classpath"
                );
            }

            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(is))) {

                reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .filter(line -> !line.startsWith("#"))
                        .forEach(line -> {
                            String[] parts = line.split("=");
                            mapping.put(
                                    parts[0].trim(),
                                    Integer.parseInt(parts[1].trim())
                            );
                        });
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Błąd podczas czytania ammo-plu.map",
                    e
            );
        }
    }

    public Integer mapUuidToPlu(String uuid) {
        Integer plu = mapping.get(uuid);
        if (plu == null) {
            throw new IllegalStateException(
                    "Brak mapowania UUID kalibru na PLU: " + uuid
            );
        }
        return plu;
    }
}

