package com.hjax.kagamine;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {

    public static String combinePaths(String path1, String path2)
    {
        File file1 = new File(path1);
        File file2 = new File(file1, path2);
        return file2.getPath();
    }

    public static String getFilenameTimeStamp()
    {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    public static String getReplayFilename(String replayFolder) {
        return Util.combinePaths(replayFolder, Util.getFilenameTimeStamp() + ".SC2Replay");
    }
}
