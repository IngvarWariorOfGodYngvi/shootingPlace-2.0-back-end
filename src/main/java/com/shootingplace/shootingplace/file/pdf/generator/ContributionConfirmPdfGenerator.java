package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.contributions.ContributionEntity;
import com.shootingplace.shootingplace.contributions.ContributionRepository;
import com.shootingplace.shootingplace.enums.ProfilesEnum;
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
import java.net.URL;
import java.time.LocalDate;

import static com.shootingplace.shootingplace.file.utils.Utils.*;

@Component
@RequiredArgsConstructor
public class ContributionConfirmPdfGenerator {

    private final ClubRepository clubRepository;
    private final Environment environment;
    private final MemberRepository memberRepository;
    private final ContributionRepository contributionRepository;

    public PdfGenerationResults generate(String memberUUID, String contributionUUID, boolean a5rotate) throws DocumentException, IOException {
        MemberEntity member = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        ContributionEntity contributionEntity = contributionRepository.findById(contributionUUID).orElseThrow(EntityNotFoundException::new);
        ClubEntity club = clubRepository.findById(1).orElseThrow(EntityNotFoundException::new);

        int count = (int) member.getHistory().getContributionList().stream().filter(f -> f.getPaymentDay().equals(contributionEntity.getPaymentDay())).count();

        LocalDate contribution = contributionEntity.getPaymentDay();
        LocalDate validThru = contributionEntity.getValidThru();

        String fileName = "Skladka " + member.getFullName() + " " + LocalDate.now().format(dateFormat()) + ".pdf";

        String clubFullName = club.getFullName().toUpperCase();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(a5rotate ? PageSize.A5.rotate() : PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new PageStamper(environment, true, true, a5rotate ? PageStampMode.A5_LANDSCAPE : PageStampMode.A4));

        document.open();

        Image img = null;
        if (isDziesiatkaOrTest()) {
            URL resource = getClass().getClassLoader().getResource("fbg_10_qrcode.jpg");
            if (resource != null) {
                img = Image.getInstance(resource);
                img.scaleAbsolute(50, 50);
            }
        }

        String group = member.isAdult() ? "OGÓLNA" : "MŁODZIEŻOWA";
        String status = member.getSex() ? "opłaciła" : "opłacił";

        String contributionLevel = String.valueOf(90 * count);

        String counter = switch (count) {
            case 1 -> "jedną kwartalną składkę członkowską";
            case 2 -> "dwie kwartalne składki członkowskie";
            case 3 -> "trzy kwartalne składki członkowskie";
            case 4 -> "cztery kwartalne składki członkowskie";
            case 5 -> "pięć kwartalnych składek członkowskich";
            case 6 -> "sześć kwartalnych składek członkowskich";
            case 7 -> "siedem kwartalnych składek członkowskich";
            case 8 -> "osiem kwartalnych składek członkowskich";
            case 9 -> "dziewięć kwartalnych składek członkowskich";
            case 10 -> "dziesięć kwartalnych składek członkowskich";
            default -> count + " kwartalnych składek członkowskich";
        };

        Paragraph newLine = new Paragraph("\n", font(5, 0));

        Paragraph p = new Paragraph(clubFullName + "\n", font(14, 1));
        Paragraph p1 = new Paragraph("Potwierdzenie opłacenia składki członkowskiej", font(11, 2));
        Paragraph h1 = new Paragraph("Grupa ", font(11, 0));
        Phrase h2 = new Phrase(group, font(13, 1));

        Paragraph p2 = new Paragraph("\nNazwisko i Imię : ", font(11, 0));
        Paragraph p211 = new Paragraph("Numer Legitymacji : ", font(11, 0));

        Phrase p3 = new Phrase(member.getSecondName() + " " + member.getFirstName(), font(15, 1));
        Phrase p5 = new Phrase(String.valueOf(member.getLegitimationNumber()), font(13, 1));

        Paragraph p6 = new Paragraph("\nData opłacenia składki : ", font(11, 0));
        Phrase p7 = new Phrase(String.valueOf(contribution), font(11, 1));

        Paragraph p8 = new Paragraph("Składka ważna do : ", font(11, 0));
        Phrase p9 = new Phrase(String.valueOf(validThru), font(11, 1));

        Paragraph p10 = new Paragraph(member.getSex() + " " + member.getFullName() + " dnia : " + contribution + " " + status + " " + counter + " w wysokości " + contributionLevel + " zł.", font(11, 0));

        Paragraph p16 = new Paragraph("\n", font(15, 0));
        Paragraph p19 = new Paragraph("               pieczęć klubu", font(10, 0));

        Phrase p20 = new Phrase("                                                  ");
        Phrase p21 = new Phrase("podpis osoby przyjmującej składkę");

        Paragraph p200 = new Paragraph("dołącz do naszej grupy na Facebooku", font(9, 0));

        p.setAlignment(Element.ALIGN_CENTER);
        p1.setAlignment(Element.ALIGN_CENTER);

        h1.add(h2);
        h1.setAlignment(Element.ALIGN_CENTER);

        p2.add(p3);
        p2.add("                                    ");
        p211.add(p5);
        p6.add(p7);
        p8.add(p9);
        p20.add(p21);
        p19.add(p20);
        p16.setIndentationLeft(25);
        p19.setIndentationLeft(40);

        document.add(p);
        document.add(p1);
        document.add(h1);
        document.add(p2);
        document.add(p211);
        document.add(p6);
        document.add(p8);

        if (isDziesiatkaOrTest()) {
            document.add(p10);
        }

        document.add(new Paragraph(" ", font(11, 0)));
        document.add(p16);
        document.add(p19);
        document.add(newLine);
        document.add(newLine);

        if (isDziesiatkaOrTest() && img != null) {
            document.add(img);
            document.add(p200);
        }

        document.close();

        return new PdfGenerationResults(fileName, baos.toByteArray());
    }

    private boolean isDziesiatkaOrTest() {
        String profile = environment.getActiveProfiles()[0];
        return profile.equals(ProfilesEnum.DZIESIATKA.getName()) || profile.equals(ProfilesEnum.TEST.getName());
    }
}
