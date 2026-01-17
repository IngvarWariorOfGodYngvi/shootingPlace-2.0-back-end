package com.shootingplace.shootingplace.file.pageStamper;

import com.shootingplace.shootingplace.enums.ProfilesEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BrandingResolver {

    private final Environment environment;

    private static final Map<ProfilesEnum, String> LOGOS = new EnumMap<>(ProfilesEnum.class);

    static {
        LOGOS.put(ProfilesEnum.DZIESIATKA, "pełna-nazwa(małe).bmp");
        LOGOS.put(ProfilesEnum.TEST, "pełna-nazwa(małe).bmp");
        LOGOS.put(ProfilesEnum.PANASZEW, "logo-panaszew.jpg");
        LOGOS.put(ProfilesEnum.MECHANIK, "logo-uks.jpg");
        LOGOS.put(ProfilesEnum.GUARDIANS, "logo-guardians.jpg");
    }

    public URL resolveFooterLogo() {
        ProfilesEnum profile = resolveProfile();
        String resource = LOGOS.get(profile);
        return resource != null
                ? getClass().getClassLoader().getResource(resource)
                : null;
    }

    private ProfilesEnum resolveProfile() {
        for (String p : environment.getActiveProfiles()) {
            for (ProfilesEnum e : ProfilesEnum.values()) {
                if (e.getName().equals(p)) {
                    return e;
                }
            }
        }
        throw new IllegalStateException("Brak rozpoznanego profilu aplikacji");
    }
}

