package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.file.pageStamper.PageStampMode;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberGroupRepository;
import com.shootingplace.shootingplace.file.pageStamper.PageStamper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import static com.shootingplace.shootingplace.file.utils.Utils.*;

@Component
public class GuardiansMembershipPdfGenerator {
    private final ClubRepository clubRepository;
    private final MemberGroupRepository memberGroupRepository;
    private final Environment environment;

    public GuardiansMembershipPdfGenerator(ClubRepository clubRepository, MemberGroupRepository memberGroupRepository, Environment environment) {
        this.clubRepository = clubRepository;
        this.memberGroupRepository = memberGroupRepository;
        this.environment = environment;
    }

    public PdfGenerationResults generate(MemberEntity member) throws DocumentException, IOException {
        String fileName = "Deklaracja_Czlonkowska_KS_Guardians.pdf";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 72, 72);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new PageStamper(environment, true, true, PageStampMode.A4));

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        Font titleFont = font(16, Font.BOLD);
        Font sectionTitle = font(12, Font.BOLD);
        Font normal = font(12, Font.NORMAL);
        Font bold = font(10, Font.BOLD);

        // ========================= STRONA 1 =========================
        addHeaderWithLogo(document, normal);
        drawPageFrame(writer, document);

        addEmptyLine(document, 1);

        Paragraph title = new Paragraph("DEKLARACJA CZŁONKOWSKA", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        Paragraph intro = new Paragraph("""
                Proszę o przyjęcie mnie w poczet członków
                Klubu Strzelecko – Kolekcjonerskiego Guardians Nowolipsk
                """, normal);
        intro.setAlignment(Element.ALIGN_CENTER);
        intro.setSpacingAfter(10);
        document.add(intro);

        Paragraph daneOsobowe = new Paragraph("Dane osobowe", sectionTitle);
        daneOsobowe.setSpacingAfter(8);
        document.add(daneOsobowe);

        PdfPTable personalTable = new PdfPTable(2);
        personalTable.setWidthPercentage(100);
        personalTable.setWidths(new float[]{32, 80});
        String phone = member.getPhoneNumber();
        String phoneSplit = phone.substring(0, 3) + " " + phone.substring(3, 6) + " " + phone.substring(6, 9) + " " + phone.substring(9, 12);
        addRow(personalTable, "Imię i nazwisko:", member.getFullName(), bold, normal);
        addRow(personalTable, "Data urodzenia:", String.valueOf(member.getBirthDate()), bold, normal);
        addRow(personalTable, "Nr dowodu osobistego:", member.getIDCard() + " wydany przez ...........................................", bold, normal);
        addRow(personalTable, "Nr PESEL:", member.getPesel(), bold, normal);
        addRow(personalTable, "Adres zamieszkania:", member.getAddress().fullAddress(), bold, normal);
        addRow(personalTable, "Nr telefonu:", phoneSplit, bold, normal);
        addRow(personalTable, "Adres e-mail:", member.getEmail(), bold, normal);

        document.add(personalTable);

        addEmptyLine(document, 1);

        Paragraph uprawnienia = new Paragraph("Uprawnienia strzeleckie", sectionTitle);
        uprawnienia.setSpacingAfter(8);
        document.add(uprawnienia);

        PdfPTable rightsTable = new PdfPTable(2);
        rightsTable.setWidthPercentage(100);
        rightsTable.setWidths(new float[]{32, 80});

        addRow(rightsTable, "Patent strzelecki PZSS nr:", "..............................................................................", bold, normal);
        addRow(rightsTable, "Licencja zawodnicza PZSS nr:", "..............................................................................", bold, normal);
        addRow(rightsTable, "Prowadzący strzelanie nr:", "............................... wydane przez: ................................................", bold, normal);
        addRow(rightsTable, "Pozwolenie na broń nr:", "............................... wydane przez: ................................................", bold, normal);
        addRow(rightsTable, "Zakres uprawnień:", "pistolet    karabin    strzelba gładkolufowa", bold, normal);
        addRow(rightsTable, "Dodatkowe uprawnienia (np. sędziowskie, instruktor):", "...............................................................", bold, normal);

        document.add(rightsTable);

        // ========================= STRONA 2 =========================
        document.newPage();
        drawPageFrame(writer, document);

        Paragraph skladkaTitle = new Paragraph("Deklarowana składka członkowska:", sectionTitle);
        skladkaTitle.setSpacingAfter(8);
        document.add(skladkaTitle);
        float startX = 70;   // pozioma pozycja checkboxa
        float textX = 90;   // pozioma pozycja tekstu
        float startY = document.top() - 40;  // PIERWSZY wiersz (dostosuj raz)
        float gap = 18;   // odstęp między liniami
        AtomicInteger index = new AtomicInteger(0);
        PdfContentByte cb = writer.getDirectContent();
        memberGroupRepository.findAll().forEach(e -> {

            float y = startY - (index.getAndIncrement() * gap);

            drawCheckbox(writer, startX, y, 11f);

            try {
                ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(e.getDescription(), font(10, 0)), textX, y + 1, 0);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        addEmptyLine(document, 4);
        Paragraph oswiadczamTitle = new Paragraph("Oświadczam, że:", sectionTitle);
        oswiadczamTitle.setSpacingAfter(5);
        document.add(oswiadczamTitle);

        Paragraph oswiadczenia = new Paragraph("""
                1. Statut Stowarzyszenia Klubu Strzeleckiego Guardians Nowolipsk jest mi znany i zobowiązuję się do\
                   przestrzegania zawartych w nim postanowień, a w szczególności w zakresie opłacania składki\
                   członkowskiej w terminie do 30 grudnia na rok następny (po 30 dniach powstałe zaległości skutkować\
                   będą automatycznym skreśleniem z listy członków Klubu).
                2. Zapoznałem/am się z zasadami bezpieczeństwa przy uprawianiu strzelectwa oraz, że udzielony mi\
                   został instruktaż w zakresie czynności związanych z bezpiecznym posługiwaniem się bronią i akceptuję\
                   je oraz zobowiązuję się do ich przestrzegania.
                3. Nie byłem/am karany/a prawomocnym orzeczeniem sądu za przestępstwa umyślne.
                4. Brak jest przeciwwskazań medycznych do uprawiania przeze mnie sportów strzeleckich.
                5. Dane zawarte w deklaracji członkowskiej są zgodne ze stanem faktycznym oraz, że o każdej zmianie\
                   będę informował/a Zarząd Klubu.
                """, normal);
        oswiadczenia.setSpacingAfter(25);
        document.add(oswiadczenia);

        Paragraph podpis1 = new Paragraph("""
                ...............................................................
                          (data i podpis)                       \s
                """, normal);
        podpis1.setAlignment(Element.ALIGN_RIGHT);
        podpis1.setSpacingAfter(25);
        document.add(podpis1);

        Paragraph rodoZgoda = new Paragraph("Wyrażam zgodę na gromadzenie oraz przetwarzanie moich danych osobowych, dobrowolnie " + "zawartych w niniejszej deklaracji, do celów statutowych Klubu oraz do sporządzania stosownych " + "sprawozdań dla podmiotów upoważnionych do nadzoru nad działalnością Klubu w zakresie " + "strzelectwa sportowego (w tym dla Polskiego Związku Strzelectwa Sportowego, Wielkopolskiego " + "Związku Strzelectwa Sportowego, Policji) zgodnie z ustawą z dnia 29 sierpnia 1997 roku o ochronie " + "danych osobowych (Dz. U. Nr 133 poz. 833, z 2000 r. z późn. zm.). Wyrażam zgodę na udostępnianie " + "danych (imię i nazwisko) w mediach społecznościowych na potrzeby Klubu (komunikaty z zawodów " + "sportowych). Jednocześnie oświadczam, iż uznaję przesyłanie przez Klub korespondencji " + "elektronicznej na podany przeze mnie adres elektroniczny za skuteczną formę doręczenia. ", normal);
        rodoZgoda.setSpacingAfter(25);
        document.add(rodoZgoda);

        Paragraph podpis2 = new Paragraph("""
                ...............................................................
                          (data i podpis)                       \s
                """, normal);
        podpis2.setAlignment(Element.ALIGN_RIGHT);
        document.add(podpis2);

        // ========================= STRONA 3 =========================
        document.newPage();
        drawPageFrame(writer, document);

        Paragraph rodoTitle = new Paragraph("INFORMACJA O PRZETWARZANIU DANYCH OSOBOWYCH", sectionTitle);
        rodoTitle.setAlignment(Element.ALIGN_CENTER);
        rodoTitle.setSpacingAfter(15);
        document.add(rodoTitle);

        Paragraph rodoText = new Paragraph("""
                Zgodnie z art. 13 ust. 1 i ust. 2 Rozporządzenia Parlamentu Europejskiego i Rady (UE) 2016/679
                z dnia 27 kwietnia 2016 r. w sprawie ochrony osób fizycznych w związku z przetwarzaniem danych
                osobowych i w sprawie swobodnego przepływu takich danych oraz uchylenia dyrektywy 95/46/WE
                informuję, że Administratorem danych osobowych jest Klub Strzelecki Guardians Nowolipsk.
                Kontakt do Administratora Danych Osobowych: ............................................................
                Państwa dane osobowe mogą być przetwarzane w celu:
                • realizacji członkostwa w Klub Strzelecki Guardians Nowolipsk i będą przetwarzane przez okres 5 lat
                  po zakończeniu okresu członkostwa,
                • uczestnictwa w zawodach strzeleckich i będą przetwarzane przez okres 5 lat od końca roku, w którym
                  odbyły się zawody.
                Pani/Pana dane osobowe mogą zostać udostępnione podmiotom upoważnionym z mocy prawa lub
                podmiotom współpracującym z Administratorem Danych Osobowych w zakresie obsługi finansowo-
                księgowej oraz IT.
                Administrator Danych Osobowych nie planuje przekazywania Pani/Pana danych osobowych do
                państw trzecich lub organizacji międzynarodowych.
                Posiada Pani/Pan prawo dostępu do treści swoich danych oraz prawo ich sprostowania, usunięcia,
                ograniczenia przetwarzania, prawo do przenoszenia danych, prawo wniesienia sprzeciwu.
                Ma Pani/Pan prawo wniesienia skargi do właściwego organu nadzorczego w zakresie ochrony danych
                osobowych.
                Podanie przez Panią/Pana danych osobowych jest dobrowolne, lecz niezbędne do uzyskania i
                zachowania członkostwa w Klub Strzelecki Guardians Nowolipsk oraz zawarcia umowy. Jest Pani/Pan
                zobowiązana/-y do ich podania, konsekwencją niepodania danych osobowych będzie brak możliwości
                współpracy z Administratorem.
                Administrator Danych Osobowych nie realizuje oraz nie planuje realizacji operacji w sposób
                zautomatyzowany, w tym profilowania, o którym mowa w art. 22 ust. 1 i 4 ogólnego rozporządzenia
                o ochronie danych.
                """, normal);
        rodoText.setAlignment(Element.ALIGN_LEFT);
        document.add(rodoText);

        // ========================= STRONA 4 =========================
        document.newPage();
        drawPageFrame(writer, document);

        Paragraph zarzadTitle = new Paragraph("WYPEŁNIA ZARZĄD STOWARZYSZENIA", sectionTitle);
        zarzadTitle.setAlignment(Element.ALIGN_CENTER);
        zarzadTitle.setSpacingAfter(15);
        document.add(zarzadTitle);
        Paragraph decyzjaPrzyjecie = new Paragraph();
        Phrase decyzjaPrzyjecieTitle = new Phrase("Decyzja Zarządu Stowarzyszenia o przyjęciu do Stowarzyszenia\n\n", font(12, 1));
        Phrase decyzjaPrzyjecieText = new Phrase("Decyzją Zarządu Stowarzyszenia Klub Strzelecki Guardians Nowolipsk\n" + (member.getSex() ? "Pani " : "Pan ") + member.getFullName() + (member.getSex() ? " została przyjęta" : " został przyjęty") + " w poczet Członków Stowarzyszenia\n" + "Uchwałą Zarządu nr .............................. z dnia ...........................................\n\n", normal);
        Paragraph decyzjaPrzyjeciePodpis = new Paragraph("""
                .........................................................................................................................
                        (pieczęć i podpis Prezesa lub Wiceprezesa Stowarzyszenia)       \s
                """, normal);
        decyzjaPrzyjecie.setAlignment(Element.ALIGN_CENTER);
        decyzjaPrzyjeciePodpis.setAlignment(Element.ALIGN_RIGHT);

        decyzjaPrzyjecie.add(decyzjaPrzyjecieTitle);
        decyzjaPrzyjecie.add(decyzjaPrzyjecieText);

        document.add(decyzjaPrzyjecie);
        document.add(decyzjaPrzyjeciePodpis);
        Paragraph decyzjaSkreslenie = new Paragraph();
        Phrase decyzjaSkleslenieTitle = new Phrase("Decyzja Zarządu Stowarzyszenia o skreśleniu z listy członków Stowarzyszenia\n\n", font(12, 1));

        Phrase decyzjaSkreslenieText = new Phrase("Decyzją Zarządu Stowarzyszenia Klub Strzelecki Guardians Nowolipsk\n" + (member.getSex() ? "Pani " : "Pan ") + member.getFullName() + (member.getSex() ? " została skreślona" : " został skreślony") + " z listy Członków Stowarzyszenia\n\n" + "Uchwałą Zarządu nr .............................. z dnia ...........................................\n\n", normal);
        addEmptyLine(document, 4);
        Paragraph decyzjaSkresleniePodpis = new Paragraph("""
                .........................................................................................................................
                        (pieczęć i podpis Prezesa lub Wiceprezesa Stowarzyszenia)       \s
                
                """, normal);
        decyzjaSkreslenie.setAlignment(Element.ALIGN_CENTER);
        decyzjaSkresleniePodpis.setAlignment(Element.ALIGN_RIGHT);

        decyzjaSkreslenie.add(decyzjaSkleslenieTitle);
        decyzjaSkreslenie.add(decyzjaSkreslenieText);

        document.add(decyzjaSkreslenie);
        document.add(decyzjaSkresleniePodpis);

        document.close();

        return new PdfGenerationResults(fileName, baos.toByteArray());
    }

    private void addHeaderWithLogo(Document document, Font normal) throws DocumentException, IOException {
        ClubEntity clubEntity = clubRepository.findById(1).orElseThrow(EntityNotFoundException::new);

        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{25, 75});

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(PdfPCell.NO_BORDER);

        URL logoUrl = getClass().getClassLoader().getResource("logo-guardians.jpg");
        if (logoUrl != null) {
            Image logo = Image.getInstance(logoUrl);
            logo.scaleAbsolute(180, 180);
            logoCell.addElement(logo);
        } else {
            logoCell.addElement(new Paragraph(" ", normal));
        }

        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(PdfPCell.NO_BORDER);
        Paragraph clubInfo = new Paragraph();

        Phrase clubInfoTitle = new Phrase("\n\nSTOWARZYSZENIE KLUB STRZELECKI\n GUARDIANS NOWOLIPSK\n", font(12, 1));
        Phrase clubInfoText = new Phrase("Nowolipsk 59, 63-613 Chocz\n" + "KRS: 0001180496, NIP: 6080128773, REGON: 542072988\n" + "Nr rachunku: BNP Paribas Bank 29 1600 1462 1779 6009 5000 0001\n" + "Nr licencji PZSS: " + clubEntity.getLicenseNumber() + "\n" + "E-mail: biuro@ksguardians.pl   Nr kontaktowy: 501 591 659", font(10, 0));
        clubInfo.add(clubInfoTitle);
        clubInfo.add(clubInfoText);
        clubInfo.setAlignment(Element.ALIGN_CENTER);
        textCell.addElement(clubInfo);

        header.addCell(logoCell);
        header.addCell(textCell);

        document.add(header);
    }

    private static void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, labelFont));
        PdfPCell c2 = new PdfPCell(new Phrase(value, valueFont));

        c1.setBorder(PdfPCell.NO_BORDER);
        c2.setBorder(PdfPCell.NO_BORDER);

        c1.setPadding(3f);
        c2.setPadding(3f);

        table.addCell(c1);
        table.addCell(c2);
    }

    private static void addEmptyLine(Document document, int count) throws DocumentException {
        for (int i = 0; i < count; i++) {
            document.add(new Paragraph(" "));
        }
    }

    public static void drawCheckbox(PdfWriter writer, float x, float y, float size) {
        PdfContentByte cb = writer.getDirectContent();
        cb.saveState();
        cb.setColorStroke(new Color(68, 114, 196));
        cb.setLineWidth(1f);
        cb.rectangle(x, y, size, size);
        cb.stroke();
        cb.restoreState();
    }

    private static void drawPageFrame(PdfWriter writer, Document document) {
        PdfContentByte cb = writer.getDirectContent();
        cb.saveState();
        cb.setLineWidth(2f);
        cb.setColorStroke(new Color(68, 114, 196));

        float x = document.left() - 10;
        float y = document.bottom() - 10;
        float w = document.right() - document.left() + 20;
        float h = document.top() - document.bottom() + 20;
        float radius = 20f;

        cb.roundRectangle(x, y, w, h, radius);
        cb.stroke();
        cb.restoreState();
    }
}
