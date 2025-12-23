package com.shootingplace.shootingplace.utils.database;

import com.shootingplace.shootingplace.file.FilesEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/database")
@CrossOrigin
@RequiredArgsConstructor
public class DatabaseController {

    private final DatabaseService databaseService;

    @GetMapping("/getDatabaseMembersToCSVFile")
    public ResponseEntity<byte[]> getDatabaseMembersToCSVFile () throws IOException {
        FilesEntity filesEntity = databaseService.getCSV();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());

    }
}
