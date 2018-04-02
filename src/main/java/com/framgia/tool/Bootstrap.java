package com.framgia.tool;

import com.framgia.tool.crawler.PhantomWebDriver;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Bootstrap {

    public static final String NEW_URL = "https://10.0.1.179";
    public static final String OLD_URL = "https://frdt2.framgia.vn";

    public static List<String> IGNORE_LIST = Arrays.asList(
            "https://10.0.1.179",
            "https://frdt2.framgia.vn",
            "http://frdt2.framgia.vn"
    );

    public static List<String> VERIFY_PATHS = Arrays.asList(
            "register",
            "mypage/?loginReq=true",
            "guide/index?name=howto",
            "guide/index?name=exchange",
            "faq/index",
            "contact/index",
            "coordinate/index",
            "item/index?brand_id=01&color=1",
            "item/index?brand_id=04&color=1",
            "item/index?brand_id=02&color=1",
            "item/index?brand_id=03&color=1",
            "tv?pr_id=29",
            "cart/index",
            "information?archive=2017",
            "information?category=44",
            "item/index?price=1",
            "information/index",
            "shoplist",
            "item",
            "item?new=1",
            "item?rearrival=1",
            "item?stock=1",
            "guide?name=company",
            "guide?name=terms",
            "guide?name=law",
            "guide?name=privacy",
            "pass_reminder",
            "detail_search",
            "mailmagazine/index",
            "mailmagazine/mail_pre_update",
            "page_history",
            "mypage/index",
            "mypage/change",
            "mypage/history/index"
    );

    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        VERIFY_PATHS.forEach(e -> {
            System.out.println("Verify PATH: " + e);
            try {
                bootstrap.verifyPath(e);
            } catch (Exception e1) {
                System.out.println("Verify PATH error: " + e1);
                e1.printStackTrace();
            }

            System.out.println("---------------------------------------------------------------");
            bootstrap.sleep(5);
        });
    }

    public void verifyPath(String path) throws Exception {
        String newUrl = String.format("%s/%s", NEW_URL, path);
        List<String> newHtml = getHtml(newUrl, false);
        //writeToFile(newHtml, "newHtml");

        sleep(3);

        String oldUrl = String.format("%s/%s", OLD_URL, path);
        List<String> oldHtml = getHtml(oldUrl, false);
        //writeToFile(oldHtml, "oldHtml");

        // Compute diff. Get the Patch object. Patch is the container for computed deltas.
        writeToFile(DiffUtils.diff(newHtml, oldHtml), normalizeURLPath(path));
    }

    public void sleep(long seconds) {
        try {
            System.out.println("Sleep in " + seconds + " seconds");
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<String> getHtml(String url, boolean mustLogin) throws Exception {
        PhantomWebDriver driver = null;
        String content = "";

        try {
            driver = new PhantomWebDriver();
            if (mustLogin) driver.login();
            driver.connectToSite(url);

            content = driver.getHtmlContent();
            for (String ignorePattern : IGNORE_LIST) {
                content = content.replaceAll(ignorePattern, "");
            }
        } finally {
            if (driver != null)
                driver.close();
        }
        System.out.println("CONTENT-SIZE: " + content.length());
        return Arrays.asList(content.split("\n"));
    }

    public void writeToFile(Patch patch, String key) {
        makeFilePaths();

        String fileName = String.format("%s_%s_%s.txt", Config.EXPORT_PATH, key, getCurrentDate());
        System.out.println("Export data to -> " + fileName);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true))) {
            System.out.println("Printing result ...");
            for (Delta delta : patch.getDeltas()) {
                String revisedText = delta.getRevised().toString();
                bw.write(revisedText + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToFile(List<String> data, String fileName) {
        makeFilePaths();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(String.format("%s_%s.txt", Config.EXPORT_PATH, fileName), true))) {
            for (String s : data) {
                bw.write(s + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeFilePaths() {
        File f = new File(Config.EXPORT_PATH);
        if (!f.exists()) f.mkdirs();
    }

    private String normalizeURLPath(String path) {
        return path.replaceAll("[^a-zA-Z0-9]", "_");
    }

    private String getCurrentDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return format.format(new Date());
    }

}
