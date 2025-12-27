package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.license.LicenseEntity;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.shootingPatent.ShootingPatentEntity;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

import static com.shootingplace.shootingplace.file.utils.Utils.*;

@Component
public class ApplicationForFirearmsLicensePdfGenerator {

    public PdfGenerationResults generate(MemberEntity member, String thirdName, String birthPlace, String fatherName, String motherName, String motherMaidenName, String issuingAuthority, LocalDate parseIDDate, LocalDate parselicenseDate, String city) throws DocumentException, IOException {

        String fileName = "Wiosek o pozwolenie na broń " + member.getFullName() + ".pdf";

        String policeCity = switch (city) {
            case "Białystok" -> "w Białymstoku";
            case "Bydgoszcz" -> "w Bydgoszczy";
            case "Gdańsk" -> "w Gdańsku";
            case "Gorzów Wielkopolski" -> "w Gorzowie Wielkopolskim";
            case "Katowice" -> "w Katowicach";
            case "Kielce" -> "w Kielcach";
            case "Kraków" -> "w Krakowie";
            case "Lublin" -> "w Lublinie";
            case "Olsztyn" -> "w Olsztynie";
            case "Opole" -> "w Opolu";
            case "Poznań" -> "w Poznaniu";
            case "Rzeszów" -> "w Rzeszowie";
            case "Szczecin" -> "w Szczecinie";
            case "Warszawa" -> "w Warszawie";
            case "Wrocław" -> "we Wrocławiu";
            case "Łódź" -> "w Łodzi";
            default -> "w Łodzi";
        };

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        Paragraph newLine = new Paragraph("\n", font(10, 0));

        Paragraph date = new Paragraph("Łódź, " + LocalDate.now().format(dateFormat()), font(10, 0));
        date.setAlignment(2);
        document.add(date);

        Paragraph memberNames = new Paragraph(member.getFirstName() + ' ' + thirdName + ' ' + member.getSecondName(), font(10, 0));
        memberNames.setAlignment(0);

        Paragraph parentsNames = new Paragraph(fatherName + ' ' + motherName + ' ' + motherMaidenName, font(10, 0));
        parentsNames.setAlignment(0);

        Paragraph birthDateAndPlace = new Paragraph((member.getBirthDate().format(dateFormat())) + ' ' + birthPlace, font(10, 0));
        parentsNames.setAlignment(0);

        Paragraph zipCodeAndCity = new Paragraph(member.getAddress().getZipCode() + ' ' + member.getAddress().getPostOfficeCity(), font(10, 0));
        zipCodeAndCity.setAlignment(0);

        Paragraph phoneNumber = new Paragraph(member.getPhoneNumber(), font(10, 0));
        phoneNumber.setAlignment(0);

        Paragraph emailP = new Paragraph(member.getEmail(), font(10, 0));
        emailP.setAlignment(0);

        Paragraph peselP = new Paragraph(member.getPesel(), font(10, 0));
        peselP.setAlignment(0);

        Paragraph IDCard = new Paragraph(member.getIDCard(), font(10, 0));
        IDCard.setAlignment(0);

        Paragraph issuing = new Paragraph(issuingAuthority, font(10, 0));
        issuing.setAlignment(0);

        Paragraph IDDate = new Paragraph(parseIDDate.format(dateFormat()), font(10, 0));
        IDCard.setAlignment(0);

        Paragraph recipient = new Paragraph("KOMENDANT WOJEWÓDZKI POLICJI", font(13, 1));
        recipient.setAlignment(2);

        Paragraph recipient1 = new Paragraph(policeCity, font(13, 1));
        recipient1.setAlignment(2);
        recipient1.setIndentationRight(80);

        document.add(memberNames);
        document.add(parentsNames);
        document.add(birthDateAndPlace);
        document.add(zipCodeAndCity);
        document.add(phoneNumber);
        document.add(emailP);
        document.add(peselP);
        document.add(issuing);
        document.add(IDCard);
        document.add(IDDate);
        document.add(recipient);
        document.add(recipient1);

        Paragraph title = new Paragraph("PODANIE", font(13, 1));
        title.setAlignment(1);
        document.add(title);

        Paragraph par1 = new Paragraph("Niniejszym wnoszę o wydanie mi (w postaci decyzji administracyjnej):\n", font(10, 0));
        par1.setFirstLineIndent(30);
        document.add(par1);

        Paragraph par2 = new Paragraph("1.   pozwolenia na posiadanie broni palnej sportowej w łącznej ilości 6 egzemplarzy do celów sportowych, w tym broni:\n", font(10, 0));
        par2.setFirstLineIndent(20);
        document.add(par2);

        Paragraph par21 = new Paragraph("a. bocznego zapłonu z lufami gwintowanymi, o kalibrze do 6 mm,\n" + "b. centralnego zapłonu z lufami gwintowanymi, o kalibrze do 12mm,\n" + "c. gładko lufowej,\n", font(10, 0));
        par21.setIndentationLeft(50);
        document.add(par21);

        Paragraph par3 = new Paragraph("2.   pozwolenia na posiadanie broni palnej sportowej w łącznej ilości 10 egzemplarzy do celów kolekcjonerskich.", font(10, 0));
        par3.setFirstLineIndent(20);
        document.add(par3);

        Paragraph par4 = new Paragraph("3.   dopuszczenia do posiadania broni palnej sportowej (A,H,I,J,L) podczas uczestnictwa, organizacji lub przeprowadzania strzeleckich zawodów sportowych.", font(10, 0));
        par4.setIndentationLeft(20);
        document.add(par4);

        Paragraph title1 = new Paragraph("UZASADNIENIE", font(13, 1));
        title1.setAlignment(1);
        document.add(newLine);
        document.add(title1);
        document.add(newLine);

        Paragraph par5 = new Paragraph("Po mojej stronie nie występują żadne negatywne przesłanki uniemożliwiające posiadanie pozwolenia na broń, a ponadto przedstawiam ważną przyczynę posiadania broni, którą jest:", font(10, 0));
        par5.setFirstLineIndent(20);
        document.add(par5);

        Paragraph par6 = new Paragraph("1.   dla broni do celów sportowych:", font(10, 0));
        par6.setFirstLineIndent(20);
        document.add(par6);

        Paragraph par61 = new Paragraph("a. członkostwo w stowarzyszeniu o charakterze strzeleckim tj. w Stowarzyszenie Strzelecko-Kolekcjonerskie RCS Panaszew 99-200 Poddębice Panaszew 4A NIP:8281419076 REGON:388545546 KRS:0000907099\n" + "b. posiadanie kwalifikacji sportowych o których mowa w art. 10b UoBiA tj. patentu strzeleckiego \n" + member.getShootingPatent().getPatentNumber() + " w dyscyplinach: " + getDisciplinesFromShootingPatentOrLicense(member.getShootingPatent(), null) + "\n" + "c. posiadanie ważnej licencji zawodniczej Polskiego Związku Strzelectwa Sportowego NR L-" + member.getLicense().getNumber() + "/" + getPartOfDate(parselicenseDate) + " Z dnia " + parselicenseDate.format(dateFormat()) + "r. w dyscyplinach: " + getDisciplinesFromShootingPatentOrLicense(null, member.getLicense()) + ",\n", font(10, 0));
        par61.setIndentationLeft(50);
        document.add(par61);

        Paragraph par7 = new Paragraph("2.   dla broni sportowej do celów kolekcjonerskich:", font(10, 0));
        par7.setFirstLineIndent(20);
        document.add(par7);

        Paragraph par71 = new Paragraph("a. członkostwo w stowarzyszeniu o charakterze kolekcjonerskim tj. w Stowarzyszenie Strzelecko-Kolekcjonerskie RCS Panaszew 99-200 Poddębice Panaszew 4A NIP:8281419076 REGON:388545546 KRS:0000907099\n" + "b. posiadanie kwalifikacji sportowych o których mowa w art. 10b UoBiA tj. patentu strzeleckiego NR " + member.getShootingPatent().getPatentNumber() + " z dnia " + member.getShootingPatent().getDateOfPosting().format(dateFormat()) + " r. w dyscyplinach: " + getDisciplinesFromShootingPatentOrLicense(member.getShootingPatent(), null) + "\n" + "c. posiadanie ważnej licencji zawodniczej Polskiego Związku Strzelectwa Sportowego L-" + member.getLicense().getNumber() + "/" + getPartOfDate(parselicenseDate) + " z dnia " + parselicenseDate.format(dateFormat()) + "r. w dyscyplinach: pistolet, karabin, strzelba gładkolufowa,\n", font(10, 0));
        par71.setIndentationLeft(50);
        document.add(par71);

        Paragraph par8 = new Paragraph("3.   dla dopuszczenia do posiadania broni palnej sportowej:", font(10, 0));
        par8.setFirstLineIndent(20);
        document.add(par8);

        Paragraph par81 = new Paragraph("spełnienie kryteriów, o których mowa w art. 30 ust. 1a ustawy o broni i amunicji, tj.  posiadanie ważnej licencji zawodnika strzelectwa sportowego nadanej przez Polski Związek Strzelectwa Sportowego nr L-" + member.getLicense().getNumber() + "/" + getPartOfDate(parselicenseDate) + " Z dnia " + parselicenseDate.format(dateFormat()) + "r.  w dyscyplinach: pistolet, karabin, strzelba gładkolufowa,", font(10, 0));
        par81.setIndentationLeft(50);
        document.add(par81);

        document.newPage();
        document.newPage();

        Paragraph par9 = new Paragraph("Na podstawie posiadanych kwalifikacji sportowych tj. posiadania patentu strzeleckiego oraz ważnej licencji wydanych przez PZSS jestem zwolniony z egzaminu przed organem Policji, w zakresie broni sportowej, ponieważ zdałem go na podstawie odrębnych przepisów. ", font(10, 0));
        par9.setFirstLineIndent(20);
        document.add(par9);

        Paragraph par10 = new Paragraph("Strzelectwo sportowe zamierzam uprawiać uczestnicząc we współzawodnictwie w ramach PZSS, jak również poza strukturami Związku. Zamierzam rozwijać się w tym sporcie i chcę strzelać z różnych rodzajów broni palnej sportowej, w wielu konkurencjach i dyscyplinach. Nie posiadając własnej broni nie jestem w stanie startować w planowanych przeze mnie konkurencjach.", font(10, 0));
        par10.setFirstLineIndent(20);
        document.add(par10);

        Paragraph par11 = new Paragraph("Stowarzyszenie strzeleckie, którego jestem członkiem organizuje zawody w kilkudziesięciu różnych konkurencjach strzeleckich, rozgrywanych z różnego rodzaju broni, na różnych dystansach i w różnych warunkach. Są to zarówno konkurencje statyczne, jak i dynamiczne. Chcę brać udział w rywalizacji w dużej części tych konkurencji. W tej liczbie między innymi:\n", font(10, 0));
        par11.setFirstLineIndent(20);
        document.add(par11);

        Paragraph par12 = new Paragraph("1. Karabin centralnego zapłonu, 50m, kategoria MANUAL\n" + "2. Karabin centralnego zapłonu, 50m, kategoria OPEN\n" + "3. Pistolet centralnego zapłonu, 25m, 10 strzałów stojąc, tarcza TS/2\n" + "4. Strzelba dynamiczna, kategoria STANDARD\n" + "5. Strzelba dynamiczna, kategoria SEMI-AUTO\n" + "6. Strzelba dynamiczna IPSC\n" + "7. Pistolet dynamiczny (IPSC), kategoria PRODUCTION\n" + "8. Pistolet dynamiczny (IPSC), kategoria STANDARD/MINOR\n" + "9. Pistolet dynamiczny (IPSC), kategoria STANDARD/MAJOR\n", font(10, 0));
        par12.setIndentationLeft(20);
        document.add(par12);

        Paragraph par13 = new Paragraph("Zdecydowałem się pominąć konkurencje podobne lub takie, w których mógłbym na początku używać broni zakupionej też do innej konkurencji.  Niemniej jednak, interesują mnie także pozostałe typy strzelectwa sportowego (np. długodystansowe, dynamiczne, czarnoprochowe) i w przyszłości planuję brać udział w zawodach i konkurencjach je obejmujących. W szczególności interesuje mnie strzelectwo dynamiczno-praktyczne takie jak: IPSC, Liga Sportera czy 3 Gun.\n" + "Oferta stowarzyszenia o charakterze strzeleckim do którego należę jest stale poszerzana i umożliwia mi szerokie eksplorowanie pasji strzeleckiej. Specyfika konkurencji, w których już startuje oraz będę startował sprawia, że wnioskowana ilość jednostek broni jest mi niezbędna do startu w nich, treningu oraz poszerzania swoich umiejętności sportowych. Mając bogatą ofertę zawodów sportowych potrzebuję dużej dozy elastyczności w wyborze zakupionej broni. Zamierzam nabywać kolejne egzemplarze, w miarę jak moje plany uprawiania sportu strzeleckiego będą tego wymagały.\n", font(10, 0));
        par13.setFirstLineIndent(20);
        document.add(par13);

        Paragraph par14 = new Paragraph("Zamierzam kolekcjonować broń palną sportową różnych rodzajów, typów i modeli. W chwili obecnej nie jestem w stanie określić po ile egzemplarzy broni każdego rodzaju będę mieć w swojej kolekcji. Dopiero zaczynam realizację pasji kolekcjonerskiej i nie jestem w stanie powiedzieć, w którą stronę będę chciał się rozwinąć w najbliższej przyszłości.\n" + "Chcę mieć dużą kolekcję stanowiącą przekrój najpopularniejszych modeli broni palnej. Na początek wielkość kolekcji, która \n" + "z natury rzeczy nie jest i nie może być zbiorem zamkniętym, oceniam szacunkowo na liczbę 10 sztuk. I dlatego wnoszę o wydanie pozwolenia na taką właśnie ilość broni.\n", font(10, 0));
        par14.setFirstLineIndent(20);
        document.add(par14);

        Paragraph par15 = new Paragraph("Rezygnuję niniejszym z prawa zapoznania się z aktami przed wydaniem decyzji, jeśli organ Policji dojdzie do wniosku, \n" + "że należy wydać decyzję zgodną z moim żądaniem.\n", font(10, 0));
        par15.setFirstLineIndent(20);
        document.add(par15);

        Paragraph par16 = new Paragraph("....................................................", font(10, 0));
        par16.setAlignment(2);
        par16.setIndentationRight(80);
        document.add(par16);

        Paragraph par17 = new Paragraph("(podpis)", font(10, 0));
        par17.setAlignment(2);
        par17.setIndentationRight(100);
        document.add(par17);

        Paragraph par18 = new Paragraph("Załączniki:", font(10, 1));
        par18.setFirstLineIndent(20);
        document.add(par18);

        Paragraph par19 = new Paragraph("1) dowód wniesienia opłaty 242 zł za wydanie pozwolenia na broń do celów sportowych (oryginał),\n" + "2) dowód wniesienia opłaty 242 zł za wydanie pozwolenia na broń do celów kolekcjonerskich (oryginał),\n" + "3) dowód wniesienia opłaty skarbowej 10 zł za dopuszczenie do broni (oryginał),\n" + "4) orzeczenie lekarskie  (oryginał),\n" + "5) orzeczenie psychologiczne  (oryginał),\n" + "6) zaświadczenie o członkostwie w strzeleckim klubie sportowym: RCS Panaszew 99-200 Poddębice Panaszew 4A NIP:8281419076 REGON:388545546 KRS:0000907099 (oryginał)\n" + "7) decyzja nadania patentu strzeleckiego PZSS NR " + member.getShootingPatent().getPatentNumber() + " z dnia " + member.getShootingPatent().getDateOfPosting().format(dateFormat()) + "r. (wydruk)\n" + "8) ważna licencja zawodnika L-" + member.getLicense().getNumber() + "/" + getPartOfDate(parselicenseDate) + " Z dnia " + parselicenseDate.format(dateFormat()) + "r. (wydruk)\n" + "9) zdjęcia – 4szt. (zdjęcia podpisane na odwrocie) \n" + "10) Kserokopia dowodu osobistego (oryginał do wglądu)\n", font(8, 0));
        par19.setIndentationLeft(20);
        document.add(par19);

        document.close();

        return new PdfGenerationResults(fileName, baos.toByteArray());
    }

    private String getDisciplinesFromShootingPatentOrLicense(ShootingPatentEntity patent, LicenseEntity license) {
        String pistol = null, rifle = null, shotgun = null;
        if (patent != null) {
            pistol = patent.isPistolPermission() ? "pistolet" : "";
            rifle = patent.isRiflePermission() ? "karabin" : "";
            shotgun = patent.isShotgunPermission() ? "strzelba gładkolufowa" : "";
        }
        if (license != null) {
            pistol = license.isPistolPermission() ? "pistolet" : "";
            rifle = license.isRiflePermission() ? "karabin" : "";
            shotgun = license.isShotgunPermission() ? "strzelba gładkolufowa" : "";
        }
        return pistol + " " + rifle + " " + shotgun;
    }

    private String getPartOfDate(LocalDate date) {
        String month, year;
        month = date.getMonthValue() < 10 ? "0" + date.getMonthValue() : String.valueOf(date.getMonthValue());
        year = String.valueOf(date.getYear());
        return month + "/" + year;
    }

}

