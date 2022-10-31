package cn.monkey.system.service;

import cn.monkey.commons.data.pojo.vo.Result;
import cn.monkey.commons.data.pojo.vo.Results;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service("demo")
public class DemoService implements IDemoService, IFileService {

    private static final String EXCEL_REGEX = "^.+\\.(?i)((xls)|(xlsx))$";

    static boolean isExcel(String filename) {
        return filename.matches(EXCEL_REGEX);
    }

    protected final MongoTemplate mongoTemplate;

    public DemoService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Result<?> upload(MultipartHttpServletRequest request) throws IOException {
        MultipartFile file = request.getFile("upload");
        if (file == null) {
            throw new NullPointerException("file: [upload] is not exists");
        }
        if (isExcel(file.getName())) {
            String collectionName = request.getParameter("collectionName");
            return this.decodeAndSaveExcel(collectionName, file);
        }
        return Results.ok();
    }

    private Result<?> decodeAndSaveExcel(String collectionName, MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        String name = file.getName();
        Workbook workbook;
        if (name.endsWith("xls")) {
            workbook = new HSSFWorkbook(inputStream);
        } else if (name.endsWith("xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("invalid file");
        }
        Sheet sheetAt = workbook.getSheetAt(0);
        int lastRowNum = sheetAt.getLastRowNum();
        List<String> head = this.readData(sheetAt.getRow(0));
        List<Map<String, String>> collect = IntStream.range(1, lastRowNum)
                .mapToObj(i -> {
                    Row row = sheetAt.getRow(i);
                    return readData(row);
                })
                .map(data -> {
                    Map<String, String> map = new HashMap<>(data.size());
                    for (int i = 0; i < head.size(); i++) {
                        map.put(head.get(i), data.get(i));
                    }
                    return map;
                }).collect(Collectors.toList());
        this.mongoTemplate.insert(collect, collectionName);
        return Results.ok(collect);
    }

    private List<String> readData(Row head) {
        List<String> vals = new ArrayList<>(head.getLastCellNum());
        for (Iterator<Cell> it = head.cellIterator(); it.hasNext(); ) {
            Cell cell = it.next();
            String cellVal = getCellVal(cell);
            vals.add(cellVal);
        }
        return vals;
    }

    protected String getCellVal(Cell cell) {
        if (cell == null) {
            return "";
        }
        CellType cellType = cell.getCellType();
        switch (cellType) {
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}
