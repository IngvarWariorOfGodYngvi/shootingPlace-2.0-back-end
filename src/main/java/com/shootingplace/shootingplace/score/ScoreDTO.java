package com.shootingplace.shootingplace.score;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScoreDTO {
    private String name;
    private int metricNumber;
    private String full;

}
