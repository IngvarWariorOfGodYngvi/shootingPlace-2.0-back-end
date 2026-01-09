package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.enums.ProfilesEnum;
import com.shootingplace.shootingplace.file.pageStamper.PageStampMode;
import com.shootingplace.shootingplace.file.pageStamper.PageStamper;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.member.MemberEntity;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;

import static com.shootingplace.shootingplace.file.utils.FilesUtils.*;

@Component
public class LokMembershipPdfGenerator implements PdfGenerator<MemberEntity> {

    private final ClubRepository clubRepository;
    private final Environment environment;


    public LokMembershipPdfGenerator(ClubRepository clubRepository, Environment environment) {
        this.clubRepository = clubRepository;
        this.environment = environment;
    }


    public PdfGenerationResults generate(MemberEntity member) throws DocumentException, IOException {

        String fileName = "Deklaracja Czlonkowska LOK " + member.getFullName() + ".pdf";

        URL resource = LokMembershipPdfGenerator.class.getClassLoader().getResource("logo_LOK.png");
        if (resource == null) {
            throw new IllegalStateException("Nie znaleziono logo_LOK.png");
        }

        Image img = Image.getInstance(resource);

        int fs = 10;
        int ls = 11;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new PageStamper(environment, true, false, PageStampMode.A4));

        document.open();

        Paragraph title = new Paragraph("     DEKLARACJA CZŁONKOWSKA", font(20, 1));
        title.setAlignment(1);

        Paragraph newLine = new Paragraph("\n", font(fs, 0));
        Paragraph page = new Paragraph("1/3", font(fs, 0));
        page.setAlignment(0);
        document.add(page);
        document.addCreator("Igor Żebrowski");
        Paragraph zalacznik = new Paragraph("Zał. nr 1. do uchwały ZG LOK", font(fs, 0));
        Paragraph zalacznik1 = new Paragraph("nr 71/2022 z dn. 28.10.2022r.", font(fs, 0));
        zalacznik.setLeading(ls);
        zalacznik.setAlignment(2);
        zalacznik1.setLeading(ls);
        zalacznik1.setAlignment(2);

        document.add(zalacznik);
        document.add(zalacznik1);

        img.setAlignment(0);
        img.scaleAbsolute(100, 65);

        float[] f = {20, 80};

        PdfPTable table = new PdfPTable(f);
        PdfPCell cellc1 = new PdfPCell(img);
        PdfPCell cellc2 = new PdfPCell(title);
        cellc1.setBorder(0);
        cellc2.setBorder(0);
        cellc2.setHorizontalAlignment(1);
        cellc2.setVerticalAlignment(5);
        table.addCell(cellc1);
        table.addCell(cellc2);

        document.add(table);

        float[] f1 = {18, 82};
        PdfPTable table1 = new PdfPTable(f1);

        PdfPCell cellt1c1 = new PdfPCell(new Paragraph("Imię i nazwisko", font(11, 1)));
        PdfPCell cellt1c2 = new PdfPCell(new Paragraph(member.getFirstName().toUpperCase() + " " + member.getSecondName().toUpperCase(), font(11, 0)));
        cellt1c1.setHorizontalAlignment(0);
        cellt1c2.setHorizontalAlignment(1);
        cellt1c1.setVerticalAlignment(5);
        cellt1c2.setVerticalAlignment(5);
        table1.addCell(cellt1c1);
        table1.addCell(cellt1c2);
        table1.completeRow();

        PdfPCell cellt2c1 = new PdfPCell(new Paragraph("Data urodzenia", font(11, 1)));
        PdfPCell cellt2c2 = new PdfPCell();
        cellt2c2.setPadding(0);
        cellt2c2.setBorder(0);
        cellt2c1.setVerticalAlignment(5);
        cellt2c1.setHorizontalAlignment(0);
        table1.addCell(cellt2c1);

        float[] f2 = {20, 10, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5};
        PdfPTable table2 = new PdfPTable(f2);
        PdfPCell cell = new PdfPCell(new Paragraph(member.getBirthDate().format(dateFormat()), font(11, 0)));
        PdfPCell cell1 = new PdfPCell(new Paragraph("PESEL", font(11, 1)));

        cell.setVerticalAlignment(5);
        cell.setHorizontalAlignment(1);
        cell1.setVerticalAlignment(5);
        cell1.setHorizontalAlignment(1);

        table2.addCell(cell);
        table2.addCell(cell1);
        for (int i = 0; i < member.getPesel().length(); i++) {
            Paragraph p = new Paragraph(member.getPesel().substring(i, i + 1), font(11, 0));
            PdfPCell cell2 = new PdfPCell(p);
            cell2.setVerticalAlignment(5);
            cell2.setHorizontalAlignment(1);
            cell2.setPaddingTop(0);
            table2.addCell(cell2);
        }
        table2.setWidthPercentage(100);
        cellt2c2.addElement(table2);
        table1.addCell(cellt2c2);
        table1.completeRow();

        PdfPCell addressTitleCell = new PdfPCell(new Paragraph("Adres \nzamieszkania", font(11, 1)));
        PdfPCell addressCell = new PdfPCell();
        addressTitleCell.setVerticalAlignment(5);
        addressTitleCell.setHorizontalAlignment(0);
        table1.addCell(addressTitleCell);

        PdfPTable table3 = new PdfPTable(1);
        table3.setWidthPercentage(100);
        table3.addCell(new PdfPCell(new Paragraph(member.getAddress().fullAddress(), font(11, 0))));
        table3.completeRow();
        table3.addCell(new PdfPCell(new Paragraph(" ", font(11, 0))));
        addressCell.setBorder(0);
        addressCell.setPadding(0);
        addressCell.addElement(table3);
        table1.addCell(addressCell);
        table1.completeRow();

        PdfPCell cell2 = new PdfPCell(new Paragraph("Posiadane\nodznaczenia\npaństwowe/LOK", font(10, 1)));
        table1.addCell(cell2);
        table1.addCell(new PdfPCell(new Paragraph(" ", font(11, 0))));
        table1.completeRow();

        PdfPCell phoneTitleCell = new PdfPCell(new Paragraph("Numer telefonu", font(11, 1)));
        phoneTitleCell.setHorizontalAlignment(0);
        phoneTitleCell.setVerticalAlignment(5);

        table1.addCell(phoneTitleCell);

        PdfPCell phoneCell = new PdfPCell();
        phoneCell.setBorder(0);
        phoneCell.setPadding(0);
        PdfPTable table4 = new PdfPTable(2);
        table4.setWidthPercentage(100);
        PdfPCell cell3 = new PdfPCell(new Paragraph("stacjonarny", font(9, 0)));
        PdfPCell cell4 = new PdfPCell(new Paragraph("komórkowy", font(9, 0)));
        cell3.setHorizontalAlignment(0);
        cell4.setHorizontalAlignment(0);
        table4.addCell(cell3);
        table4.addCell(cell4);
        table4.completeRow();
        table4.addCell(new PdfPCell(new Paragraph(" ", font(11, 0))));

        String phone = member.getPhoneNumber();
        String split = phone.substring(0, 3) + " ";
        String split1 = phone.substring(3, 6) + " ";
        String split2 = phone.substring(6, 9) + " ";
        String split3 = phone.substring(9, 12) + " ";
        String phoneSplit = split + split1 + split2 + split3;

        table4.addCell(new PdfPCell(new Paragraph(phoneSplit, font(11, 0))));
        phoneCell.addElement(table4);
        table1.addCell(phoneCell);
        table1.completeRow();

        PdfPCell emailTitleCell = new PdfPCell(new Paragraph("Adres e-mail", font(11, 1)));
        emailTitleCell.setHorizontalAlignment(0);
        emailTitleCell.setVerticalAlignment(5);
        table1.addCell(emailTitleCell);
        PdfPCell emailCell = new PdfPCell(new Paragraph(member.getEmail(), font(11, 0)));
        emailCell.setHorizontalAlignment(1);
        emailCell.setVerticalAlignment(5);
        table1.addCell(emailCell);
        table1.completeRow();

        document.add(table1);

        Paragraph p = new Paragraph("Proszę o przyjęcie mnie w poczet członków Stowarzyszenia Liga Obrony Kraju.", font(fs, 0));
        p.setIndentationLeft(55);
        p.setLeading(ls);
//        document.add(newLine);
        document.add(p);
        Paragraph p1 = new Paragraph("1.     Po zapoznaniu się ze Statutem Stowarzyszenia Liga Obrony Kraju w szczególności z§ 21, § 22.1,2,\n" + "§23, §24.1, §27, §29 i §32 oświadczam, że:", font(fs, 0));
        p1.setIndentationLeft(55);
        Paragraph p1a = new Paragraph("     •   będę godnie reprezentować Ligę obrony kraju, dbać o prestiż i wizerunek stowarzyszenia oraz\n" + "propagować jego cele i zadania,", font(fs, 0));
        p1a.setIndentationLeft(65);
        Paragraph p1b = new Paragraph("     •   będę przestrzegać postanowień statutu, regulaminów i uchwał władz stowarzyszenia,", font(fs, 0));
        p1b.setIndentationLeft(65);
        Paragraph p1c = new Paragraph("     •   będę brać czynny udział w pracy klubu (koła) do którego wstępuję, znam jego regulamin, cele i zadania,", font(fs, 0));
        p1c.setIndentationLeft(65);
        Paragraph p1d = new Paragraph("     •   będę opłacać regularnie składkę członkowską i inne świadczenia obowiązujące w stowarzyszeniu.", font(fs, 0));
        p1d.setIndentationLeft(65);
        p1.setLeading(ls);
        p1a.setLeading(ls);
        p1b.setLeading(ls);
        p1c.setLeading(ls);
        p1d.setLeading(ls);
        document.add(p1);
        document.add(p1a);
        document.add(p1b);
        document.add(p1c);
        document.add(p1d);

        String state;

        Paragraph p2 = new Paragraph("2.     Wyrażam zgodę na przetwarzanie moich ww. danych osobowych przez Stowarzyszenie Liga Obrony\n" + "     Kraju zgodnie z Rozporządzeniem Parlamentu Europejskiego i Rady (UE) 2016/679 z dn. 27.04.2016r.\n" + "     (Rozporządzenie 2016/679).", font(fs, 0));
        p2.setIndentationLeft(55);
        p2.setLeading(ls);
        document.add(p2);
        state = member.getSex() ? "zostałem poinformowany" : "zostałam poinformowana";
        Paragraph p3 = new Paragraph("3.     Potwierdzam, że " + state + " o tym, że:", font(fs, 0));
        p3.setIndentationLeft(55);
        p3.setLeading(ls);
        document.add(p3);
        Paragraph p3a = new Paragraph("a)     Administratorem podanych danych osobowych jest Stowarzyszenie Liga Obrony Kraju mające siedzibę\n" + "główną w Warszawie pod adresem: ul. Chocimska 14, 00-791 Warszawa.", font(fs, 0));
        p3a.setIndentationLeft(65);
        Paragraph p3b = new Paragraph("b)     W Stowarzyszeniu Liga Obrony Kraju wyznaczono inspektora ochrony danych.\n" + "     Dane kontaktowe inspektora są następujące:\n" + "        - adres korespondencyjny:                    " + "          Inspektor Ochrony Danych\n" + "                                                                                Liga Obrony Kraju, Biuro Zarządu Głównego\n" + "                                                                                ul. Chocimska 14, 00-791 Warszawa\n" + "        - adres poczty elektronicznej:               " + "          iod@lok.org.pl", font(fs, 0));
        p3b.setIndentationLeft(65);
        state = member.getSex() ? "Pana" : "Pani";
        Paragraph p3c = new Paragraph("c)     " + state + " dane będą przekazane w celu:\n" + "        - realizacji zadań określonych w Statucie LOK na podstawie art. 6 ust. 1 lit. a) Rozp. 2016/679;\n" + "        - udokumentowania organom kontrolującym posiadanych przez " + (member.getSex() ? "Pana" : "Panią") + " kwalifikacji;\n" + "        - wypełnienia obowiązków prawnych ciążących na LOK na podstawie powszechnie obowiązujących\n" + "        przepisów prawa, m.in. przepisów podatkowych oraz o rachunkowości, na podstawie\n" + "        art. 6 ust. 1 lit, c) Rozp. 2016/679;\n" + "        - rozliczenia finansowego zleconych usług, w tym egzekucji należności wynikających z wzajemnej\n" + "        umowy, na podstawie art. 6 ust. 1 lit f) Rozp. 2016/679. Prawnie uzasadnionym interesem LOK jest\n" + "        zapewnienie odpowiednich dochodów z prowadzonej działalności;\n" + "        - badania jakości realizacji usług szkoleniowych na podstawie art. 6 ust. 1 lit f) Rozp. 2016/679.\n" + "        Prawnie uzasadnionym interesem LOK jest pozyskanie informacji o poziomie satysfakcji klientów ze\n" + "        świadczonych usług;\n" + "        - oraz w celach analitycznych i statystycznych na podstawie art, 6 ust. 1 lit f) Rozp. 2016/679.\n" + "        Prawnie uzasadnionym interesem LOK jest prowadzenie analizy wyników prowadzonej działalności.", font(fs, 0));
        p3c.setIndentationLeft(65);
        state = member.getSex() ? "Pana" : "Pani";
        Paragraph p3d = new Paragraph("d)     " + state + " dane osobowe będą (mogą być) przekazywane:\n" + "        - nadrzędnym władzom LOK oraz w niezbędnym zakresie:\n" + "           • współpracującym z LOK instytucjom, urzędom administracji państwowej i samorządowej oraz firmom\n" + "           w związku z realizacją zadań statutowych;\n" + "           • innym podwykonawcom realizującym wspólnie z LOK zadania statutowe;\n" + "           • urzędom uprawnionym do nadawania wyróżnień i odznaczeń państwowych;\n" + "           • klientom LOK oraz innym członkom LOK, którzy będą chcieli sprawdzić kwalifikacje kadry szkolącej\n" + "           realizującej usługę szkoleniową;\n" + "           • redakcji Biuletynu Ligi Obrony Kraju \"Czata\";\n" + "           • administratorom: stron internetowych, mediów społecznościowych dokumentujących działalność\n" + "           klubów i kół LOK w celu np. publikacji wyników z zawodów (imprez);\n" + "        - operatorom pocztowym w zakresie niezbędnym do przesyłania korespondencji;\n" + "        - bankom w zakresie realizacji płatności;\n" + "        - organom publicznym uprawnionym do otrzymania " + state + " danych na podstawie przepisów\n" + "        prawa (np. organy wymiaru sprawiedliwości, organy skarbowe, komornicy itd.);", font(fs, 0));
        p3d.setIndentationLeft(65);
        Paragraph p3e = new Paragraph("e)    " + state + " dane osobowe nie będą przekazywane do państwa trzeciego lub organizacji międzynarodowej.\n" + "Jeśli w związku z działalnością statutową zajdzie potrzeba przekazania danych za granicę to dane zostaną\n" + "przekazane na podstawie odrębnej zgody;", font(fs, 0));
        p3e.setIndentationLeft(65);
        Paragraph p3f = new Paragraph("f)     " + state + " dane będą przechowywane przez okres:\n" + "        - bezterminowo - dane opublikowane w Księdze Honorowej lub Kronice Ligi Obrony Kraju;\n" + "        - deklaracja członkowska - 5 lat od zakończenia członkostwa w LOK;\n" + "        - 5 lat - dokumenty finansowe, podatkowe;\n" + "        - do 5 lat pozostałe (zgodnie z odrębnymi regulacjami dla poszczególnych rodzajów dokumentów);", font(fs, 0));
        p3f.setIndentationLeft(65);
        state = member.getSex() ? "Panu" : "Pani";
        Paragraph p3g = new Paragraph("g)     Przysługuje " + state + " prawo do:\n" + "        - żądania od administratora dostępu do swoich danych osobowych, ich sprostowania, usunięcia lub\n" + "        ograniczenia przetwarzania; wniesienia sprzeciwu wobec przetwarzania; przenoszenia danych;", font(fs, 0));
        p3g.setIndentationLeft(65);
        state = member.getSex() ? "Pan" : "Pani";
        Paragraph p3h = new Paragraph("h)     Ma " + state + " prawo do cofnięcia zgody na przetwarzanie w dowolnym momencie bez wpływu na\n" + "zgodność z prawem przetwarzania, którego dokonano na podstawie zgody przed jej cofnięciem;", font(fs, 0));
        p3h.setIndentationLeft(65);
        Paragraph p3i = new Paragraph("i)     Ma " + state + " prawo do wniesienia skargi do organu nadzorczego zajmującego się ochroną danych\n" + "osobowych w Polsce (Prezes Urzędu Ochrony Danych Osobowych), jeśli uzna " + state + ", że jej dane są\n" + "przetwarzane z naruszeniem przepisów Rozporządzenia 2016/679 oraz przepisów krajowych dotyczących\n" + "ochrony danych osobowych;", font(fs, 0));
        p3i.setIndentationLeft(65);
        state = member.getSex() ? "Pana" : "Panią";
        Paragraph p3j = new Paragraph("j)     Podanie przez " + state + " danych jest wymogiem umownym. Konsekwencją niepodania danych\n" + "jest odmowa przyjęcia w poczet członków Stowarzyszenia Liga Obrony Kraju;", font(fs, 1));
        p3j.setIndentationLeft(65);
        Paragraph p3k = new Paragraph("k)     Podane przez " + state + " dane osobowe nie będą przetwarzane w systemach automatycznie\n" + "podejmujących decyzje, nie będą profilowane.", font(fs, 0));
        p3k.setIndentationLeft(65);
        p3a.setLeading(ls);
        p3b.setLeading(ls);
        p3c.setLeading(ls);
        p3d.setLeading(ls);
        p3e.setLeading(ls);
        p3f.setLeading(ls);
        p3g.setLeading(ls);
        p3h.setLeading(ls);
        p3i.setLeading(ls);
        p3j.setLeading(ls);
        p3k.setLeading(ls);
        document.add(p3a);
        document.add(p3b);
        document.add(p3c);
        document.newPage();
        page = new Paragraph("2/3", font(fs, 0));
        page.setAlignment(0);
        document.add(page);
        document.add(zalacznik);
        document.add(zalacznik1);
        document.add(p3d);
        document.add(p3e);
        document.add(p3f);
        document.add(p3g);
        document.add(p3h);
        document.add(p3i);
        document.add(p3j);
        document.add(p3k);

        Paragraph p4 = new Paragraph("4.     Jednocześnie oświadczam źe:", font(fs, 0));
        p4.setIndentationLeft(55);
        p4.setLeading(ls);
        document.add(p4);

        Paragraph p4a = new Paragraph("a)     zachowam w poufności dane osobowe: klientów LOK, pracowników i członków LOK, otrzymane w związku\n" + "z prowadzoną działalnością statutową.\n" + "W szczególności nie będę wykorzystywać powierzonych danych osobowych:\n" + "do prowadzenia działalności reklamowej usług i produktów własnych i firm trzecich, w celach prywatnych\n" + "(np. matrymonialnych), w celu ich \"sprzedaży\" innym osobom, podmiotom gospodarczym;", font(fs, 0));
        p4a.setIndentationLeft(65);
        state = member.getSex() ? "przetwarzał" : "przetwarzała";
        Paragraph p4b = new Paragraph("b)     powierzone dane osobowe będę " + state + " tylko w zakresie niezbędnym do prawidłowej realizacji\n" + "zadań statutowych, a po ich wykonaniu nie będę danych osobowych przetwarzać dłużej niź jest to\n" + "potrzebne lub wymagane przez stosowne przepisy np. podatkowe;", font(fs, 0));
        p4b.setIndentationLeft(65);
        Paragraph p4c = new Paragraph("c)     powierzonych danych osobowych nie będę przekazywał poza granice kraju oraz organizacjom\n" + "międzynarodowym;", font(fs, 0));
        p4c.setIndentationLeft(65);
        state = member.getSex() ? "realizował" : "realizowała";
        Paragraph p4d = new Paragraph("d)     na żądanie administratora będę niezwłocznie " + state + " żądania osób fizycznych wynikające z praw\n" + "określonych w art. 15-22 Rozporządzenia 2016/679;", font(fs, 0));
        p4d.setIndentationLeft(65);
        Paragraph p4e = new Paragraph("e)     jeśli ww. żądanie wpłynie bezpośrednio do mnie, to w ciągu 72h przekażę je inspektorowi ochrony danych;", font(fs, 0));
        p4e.setIndentationLeft(65);
        Paragraph p4f = new Paragraph("f)     zawiadomię w ciągu 48h inspektora ochrony danych o każdym naruszeniu ochrony powierzonych przez\n" + "LOK danych osobowych w sposób określony w pkt. 3.8 procedury PW 1.7 ochrony danych osobowych.\n" + "Procedura jest dostępna po zalogowaniu na stronie - www.lok.org.pl/iso\n" + "dane użytkownika - procedury@lok.org.pl, hasło - procedury);", font(fs, 0));
        p4f.setIndentationLeft(65);
        state = member.getSex() ? "przestrzegał" : "przestrzegała";
        Paragraph p4g = new Paragraph("g)     będę " + state + " uregulowań dotyczących zachowania poufności danych firmowych określonych\n" + "w umowach zawartych ze współpracującymi z LOK firmami. Jeśli takiej umowy nie sporządzono na piśmie\n" + "lub nie zawiera ona stosownych regulacji, to zachowam w poufności wobec stron trzecich wszelkie\n" + "informacje pozyskane z firm współpracujących z LOK, również po wystąpieniu z LOK.", font(fs, 0));
        p4g.setIndentationLeft(65);
        p4a.setLeading(ls);
        p4b.setLeading(ls);
        p4c.setLeading(ls);
        p4d.setLeading(ls);
        p4e.setLeading(ls);
        p4f.setLeading(ls);
        p4g.setLeading(ls);
        document.add(p4a);
        document.add(p4b);
        document.add(p4c);
        document.add(p4d);
        document.add(p4e);
        document.add(p4f);
        document.add(p4g);
        document.add(newLine);

        PdfPTable table5 = new PdfPTable(2);
        Paragraph p5 = new Paragraph("........................................ dnia " + LocalDate.now().format(dateFormat()), font(fs, 0));
        p5.setLeading(ls);
        PdfPCell cell5 = new PdfPCell();
        cell5.addElement(p5);
        cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell5.setBorder(0);
        cell5.setPadding(0);
        Paragraph p6 = new Paragraph("...............................................................", font(fs, 0));
        p6.setLeading(ls);
        p6.setAlignment(Element.ALIGN_RIGHT);
        PdfPCell cell6 = new PdfPCell();
        cell6.addElement(p6);
        cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell6.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell6.setBorder(0);
        cell6.setPadding(0);
        table5.addCell(cell5);
        table5.addCell(cell6);
        table5.completeRow();

        p5 = new Paragraph("           (miejscowość)", font(8, 0));
        p5.setLeading(ls);
        cell5 = new PdfPCell();
        cell5.addElement(p5);
        cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell5.setBorder(0);
        cell5.setPadding(0);
        p6 = new Paragraph("(czytelny podpis osoby składającej deklarację)    ", font(8, 0));
        p6.setLeading(ls);
        p6.setAlignment(Element.ALIGN_RIGHT);
        cell6 = new PdfPCell();
        cell6.addElement(p6);
        cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell6.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell6.setBorder(0);
        cell6.setPadding(0);
        table5.addCell(cell5);
        table5.addCell(cell6);
        document.add(table5);
        document.newPage();
        page = new Paragraph("3/3", font(fs, 0));
        page.setAlignment(0);
        document.add(page);
        document.add(zalacznik);
        document.add(zalacznik1);
        Paragraph title2 = new Paragraph("Oświadczenie opiekuna prawnego *)", !member.isAdult() ? font(15, 1) : font(15, 8));
        title2.setAlignment(1);
        document.add(title2);
        document.add(newLine);

        float[] f3 = {20, 80};
        PdfPTable table7 = new PdfPTable(f3);
        Paragraph p7 = new Paragraph("Imię i nazwisko:", font(11, 1));
        PdfPCell cell7 = new PdfPCell();
        cell7.setHorizontalAlignment(0);
        cell7.setVerticalAlignment(5);
        cell7.addElement(p7);
        table7.addCell(cell7);
        table7.addCell(new PdfPCell());
        table7.completeRow();

        PdfPCell addressTitleCell1 = new PdfPCell(new Paragraph("Adres", font(11, 1)));
        PdfPCell addressCell1 = new PdfPCell();
        addressTitleCell1.setVerticalAlignment(5);
        addressTitleCell1.setHorizontalAlignment(0);
        table7.addCell(addressTitleCell1);

        PdfPTable table8 = new PdfPTable(1);
        table8.setWidthPercentage(100);
        table8.addCell(new PdfPCell(new Paragraph(" ", font(11, 0))));
        table8.completeRow();
        table8.addCell(new PdfPCell(new Paragraph(" ", font(11, 0))));
        addressCell1.setBorder(0);
        addressCell1.setPadding(0);
        addressCell1.addElement(table8);
        table7.addCell(addressCell1);
        table7.completeRow();

        table7.addCell(new PdfPCell(new Paragraph("PESEL", font(11, 1))));
        PdfPCell cell8 = new PdfPCell();

        PdfPTable table9 = new PdfPTable(new float[]{35, 15, 50});
        table9.setWidthPercentage(100);
        table9.addCell(new PdfPCell());
        table9.addCell(new PdfPCell(new Paragraph("Nr. telefonu:", font(11, 1))));
        table9.addCell(new PdfPCell());
        cell8.addElement(table9);
        cell8.setBorder(0);
        cell8.setPadding(0);
        table7.addCell(cell8);
        document.add(table7);
        state = member.getSex() ? "mojego podopiecznego" : "mojej podopiecznej";
        Paragraph p9 = new Paragraph("     Oświadczam, że wyrażam zgodę na wstąpienie " + state + " do\n" + "Stowarzyszenia Liga Obrony Kraju. Akceptuję i potwierdzam wyrażoną w deklaracji członkowskiej przez\n" + state + " zgodę dotyczącą przetwarzania danych osobowych. Jednocześnie wyrażam\n" + "zgodę na przetwarzanie moich powyżej podanych danych osobowych zgodnie z informacją w pk. 3 deklaracji\n" + "członkowskiej. Potwierdzam, że się z tą informacją zapoznałem.", font(fs, 0));
        p9.setIndentationLeft(55);
        p9.setLeading(ls);
        document.add(p9);
        document.add(newLine);
        document.add(newLine);

        table5 = new PdfPTable(2);
        p5 = new Paragraph("........................................ dnia " + LocalDate.now().format(dateFormat()), font(fs, 0));
        p5.setLeading(ls);
        cell5 = new PdfPCell();
        cell5.addElement(p5);
        cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell5.setBorder(0);
        cell5.setPadding(0);
        p6 = new Paragraph("...............................................................", font(fs, 0));
        p6.setLeading(ls);
        p6.setAlignment(Element.ALIGN_RIGHT);
        cell6 = new PdfPCell();
        cell6.addElement(p6);
        cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell6.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell6.setBorder(0);
        cell6.setPadding(0);
        table5.addCell(cell5);
        table5.addCell(cell6);
        table5.completeRow();

        p5 = new Paragraph("           (miejscowość)", font(8, 0));
        p5.setLeading(ls);
        cell5 = new PdfPCell();
        cell5.addElement(p5);
        cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell5.setBorder(0);
        cell5.setPadding(0);
        p6 = new Paragraph("(czytelny podpis opiekuna prawnego)          ", font(8, 0));
        p6.setLeading(ls);
        p6.setAlignment(Element.ALIGN_RIGHT);
        cell6 = new PdfPCell();
        cell6.addElement(p6);
        cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell6.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell6.setBorder(0);
        cell6.setPadding(0);
        table5.addCell(cell5);
        table5.addCell(cell6);
        document.add(table5);
        document.add(newLine);
        document.add(newLine);
        document.add(newLine);

        PdfPTable line = new PdfPTable(1);
        PdfPCell line1 = new PdfPCell(new Paragraph(" "));
        line1.setBorderWidthLeft(0);
        line1.setBorderWidthRight(0);
        line1.setBorderWidthTop(0);
        line1.setPadding(0);
        line.addCell(line1);
        document.add(line);
        document.add(newLine);
        document.add(newLine);
        document.add(newLine);
        state = member.getSex() ? " został przyjęty" : " została przyjęta";
        Paragraph p12 = new Paragraph("Potwierdzam, że " + member.getSex() + " " + member.getFullName().toUpperCase() + state + " do:", font(12, 0));
        Paragraph p13 = new Paragraph(clubRepository.findById(1).orElseThrow(EntityNotFoundException::new).getFullName(), font(12, 0));
        Paragraph p14 = new Paragraph("na podstawie uchwały nr: ...................................... z dnia .........................", font(12, 0));
        Paragraph p15 = new Paragraph("Numer legitymacji członkowskiej " + member.getLegitimationNumber(), font(12, 0));
        p12.setIndentationLeft(55);
        p13.setIndentationLeft(55);
        p13.setAlignment(1);
        p14.setIndentationLeft(55);
        p15.setIndentationLeft(55);

        document.add(p12);
        document.add(newLine);
        document.add(newLine);
        document.add(p13);
        document.add(newLine);
        document.add(newLine);
        document.add(p14);
        document.add(p15);
        document.add(newLine);
        document.add(newLine);
        document.add(newLine);
        document.add(newLine);

        table5 = new PdfPTable(2);
        String city = environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName()) ? "Łódź" : environment.getActiveProfiles()[0].equals(ProfilesEnum.PANASZEW.getName()) ? "Panaszew" : "";
        p5 = new Paragraph(".............. " + city + " .............. dnia ...........................", font(fs, 0));
        p5.setLeading(ls);
        cell5 = new PdfPCell();
        cell5.addElement(p5);
        cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell5.setBorder(0);
        cell5.setPadding(0);
        p6 = new Paragraph("...............................................................", font(fs, 0));
        p6.setLeading(ls);
        p6.setAlignment(Element.ALIGN_RIGHT);
        cell6 = new PdfPCell();
        cell6.addElement(p6);
        cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell6.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell6.setBorder(0);
        cell6.setPadding(0);
        table5.addCell(cell5);
        table5.addCell(cell6);
        table5.completeRow();

        p5 = new Paragraph("           (miejscowość)", font(8, 0));
        p5.setLeading(ls);
        cell5 = new PdfPCell();
        cell5.addElement(p5);
        cell5.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell5.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell5.setBorder(0);
        cell5.setPadding(0);
        p6 = new Paragraph("(funkcja w LOK, czytelny podpis)          ", font(8, 0));
        p6.setLeading(ls);
        p6.setAlignment(Element.ALIGN_RIGHT);
        cell6 = new PdfPCell();
        cell6.addElement(p6);
        cell6.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell6.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell6.setBorder(0);
        cell6.setPadding(0);
        table5.addCell(cell5);
        table5.addCell(cell6);
        document.add(table5);

        document.close();

        return new PdfGenerationResults(fileName, baos.toByteArray());
    }
}


