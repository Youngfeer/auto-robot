package com.yfrao.autorobot.page;

import com.yfrao.autorobot.base.BasePage;
import io.appium.java_client.android.AndroidDriver;

import java.io.IOException;


public class HomePage extends BasePage {

//    private AndroidDriver driver;
    private String findText = "发现";
    private String smallText = "小程序";
    private String rainbowText = "彩虹鱼一店通";

    public HomePage(AndroidDriver driver) {
        super(driver);
    }

    public void startSmallProgram() throws IOException {
        clickElement(getElementByText(findText));
        clickElement(getElementByText(smallText));
        clickElement(getElementByText(rainbowText));
    }
}
