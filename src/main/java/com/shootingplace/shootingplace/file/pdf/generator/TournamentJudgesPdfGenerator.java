package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.shootingplace.shootingplace.enums.ProfilesEnum;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.tournament.TournamentEntity;
import com.shootingplace.shootingplace.tournament.TournamentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.shootingplace.shootingplace.file.utils.Utils.*;

@Component
@RequiredArgsConstructor
public class TournamentJudgesPdfGenerator implements PdfGenerator<String> {

    private final TournamentRepository tournamentRepository;
    private final Environment environment;

    @Override
    public PdfGenerationResults generate(String tournamentUUID) throws DocumentException, IOException {

        TournamentEntity tournament = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);

        String fileName = "Lista_sędziów_na_zawodach_" + tournament.getName() + ".pdf";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        document.add(new Paragraph(tournament.getName().toUpperCase(), font(13, Font.BOLD)));

        document.add(new Paragraph(resolveCity() + ", " + dateFormat(tournament.getDate()), font(10, Font.ITALIC)));

        document.add(new Paragraph("\n", font(13, Font.NORMAL)));
        document.add(new Paragraph("WYKAZ SĘDZIÓW", font(13, Font.NORMAL)));

        addMainArbiterSection(document, tournament);
        addRTSCommissionSection(document, tournament);
        addStandArbiters(document, tournament);
        addRTSArbiters(document, tournament);

        document.close();
        return new PdfGenerationResults(fileName, baos.toByteArray());
    }

    /* ================= sections ================= */

    private void addMainArbiterSection(Document document, TournamentEntity t) throws DocumentException, IOException {

        document.add(new Paragraph("\nSędzia Główny", font(13, Font.NORMAL)));

        document.add(new Paragraph(resolveArbiter(t.getMainArbiter(), t.getOtherMainArbiter()), font(12, Font.NORMAL)));
    }

    private void addRTSCommissionSection(Document document, TournamentEntity t) throws DocumentException, IOException {

        document.add(new Paragraph("\nPrzewodniczący Komisji RTS", font(13, Font.NORMAL)));

        document.add(new Paragraph(resolveArbiter(t.getCommissionRTSArbiter(), t.getOtherCommissionRTSArbiter()), font(12, Font.NORMAL)));
    }

    private void addStandArbiters(Document document, TournamentEntity t) throws DocumentException, IOException {

        if (t.getArbitersList().isEmpty() && t.getOtherArbitersList().isEmpty()) {
            return;
        }

        document.add(new Paragraph("\nSędziowie Stanowiskowi\n", font(12, Font.NORMAL)));

        t.getArbitersList().forEach(a -> {
            try {
                addArbiter(document, a);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        t.getOtherArbitersList().forEach(a -> {
            try {
                addArbiter(document, a);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void addRTSArbiters(Document document, TournamentEntity t) throws DocumentException, IOException {

        if (t.getArbitersRTSList().isEmpty() && t.getOtherArbitersRTSList().isEmpty()) {
            return;
        }

        document.add(new Paragraph("\nSędziowie Biura Obliczeń\n", font(12, Font.NORMAL)));

        t.getArbitersRTSList().forEach(a -> {
            try {
                addArbiter(document, a);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        t.getOtherArbitersRTSList().forEach(a -> {
            try {
                addArbiter(document, a);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    private void addArbiter(Document document, MemberEntity m) throws IOException {
        String cls = getArbiterClass(m.getMemberPermissions().getArbiterStaticClass());
        document.add(new Paragraph(m.getFirstName() + " " + m.getSecondName() + " " + cls, font(12, Font.NORMAL)));
    }

    private void addArbiter(Document document, OtherPersonEntity p) throws IOException {
        String cls = getArbiterClass(p.getPermissionsEntity().getArbiterStaticClass());
        document.add(new Paragraph(p.getFirstName() + " " + p.getSecondName() + " " + cls, font(12, Font.NORMAL)));
    }

    private String resolveArbiter(MemberEntity member, OtherPersonEntity other) {
        if (member != null) {
            return member.getFirstName() + " " + member.getSecondName() + " " + getArbiterClass(member.getMemberPermissions().getArbiterStaticClass());
        }
        if (other != null) {
            return other.getFirstName() + " " + other.getSecondName() + " " + getArbiterClass(other.getPermissionsEntity().getArbiterStaticClass());
        }
        return "Nie Wskazano";
    }

    // #TODO poprawić tak by nie było tylko dla łodzi i panaszewa ale ogólnie dla wszystkich
    private String resolveCity() {
        return environment.getActiveProfiles()[0].equals(ProfilesEnum.DZIESIATKA.getName()) || environment.getActiveProfiles()[0].equals(ProfilesEnum.TEST.getName()) ? "Łódź" : environment.getActiveProfiles()[0].equals(ProfilesEnum.PANASZEW.getName()) ? "Panaszew" : "";
    }
}
