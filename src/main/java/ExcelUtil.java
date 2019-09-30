import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;

public class ExcelUtil {
    private static final Logger logger = LogManager.getLogger(ExcelUtil.class);
    private static final String dataFilePath = System.getProperty("user.dir").replace("\\", "/") + "/src/main/resources/";


    public static synchronized Object[] getData(String fileName, String sheetName, int headerRowNumber) throws JSONException {
        JSONObject[][] returnData = (JSONObject[][])null;

        try {
            FileInputStream file = new FileInputStream(new File(dataFilePath + fileName));
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheet(sheetName);
            int firstDataRow = headerRowNumber + 1;
            int columnTCStatus = 1;
            int lastDataRow = 0;

            try {
                lastDataRow = sheet.getLastRowNum();
            } catch (Exception var20) {
                logger.error("Cannot locate the sheet specified - " + sheetName + " in file - " + fileName);
            }

            int TCStatusOnRows = lastDataRow - headerRowNumber;
            XSSFRow headerRow = sheet.getRow(headerRowNumber);
            int lastColumn = headerRow.getLastCellNum();
            logger.info("File='" + dataFilePath + fileName + "' Sheet='" + sheetName + "' Rows=" + lastDataRow + " and Columns=" + lastColumn);
            JSONObject[][] data = new JSONObject[lastDataRow][1];

            int j;
            for(j = firstDataRow; j <= lastDataRow; ++j) {
                JSONObject jo = new JSONObject();
                XSSFRow row = sheet.getRow(j);
                if (row != null && !getCellValue(row.getCell(columnTCStatus, XSSFRow.CREATE_NULL_AS_BLANK)).equalsIgnoreCase("OFF") && !getCellValue(row.getCell(columnTCStatus, XSSFRow.CREATE_NULL_AS_BLANK)).equalsIgnoreCase("HOLD") && !getCellValue(row.getCell(columnTCStatus, XSSFRow.CREATE_NULL_AS_BLANK)).equalsIgnoreCase("")) {
                    int column = 0;

                    try {
                        for(; column < lastColumn; ++column) {
                            XSSFCell cell = row.getCell(column, XSSFRow.CREATE_NULL_AS_BLANK);
                            String columnHeader = getCellValue(headerRow.getCell(column, XSSFRow.CREATE_NULL_AS_BLANK));
                            if (getCellValue(cell).equals("")) {
                                jo.put(columnHeader, "");
                            } else {
                                jo.put(columnHeader, getCellValue(cell));
                            }
                        }
                    } catch (NullPointerException var21) {
                        logger.warn("Could not read Sheet=" + sheetName + " Row=" + j + " Column=" + column);
                    }

                    jo.put("fileNameRef", fileName);
                    jo.put("sheetNameRef", sheetName);
                    data[j - firstDataRow][0] = jo;
                } else {
                    --TCStatusOnRows;
                }
            }

            file.close();
            logger.info("Found " + TCStatusOnRows + " test cases");
            returnData = new JSONObject[TCStatusOnRows][1];
            j = 0;

            for(int i = 0; i < lastDataRow; ++i) {
                if (data[i][0] != null) {
                    returnData[j][0] = data[i][0];
                    ++j;
                }
            }
        } catch (IOException var22) {
            var22.printStackTrace();
        }

        return returnData;
    }

    private static synchronized String getCellValue(Cell cell) {
        if (cell.getCellType() == 2) {
            switch(cell.getCachedFormulaResultType()) {
                case 0:
                    return cell.getNumericCellValue() + "";
                case 1:
                    return cell.getRichStringCellValue().toString().trim();
                default:
                    return "";
            }
        } else if (cell.getCellType() == 0) {
            float f = (new BigDecimal(cell.getNumericCellValue() + "")).floatValue();
            int i = (int)f;
            return f % (float)i <= 0.0F && f >= 1.0F ? (new BigDecimal(cell.getNumericCellValue() + "")).longValue() + "" : (new BigDecimal(cell.getNumericCellValue() + "")).floatValue() + "";
        } else {
            return cell.getStringCellValue();
        }
    }
}
