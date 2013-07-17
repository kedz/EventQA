package edu.columbia.cs.event.qa.util;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import weka.classifiers.Evaluation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * User: wojo
 * Date: 7/16/13
 * Time: 2:21 PM
 */

public class ExcelMachine {

    private HSSFWorkbook workbook;

    public ExcelMachine (String eval1, String eval2) {
        workbook = new HSSFWorkbook();
        workbook.createSheet(eval1);
        workbook.createSheet(eval2);
    }

    public String updateSheet (String sheetName, Evaluation eval) {
        HSSFSheet sheet = workbook.getSheet(sheetName);
        int lastRowNum = sheet.getLastRowNum();
        Row row = sheet.createRow(lastRowNum+1);
        row.createCell(0).setCellValue(lastRowNum+1);
        row.createCell(1).setCellValue(eval.pctCorrect()/100);
        row.createCell(2).setCellValue(eval.precision(0));
        row.createCell(3).setCellValue(eval.recall(0));
        row.createCell(4).setCellValue(eval.fMeasure(0));
        row.createCell(5).setCellValue(eval.precision(1));
        row.createCell(6).setCellValue(eval.recall(1));
        row.createCell(7).setCellValue(eval.fMeasure(1));
        return "Accuracy: "+eval.pctCorrect();
    }

    public void saveWorkbook (String fileName) {
        System.out.println("**************************************************************");
        System.out.println("Saving Workbook to: "+fileName);
        try {
            FileOutputStream out = new FileOutputStream(new File(fileName));
            workbook.write(out);
            out.close();
        } catch (IOException e) {
            System.err.println("Error: Writing excel file: "+fileName);
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
