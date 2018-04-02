package com.framgia.tool.crawler;

import com.framgia.tool.Config;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PhantomWebDriver {

    public WebDriver driver = null;

    public PhantomWebDriver() {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setCapability("takesScreenshot", false);
        caps.setJavascriptEnabled(true);

        PhantomJSDriverService builder =
                new PhantomJSDriverService.Builder()
                        .usingPhantomJSExecutable(new File(Config.PHANTOMJS_DRIVER_PATH))
                        .withLogFile(new File(getLogFileName()))
                        .usingCommandLineArguments(getCommandLineArguments())
                        .build();
        driver = new PhantomJSDriver(builder, caps);

        System.out.println("Initial phantomJS driver done !");
    }

    private String getLogFileName(){
        Date today = Calendar.getInstance().getTime();
        return "/data/log/phantomjs_" + new SimpleDateFormat("yyyyMMdd").format(today) + ".log";
    }

    protected String[] getCommandLineArguments() {
        List<String> arguments = new ArrayList<>();

        arguments.add("--ignore-ssl-errors=true");
        arguments.add("--load-images=false");

        return arguments.toArray(new String[arguments.size()]);
    }

    public void login() {
        //Implement later
    }

    public void connectToSite(String url) {
        System.out.println("Connecting to: " + url);
        driver.navigate().to(url);

        System.out.println("Navigator success !");
    }

    public String getHtmlContent() {
        WebDriverWait driverWait = new WebDriverWait(driver, 15);
        driverWait.until(ExpectedConditions.elementToBeClickable(By.className("wrapper")));

        System.out.println("Get page source ...");
        return driver.getPageSource();
    }

    public void close() {
        if(driver != null){
            driver.close();
            driver.quit();
        }

        driver = null;

        try {
            // Kill phantomjs process in Window
            if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) {
                Runtime.getRuntime().exec("taskkill /F /IM phantomjs.exe");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("PhantomJS driver closed !");
    }
}
