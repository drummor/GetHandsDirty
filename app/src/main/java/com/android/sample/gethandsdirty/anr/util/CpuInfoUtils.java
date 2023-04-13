package com.android.sample.gethandsdirty.anr.util;


import android.os.Build;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public final class CpuInfoUtils {

    private static final CpuInfoUtils instance = new CpuInfoUtils();

    public static CpuInfoUtils getInstance() {
        return instance;
    }

    private CpuInfoUtils() {
    }

    private static final @NotNull String SYSTEM_CPU_PATH = "/sys/devices/system/cpu";

    static final @NotNull String CPUINFO_MAX_FREQ_PATH = "cpufreq/cpuinfo_max_freq";

    /**
     * Cached max frequencies to avoid reading files multiple times
     */
    private final @NotNull List<Integer> cpuMaxFrequenciesMhz = new ArrayList<>();

    /**
     * Read the max frequency of each core of the cpu and returns it in Mhz
     *
     * @return A list with the frequency of each core of the cpu in Mhz
     */
    public @NotNull List<Integer> readMaxFrequencies() {
        if (!cpuMaxFrequenciesMhz.isEmpty()) {
            return cpuMaxFrequenciesMhz;
        }
        File[] cpuDirs = new File(getSystemCpuPath()).listFiles();
        if (cpuDirs == null) {
            return new ArrayList<>();
        }

        for (File cpuDir : cpuDirs) {
            if (!cpuDir.getName().matches("cpu[0-9]+")) continue;
            File cpuMaxFreqFile = new File(cpuDir, CPUINFO_MAX_FREQ_PATH);

            if (!cpuMaxFreqFile.exists() || !cpuMaxFreqFile.canRead()) continue;

            long khz;
            try {
                String content = readText(cpuMaxFreqFile);
                if (content == null) continue;
                khz = Long.parseLong(content.trim());
            } catch (NumberFormatException e) {
                continue;
            } catch (IOException e) {
                continue;
            }
            cpuMaxFrequenciesMhz.add((int) (khz / 1000));
        }
        return cpuMaxFrequenciesMhz;
    }


    @NotNull
    String getSystemCpuPath() {
        return SYSTEM_CPU_PATH;
    }


    final void clear() {
        cpuMaxFrequenciesMhz.clear();
    }


    public static String readText(File file) throws IOException {
        if (file == null || !file.exists() || !file.isFile() || !file.canRead()) {
            return null;
        }
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            // The first line doesn't need the leading \n
            if ((line = br.readLine()) != null) {
                contentBuilder.append(line);
            }
            while ((line = br.readLine()) != null) {
                contentBuilder.append("\n").append(line);
            }
        }
        return contentBuilder.toString();
    }


    private static String cpuUseStateInfoFile = "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";

    //获取某个CPU的使用时长
    public static long fetchSingleCpuTotalTime() {
        final long[] totalTime = {0L};
        List<DeviceUtil.CpuInfo> cpuInfos = new ArrayList<>();
        try {
            ///sys/devices/system/cpu/cpufreq/policy[X]/stats/time_in_state
            ///sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state
            File file = new File("/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state");
            FileInputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] s = line.split(" ");
                DeviceUtil.CpuInfo cpuInfo = new DeviceUtil.CpuInfo();
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


    private final static int DEVICEINFO_UNKNOWN = -1;

    public static int getNumberOfCPUCores() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            // Gingerbread doesn't support giving a single application access to both cores, but a
            // handful of devices (Atrix 4G and Droid X2 for example) were released with a dual-core
            // chipset and Gingerbread; that can let an app in the background run without impacting
            // the foreground application. But for our purposes, it makes them single core.
            return 1;
        }
        int cores;
        try {
            cores = new File("/sys/devices/system/cpu/").listFiles(CPU_FILTER).length;
        } catch (SecurityException e) {
            cores = DEVICEINFO_UNKNOWN;
        } catch (NullPointerException e) {
            cores = DEVICEINFO_UNKNOWN;
        }
        return cores;
    }

    private static final FileFilter CPU_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            String path = pathname.getName();
            //regex is slow, so checking char by char.
            if (path.startsWith("cpu")) {
                for (int i = 3; i < path.length(); i++) {
                    if (path.charAt(i) < '0' || path.charAt(i) > '9') {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    };
}
