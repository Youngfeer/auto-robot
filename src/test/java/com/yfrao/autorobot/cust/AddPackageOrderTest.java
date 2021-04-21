package com.yfrao.autorobot.cust;

import com.yfrao.autoapi.datahelper.DataSourceConfig;
import com.yfrao.autorobot.AutoRobotApplication;
import com.yfrao.autorobot.BaseTest;
import com.yfrao.autorobot.page.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


@Slf4j
@SpringBootTest(classes = {AutoRobotApplication.class, DataSourceConfig.class})
public class AddPackageOrderTest extends BaseTest {

    @Test(dataProvider = GET_DATA_BY_EXCEL)
    public void autoBegin(String caseNo, String caseDescrption, String commodity, String pkgName, String expectResult){
        HomePage homePage = new HomePage(driver);
        homePage.startSmallProgram();
        CustHomePage custHomePage = new CustHomePage(driver);
        if(custHomePage.isHomePage()){
            custHomePage.clickElement(custHomePage.orderMenu);
        }
        OrderCartPage orderCartPage = new OrderCartPage(driver);
        orderCartPage.addPackageOrder(pkgName);
        PackagePage packagePage = new PackagePage(driver);
        packagePage.addPkgCommodity(commodity, 3);
        packagePage.commitOrder();
        OrderConfirmPage orderConfirmPage = new OrderConfirmPage(driver);
        orderConfirmPage.payOrder();
        log.info(orderConfirmPage.successMsg.getText());
        Assert.assertTrue(orderConfirmPage.successMsg.getText().equals(expectResult));
    }
}
