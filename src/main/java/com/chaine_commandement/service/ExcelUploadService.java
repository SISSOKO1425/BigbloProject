package com.chaine_commandement.service;

import com.chaine_commandement.dto.Fonction;
import com.chaine_commandement.dto.Personne;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

@Service

public class ExcelUploadService {

    public Fonction envoyeFichierExcelChaine(MultipartFile file,String nomRegionMilitaire) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        List<Map<String, String>> fonctionsList =  new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheet("fonctions"); // Première feuille du fichier Excel
            Iterator<Row> rowIterator = sheet.iterator();

            //traitement de la page des fonctions
            Row headerRow = rowIterator.next();
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue().replaceAll("\\s+", ""));
            }
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, String> data = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i);
                    data.put(headers.get(i), cellToString(cell).replaceAll("\\s+", ""));
                }
                fonctionsList.add(data);
            }
            Map<String, Fonction> fonctionsMap = new HashMap<>();

            // Créer les objets Fonction
            int id=0;
            for (Map<String, String> entry : fonctionsList) {

                Fonction fonction = new Fonction();
                fonction.setNom(entry.get("FONCTION").replaceAll("\\s+", ""));
                fonction.setRM(entry.get("RM").replaceAll("\\s+", ""));
                fonction.setId(++id); // Génération simple d'ID
                fonctionsMap.put(fonction.getNom(), fonction);
            }
            // Construire la hiérarchie
            List<Fonction> racines = new ArrayList<>();
            for (Map<String, String> entry : fonctionsList) {
                String fonctionNom = entry.get("FONCTION").replaceAll("\\s+", "");
                String fonctionSupNom = entry.get("FONCTION_SUP").replaceAll("\\s+", "");
                Fonction fonction = fonctionsMap.get(fonctionNom);

                if (fonctionsMap.containsKey(fonctionSupNom)) {
                    fonctionsMap.get(fonctionSupNom).getSous_fonctions().add(fonction);
                } else {
                    racines.add(fonction);
                }
            }

            //recuperation des militaires et les associer a leur fonction respective
             sheet = workbook.getSheet("listeMilitaires");
             rowIterator = sheet.iterator();
            rowIterator.next(); // Ignorer la ligne d'en-tête

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row.getCell(0).getCellType().equals(CellType.BLANK) ||
                        row.getCell(0).getCellType().equals(CellType._NONE)) continue;
                Personne p = new Personne();
                p.setMle(cellToString(row.getCell(0)).replaceAll("\\s+", ""));
                p.setPrenoms(row.getCell(1).getStringCellValue());
                p.setNom(row.getCell(2).getStringCellValue());
                p.setGrade(row.getCell(3).getStringCellValue().replaceAll("\\s+", ""));
                p.setUnite(row.getCell(4).getStringCellValue().replaceAll("\\s+", ""));
                p.setDate(cellToString(row.getCell(5)).replaceAll("\\s+", ""));
                p.setReference(row.getCell(6).getStringCellValue());
                if (fonctionsMap.containsKey(row.getCell(7).getStringCellValue().replaceAll("\\s+", ""))) {
                    fonctionsMap.get(row.getCell(7).getStringCellValue().replaceAll("\\s+", "")).getPersonnes().add(p);
                }
            }
            //exportFonctionsToExcel(racines,"hirarchie.xlsx");
            // Convertir en JSON
            ObjectMapper mapper = new ObjectMapper();

            // Sauvegarder la hiérarchie et les militaires asscocies
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("chaine.json"), racines);




            // Sauvegarder le fichier excel en question

            try (FileOutputStream fos = new FileOutputStream("chaine.xlsx")) {
                fos.write(file.getBytes());
            }



        } catch (IOException e) {
            e.printStackTrace();

        }
        return retournerChaineRegionMilitaire(nomRegionMilitaire);
    }


    private String cellToString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
    public Fonction retournerChaineRegionMilitaire(String nomRegionMilitaire) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Fonction returnFonction=null;
        // Charger les fonctions
        List<Fonction> fonctionsList = mapper.readValue(new File("chaine.json"), new TypeReference<List<Fonction>>() {});
        for (Fonction fonction : fonctionsList) {
            if (fonction.getRM().equals(nomRegionMilitaire)) {
                returnFonction= fonction;
                break;
            }
        }

        chargerPhotosToutesPersonnes(returnFonction,"photos");
        return returnFonction;
    }
    public Fonction retournerRegiment(String nomFonctionRegiment) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Fonction returnFonction=null;
        Fonction regionMilitaireFonction=null;
        // Charger les fonctions
        char numeroRegionMilitaireDuRegiment = nomFonctionRegiment.chars()
                .mapToObj(c -> (char) c)
                .filter(Character::isDigit)
                .findFirst().get();
        List<Fonction> fonctionsList = mapper.readValue(new File("chaine.json"), new TypeReference<List<Fonction>>() {});
        for (Fonction fonction : fonctionsList) {
            if (fonction.getRM().equals("RM"+nomFonctionRegiment)) {
                regionMilitaireFonction= fonction;
                break;
            }
        }
        returnFonction= chercherFonctionParNom(regionMilitaireFonction,nomFonctionRegiment);

        chargerPhotosToutesPersonnes(returnFonction,"photos");
        return returnFonction;
    }

    private static Fonction chercherFonctionParNom(Fonction racine, String nomRecherche) {
        if (racine.getNom().equalsIgnoreCase(nomRecherche)) {
            return racine;
        }

        if (racine.getSous_fonctions() != null) {
            for (Fonction sous : racine.getSous_fonctions()) {
                Fonction trouve = chercherFonctionParNom(sous, nomRecherche);
                if (trouve != null) {
                    return trouve;
                }
            }
        }

        return null; // Pas trouvé
    }

    public void chargerPhotosToutesPersonnes(Fonction fonction, String dossierPhotos) {
        if (fonction == null) return;

        // Fonction pour charger la photo depuis le dossier
        Function<Personne, byte[]> photoLoader = personne -> {
            String cheminPhoto = dossierPhotos + "/" + personne.getMle() + ".jpg"; // Supposons que le fichier est ID.jpg

            return recupererLaPhotoDUnElement(cheminPhoto);
        };

        // Charger les photos pour les personnes de cette fonction
        fonction.getPersonnes().forEach(personne -> personne.setPhoto(photoLoader.apply(personne)));

        // Charger récursivement les photos pour les sous-fonctions
        fonction.getSous_fonctions().forEach(sousFonction -> chargerPhotosToutesPersonnes(sousFonction, dossierPhotos));
    }
    public static byte[] recupererLaPhotoDUnElement(String cheminPhoto)  {
        File file = new File(cheminPhoto);


        if (!file.exists()) {

            return null;
        }

        // Crée un tableau de bytes pour stocker le contenu du fichier
        byte[] bytesArray = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file)) {
            // Lit le contenu du fichier dans le tableau de bytes
            fis.read(bytesArray);
        } catch (IOException e) {
            // S'il y'a erreur return null
            System.out.println("erreur de lecture cas exception");
            return null;
        }

        return bytesArray;
    }
    public ResponseEntity<Resource> downloadFileExcelChaine() {
        try {
            Path filePath = Paths.get("chaine.xlsx");
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"chaine.xlsx\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }

    }
            public static void exportFonctionsToExcel(List<Fonction> fonctions, String filePath) throws IOException {
                Workbook workbook = new XSSFWorkbook();

                for (Fonction fonction : fonctions) {
                    Sheet sheet = workbook.createSheet(fonction.getNom().replaceAll("[\\\\/:*?\"<>|]", "_")); // nom de feuille sécurisé
                    int[] rowIndex = {0}; // pour suivre la ligne actuelle
                    //writeFonctionHierarchy(sheet, fonction, 0, rowIndex);
                   // writeHierarchy(sheet, fonction, 0, rowIndex);
                    writeHierarchyBreadthFirst(sheet,fonction,rowIndex);
                }

                try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                    workbook.write(fileOut);
                }
                workbook.close();
            }

            /*private static void writeFonctionHierarchy(Sheet sheet, Fonction fonction, int level, int[] rowIndex) {
                Row row = sheet.createRow(rowIndex[0]++);
                Cell cell = row.createCell(0);
                cell.setCellValue("  ".repeat(level) + fonction.getNom());

                // Ajouter les fonctions cette fonction

                    Cell fonctionCell = row.createCell(1);
                    fonctionCell.setCellValue(fonction.getNom());

                // Récurse sur les sous-fonctions
                if (fonction.getSous_fonctions() != null) {
                    for (Fonction sousFonction : fonction.getSous_fonctions()) {
                        writeFonctionHierarchy(sheet, sousFonction, level + 1, rowIndex);
                    }
                }
            }*/

    // Méthode récursive qui parcourt la hiérarchie
    private static void writeHierarchy(Sheet sheet, Fonction fonction, int level, int[] rowIndex) {
        Row row = sheet.createRow(rowIndex[0]++);
        Cell nameCell = row.createCell(0);
        nameCell.setCellValue("  ".repeat(level) + fonction.getNom());

        if (fonction.getRM() != null) {
            row.createCell(1).setCellValue(fonction.getRM());
        }



        if (fonction.getSous_fonctions() != null) {
            for (Fonction sous : fonction.getSous_fonctions()) {
                writeHierarchy(sheet, sous, level + 1, rowIndex); // récursion
            }
        }
    }
    private static void writeHierarchyBreadthFirst(Sheet sheet, Fonction racine, int[] rowIndex) {
        Queue<Fonction> queue = new LinkedList<>();
        queue.add(racine);

        while (!queue.isEmpty()) {
            Fonction fonction = queue.poll();

            // Écriture dans la feuille Excel
            Row row = sheet.createRow(rowIndex[0]++);
            row.createCell(0).setCellValue(fonction.getNom());

            if (fonction.getRM() != null) {
                row.createCell(1).setCellValue(fonction.getRM());
            }

            // Ajouter les sous-fonctions à la queue pour les traiter après
            if (fonction.getSous_fonctions() != null) {
                queue.addAll(fonction.getSous_fonctions());
            }
        }
    }

    private static String safeSheetName(String nom) {
        return nom.replaceAll("[\\\\/*:?\\[\\]]", "_").substring(0, Math.min(nom.length(), 31));
    }
}


