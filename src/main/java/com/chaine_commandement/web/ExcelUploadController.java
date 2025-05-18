package com.chaine_commandement.web;



import com.chaine_commandement.dto.Fonction;
import com.chaine_commandement.service.ExcelUploadService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/chaine-commandement")
@AllArgsConstructor
@CrossOrigin("*")
public class ExcelUploadController {
    private ExcelUploadService excelUploadService;



    @PostMapping(value = "/excel/chaine",consumes = "multipart/form-data",produces = "application/json")
    public Fonction envoyeFichierExcelChaine(@RequestPart("file") MultipartFile file, @RequestParam String regionMilitaire) throws IOException {

        return excelUploadService.envoyeFichierExcelChaine(file,regionMilitaire);
    }
    @GetMapping("/json/chaine")
    public Fonction retournerChaineRegionMilitaire(@RequestParam String regionMilitaire) throws IOException {
        return excelUploadService.retournerChaineRegionMilitaire(regionMilitaire);
    }
    @GetMapping("/json/chaine-regiment")
    public Fonction retournerRegiment(@RequestParam String regiment) throws IOException {
        return excelUploadService.retournerRegiment(regiment);
    }
    @GetMapping(value = "/excel/chaine")
    public ResponseEntity<Resource> envoyeFichierExcelChaine() {

        return excelUploadService.downloadFileExcelChaine();
    }


}

