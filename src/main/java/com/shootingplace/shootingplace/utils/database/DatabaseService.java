package com.shootingplace.shootingplace.utils.database;

import com.shootingplace.shootingplace.file.FilesEntity;
import com.shootingplace.shootingplace.file.XLSXFilesService;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DatabaseService {
    private final XLSXFilesService xlsxFilesService;

    public DatabaseService(XLSXFilesService xlsxFilesService) {
        this.xlsxFilesService = xlsxFilesService;
    }

    public FilesEntity getCSV() throws IOException {

        return xlsxFilesService.getCSV();
    }
}
