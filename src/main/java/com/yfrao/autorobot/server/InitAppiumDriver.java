package com.yfrao.autorobot.server;

import com.yfrao.autorobot.page.HomePage;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class InitAppiumDriver {
    public AndroidDriver driver;

    public AndroidDriver initDriver(AndroidDriver driver){
        DesiredCapabilities capabilities = new DesiredCapabilities();
        File classpathRoot = new File(System.getProperty("user.dir"));
        File appDir = new File(classpathRoot, "src/app");
        File app = new File(appDir, "weixin.apk");
        //1、指定platformName--平台名
        capabilities.setCapability("platformName","Android");
        //2、指定deviceName-设备名
        capabilities.setCapability("deviceName","test");
//        capabilities.setCapability("app", app.getAbsolutePath());
        //3、指定appPackage --测试App标识
        capabilities.setCapability("appPackage","com.tencent.mm");
        //4、指定appActivity --启动App的
        capabilities.setCapability("appActivity","com.tencent.mm.ui.LauncherUI");
        //!!!不清除掉微信的数据
        capabilities.setCapability("noReset",true);
        URL url = null;
        try {
            url = new URL("http://127.0.0.1:4723/wd/hub");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        //初始化动作，打开测试App
        driver = new AndroidDriver(url,capabilities);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        return driver;
    }
}
