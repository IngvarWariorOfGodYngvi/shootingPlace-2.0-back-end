package com.shootingplace.shootingplace.utils.database;

import com.shootingplace.shootingplace.file.FilesEntity;
import com.shootingplace.shootingplace.file.XLSXFilesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class DatabaseService {
    private final XLSXFilesService xlsxFilesService;

    public FilesEntity getCSV() throws IOException {

        return xlsxFilesService.getCSV();
    }
}
