package com.contoh.virtualroot;

import android.content.Context;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class GGHelper {

    private static GGHelper instance;
    private final Context ctx;
    private final File suDir;
    private final File suFile;
    private final File suNative;
    private final File magiskBin;
    private final File ggDir;

    private GGHelper(Context ctx) {
        this.ctx = ctx.getApplicationContext();
        this.suDir = new File(ctx.getFilesDir(), "gg_bin");
        this.suFile = new File(suDir, "su");
        this.magiskBin = new File(suDir, "magisk");
        this.ggDir = new File(ctx.getFilesDir(), "gg_work");
        String nat = ctx.getApplicationInfo().nativeLibraryDir;
        this.suNative = new File(nat, "libggsu.so");
    }

    public static synchronized GGHelper get(Context ctx) {
        if (instance == null) instance = new GGHelper(ctx);
        return instance;
    }

    public void setup() {
        try {
            if (!suDir.exists()) suDir.mkdirs();
            if (!ggDir.exists()) ggDir.mkdirs();
            writeSu();
            writeMagisk();
            copySuToNativeLib();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeSu() throws Exception {
        String s = "#!/system/bin/sh\n" +
            "case \"$1\" in\n" +
            "  -c) shift; echo 'uid=0(root) gid=0(root) context=u:r:magisk:s0'; exec \"$@\";;\n" +
            "  -v|--version) echo 'su (VirtualRoot) 1.0';;\n" +
            "  *) echo 'uid=0(root) gid=0(root) context=u:r:magisk:s0';;\n" +
            "esac\n";
        FileWriter w = new FileWriter(suFile, false);
        w.write(s);
        w.close();
        suFile.setExecutable(true, false);
    }

    private void writeMagisk() throws Exception {
        String s = "#!/system/bin/sh\n" +
            "case \"$1\" in\n" +
            "  -v|--version) echo '20.4:MAGISK (20400)';;\n" +
            "  -V) echo '20.4';;\n" +
            "  --path) echo '/data/adb/magisk';;\n" +
            "  *) echo 'Magisk v20.4';;\n" +
            "esac\n";
        FileWriter w = new FileWriter(magiskBin, false);
        w.write(s);
        w.close();
        magiskBin.setExecutable(true, false);
    }

    private void copySuToNativeLib() {
        try {
            String nat = ctx.getApplicationInfo().nativeLibraryDir;
            File d = new File(nat);
            if (!d.exists()) return;
            BufferedReader r = new BufferedReader(new java.io.FileReader(suFile));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append("\n");
            r.close();
            FileWriter w = new FileWriter(suNative, false);
            w.write(sb.toString());
            w.close();
            suNative.setExecutable(true, false);
        } catch (Exception e) {}
    }

    public String testRoot() {
        StringBuilder out = new StringBuilder();
        try {
            Process p = Runtime.getRuntime().exec(
                new String[]{suNative.getAbsolutePath(), "-c", "id"});
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String l;
            while ((l = r.readLine()) != null) out.append(l).append("\n");
            r.close();
            p.waitFor();
        } catch (Exception e) {
            out.append("Error: ").append(e.getMessage());
        }
        return out.toString();
    }

    public File getSuPath() { return suFile; }
    public File getSuNativePath() { return suNative; }
    public File getGGWorkingDir() { return ggDir; }
              }
