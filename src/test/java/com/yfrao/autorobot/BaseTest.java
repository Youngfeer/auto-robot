package com.yfrao.autorobot;

import com.yfrao.autorobot.common.ScreenShoot;
import com.yfrao.autorobot.server.InitAppiumDriver;
import com.yfrao.autorobot.server.StartAppiumServer;
import io.appium.java_client.android.AndroidDriver;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

@SpringBootTest
public class BaseTest extends AbstractTestNGSpringContextTests {

    public static final String GET_DATA_BY_EXCEL = "getDataByExcel";
    // 定义测试数据存放的路径
    public static final String DATA_PROVIDER_PACKAGENAME = "/src/test/resources/testdata";

    //每个类的路径，如果在子类，就是子类的路径
    protected String className = this.getClass().getName();

    public AndroidDriver driver;
    public InitAppiumDriver initAppiumDriver;
    public StartAppiumServer startAppiumServer;
    public ScreenShoot screenShoot;

    @BeforeClass
    public void initApp(){
        startAppiumServer = new StartAppiumServer();
        startAppiumServer.startServcer();
        initAppiumDriver = new InitAppiumDriver();
        driver = initAppiumDriver.initDriver(driver);
        screenShoot = new ScreenShoot(driver);
    }

    @SuppressWarnings("resource")
    @DataProvider(name = GET_DATA_BY_EXCEL)
    public static Object[][] getExcelData(Method method){
        String dataPath = System.getProperty("user.dir") + DATA_PROVIDER_PACKAGENAME;
        String methodName = method.getName();
        String[] packageName = method.getDeclaringClass().getName().toString().split("\\.");
        //文件读入流
        FileInputStream fileInputStream = null;

        // 指定当前类当前方法对应的excel文件retPath
        String excPath = dataPath;
        for (int i = 1; i < packageName.length; i++) {
            excPath += File.separator + packageName[i];
        }
        excPath += ".xlsx";
        try {
            File file = new File(excPath);//获取文件
            fileInputStream = new FileInputStream(file);//读数据
            XSSFWorkbook workbook = new XSSFWorkbook(new BufferedInputStream(fileInputStream));


            XSSFSheet sheet = workbook.getSheet(methodName);//读取指定标签页的数据

            int rowNum = sheet.getPhysicalNumberOfRows();//获取行数(获取的是物理行数，也就是不包括那些空行（隔行）的情况)
            int columnNum = sheet.getRow(0).getPhysicalNumberOfCells();//获取列数

            //可变的测试数据长度、因此先定义一个ArrayList来存object【】
            ArrayList<Object[]> preObject = new ArrayList<>();


            // 判断是否需要判断执行Flag
            Boolean executeFlag = sheet.getRow(0).getCell(columnNum - 1).toString().equals("executeFlag");

            // 如果最后一个字段为executeFlag，则最后一个字段不需要获取
            if (executeFlag) {
                columnNum--;
            }

            for (int i = 1; i < rowNum; i++) {
                Object[] rowData = new Object[columnNum];
                String executeValue = "false";

                // 获取最后一个字段的值
                if (null != sheet.getRow(i) && null != sheet.getRow(i).getCell(columnNum)) {
                    sheet.getRow(i).getCell(columnNum).setCellType(Cell.CELL_TYPE_STRING);//先把类型设置为string
                    executeValue = sheet.getRow(i).getCell(columnNum).getStringCellValue().toLowerCase();
                }

                //有executeFlag 且 该字段数据不为true，则该条数据不需要测试，忽略。
                if (executeFlag && !executeValue.equals("true")) {
                    continue;
                }

                for (int h = 0; h < columnNum; h++) {
                    if (null != sheet.getRow(i) && null != sheet.getRow(i).getCell(h)) {
                        sheet.getRow(i).getCell(h).setCellType(Cell.CELL_TYPE_STRING);//先把类型设置为string
                        rowData[h] = sheet.getRow(i).getCell(h).getStringCellValue();//填充数组
                    } else {
                        rowData[h] = "";//填充字段为空的数据
                    }
                }
                preObject.add(rowData);
            }
            // 从Array中获取测试数据，拼装成Object[][]，并返回。
            Object[][] data = new Object[preObject.size()][columnNum];
            for (int j = 0; j < preObject.size(); j++) {
                data[j] = preObject.get(j);
            }

            fileInputStream.close();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fileInputStream) {
                try {
                    // 关闭文件流
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

}
