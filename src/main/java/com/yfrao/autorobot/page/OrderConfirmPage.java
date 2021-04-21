package com.yfrao.autorobot.page;

import com.yfrao.autorobot.base.BasePage;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;

public class OrderConfirmPage extends BasePage {

    public OrderConfirmPage(AndroidDriver driver) {
        super(driver);
    }

    private String confirmOrder = "提交订单";
    private String locateMode = "text";
    private String confirmPay = "确认支付";
    private String addOrderSuc = "订单提交成功";

    public WebElement confirmOrderBtn = getElementByText(locateMode, confirmOrder);
    public WebElement successMsg;

    public void payOrder(){
        clickElement(confirmOrderBtn);
        WebElement confirmPayBtn = getElementByText(locateMode, confirmPay);
        clickElement(confirmPayBtn);
        successMsg = getElementByText(locateMode, addOrderSuc);
    }
}
