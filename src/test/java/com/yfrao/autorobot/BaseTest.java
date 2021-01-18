package com.yfrao.autorobot;

import com.yfrao.autorobot.common.ScreenShoot;
import com.yfrao.autorobot.server.InitAppiumDriver;
import com.yfrao.autorobot.server.StartAppiumServer;
import io.appium.java_client.android.AndroidDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;

@SpringBootTest
public class BaseTest {

    public AndroidDriver driver;
    public InitAppiumDriver initAppiumDriver;
    public StartAppiumServer startAppiumServer;
    public ScreenShoot screenShoot;

    @BeforeClass
    public void initApp(){
        startAppiumServer = new StartAppiumServer();
        startAppiumServer.startServcer();
        initAppiumDriver = new InitAppiumDriver();
        driver = initAppiumDriver.initDriver(driver);
        screenShoot = new ScreenShoot(driver);
    }
}
