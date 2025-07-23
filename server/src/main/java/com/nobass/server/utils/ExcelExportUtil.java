package com.nobass.server.utils;

import com.nobass.server.entities.UserMovieList;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Utilitaire pour l'export des listes de films utilisateur au format Excel (.xlsx).
 * <p>
 * Génère un fichier Excel contenant les listes "à voir" et "vus" d'un utilisateur.
 * </p>
 */
public class ExcelExportUtil {

    private ExcelExportUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Exporte les listes de films "vus" et "à voir" au format Excel.
     * @param watched Liste des UserMovieList vus
     * @param watchlist Liste des UserMovieList à voir
     * @return Un ByteArrayOutputStream contenant le fichier Excel généré
     */
    public static ByteArrayOutputStream exportMovieListsToExcel(List<UserMovieList> watched, List<UserMovieList> watchlist) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet watchedSheet = createSheetWithData(workbook, "Films vus", watched);
            Sheet watchlistSheet = createSheetWithData(workbook, "À voir", watchlist);

            adjustColumnWidths(watchedSheet);
            adjustColumnWidths(watchlistSheet);

            return writeWorkbookToStream(workbook);
        } catch (Exception e) {
            throw new RuntimeException("Erreur export Excel: " + e.getMessage(), e);
        }
    }

    private static Sheet createSheetWithData(Workbook workbook, String sheetName, List<UserMovieList> data) {
        Sheet sheet = workbook.createSheet(sheetName);
        createHeaderRow(sheet);
            int rowIdx = 1;
        for (UserMovieList uml : data) {
            if (uml == null || uml.getMovie() == null) {
                continue;
            }
            Row row = sheet.createRow(rowIdx++);
                fillMovieRow(row, uml);
            }
        return sheet;
    }

    private static void adjustColumnWidths(Sheet sheet) {
        for (int i = 0; i < 7; i++) {
            sheet.setColumnWidth(i, 20 * 256);
            }
    }

    private static ByteArrayOutputStream writeWorkbookToStream(Workbook workbook) throws Exception {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out;
    }
    private static void createHeaderRow(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Titre");
        header.createCell(1).setCellValue("Genres");
        header.createCell(2).setCellValue("Date ajoutée");
        header.createCell(3).setCellValue("Date vue");
        header.createCell(4).setCellValue("Note perso");
        header.createCell(5).setCellValue("Commentaire");
        header.createCell(6).setCellValue("Année sortie");
    }
    private static void fillMovieRow(Row row, UserMovieList uml) {
        row.createCell(0).setCellValue(uml.getMovie().getTitle());
        row.createCell(1).setCellValue(String.join(", ", uml.getMovie().getGenres() != null ? uml.getMovie().getGenres() : emptyList()));
        row.createCell(2).setCellValue(uml.getAddedDate() != null ? uml.getAddedDate().toLocalDate().toString() : "");
        row.createCell(3).setCellValue(uml.getWatchedDate() != null ? uml.getWatchedDate().toLocalDate().toString() : "");
        if (uml.getUserRating() != null) {
            row.createCell(4).setCellValue(uml.getUserRating());
        } else {
            row.createCell(4).setCellValue("");
        }
        row.createCell(5).setCellValue(uml.getUserNotes() != null ? uml.getUserNotes() : "");
        if (uml.getMovie().getReleaseDate() != null) {
            row.createCell(6).setCellValue(uml.getMovie().getReleaseDate().getYear());
        } else {
            row.createCell(6).setCellValue("");
        }
    }
}
