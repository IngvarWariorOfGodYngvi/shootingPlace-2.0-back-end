package com.shootingplace.shootingplace.member;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErasedEntity {

    @Id
    @UuidGenerator
    private String uuid;

    private LocalDate date;
    private LocalDate inputDate;
    private String erasedType;
    private String additionalDescription;

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setErasedType(String erasedType) {
        this.erasedType = erasedType;
    }

    public void setAdditionalDescription(String additionalDescription) {
        this.additionalDescription = additionalDescription;
    }

    public void setInputDate(LocalDate inputDate) {
        if (this.inputDate != null) {
            System.out.println("nie można zmienić tej daty");
        }else this.inputDate = inputDate;
    }
}
