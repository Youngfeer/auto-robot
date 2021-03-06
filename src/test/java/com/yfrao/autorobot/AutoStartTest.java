package com.yfrao.autorobot;


import com.yfrao.autoapi.datahelper.DataSourceConfig;
import com.yfrao.autorobot.common.EnterIntoSmallProgram;
import com.yfrao.autorobot.page.CustHomePage;
import com.yfrao.autorobot.page.HomePage;
import com.yfrao.autorobot.page.OrderCartPage;
import com.yfrao.autorobot.page.OrderConfirmPage;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest(classes = {AutoRobotApplication.class, DataSourceConfig.class})
public class AutoStartTest extends BaseTest{

    @Test(dataProvider = GET_DATA_BY_EXCEL)
    public void autoBegin(String caseNo, String caseDescrption, String commodity, String num, String expectResult){
        HomePage homePage = new HomePage(driver);
        homePage.startSmallProgram();
        CustHomePage custHomePage = new CustHomePage(driver);
        if(custHomePage.isHomePage()){
            custHomePage.clickElement(custHomePage.orderMenu);
        }
        OrderCartPage orderCartPage = new OrderCartPage(driver);
        orderCartPage.addCommodityOrder(commodity);
        OrderConfirmPage orderConfirmPage = new OrderConfirmPage(driver);
        orderConfirmPage.payOrder();
        log.info(orderConfirmPage.successMsg.getText());
        Assert.assertTrue(orderConfirmPage.successMsg.getText().equals(expectResult));
    }

}
