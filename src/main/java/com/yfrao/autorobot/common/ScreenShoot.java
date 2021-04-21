package com.yfrao.autorobot.common;

import com.yfrao.autorobot.util.FileUtil;
import io.appium.java_client.android.AndroidDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ScreenShoot {
    private AndroidDriver driver;
    FileUtil fileUtil = new FileUtil();


    public ScreenShoot(AndroidDriver driver){
        this.driver= driver;
    }

    public void getScreenShoot(AndroidDriver driver) {
        SimpleDateFormat Timeformat = new SimpleDateFormat("yyy-MM-dd-HH-mm-ss");
        String dateString = Timeformat.format(new Date());
        String dirName = System.getProperty("user.dir") + "/screenshoot";
        if (!(new File(dirName).isDirectory())) {
            new File(dirName).mkdir();
        }else{
            fileUtil.deleteDirectory(new File(dirName));
        }
        File screen =  driver.getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(screen,new File(dirName+"/"+dateString +".jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
