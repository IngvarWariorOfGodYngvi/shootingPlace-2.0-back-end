package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.utils.PageStamper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

import static com.shootingplace.shootingplace.file.pdf.PdfUtils.dateFormat;
import static com.shootingplace.shootingplace.file.pdf.PdfUtils.font;

@Component
@RequiredArgsConstructor
public class CertificateOfClubMembershipPanaszewPdfGenerator {

    private final Environment environment;

    public PdfGenerationResults generate(MemberEntity member, String reason)
            throws DocumentException, IOException {

        String[] choice = {"ZAŚWIADCZENIE ZWYKŁE", "ZAŚWIADCZENIE DO WPA"};

        String fileName = reason + " " + member.getFullName() + ".pdf";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new PageStamper(environment, true, true));

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        Paragraph newLine = new Paragraph("\n", font(12, 0));

        String city = "Panaszew";

        Paragraph date = new Paragraph(
                city + ", " + LocalDate.now().format(dateFormat()),
                font(12, 0)
        );
        date.setAlignment(Element.ALIGN_RIGHT);
        document.add(date);

        Paragraph title = new Paragraph("\n\nZaświadczenie\n", font(14, 1));
        title.setAlignment(Element.ALIGN_CENTER);

        Paragraph subTitle1 = new Paragraph(
                "O CZŁONKOSTWIE W STOWARZYSZENIU \n",
                font(13, 1)
        );
        subTitle1.setAlignment(Element.ALIGN_CENTER);

        Paragraph subTitle2 = new Paragraph(
                "O CHARAKTERZE STRZELECKIM I KOLEKCJONERSKIM\n\n",
                font(13, 1)
        );
        subTitle2.setAlignment(Element.ALIGN_CENTER);

        String pesel = "";
        if (!reason.equals(choice[0])) {
            pesel = " PESEL: " + member.getPesel();
        }

        String sex;
        String address;

        if (member.getSex()) {
            sex = "Pani";
            address = "zamieszkała ";
        } else {
            sex = "Pan";
            address = "zamieszkały ";
        }

        Paragraph par1 = new Paragraph(
                "Niniejszym zaświadczam, że " + sex + " " +
                        member.getFullName() + " " +
                        address + member.getAddress().toString() +
                        ", nr. " + pesel +
                        ", numer legitymacji klubowej: " +
                        member.getLegitimationNumber() +
                        ", jest członkiem Stowarzyszenia Klub Strzelecki RCS Panaszew.",
                font(12, 0)
        );
        par1.setFirstLineIndent(40);

        Paragraph par2 = new Paragraph(
                "Niniejsze zaświadczenie stanowi potwierdzenie spełnienia jednego z warunków " +
                        "koniecznych do wydania pozwolenia na broń do celów sportowych i/lub kolekcjonerskich, " +
                        "o których mowa w Art. 10 ust. 3 pkt. 3 i 5 Ustawy z dnia 21 maja 1999r. o broni i amunicji " +
                        "(Dz.U. 1999 nr 53 poz. 549).",
                font(12, 0)
        );
        par2.setFirstLineIndent(40);

        Paragraph par21 = new Paragraph(
                "Zaświadczenie wydaje się na wniosek zainteresowanego.",
                font(12, 0)
        );
        par21.setFirstLineIndent(40);

        Paragraph par3 = new Paragraph(
                "W załączeniu przekazujemy potwierdzony za zgodność wyciąg ze statutu Stowarzyszenia.",
                font(12, 0)
        );
        par3.setFirstLineIndent(40);

        Paragraph par4 = new Paragraph(
                "....................................................",
                font(12, 0)
        );
        par4.setAlignment(Element.ALIGN_RIGHT);
        par4.setIndentationRight(40);

        Paragraph par5 = new Paragraph(
                "Podpis członka Zarządu",
                font(12, 0)
        );
        par5.setAlignment(Element.ALIGN_RIGHT);
        par5.setIndentationRight(60);

        document.add(title);
        document.add(subTitle1);
        document.add(subTitle2);
        document.add(par1);

        if (!reason.equals(choice[0])) {
            document.add(par2);
            document.add(par21);
            document.add(par3);
        }

        document.add(newLine);
        document.add(newLine);
        document.add(newLine);
        document.add(par4);
        document.add(par5);

        document.close();

        return new PdfGenerationResults(fileName, baos.toByteArray());
    }
}

