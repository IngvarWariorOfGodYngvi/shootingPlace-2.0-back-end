package com.shootingplace.shootingplace.file.pdf.generator;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.shootingplace.shootingplace.file.FilesRepository;
import com.shootingplace.shootingplace.file.IFile;
import com.shootingplace.shootingplace.file.pdf.model.PdfGenerationResults;
import com.shootingplace.shootingplace.users.UserEntity;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceEntity;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceRepository;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static com.shootingplace.shootingplace.file.pdf.PdfUtils.font;

@Component
@RequiredArgsConstructor
public class WorkTimeReportPdfGenerator {

    private final WorkingTimeEvidenceRepository workRepo;
    private final WorkingTimeEvidenceService workServ;
    private final FilesRepository filesRepository;

    public PdfGenerationResults generate(int year, String month, boolean detailed)
            throws DocumentException, IOException {

        int reportNumber = 1;
        List<IFile> existing =
                filesRepository.findAllByNameContains(
                        "%" + month.toLowerCase() + "%",
                        "%" + year + "%"
                );

        if (!existing.isEmpty()) {
            reportNumber = existing.stream()
                    .max(Comparator.comparing(IFile::getVersion))
                    .orElseThrow(EntityNotFoundException::new)
                    .getVersion();
        }

        String fileName =
                "raport_pracy_"
                        + month.toLowerCase()
                        + "_" + reportNumber
                        + "_" + year + "_.pdf";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();
        document.addTitle(fileName);
        document.addCreationDate();

        int monthNumber = monthNumber(month);

        List<WorkingTimeEvidenceEntity> evidences =
                new ArrayList<>(workRepo.findAllByStopQuery(year, monthNumber));

        List<UserEntity> users =
                evidences.stream()
                        .map(WorkingTimeEvidenceEntity::getUser)
                        .distinct()
                        .toList();

        float[] columnWidths = {4F, 12F, 12F, 14F, 24F, 48F};
        int fontSize = 10;
        Paragraph newLine = new Paragraph(" ", font(fontSize, 1));
        AtomicInteger pageIndex = new AtomicInteger();

        for (UserEntity user : users) {

            document.add(
                    new Paragraph(
                            "Raport Pracy - "
                                    + monthNumber + "/"
                                    + year + "/"
                                    + reportNumber,
                            font(13, 1)
                    )
            );

            document.add(
                    new Paragraph(
                            user.getFirstName() + " " + user.getSecondName()
                                    + (detailed ? " szczegółowy" : ""),
                            font(fontSize, 0)
                    )
            );

            document.add(newLine);

            PdfPTable header = new PdfPTable(columnWidths);
            header.setWidthPercentage(100);

            header.addCell(cell("lp", fontSize));
            header.addCell(cell("Start", fontSize));
            header.addCell(cell("Stop", fontSize));
            header.addCell(cell("Czas pracy", fontSize));
            header.addCell(cell("Czy Zatwierdzony", fontSize));
            header.addCell(cell("Uwagi", fontSize));

            document.add(header);
            document.add(newLine);

            List<WorkingTimeEvidenceEntity> userWork =
                    evidences.stream()
                            .filter(e -> e.getUser().equals(user))
                            .sorted(Comparator.comparing(
                                    WorkingTimeEvidenceEntity::getStart
                            ).reversed())
                            .toList();

            AtomicInteger sumHours = new AtomicInteger();
            AtomicInteger sumMinutes = new AtomicInteger();

            for (int i = 0; i < userWork.size(); i++) {
                WorkingTimeEvidenceEntity w = userWork.get(i);

                LocalDateTime start = w.getStart();
                LocalDateTime stop = w.getStop();
                String workTime = workServ.countTime(start, stop);

                if (!detailed) {
                    start = workServ.getTime(start, true);
                    stop = workServ.getTime(stop, false);
                    workTime = w.getWorkTime();
                }

                int h = sumInt(workTime, 0, 2);
                int m = sumInt(workTime, 3, 5);

                sumHours.addAndGet(h);
                sumMinutes.addAndGet(m);

                String startFmt = start.format(
                        DateTimeFormatter.ofPattern(
                                detailed ? "dd/MM/yy HH:mm:ss" : "dd/MM/yy HH:mm"
                        )
                );
                String stopFmt = stop.format(
                        DateTimeFormatter.ofPattern(
                                detailed ? "dd/MM/yy HH:mm:ss" : "dd/MM/yy HH:mm"
                        )
                );

                PdfPTable row = new PdfPTable(columnWidths);
                row.setWidthPercentage(100);

                row.addCell(cell(String.valueOf(i + 1), fontSize));
                row.addCell(cell(startFmt, fontSize));
                row.addCell(cell(stopFmt, fontSize));
                row.addCell(cell(workTime.substring(0, 5), fontSize));
                row.addCell(
                        cell(
                                w.isAccepted() ? "tak" : "oczekuje na zatwierdzenie",
                                fontSize
                        )
                );

                String desc = "";
                if (w.isAutomatedClosed()) desc += "-Zamknięte automatycznie-";
                if (w.isToClarify()) desc += "-Nadgodziny-";

                row.addCell(cell(desc, fontSize));

                document.add(row);
            }

            int extraHours = sumMinutes.get() / 60;
            int restMinutes = sumMinutes.get() % 60;
            sumHours.addAndGet(extraHours);

            document.add(
                    new Paragraph(
                            "Suma godzin: "
                                    + String.format(
                                    "%02d:%02d",
                                    sumHours.get(),
                                    restMinutes
                            ),
                            font(fontSize, 1)
                    )
            );

            document.add(newLine);
            document.add(newLine);
            document.add(new Paragraph("Dokument Zatwierdził", font(fontSize, 0)));
            document.add(new Paragraph(".....................................", font(fontSize, 0)));

            if (pageIndex.incrementAndGet() < users.size()) {
                document.newPage();
            }
        }

        document.close();
        return new PdfGenerationResults(fileName, baos.toByteArray());
    }

    private PdfPCell cell(String text, int size) throws IOException {
        return new PdfPCell(new Paragraph(text, font(size, 0)));
    }

    private int sumInt(String s, int from, int to) {
        return Integer.parseInt(s.substring(from, to));
    }

    private int monthNumber(String month) {
        return switch (month.toLowerCase(Locale.ROOT)) {
            case "styczeń" -> 1;
            case "luty" -> 2;
            case "marzec" -> 3;
            case "kwiecień" -> 4;
            case "maj" -> 5;
            case "czerwiec" -> 6;
            case "lipiec" -> 7;
            case "sierpień" -> 8;
            case "wrzesień" -> 9;
            case "październik" -> 10;
            case "listopad" -> 11;
            case "grudzień" -> 12;
            default -> throw new IllegalArgumentException("Nieznany miesiąc");
        };
    }

}
