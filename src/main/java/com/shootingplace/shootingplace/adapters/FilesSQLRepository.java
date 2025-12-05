package com.shootingplace.shootingplace.adapters;

import com.shootingplace.shootingplace.file.FilesEntity;
import com.shootingplace.shootingplace.file.FilesRepository;
import org.springframework.data.jpa.repository.JpaRepository;

interface FilesSQLRepository  extends FilesRepository, JpaRepository<FilesEntity, String> {

}
