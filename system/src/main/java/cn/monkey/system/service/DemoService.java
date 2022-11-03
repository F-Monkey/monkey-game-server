package cn.monkey.system.service;

import cn.monkey.commons.data.pojo.vo.Result;
import cn.monkey.commons.data.pojo.vo.Results;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Service("demo")
public class DemoService implements IDemoService, IFileService {

    private static final Logger log = LoggerFactory.getLogger(DemoService.class);

    static boolean isExcel(String filename) {
        return filename.endsWith("xls") || filename.endsWith("xlsx");
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
        String filename = file.getOriginalFilename();
        if (isExcel(Objects.requireNonNull(filename))) {
            String collectionName = request.getParameter("collectionName");

            return this.decodeAndSaveExcel(collectionName, file, filename);
        }
        return Results.ok();
    }

    private Result<?> decodeAndSaveExcel(String collectionName, MultipartFile file, String fileName) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = StreamingReader.builder()
                    .rowCacheSize(100)
                    .bufferSize(8192)
                    .open(inputStream);
            Sheet sheetAt = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheetAt.rowIterator();
            if (!rowIterator.hasNext()) {
                return Results.fail("sheet data is empty");
            }
            Row next = rowIterator.next();
            List<String> head = readData(next);
            List<Map<String, String>> collect = new ArrayList<>();
            while (rowIterator.hasNext()) {
                List<String> data = readData(rowIterator.next());
                Map<String, String> map = new HashMap<>(data.size());
                for (int i = 0; i < head.size(); i++) {
                    map.put(head.get(i), data.get(i));
                }
                collect.add(map);
            }
            this.mongoTemplate.insert(collect, collectionName);
        }
        return Results.ok();
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

    class SheetTask implements Runnable {

        private final SXSSFSheet sheet;
        private final int from;
        private final int sheetRowSize;

        public SheetTask(SXSSFSheet sheet,
                         int from,
                         int sheetRowSize) {
            this.sheet = sheet;
            this.from = from;
            this.sheetRowSize = sheetRowSize;
        }

        @Override
        public void run() {
            try {
                DemoService.this.readDataAndWrite2Sheet(this.sheet, this.from, this.sheetRowSize);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void download(HttpServletRequest request, HttpServletResponse response) throws IOException {
        long count = this.mongoTemplate.count(new Query(), "demo");
        int sheetRowSize = 200000;
        int sheetSize = (int) Math.ceil((double) count / sheetRowSize);
        SXSSFWorkbook hssfWorkbook = new SXSSFWorkbook();
        List<SheetTask> sheetTasks = new ArrayList<>(sheetSize);
        for (int i = 0; i < sheetSize; i++) {
            sheetTasks.add(new SheetTask(hssfWorkbook.createSheet("sheet_" + i), i * sheetRowSize, sheetRowSize));
        }
        sheetTasks.parallelStream()
                .forEach(SheetTask::run);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "Attachment;Filename=demo.xlsx");
        hssfWorkbook.write(response.getOutputStream());
    }

    private void readDataAndWrite2Sheet(SXSSFSheet sheet, int from, int maxSize) throws IOException {
        int pageSize = 1000;
        int startPage = from / maxSize;
        for (int i = 0; i * pageSize <= maxSize; i++) {
            Pageable pageable = PageRequest.of(startPage + i, pageSize);
            Query query = new Query();
            query.with(pageable);
            List<LinkedHashMap> list = this.mongoTemplate.find(query, LinkedHashMap.class, "demo");
            this.writeRow(sheet, list, i * pageSize);
            sheet.flushRows();
        }
    }

    private static Gson gson = new Gson();
    private static Type MAP_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();

    private void writeRow(SXSSFSheet sheet, List<LinkedHashMap> list, int index) {
        for (int i = 0; i < list.size(); i++) {
            SXSSFRow row = sheet.createRow(index + i);
            LinkedHashMap o = list.get(i);
            o.remove("_id");
            Map<String, String> data;
            String s = gson.toJson(o);
            try {
                data = gson.fromJson(s, MAP_TYPE);
            } catch (Exception e) {
                log.error("gson parse error:\n", e);
                throw e;
            }
            int x = 0;
            for (Map.Entry<String, String> e : data.entrySet()) {
                SXSSFCell cell = row.createCell(x);
                cell.setCellValue(e.getValue());
                x++;
            }
        }
    }
}
