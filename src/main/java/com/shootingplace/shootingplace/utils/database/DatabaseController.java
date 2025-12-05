package com.shootingplace.shootingplace.utils.database;

import com.shootingplace.shootingplace.file.FilesEntity;
import com.shootingplace.shootingplace.history.ChangeHistoryService;
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
public class DatabaseController {

    private final DatabaseService databaseService;
    private final ChangeHistoryService changeHistoryService;

    public DatabaseController(DatabaseService databaseService, ChangeHistoryService changeHistoryService) {
        this.databaseService = databaseService;
        this.changeHistoryService = changeHistoryService;
    }

    @GetMapping("/getDatabaseMembersToCSVFile")
    public ResponseEntity<byte[]> getDatabaseMembersToCSVFile () throws IOException {
//        ResponseEntity<?> code = changeHistoryService.comparePinCode(pinCode);
        FilesEntity filesEntity = databaseService.getCSV();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(filesEntity.getType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment:filename=\"" + filesEntity.getName().trim() + "\"")
                .body(filesEntity.getData());

    }
}
