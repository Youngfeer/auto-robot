package com.yfrao.autorobot;


import com.yfrao.autoapi.BaseRequest.HttpRequestBase;
import com.yfrao.autoapi.util.HttpExecute;
import com.yfrao.autoapi.util.JsonCompareUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

@SpringBootTest
public class ApiBaseTest extends AbstractTestNGSpringContextTests {

    public static final String GET_DATA_BY_EXCEL = "getDataByExcel";
    // 定义测试数据存放的路径
    public static final String DATA_PROVIDER_PACKAGENAME = "/src/test/resources/testdata";

    //每个类的路径，如果在子类，就是子类的路径
    protected String className = this.getClass().getName();


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


    protected HttpRequestBase initHttpRequestBase(String url, String requestType, String header, String formData, String postBody, String expectResult){
        HttpRequestBase httpRequest = new HttpRequestBase();
        httpRequest.setUrl(url);
        httpRequest.setRequestType(requestType);
        HashMap<String, String> headMap = castStringToHashmap(header);
        HashMap<String, String> paramMap = castStringToParamMap(formData);
        httpRequest.setHeader(headMap);
        httpRequest.setFormData(paramMap);
        httpRequest.setPostBody(postBody);
        httpRequest.setExpectResult(expectResult);
        return httpRequest;

    }

    private HashMap<String,String> castStringToHashmap(String str){
        if(StringUtils.isBlank(str)){
            return null;
        }
        String[] strArr = str.split("\\|");
        HashMap<String, String> map = new HashMap<String, String>();
        for(String s : strArr){
            map.put(s.split(":")[0], s.split(":")[1]);
        }
        return map;
    }

    private HashMap<String,String> castStringToParamMap(String str){
        if(StringUtils.isBlank(str)){
            return null;
        }
        String[] strArr = str.split("&");
        HashMap<String, String> map = new HashMap<String, String>();
        for(String s : strArr){
            map.put(s.split(":")[0], s.split("=")[1]);
        }
        return map;
    }

    protected Boolean process(HttpRequestBase httpRequest) throws IOException {
        HttpExecute httpExecute = new HttpExecute();
        String res = "";
        res = httpExecute.execute(httpRequest);
        if(StringUtils.isBlank(res)){
            return false;
        }else{
            String result = JsonCompareUtil.jsonCompare(res, httpRequest.getExpectResult());
            if(result.contains("结果：fail") || result.contains("结果：异常")){
                throw new RuntimeException("http接口返回结果与期望结果不一致。" + result);
            }
            return true;
        }
    }

}
