/*
 * Tencent is pleased to support the open source community by making wechat-matrix available.
 * Copyright (C) 2018 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sample.gethandsdirty.anr.util;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Debug;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by caichongyang on 17/5/18.
 * about Device Info
 */

//https://juejin.cn/post/7135034198158475300
//http://gityuan.com/2015/12/20/signal/  各种signal
//https://mp.weixin.qq.com/s?__biz=MzAwNDY1ODY2OQ==&mid=2649288031&idx=1&sn=91c94e16460a4685a9c0c8e1b9c362a6&chksm=8334c9ddb44340cb66e6ce512ca41592fb483c148419737dbe21f9bbc2bfc2f872d1e54d1641&cur_album_id=1955379809983741955&scene=189#wechat_redirect
public class DeviceUtil {
    private static final long MB = 1024 * 1024;

    private static final String TAG = "Matrix.DeviceUtil";
    private static final int INVALID = 0;
    private static final String MEMORY_FILE_PATH = "/proc/meminfo";
    private static final String CPU_FILE_PATH_0 = "/sys/devices/system/cpu/";
    private static final String CPU_FILE_PATH_1 = "/sys/devices/system/cpu/possible";
    private static final String CPU_FILE_PATH_2 = "/sys/devices/system/cpu/present";
    private static LEVEL sLevelCache = null;

    public static final String DEVICE_MACHINE = "machine";
    private static final String DEVICE_MEMORY_FREE = "mem_free";
    private static final String DEVICE_MEMORY = "mem";
    private static final String DEVICE_CPU = "cpu_app";

    private static long sTotalMemory = 0;
    private static long sLowMemoryThresold = 0;
    private static int sMemoryClass = 0;

    public enum LEVEL {

        BEST(5), HIGH(4), MIDDLE(3), LOW(2), BAD(1), UN_KNOW(-1);

        int value;

        LEVEL(int val) {
            this.value = val;
        }

        public int getValue() {
            return value;
        }
    }

    public static JSONObject getDeviceInfo(JSONObject oldObj, Application context) {
        try {
            oldObj.put(DEVICE_MACHINE, getLevel(context));
            oldObj.put(DEVICE_CPU, getAppCpuRate());
            oldObj.put(DEVICE_MEMORY, getTotalMemory(context));
            oldObj.put(DEVICE_MEMORY_FREE, getMemFree(context));

        } catch (JSONException e) {
        }

        return oldObj;
    }

    public static LEVEL getLevel(Context context) {
        if (null != sLevelCache) {
            return sLevelCache;
        }
        long start = System.currentTimeMillis();
        long totalMemory = getTotalMemory(context);
        int coresNum = getNumOfCores();
        if (totalMemory >= 8 * 1024 * MB) {
            sLevelCache = LEVEL.BEST;
        } else if (totalMemory >= 6 * 1024 * MB) {
            sLevelCache = LEVEL.HIGH;
        } else if (totalMemory >= 4 * 1024 * MB) {
            sLevelCache = LEVEL.MIDDLE;
        } else if (totalMemory >= 2 * 1024 * MB) {
            if (coresNum >= 4) {
                sLevelCache = LEVEL.MIDDLE;
            } else if (coresNum > 0) {
                sLevelCache = LEVEL.LOW;
            }
        } else if (totalMemory >= 0) {
            sLevelCache = LEVEL.BAD;
        } else {
            sLevelCache = LEVEL.UN_KNOW;
        }
        return sLevelCache;
    }

    private static int getAppId() {
        return android.os.Process.myPid();
    }

    public static long getLowMemoryThresold(Context context) {
        if (0 != sLowMemoryThresold) {
            return sLowMemoryThresold;
        }

        getTotalMemory(context);
        return sLowMemoryThresold;
    }

    //in KB
    public static int getMemoryClass(Context context) {
        if (0 != sMemoryClass) {
            return sMemoryClass * 1024;
        }
        getTotalMemory(context);
        return sMemoryClass * 1024;
    }

    public static long getTotalMemory(Context context) {
        if (0 != sTotalMemory) {
            return sTotalMemory;
        }

        long start = System.currentTimeMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            am.getMemoryInfo(memInfo);
            sTotalMemory = memInfo.totalMem;
            sLowMemoryThresold = memInfo.threshold;

            long memClass = Runtime.getRuntime().maxMemory();
            if (memClass == Long.MAX_VALUE) {
                sMemoryClass = am.getMemoryClass(); //if not set maxMemory, then is not large heap
            } else {
                sMemoryClass = (int) (memClass / MB);
            }
//            int isLargeHeap = (context.getApplicationInfo().flags | ApplicationInfo.FLAG_LARGE_HEAP);
//            if (isLargeHeap > 0) {
//                sMemoryClass = am.getLargeMemoryClass();
//            } else {
//                sMemoryClass = am.getMemoryClass();
//            }

            return sTotalMemory;
        }
        return 0;
    }

    public static boolean isLowMemory(Context context) {
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.getMemoryInfo(memInfo);
        return memInfo.lowMemory;
    }

    //return in KB
    public static long getAvailMemory(Context context) {
        Runtime runtime = Runtime.getRuntime();
        return runtime.freeMemory() / 1024;   //in KB
    }

    public static long getMemFree(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            am.getMemoryInfo(memInfo);
            return memInfo.availMem / 1024;
        } else {
            long availMemory = INVALID;
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(MEMORY_FILE_PATH), "UTF-8"));
                String line = bufferedReader.readLine();
                while (null != line) {
                    String[] args = line.split("\\s+");
                    if ("MemAvailable:".equals(args[0])) {
                        availMemory = Integer.parseInt(args[1]) * 1024L;
                        break;
                    } else {
                        line = bufferedReader.readLine();
                    }
                }

            } catch (Exception e) {
            } finally {
                try {
                    if (null != bufferedReader) {
                        bufferedReader.close();
                    }
                } catch (Exception e) {
                }
            }
            return availMemory / 1024;
        }
    }

    public static double getAppCpuRate() {
        long start = System.currentTimeMillis();
        long cpuTime = 0L;
        long appTime = 0L;
        double cpuRate = 0.0D;
        appTime = fetchAppCpuTime();
        cpuTime = fetchTotalCpuTime();
        if (0 != cpuTime) {
            cpuRate = ((double) (appTime) / (double) (cpuTime)) * 100D;
        }
        return cpuRate;
    }


    static class CpuInfo {
        String freze = "";
        Long time = 0L;

        @NonNull
        @Override
        public String toString() {
            return freze + ":" + time;

        }
    }

    public static long fetchSingleCpuTotalTime() {
        final long[] totalTime = {0L};
        List<CpuInfo> cpuInfos = new ArrayList<>();
        try {
            File file = new File("/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state");
            FileInputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] s = line.split(" ");
                CpuInfo cpuInfo = new CpuInfo();
                cpuInfo.freze = s[0];
                cpuInfo.time = Long.parseLong(s[1]);
                cpuInfos.add(cpuInfo);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        cpuInfos.forEach(cpuInfo -> totalTime[0] += cpuInfo.time);
        return totalTime[0];
    }

    private static Long fetchTotalCpuTime() {
        RandomAccessFile procStatFile = null;
        Long cpuTime = 0L;

        try {
            procStatFile = new RandomAccessFile("/proc/stat", "r");
            String procStatString = procStatFile.readLine();
            String[] procStats = procStatString.split(" ");
            cpuTime = Long.parseLong(procStats[2]) + Long.parseLong(procStats[3]) + Long.parseLong(procStats[4]) + Long.parseLong(procStats[5]) + Long.parseLong(procStats[6]) + Long.parseLong(procStats[7]) + Long.parseLong(procStats[8]);

        } catch (Exception e) {
        } finally {
            try {
                if (null != procStatFile) {
                    procStatFile.close();
                }
            } catch (Exception e) {
            }
        }
        return cpuTime;
    }

    private static Long fetchAppCpuTime() {
        Long appTime = 0L;
        RandomAccessFile appStatFile = null;
        try {
            appStatFile = new RandomAccessFile("/proc/" + getAppId() + "/stat", "r");
            String appStatString = appStatFile.readLine();
            String[] appStats = appStatString.split(" ");
            appTime = Long.parseLong(appStats[13]) + Long.parseLong(appStats[14]);
        } catch (Exception e) {
        } finally {
            try {
                if (null != appStatFile) {
                    appStatFile.close();
                }
            } catch (Exception e) {
            }
        }
        return appTime;
    }

    public static Debug.MemoryInfo getAppMemory(Context context) {
        try {
            // 统计进程的内存信息 totalPss
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            final Debug.MemoryInfo[] memInfo = activityManager.getProcessMemoryInfo(new int[]{getAppId()});
            if (memInfo.length > 0) {
                return memInfo[0];
            }
        } catch (Exception e) {
        }
        return null;
    }

    private static int getNumOfCores() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            return 1;
        }
        int cores;
        try {
            cores = getCoresFromFile(CPU_FILE_PATH_1);
            if (cores == INVALID) {
                cores = getCoresFromFile(CPU_FILE_PATH_2);
            }
            if (cores == INVALID) {
                cores = getCoresFromCPUFiles(CPU_FILE_PATH_0);
            }
        } catch (Exception e) {
            cores = INVALID;
        }
        if (cores == INVALID) {
            cores = 1;
        }
        return cores;
    }

    private static int getCoresFromCPUFiles(String path) {
        File[] list = new File(path).listFiles(CPU_FILTER);
        return null == list ? 0 : list.length;
    }

    private static int getCoresFromFile(String file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            BufferedReader buf = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String fileContents = buf.readLine();
            buf.close();
            if (fileContents == null || !fileContents.matches("0-[\\d]+$")) {
                return INVALID;
            }
            String num = fileContents.substring(2);
            return Integer.parseInt(num) + 1;
        } catch (IOException e) {
            return INVALID;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
            }
        }
    }

    private static final FileFilter CPU_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return Pattern.matches("cpu[0-9]", pathname.getName());
        }
    };

    public static long getDalvikHeap() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024;   //in KB
    }

    public static long getNativeHeap() {
        return Debug.getNativeHeapAllocatedSize() / 1024;   //in KB
    }

    public static long getVmSize() {
        String status = String.format("/proc/%s/status", getAppId());
        try {
            String content = getStringFromFile(status).trim();
            String[] args = content.split("\n");
            for (String str : args) {
                if (str.startsWith("VmSize")) {
                    Pattern p = Pattern.compile("\\d+");
                    Matcher matcher = p.matcher(str);
                    if (matcher.find()) {
                        return Long.parseLong(matcher.group());
                    }
                }
            }
            if (args.length > 12) {
                Pattern p = Pattern.compile("\\d+");
                Matcher matcher = p.matcher(args[12]);
                if (matcher.find()) {
                    return Long.parseLong(matcher.group());
                }
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } finally {
            if (null != reader) {
                reader.close();
            }
        }

        return sb.toString();
    }

    public static String getStringFromFile(String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = null;
        String ret;
        try {
            fin = new FileInputStream(fl);
            ret = convertStreamToString(fin);
        } finally {
            if (null != fin) {
                fin.close();
            }
        }
        return ret;
    }

    /**
     * Check if current runtime is 64bit.
     *
     * @return True if current runtime is 64bit abi. Otherwise return false instead.
     */
    public static boolean is64BitRuntime() {
        final String currRuntimeABI = Build.CPU_ABI;
        return "arm64-v8a".equalsIgnoreCase(currRuntimeABI) || "x86_64".equalsIgnoreCase(currRuntimeABI) || "mips64".equalsIgnoreCase(currRuntimeABI);
    }


}
