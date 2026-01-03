package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.enums.ProfilesEnum;
import com.shootingplace.shootingplace.file.pageStamper.PageStampMode;
import com.shootingplace.shootingplace.file.pageStamper.PageStamper;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.member.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

import static com.shootingplace.shootingplace.file.utils.Utils.*;

@Component
@RequiredArgsConstructor
public class PersonalCardPdfGenerator {

    private final Environment environment;

    public PdfGenerationResults generate(MemberEntity member) throws DocumentException, IOException {

        String fileName = "Karta Czlonkowska " + member.getFirstName().stripTrailing() + " " + member.getSecondName().stripTrailing() + ".pdf";

        LocalDate birthDate = member.getBirthDate();

        // ====== STREAM ======
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        document.setMargins(35F, 35F, 35F, 70F);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new PageStamper(environment, false, true, PageStampMode.A4));

        document.open();

        document.addTitle(fileName);
        document.addCreationDate();

        // ====== KONFIGURACJA TEKSTÓW KLUBOWYCH ======
        String s = "", s1 = "", s2 = "", s3 = "", s4 = "", s5 = "";

        String profile = environment.getActiveProfiles()[0];

        if (profile.equals(ProfilesEnum.DZIESIATKA.getName()) || profile.equals(ProfilesEnum.TEST.getName())) {
            s = "Klubu Strzeleckiego „Dziesiątka” Ligi Obrony Kraju w Łodzi";
            s1 = "Klub Strzelecki „Dziesiątka”";
            s2 = "biuro@ksdziesiatka.pl";
            s3 = "Stowarzyszenie Liga Obrony Kraju mające siedzibę główną w Warszawie pod adresem: ";
            s4 = "ul. Chocimska 14, 00-791 Warszawa";
            s5 = "KS „Dziesiątka” LOK Łódź";
        }

        if (profile.equals(ProfilesEnum.PANASZEW.getName())) {
            s = "Klubu Strzeleckiego RCS Panaszew w Panaszewie";
            s1 = "Klubu Strzeleckiego RCS Panaszew";
            s2 = "biuro@rcspanaszew.pl";
            s3 = "Stowarzyszenie Klub Strzelecki RCS Panaszew mające siedzibę w Panaszewie pod adresem: ";
            s4 = "ul. Panaszew 4A, 99-200 Poddębice";
            s5 = "KS „RCS” Panaszew";
        }

        if (profile.equals(ProfilesEnum.MECHANIK.getName())) {
            s = "Uczniowskiego Klubu Sportowego Mechanik w Tomaszowie Mazowieckim";
            s1 = "Uczniowski Klub Sportowy Mechanik w Tomaszowie Mazowieckim";
            s2 = "biuro@uksmechanik.com";
            s3 = "Uczniowski Klub Sportowy MECHANIK mający siedzibę w Tomaszowie Mazowieckim pod adresem: ";
            s4 = "ul. Św. Antoniego 29, 93-200 Tomaszów Mazowiecki";
            s5 = "UKS „Mechanik” Tomaszów Mazowiecki";
        }

        String statement = "Oświadczenie:\n" + "- Zobowiązuję się do przestrzegania Regulaminu Strzelnicy, oraz Regulaminu " + s + ".\n" + "- Wyrażam zgodę na przesyłanie mi informacji przez " + s1 + " za pomocą środków komunikacji elektronicznej, w szczególności pocztą elektroniczną oraz w postaci sms-ów/mms-ów.\n" + "Wyrażenie zgody jest dobrowolne i może być odwołane w każdym czasie w na podstawie oświadczenia skierowanego na adres siedziby Klubu, na podstawie oświadczenia przesłanego za pośrednictwem poczty elektronicznej na adres: " + s2 + " lub w inny uzgodniony sposób.\n" + "- Zgadzam się na przetwarzanie moich danych osobowych przez Administratora Danych, którym jest " + s3 + "\n" + s4 + " w celach związanych z moim członkostwem w " + s5 + ".";

        String sex = member.getSex() ? "córki" : "syna";
        String adultAcceptation = "- Wyrażam zgodę na udział i członkostwo " + sex + " w Klubie";
        ClubEntity club = member.getClub();
        Paragraph p = new Paragraph(club.getFullName() + "\n", font(14, 1));
        p.setAlignment(1);

        String group = member.getMemberEntityGroup().getName();

        Paragraph p1 = new Paragraph("Karta Członkowska\n", font(13, 2));
        Phrase p2 = new Phrase(group, font(14, 1));

        Paragraph p3 = new Paragraph("\nNazwisko i Imię : ", font(11, 0));
        Phrase p4 = new Phrase(member.getSecondName() + " " + member.getFirstName(), font(18, 1));

        Phrase p5 = new Phrase("Numer Legitymacji : ", font(11, 0));
        Phrase p6 = new Phrase(String.valueOf(member.getLegitimationNumber()), font(18, 1));

        Paragraph p7 = new Paragraph("\nData Wstąpienia : ", font(11, 0));
        Phrase p8 = new Phrase(String.valueOf(member.getJoinDate()), font(15, 0));

        Paragraph p9 = new Paragraph("Data Urodzenia : ", font(11, 0));
        Phrase p10 = new Phrase(birthDate.format(dateFormat()), font(11, 0));

        Paragraph p11 = new Paragraph("PESEL : " + member.getPesel(), font(11, 0));
        Paragraph p12 = new Paragraph("", font(11, 0));
        Phrase p13 = new Phrase(member.getIDCard());

        String phone = member.getPhoneNumber();
        String phoneSplit = phone.substring(0, 3) + " " + phone.substring(3, 6) + " " + phone.substring(6, 9) + " " + phone.substring(9, 12);

        Paragraph p14 = new Paragraph("Telefon Kontaktowy : " + phoneSplit, font(11, 0));
        Paragraph p15 = new Paragraph(member.getEmail() != null ? "Email : " + member.getEmail() : "Email : Nie podano", font(11, 0));

        Paragraph p16 = new Paragraph("Adres Zamieszkania", font(11, 0));
        Paragraph p17 = new Paragraph("", font(11, 0));

        if (member.getAddress().getPostOfficeCity() != null)
            p17.add("Miasto : " + member.getAddress().getPostOfficeCity() + "\n");
        if (member.getAddress().getZipCode() != null)
            p17.add("Kod pocztowy : " + member.getAddress().getZipCode() + "\n");
        if (member.getAddress().getStreet() != null) p17.add("Ulica : " + member.getAddress().getStreet() + "\n");
        if (member.getAddress().getFlatNumber() != null)
            p17.add("Numer Mieszkania : " + member.getAddress().getFlatNumber() + "\n");

        Paragraph p18 = new Paragraph(statement, font(11, 0));
        Paragraph p19 = new Paragraph("\n\n.............................................", font(9, 0));

        p1.add("Grupa ");
        p1.add(p2);
        p1.setAlignment(1);

        p3.add(p4);
        p5.add(p6);
        p3.add(p5);

        p7.add(p8);
        p9.add(p10);
        p12.add(p13);

        document.add(p);
        document.add(p1);
        document.add(p3);
        document.add(p7);
        document.add(p9);
        document.add(p11);
        document.add(p12);
        document.add(p14);
        document.add(p15);
        document.add(p16);
        document.add(p17);
        document.add(p18);
        document.add(p19);

        document.close();

        return new PdfGenerationResults(fileName, baos.toByteArray());
    }
}