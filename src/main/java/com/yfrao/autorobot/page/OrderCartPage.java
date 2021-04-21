package com.yfrao.autorobot.page;

import com.yfrao.autorobot.base.BasePage;
import io.appium.java_client.android.AndroidDriver;
import lombok.Data;
import org.openqa.selenium.WebElement;


public class OrderCartPage extends BasePage {
    public OrderCartPage(AndroidDriver driver) {
        super(driver);
    }
    private String locateMode = "text";
    private String add = "添加商品";
    private String addConfirm = "确认添加";
    private String settlement = "结算";
    private String addPkg = "添加套餐";


    public WebElement addCommodityBtn = getElementByText(locateMode, add);
    public WebElement addPkgBtn = getElementByText(locateMode, addPkg);
    WebElement money;
    WebElement confirmPay;
    WebElement addConfirmBtn;

    public void addCommodityOrder(String commodity){
        clickElement(addCommodityBtn);
        WebElement waterIcon = scrollToElementAppear("text", commodity);
        WebElement waterTF = getBrotherElement("new UiSelector().text(" + "\"" + commodity + "\"" + ").fromParent(new UiSelector().index(62)).childSelector(new UiSelector().index(0))");
        waterTF.click();
        WebElement shopCart = getBrotherElement("new UiSelector().resourceId(\"shopCart\").childSelector(new UiSelector().index(0)).childSelector(new UiSelector().index(1)).childSelector(new UiSelector().index(1))");
//        clickElement(getElementByText(locateMode, addConfirm));
//        waitElementTillAtrAppear("new UiSelector().textContains(\"合计\").fromParent(new UiSelector().className(\"android.view.View\")).childSelector(new UiSelector().index(2))",locateMode, "26");
//        WebElement money = getBrotherElement("new UiSelector().resourceId(\"合计\").fromParent(new UiSelector().index(2).textMatches(\"[1-9]?\"))");
//        getBrotherElement("new UiSelector().textContains(\"合计\").fromParent(new UiSelector().className(\"android.view.View\"))");
    }

    public void addPackageOrder(String packageName){
        clickElement(addPkgBtn);
        clickElement(getElementByText(locateMode, packageName));

    }

    public void commitOrder(){
        addConfirmBtn = getElementByText(locateMode, addConfirm);
        clickElement(addConfirmBtn);
        money = getBrotherElement("new UiSelector().textContains(\"合计\").fromParent(new UiSelector().className(\"android.view.View\"))");
        confirmPay =  getElementByText(locateMode, settlement);
        eleGetPicture(confirmPay, "jiesuan");
        confirmPay.click();
    }
}
