package com.shootingplace.shootingplace.posnet;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "ammo")
public class AmmoPluMappingProperties {

    private Map<String, Integer> pluMapping;

}
