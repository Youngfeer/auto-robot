package com.yfrao.autorobot.page;

import com.yfrao.autorobot.base.BasePage;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CustHomePage extends BasePage {

    private String homeTagText = "销售目标";
    Logger logger = LoggerFactory.getLogger(getClass());


    public CustHomePage(AndroidDriver driver) {
        super(driver);
    }

    public boolean isHomePage() throws IOException {
        WebElement webElement = getElementByText(homeTagText);
        if (webElement == null) {
            logger.error("不在首页！");
            return false;
        }
        if(webElement.getText().equals("销售目标")){
            return true;
        }
        return false;
    }
}
