package com.yfrao.autorobot.base;

import com.yfrao.autorobot.common.ScreenShoot;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BasePage {
    private AndroidDriver driver;
    Logger logger = LoggerFactory.getLogger(getClass());
    ScreenShoot screenShoot = new ScreenShoot(driver);

    public WebElement getElementByText(String text) throws IOException {
        WebElement webElement;
        try {
            webElement = driver.findElementByAndroidUIAutomator("new UiSelector().text(" + "\"" + text + "\"" + ")");
        } catch (Exception e) {
            screenShoot.getScreenShoot(driver);
            logger.error("未找到元素！");
            return null;
        }
        return webElement;
    }

    public BasePage(AndroidDriver driver) {
        this.driver = driver;
    }

    public void clickElement(WebElement element) throws IOException {
        element.click();
    }

}