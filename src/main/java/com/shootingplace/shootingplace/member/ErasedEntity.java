package com.shootingplace.shootingplace.member;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErasedEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String uuid;

    private LocalDate date;
    private LocalDate inputDate;
    private String erasedType;
    private String additionalDescription;

    public String getUuid() {
        return uuid;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getErasedType() {
        return erasedType;
    }

    public void setErasedType(String erasedType) {
        this.erasedType = erasedType;
    }

    public String getAdditionalDescription() {
        return additionalDescription;
    }

    public void setAdditionalDescription(String additionalDescription) {
        this.additionalDescription = additionalDescription;
    }

    public LocalDate getInputDate() {
        return inputDate;
    }

    public void setInputDate(LocalDate inputDate) {
        if (this.inputDate != null) {
            System.out.println("nie można zmienić tej daty");
        }else this.inputDate = inputDate;
    }
}
