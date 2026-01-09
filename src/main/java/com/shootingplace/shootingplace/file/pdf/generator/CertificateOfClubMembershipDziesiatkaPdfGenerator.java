package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.shootingplace.shootingplace.file.pageStamper.PageStampMode;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.file.pageStamper.PageStamper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

import static com.shootingplace.shootingplace.file.utils.FilesUtils.*;

@Component
@RequiredArgsConstructor
public class CertificateOfClubMembershipDziesiatkaPdfGenerator {

    private final MemberRepository memberRepository;
    private final Environment environment;

    public PdfGenerationResults generate(String memberUUID, String reason, String city, boolean enlargement) throws DocumentException, IOException {
        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        String fileName = reason + " " + member.getFullName() + ".pdf";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new PageStamper(environment, false, true, PageStampMode.A4));

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        Paragraph newline = new Paragraph("\n", font(12, 0));

        // ========== MAPOWANIE DANYCH WPA ==========
        String[] choice = {"ZAŚWIADCZENIE ZWYKŁE", "BROŃ SPORTOWA DO CELÓW SPORTOWYCH", "BROŃ SPORTOWA DO CELÓW KOLEKCJONERSKICH", "BROŃ CIĘCIWOWA W POSTACI KUSZ"};

        String policeCity = "";
        String policeZip = "";
        String policeStreet = "";
        String policeNr = "";
        String policeAddress = "";

        switch (city) {
            case "Białystok" -> {
                policeCity = "w Białymstoku";
                policeZip = "15-369";
                policeStreet = "ul. Bema";
                policeNr = "4";
            }
            case "Bydgoszcz" -> {
                policeCity = "w Bydgoszczy";
                policeZip = "85-090";
                policeStreet = "al. Powstańców Wielkopolskich";
                policeNr = "7";
            }
            case "Gdańsk" -> {
                policeCity = "w Gdańsku";
                policeZip = "80-298";
                policeStreet = "ul. Harfowa";
                policeNr = "60";
            }
            case "Gorzów Wielkopolski" -> {
                policeCity = "w Gorzowie Wielkopolskim";
                policeZip = "66-400";
                policeStreet = "ul. Kwiatowa";
                policeNr = "10";
            }
            case "Katowice" -> {
                policeCity = "w Katowicach";
                policeZip = "40-038";
                policeStreet = "ul. Lompy";
                policeNr = "19";
            }
            case "Kielce" -> {
                policeCity = "w Kielcach";
                policeZip = "25-366";
                policeStreet = "ul. Śniadeckich";
                policeNr = "4";
            }
            case "Kraków" -> {
                policeCity = "w Krakowie";
                policeZip = "31-571";
                policeStreet = "ul. Mogilska";
                policeNr = "109";
            }
            case "Lublin" -> {
                policeCity = "w Lublinie";
                policeZip = "20-213";
                policeStreet = "ul. Gospodarcza";
                policeNr = "1b";
            }
            case "Łódź" -> {
                policeCity = "w Łodzi";
                policeZip = "91-048";
                policeStreet = "Lutomierska";
                policeNr = "108/112";
            }
            case "Olsztyn" -> {
                policeCity = "w Olsztynie";
                policeZip = "10-049";
                policeStreet = "ul. Wincentego Pstrowskiego";
                policeNr = "3";
            }
            case "Opole" -> {
                policeCity = "w Opolu";
                policeZip = "46-020";
                policeStreet = "ul. Powstańców Śląskich";
                policeNr = "20";
            }
            case "Poznań" -> {
                policeCity = "w Poznaniu";
                policeZip = "60-844";
                policeStreet = "ul. Kochanowskiego";
                policeNr = "2a";
            }
            case "Rzeszów" -> {
                policeCity = "w Rzeszowie";
                policeZip = "35-036";
                policeStreet = "ul. Dąbrowskiego";
                policeNr = "30";
            }
            case "Szczecin" -> {
                policeCity = "w Szczecinie";
                policeZip = "71-710";
                policeStreet = "ul. Bardzińska";
                policeNr = "1a";
            }
            case "Warszawa" -> {
                policeCity = "w Warszawie";
                policeZip = "00-150";
                policeStreet = "ul. Nowolipie";
                policeNr = "2";
            }
            case "Wrocław" -> {
                policeCity = "we Wrocławiu";
                policeZip = "50-040";
                policeStreet = "ul. Podwale";
                policeNr = "31/33";
            }
        }

        // jeśli z innego klubu → zawsze zwykłe
        if (!member.getClub().getId().equals(1)) {
            reason = choice[0];
        } else {
            if (reason.equals(choice[3])) {
                policeAddress = "\nKomendant Miejski Policji w Łodzi\n90-114 Łódź, ul. Henryka Sienkiewicza 28/30";
            } else {
                policeAddress = "\nKomendant Wojewódzki Policji " + policeCity + "\nWydział Postępowań Administracyjnych" + "\n" + policeZip + " " + city + ", " + policeStreet + " " + policeNr;
            }
        }

        // ========== DATA I NAGŁÓWEK ==========
        Paragraph date = new Paragraph("Panaszew, " + LocalDate.now().format(dateFormat()), font(12, 0));
        date.setAlignment(Element.ALIGN_RIGHT);
        document.add(date);

        if (!reason.equals(choice[0])) {
            Paragraph wpa = new Paragraph(policeAddress, font(12, 0));
            wpa.setAlignment(Element.ALIGN_RIGHT);
            document.add(wpa);
        }

        Paragraph title = new Paragraph("\n\nZaświadczenie\n\n", font(14, 1));
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // ========== TREŚĆ ==========
        String pesel = (!reason.equals(choice[0])) ? " PESEL: " + member.getPesel() : "";

        String sexWord = member.getSex() ? "Pani" : "Pan";
        String verb = member.getSex() ? "wystąpiła" : "wystąpił";

        Paragraph p1 = new Paragraph(sexWord + " " + member.getFullName() + pesel + " jest czynnym członkiem Klubu Strzeleckiego „Dziesiątka” LOK w Łodzi. " + "Numer legitymacji klubowej: " + member.getLegitimationNumber() + ". Uczestniczy w zawodach i treningach strzeleckich osiągając bardzo dobre wyniki. " + "Czynnie uczestniczy w życiu Klubu.", font(12, 0));
        p1.setFirstLineIndent(40);
        document.add(p1);

        if (!reason.equals(choice[0])) {
            Paragraph p2 = new Paragraph(sexWord + " " + member.getFullName() + " wyraża chęć pogłębiania swojej wiedzy i umiejętności w sporcie strzeleckim.", font(12, 0));
            p2.setFirstLineIndent(40);
            document.add(p2);
        }

        String year = (member.getLicense() != null && member.getLicense().getValidThru() != null) ? String.valueOf(member.getLicense().getValidThru().getYear()) : "";

        if (!reason.equals(choice[0]) && member.getLicense() != null && member.getLicense().getNumber() != null && member.getMemberPermissions() != null && member.getMemberPermissions().getArbiterStaticNumber() != null) {

            Paragraph p3 = new Paragraph(sexWord + " " + member.getFullName() + " posiada Patent Strzelecki PZSS oraz ważną Licencję Zawodniczą PZSS na rok " + year, font(12, 0));
            p3.setFirstLineIndent(40);
            document.add(p3);
        }

        if (!reason.equals(choice[0])) {
            Paragraph p4 = new Paragraph(member.getClub().getFullName() + " jest członkiem PZSS i posiada Licencję Klubową nr LK-" + member.getClub().getLicenseNumber() + ", jest członkiem ŁZSS (nr ewidencyjny 6).", font(12, 0));
            p4.setFirstLineIndent(40);
            document.add(p4);

            String rText = switch (reason) {
                case "BROŃ SPORTOWA DO CELÓW SPORTOWYCH" -> "na broń sportową do celów sportowych.";
                case "BROŃ SPORTOWA DO CELÓW KOLEKCJONERSKICH" -> "na broń sportową do celów kolekcjonerskich.";
                case "BROŃ CIĘCIWOWA W POSTACI KUSZ" -> "na broń cięciwową w postaci kusz.";
                default -> "";
            };

            String s1 = enlargement ? "rozszerzenie pozwolenia" : "pozwolenie";

            Paragraph p5 = new Paragraph(sexWord + " " + member.getFullName() + " " + verb + " z prośbą o wydanie niniejszego zaświadczenia, skutkiem którego będzie złożenie wniosku o " + s1 + " " + rText, font(12, 0));
            p5.setFirstLineIndent(40);
            document.add(p5);
        }

        // ========== PODSUMOWANIE ==========
        document.add(newline);
        document.add(newline);
        document.add(newline);

        Paragraph p6 = new Paragraph("Sporządzono w 2 egz.", font(12, 0));
        document.add(p6);

        Paragraph p7 = new Paragraph("Egz. Nr 1 – a/a\nEgz. Nr 2 – Adresat", font(12, 0));
        p7.setIndentationLeft(40);
        document.add(p7);

        document.close();

        return new PdfGenerationResults(fileName, baos.toByteArray());
    }
}

