package com.yfrao.autorobot.common;

import com.yfrao.autorobot.page.CustHomePage;
import com.yfrao.autorobot.page.HomePage;
import io.appium.java_client.android.AndroidDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class EnterIntoSmallProgram {
    private AndroidDriver driver;
    Logger logger = LoggerFactory.getLogger(getClass());

    public EnterIntoSmallProgram(AndroidDriver driver) throws IOException {
        this.driver = driver;
    }
    public boolean isEnterProgram() {
        HomePage homePage = new HomePage(driver);
        homePage.startSmallProgram();
//        driver.manage().timeouts().implicitlyWait(5000, TimeUnit.MILLISECONDS);
        CustHomePage custHomePage = new CustHomePage(driver);
        if(custHomePage.isHomePage()){
            logger.info("成功进入首页！");
            return true;
        }
        return false;
    }

}
