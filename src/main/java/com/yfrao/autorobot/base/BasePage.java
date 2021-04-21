package com.yfrao.autorobot.base;

import com.yfrao.autorobot.common.ScreenShoot;
import io.appium.java_client.MobileBy;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.offset.PointOption;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class BasePage {
    private AndroidDriver driver;
    Logger logger = LoggerFactory.getLogger(getClass());
    ScreenShoot screenShoot = new ScreenShoot(driver);

    public WebElement getElementByText(String locateMode, String locateTxt) {
        WebElement webElement;
        WebDriverWait wait = new WebDriverWait(driver,30);
//        try {
//            webElement = driver.findElementByAndroidUIAutomator("new UiSelector().text(" + "\"" + text + "\"" + ")");
//        } catch (Exception e) {
//            screenShoot.getScreenShoot(driver);
//            logger.error("未找到元素！");
//            return null;
//        }
        try{
            webElement = wait.until(new ExpectedCondition<WebElement>() {
                @Override
                public WebElement apply(WebDriver input) {
                        return driver.findElementByAndroidUIAutomator("new UiSelector()." + locateMode + "(" + "\"" + locateTxt + "\"" + ")");
                }
            });
            return webElement;
        }catch (Exception e){
            screenShoot.getScreenShoot(driver);
            logger.error("未找到元素！");
            return null;
        }
    }

    public WebElement scrollToElementAppear(String locateMode, String locateTxt){
        WebElement webElement;
        WebDriverWait wait = new WebDriverWait(driver,50);
        try{
            webElement = wait.until(new ExpectedCondition<WebElement>() {
                @Override
                public WebElement apply(WebDriver input) {
//                    UiScrollable collectionObject = new UiScrollable(new UiSelector().scrollable(true));
                    return driver.findElementByAndroidUIAutomator("new UiScrollable(new UiSelector().scrollable(true)).scrollIntoView(new UiSelector()." + locateMode + "(" + "\"" + locateTxt + "\"" + "))");
                }
            });
            return webElement;
        }catch (Exception e){
            screenShoot.getScreenShoot(driver);
            logger.error("未找到元素！");
            return null;
        }

    }

    public WebElement getBrotherElement(String find){
        WebElement webElement;
        WebDriverWait wait = new WebDriverWait(driver,50);
        try{
            webElement = wait.until(new ExpectedCondition<WebElement>() {
                @Override
                public WebElement apply(WebDriver input) {
//                    UiScrollable collectionObject = new UiScrollable(new UiSelector().scrollable(true));
                    return driver.findElementByAndroidUIAutomator(find);
                }
            });
            return webElement;
        }catch (Exception e){
            screenShoot.getScreenShoot(driver);
            logger.error("未找到元素！");
            return null;
        }
    }

    public WebElement waitElementTillAtrAppear(String find, String condition, String value){
        WebElement webElement;
        WebDriverWait wait = new WebDriverWait(driver,50);
        try{
            webElement = wait.until(new ExpectedCondition<WebElement>() {
                @Override
                public WebElement apply(WebDriver input) {
                    while(!driver.findElementByAndroidUIAutomator(find).getAttribute(condition).equals(value)){
                        continue;
                    }
                    return driver.findElementByAndroidUIAutomator(find);
                }
            });
            return webElement;
        }catch (Exception e){
            screenShoot.getScreenShoot(driver);
            logger.error("未找到元素！");
            return null;
        }
    }

    public Set<String> getHandles(){
        Set contextHandles=driver.getContextHandles();
        return contextHandles;
    }

    public void await(int time){
        driver.manage().timeouts().implicitlyWait(time, TimeUnit.SECONDS);
    }
    public void clickCoordinate(int x, int y){
        TouchAction action = new TouchAction(driver);
        action.tap(PointOption.point(x, y)).perform().release();
    }

    public BasePage(AndroidDriver driver) {
        this.driver = driver;
    }

    public void clickElement(WebElement element) {
        element.click();
    }

    public void eleGetPicture(WebElement element, String fileName){
        File picture = element.getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(picture,new File( fileName + ".jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void waitRegularTime(int seconds){
        long time = seconds * 1000;
        try{
            Thread.sleep(time);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}