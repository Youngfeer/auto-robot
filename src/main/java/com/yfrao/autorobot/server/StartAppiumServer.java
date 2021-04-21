package com.yfrao.autorobot.server;

import com.yfrao.autorobot.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

@Resource
public class StartAppiumServer {

    public Process process;
    Logger logger = LoggerFactory.getLogger(getClass());
    FileUtil fileUtil = new FileUtil();

    public void startServcer() {

        try {
            executed("killall -9 node", false, 0);
            List<String> executinfo = executed("adb devices", true, 0);
            executinfo.remove(0);
            executinfo.remove(executinfo.size() - 1);
            if(executinfo.size() < 1){
                logger.error("无可用设备！！！");
            }
            String uuid = executinfo.get(0).split("\t")[0];
            String logStr = "";
            Date dNow = new Date();
            SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddhhmmss");
            if (System.getProperty("os.name").contains("indow")) {
                logStr = System.getProperty("user.dir") + "\\logs\\" + ft.format(dNow) + "-" + uuid + ".log";
            } else {
                fileUtil.deleteDirectory(new File(System.getProperty("user.dir") + "/logs"));
                logStr = System.getProperty("user.dir") + "/logs/" + ft.format(dNow) + "-" + uuid + ".log";
            }
            String cmd = "appium --log" + " " + logStr;
            executed(cmd, false, 2);

        } catch (Exception e1) {
//            log.info("[startAppiumServer] 清除node进程失败");
//            log.info("[Class-startAppiumServer][Method-Map] 清除node进程失败");
            e1.printStackTrace();
        }

    }


    public List<String> executed(String Command, boolean isgetInfo, int seconds) throws Exception {

        List<String> context = null;
        String osName = System.getProperty("os.name");
        try {
            if (osName.contains("indow")) {
                process = Runtime.getRuntime().exec("cmd /c " + Command);
            } else {
                String[] cmd = new String[]{"/bin/sh", "-c", Command};
                process = Runtime.getRuntime().exec(cmd);
            }
            if (seconds >= 0) {
//                log.info("[Class-executeCommand][Method-executed] 当前Appium-Server开始延时启动中....");
            }
            Thread.sleep(seconds * 1000);// 注意 这里一定要延时 不然启动不了Appium
            // log.info("[executeCommand] 当前Appium-Server启动延时已经结束");
//            log.info("[Class-executeCommand][Method-executed] 当前Appium-Server启动延时已经结束");
        } catch (Exception e) {
            RuntimeException exception = new RuntimeException();
            exception.printStackTrace();
        }
        if (isgetInfo) {
            context = new ArrayList();
            InputStream in = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while ((line = reader.readLine()) != null) {
                context.add(line);
            }
            process.waitFor();
            process.destroy();
        }
//        log.info("[Class-executeCommand][Method-executed] 返回执行后的日志集合");
        return context;
    }
}
