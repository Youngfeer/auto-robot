package com.yfrao.autorobot;


import com.yfrao.autorobot.common.EnterIntoSmallProgram;
import com.yfrao.autorobot.page.CustHomePage;
import com.yfrao.autorobot.page.HomePage;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

@SpringBootTest
public class AutoStartTest extends BaseTest{

    @BeforeClass
    public void initStart(){
        super.initApp();
    }
    @Test
    public void autoBegin() throws IOException {
        EnterIntoSmallProgram enterIntoSmallProgram = new EnterIntoSmallProgram(driver);
        Assert.assertTrue(enterIntoSmallProgram.isEnterProgram());
    }

}
