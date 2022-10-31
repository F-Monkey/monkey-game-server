package cn.monkey.system;

import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileTest {
    public static void createExcel() throws IOException {
        File file = new File("/home/monkey/Desktop/test.xlsx");
        SXSSFWorkbook hssfWorkbook = new SXSSFWorkbook();
        SXSSFSheet sheet = hssfWorkbook.createSheet("test");
        SXSSFRow head = sheet.createRow(0);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        for (int i = 0; i < 10; i++) {
            SXSSFCell cell = head.createCell(i);
            cell.setCellValue("data_" + i);
        }
        for (int i = 1; i < 1000_000; i++) {
            SXSSFRow row = sheet.createRow(i);
            for (int j = 0; j < 10; j++) {
                SXSSFCell cell = row.createCell(j);
                cell.setCellValue(String.valueOf(i));
            }
        }
        hssfWorkbook.write(fileOutputStream);
        hssfWorkbook.close();
    }

    public static void main(String[] args) throws IOException {
        createExcel();
    }
}
