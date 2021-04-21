package com.yfrao.autorobot.page;

import com.yfrao.autorobot.base.BasePage;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CustHomePage extends BasePage {

    private String homeTagText = "销售目标";
    private String orderMenuText = "我要订货";
    private String locateMode = "text";
    Logger logger = LoggerFactory.getLogger(getClass());


    public CustHomePage(AndroidDriver driver) {
        super(driver);
    }
    WebElement webElement = getElementByText(locateMode,homeTagText);
    public WebElement orderMenu = getElementByText(locateMode,orderMenuText);

    public boolean isHomePage() {
        if (webElement == null) {
            logger.error("不在首页！");
            return false;
        }
        if(webElement.getText().equals("销售目标") && orderMenu.getText().equals("我要订货")){
            clickElement(orderMenu);
            return true;
        }
        return false;
    }

}
