package com.yfrao.autorobot;

import com.yfrao.autoapi.BaseRequest.HttpRequestBase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.annotations.Test;

import javax.annotation.Resource;


public class ListBusinessLine extends ApiBaseTest{

    @Resource
    @Qualifier("mesfJdbcTemplate")
    private JdbcTemplate mesfJdbcTemplate;

    @Test(dataProvider = GET_DATA_BY_EXCEL)
    public void run(String caseNo, String caseDescrption, String url,String requestType,String headParam, String loginUser,
                    String loginPassword, String paramData, String postBody, String requestExpectResult, String dbInfo,
                    String sqlList, String dbExpectResult, String author){
        String sql1 = "update  sf_dealer_bill_confirm set bill_status = 0 where id = 1315533055359737858";
        mesfJdbcTemplate.update(sql1);

        try {
            HttpRequestBase httpRequest = initHttpRequestBase(url, requestType, headParam,paramData,postBody,requestExpectResult);
            boolean res = this.process(httpRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
