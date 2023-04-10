package com.m.d.turnuscreator.viewmodel;

import com.m.d.turnuscreator.bean.Spoj;
import com.m.d.turnuscreator.bean.Turnus;
import com.m.d.turnuscreator.repository.BaseDataRepository;
import de.saxsys.mvvmfx.SceneLifecycle;
import de.saxsys.mvvmfx.ViewModel;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import java.awt.print.PrinterException;

import java.awt.print.PrinterJob;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.printing.PDFPageable;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExportPdfViewModel implements ViewModel, SceneLifecycle {

    private BaseDataRepository dataRepository = BaseDataRepository.getInstance();

    @Getter
    private ObservableList<Turnus> turnusToExportObservableList = FXCollections.observableArrayList();

    @Getter
    private ObservableList<Turnus> turnusNotToExportObservableList = FXCollections.observableArrayList();


    private void init() {
        dataRepository.getTurnusObservableList().addListener((ListChangeListener<? super Turnus>) change -> {
            turnusNotToExportObservableList.clear();
            turnusNotToExportObservableList.addAll(dataRepository.getTurnusObservableList());
        });
    }

    @Override
    public void onViewAdded() {
        init();

    }

    @Override
    public void onViewRemoved() {

    }

    public void addToExportAll() {
        turnusToExportObservableList.addAll(turnusNotToExportObservableList);
        turnusNotToExportObservableList.clear();
    }

    public void removeFromExportAll() {
        turnusNotToExportObservableList.addAll(turnusToExportObservableList);
        turnusToExportObservableList.clear();
    }

    public void addOneToExport(Turnus turnus) {
        turnusToExportObservableList.add(turnus);
        turnusNotToExportObservableList.remove(turnus);
    }

    public void removeOneFromExport(Turnus turnus) {
        turnusNotToExportObservableList.add(turnus);
        turnusToExportObservableList.remove(turnus);
    }

    @SneakyThrows
    public void export(String absolutePath) {
        // Create a new document
        PDDocument document = new PDDocument();

        // Create a new page
        PDPage page = new PDPage();
        document.addPage(page);

        // Create a new content stream for the page
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        // Define table parameters
        int rows = 5;
        int columns = 3;
        float rowHeight = 20f;
        float tableWidth = page.getMediaBox().getWidth() - 72f; // 1 inch margin on each side
        float tableX = page.getMediaBox().getLowerLeftX() + 36f; // 0.5 inch margin on left
        float tableY = page.getMediaBox().getUpperRightY() - 72f; // 1 inch margin on top
        float columnWidth = tableWidth / columns;

        // Define header and data
        String[] header = {"Name", "Age", "Gender"};
        String[][] data = {
                {"John Doe", "35", "Male"},
                {"Jane Smith", "28", "Female"},
                {"Bob Johnson", "42", "Male"},
                {"Sally Brown", "19", "Female"},
                {"Joe Davis", "56", "Male"}
        };

        // Draw header row
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(tableX, tableY);
        for (int i = 0; i < header.length; i++) {
            contentStream.showText(header[i]);
            contentStream.newLineAtOffset(columnWidth, 0);
        }
        contentStream.endText();

        // Draw data rows
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.newLineAtOffset(tableX, tableY - rowHeight);
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                contentStream.showText(data[i][j]);
                contentStream.newLineAtOffset(columnWidth, 0);
            }
            contentStream.newLineAtOffset(-columnWidth * columns, -rowHeight);
        }
        contentStream.endText();

        // Close content stream and save document
        contentStream.close();
        document.save("table.pdf");

        // Create a PrinterJob
        PrinterJob job = PrinterJob.getPrinterJob();

        // Set the PDF document as the printable object
        job.setPageable(new PDFPageable(document));

        // Print the document
        job.print();

        // Close the document
        document.close();
    }
}
