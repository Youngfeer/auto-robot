package com.yfrao.autorobot.util;

import java.io.File;

public class FileUtil {

    public void deleteDirectory(File file) {
        if (file.isFile()) {
            file.delete();//清理文件
        } else {
            File list[] = file.listFiles();
            if (list != null) {
                for (File f : list) {
                    deleteDirectory(f);
                }
                file.delete();//清理目录
            }
        }
    }
}
