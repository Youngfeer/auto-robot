package com.yfrao.autorobot.page;

import com.yfrao.autorobot.base.BasePage;
import io.appium.java_client.android.AndroidDriver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.util.Set;


@Slf4j
public class PackagePage extends BasePage {

    public PackagePage(AndroidDriver driver) {
        super(driver);
    }
    private String giftCom = "赠品";
    private String textMode = "text";
    private String loading = "正在加载";
    private String resourceMode = "resourceId";
    private String oneResource = "_n_499";
    private String complete = "完成";
    private String addConfirm = "确认添加";
    private String settlement = "结算";


    WebElement waterIcon;
    WebElement waterTF;
    WebElement waterAddBtn;
    WebElement giftBtn;
    WebElement oneBtn;
    WebElement addConfirmBtn;
    WebElement money;
    WebElement confirmPay;
    WebElement commodityNum;

    public void addPkgCommodityOrder(String commodity, int prodAmount, int giftAmount){
        WebElement giftBtn = getElementByText(textMode, giftCom);
        clickElement(giftBtn);
        waitRegularTime(10);
        addPkgGiftCommodity(1000, 1000);
    }

    public void addPkgCommodity(String commodity, int num){
        waterIcon = scrollToElementAppear("text", commodity);
        waterIcon = getBrotherElement("new UiSelector().resourceId("+"\"" + waterIcon.getAttribute("resource-id") + "\"" + ")");
        int resourceId = Integer.valueOf(waterIcon.getAttribute("resource-id").split("_")[2])+11;
        String resource = "_n_" + resourceId;
        waterTF = getBrotherElement("new UiSelector().resourceId(" + "\"" + resource + "\"" + ").childSelector(new UiSelector().index(0))");
        waterTF.click();
        for(int i = 0; i < num - 1; i++){
            waterAddBtn = getBrotherElement("new UiSelector().resourceId(" + "\"" + resource + "\"" + ").childSelector(new UiSelector().index(2))");
            clickElement(waterAddBtn);
            waitRegularTime(10);
        }
        giftBtn = getElementByText(textMode, giftCom);
        clickElement(giftBtn);
        waitRegularTime(10);
        addPkgGiftCommodity(1000, 1000);
        oneBtn = getElementByText(resourceMode, oneResource);
        oneBtn.click();
        clickElement(getElementByText(textMode, complete));

    }

    public void addPkgGiftCommodity(int x, int y){
        clickCoordinate(1000, 1100);
    }

    public void commitOrder(){
        commodityNum = getBrotherElement("new UiSelector().resourceId(\"shopCarts\").childSelector(new UiSelector().index(1)).childSelector(new UiSelector().index(1).text(\"2\"))");
//        waitRegularTime(5);
        addConfirmBtn = getElementByText(textMode, addConfirm);
        addConfirmBtn.click();
//        money = getBrotherElement("new UiSelector().textContains(\"合计\").fromParent(new UiSelector().className(\"android.view.View\"))");
        money = getElementByText(textMode, "共4箱0瓶");
        confirmPay =  getElementByText(textMode, settlement);
        confirmPay.click();
    }
}
