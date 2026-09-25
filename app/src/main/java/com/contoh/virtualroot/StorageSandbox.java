package com.contoh.virtualroot;

import android.content.Context;
import android.os.Environment;
import java.io.File;
import java.io.RandomAccessFile;

public class StorageSandbox {

    public static final String SANDBOX_DIR = "VirtualRoot";
    public static final long DEFAULT_QUOTA = 3L * 1024 * 1024 * 1024;

    private static StorageSandbox instance;
    private final Context ctx;
    private final File root;
    private final File quotaFile;

    private StorageSandbox(Context ctx) {
        this.ctx = ctx.getApplicationContext();
        this.root = new File(Environment.getExternalStorageDirectory(), SANDBOX_DIR);
        this.quotaFile = new File(root, ".quota");
        if (!root.exists()) root.mkdirs();
        if (!quotaFile.exists()) {
            try {
                RandomAccessFile raf = new RandomAccessFile(quotaFile, "rw");
                raf.setLength(DEFAULT_QUOTA);
                raf.close();
            } catch (Exception e) {}
        }
    }

    public static synchronized StorageSandbox get(Context ctx) {
        if (instance == null) instance = new StorageSandbox(ctx);
        return instance;
    }

    public File getRoot() { return root; }

    public File getAppDir(String pkg) {
        File d = new File(root, "apps/" + pkg);
        if (!d.exists()) d.mkdirs();
        return d;
    }

    public File getDataDir(String pkg) {
        File d = new File(getAppDir(pkg), "data");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    public File getCacheDir(String pkg) {
        File d = new File(getAppDir(pkg), "cache");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    public File getFilesDir(String pkg) {
        File d = new File(getAppDir(pkg), "files");
        if (!d.exists()) d.mkdirs();
        return d;
    }

    public long getQuota() {
        return quotaFile.exists() ? quotaFile.length() : DEFAULT_QUOTA;
    }

    public void setQuotaGB(int gb) {
        try {
            RandomAccessFile raf = new RandomAccessFile(quotaFile, "rw");
            raf.setLength(gb * 1024L * 1024L * 1024L);
            raf.close();
        } catch (Exception e) {}
    }

    public long getUsed() { return dirSize(root); }

    public long getFree() {
        long f = getQuota() - getUsed();
        return f > 0 ? f : 0;
    }

    public boolean hasSpace(long bytes) { return getUsed() + bytes <= getQuota(); }

    public String getStatus() {
        return "Storage: " + fmt(getUsed()) + " / " + fmt(getQuota()) +
               "\nPath: " + root.getAbsolutePath();
    }

    private long dirSize(File dir) {
        if (dir == null || !dir.exists()) return 0;
        if (dir.isFile()) return dir.length();
        long s = 0;
        File[] arr = dir.listFiles();
        if (arr != null) for (File f : arr) s += dirSize(f);
        return s;
    }

    public static String fmt(long b) {
        if (b < 1024) return b + " B";
        double k = b / 1024.0;
        if (k < 1024) return String.format("%.1f KB", k);
        double m = k / 1024.0;
        if (m < 1024) return String.format("%.1f MB", m);
        return String.format("%.2f GB", m / 1024.0);
    }
      }
