package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.file.pageStamper.PageStampMode;
import com.shootingplace.shootingplace.file.pageStamper.PageStamper;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.score.ScoreEntity;
import com.shootingplace.shootingplace.tournament.TournamentEntity;
import com.shootingplace.shootingplace.tournament.TournamentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.shootingplace.shootingplace.file.utils.FilesUtils.dateFormat;
import static com.shootingplace.shootingplace.file.utils.FilesUtils.font;

@Component
@RequiredArgsConstructor
public class TournamentCompetitorsPdfGenerator implements PdfGenerator<String> {

    private final TournamentRepository tournamentRepository;
    private final ClubRepository clubRepository;
    private final Environment environment;

    @Override
    public PdfGenerationResults generate(String tournamentUUID) throws DocumentException, IOException {

        TournamentEntity tournament = tournamentRepository.findById(tournamentUUID).orElseThrow(EntityNotFoundException::new);
        ClubEntity club = clubRepository.findById(1).orElseThrow(EntityNotFoundException::new);
        String fileName = "Lista_zawodników_na_zawodach_" + tournament.getName() + ".pdf";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new PageStamper(environment, false, true, PageStampMode.A4));
        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        document.add(new Paragraph(tournament.getName().toUpperCase(), font(13, Font.BOLD)));

        document.add(new Paragraph(club.getCity() + ", " + dateFormat(tournament.getDate()), font(10, Font.ITALIC)));

        document.add(new Paragraph("\n", font(13, Font.NORMAL)));
        document.add(new Paragraph("WYKAZ Zawodników", font(13, Font.NORMAL)));
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setWidths(new float[]{4f, 3f, 2f}); // proporcje kolumn

        tournament.getCompetitionsList().stream()
                .flatMap(list -> list.getScoreList().stream())
                .filter(score -> score.getMember() != null || score.getOtherPersonEntity() != null)
                .map(this::competitorRow)
                .collect(Collectors.toCollection(() ->
                        new TreeSet<>(Comparator.comparing(
                                CompetitorRow::name, String.CASE_INSENSITIVE_ORDER
                        ))
                ))
                .forEach(row -> {
                    try {
                        table.addCell(cell(row.name(), Element.ALIGN_LEFT));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        table.addCell(cell(row.club(), Element.ALIGN_LEFT));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        table.addCell(cell(row.license(), Element.ALIGN_RIGHT));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        document.add(table);

        document.close();
        return new PdfGenerationResults(fileName, baos.toByteArray());
    }


//    private String competitorLabel(ScoreEntity score) {
//        if (score.getMember() != null) {
//            return score.getMember().getFullName() + " " + score.getMember().getClub().getShortName()
//                    + " L-" + score.getMember().getLicense().getNumber();
//        }
//        return score.getOtherPersonEntity().getFirstName() + " " + score.getOtherPersonEntity().getClub().getShortName()
//                + " L-" + score.getOtherPersonEntity().getLicenseNumber();
//    }
    private PdfPCell cell(String text, int align) throws IOException {
        PdfPCell cell = new PdfPCell(new Phrase(text, font(11, Font.NORMAL)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(align);
        cell.setPaddingBottom(4f);
        return cell;
    }
    private record CompetitorRow(String name, String club, String license) {}
    private CompetitorRow competitorRow(ScoreEntity score) {
        if (score.getMember() != null) {
            return new CompetitorRow(
                    score.getMember().getFullName(),
                    score.getMember().getClub().getShortName(),
                    score.getMember().getLicense() != null
                            ? "L-" + score.getMember().getLicense().getNumber()
                            : "—"
            );
        }
        return new CompetitorRow(
                score.getOtherPersonEntity().getFirstName(),
                score.getOtherPersonEntity().getClub().getShortName(),
                score.getOtherPersonEntity().getLicenseNumber() != null
                        ? "L-" + score.getOtherPersonEntity().getLicenseNumber()
                        : "—"
        );
    }

}
