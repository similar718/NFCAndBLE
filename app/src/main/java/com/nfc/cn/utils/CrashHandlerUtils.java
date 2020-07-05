package com.nfc.cn.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * written to the SD card
 */
public class CrashHandlerUtils implements UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private static final boolean DEBUG = true;

    private static final String PATH = Environment.getExternalStorageDirectory().getPath() + "/cn_xmzt_www_crash/";
    private static final String FILE_NAME = "crash";

    //Log file suffix
    private static final String FILE_NAME_SUFFIX = ".txt";

    private static CrashHandlerUtils mInstance = new CrashHandlerUtils();

    //System default exception handling (by default, the system terminates the current exception program).
    private UncaughtExceptionHandler mDefaultCrashHandler;

    private Context mContext;

    //The construction method is private to prevent the external structure of multiple instances, that is, the use of a single model
    private CrashHandlerUtils() {
    }

    public static CrashHandlerUtils getInstance() {
        return mInstance;
    }

    //the main work is done here
    public void init(Context context) {
        //Gets the default exception handler for the system
        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        //Set the current instance to the system default exception handler
        Thread.setDefaultUncaughtExceptionHandler(this);
        //request Context，Convenient for internal use
        mContext = context.getApplicationContext();
    }

    /**
     * 这个是最关键的函数，当程序中有未被捕获的异常，系统将会自动调用#uncaughtException方法
     * thread为出现未捕获异常的线程，ex为未捕获的异常，有了这个ex，我们就可以得到异常信息。
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        try {
            //导出异常信息到SD卡中
            dumpExceptionToSDCard(ex);
            //这里可以通过网络上传异常信息到服务器，便于开发人员分析日志从而解决bug
            uploadExceptionToServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Print out the current call stack information
        ex.printStackTrace();

        //If the system provides the default exception handler, it is handed over to the system to end our program, otherwise we will end up by ourselves.
        if (mDefaultCrashHandler != null) {
            mDefaultCrashHandler.uncaughtException(thread, ex);
        } else {
            int pid = Process.myPid();
            Process.killProcess(pid);
        }
    }

    private void dumpExceptionToSDCard(Throwable ex) throws IOException {
        //If the SD card does not exist or can not be used, then the exception information can not be written to the SD card
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (DEBUG) {
                Log.w(TAG, "sdcard unmounted,skip dump exception");
                return;
            }
        }

        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        long current = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(current));
        //Create a log file at the current timer
        File file = new File(PATH + FILE_NAME + time + FILE_NAME_SUFFIX);

        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            //The timer of occurrence of an exception is derived.
            pw.println(time);

            //Export mobile phone information
            dumpPhoneInfo(pw);

            pw.println();
            //Export exception call stack information
            ex.printStackTrace(pw);

            pw.close();
        } catch (Exception e) {
            Log.e(TAG, "dump crash info failed");
        }
    }

    private void dumpPhoneInfo(PrintWriter pw) throws NameNotFoundException {
        //The version name and version number of the application
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        pw.print("App Version: ");
        pw.print(pi.versionName);
        pw.print('_');
        pw.println(pi.versionCode);

        //android version number
        pw.print("OS Version: ");
        pw.print("Build.VERSION.RELEASE");
        pw.print("");
        pw.println("Build.VERSION.SDK_INT");

        //Handset maker
        pw.print("Vendor: ");
        pw.println("Build.MANUFACTURER");

        //Cell phone model
        pw.print("Model: ");
        pw.println("Build.MODEL");

        //cpu
        pw.print("CPU ABI: ");
        pw.println("Build.CPU_ABI");
    }

    private void uploadExceptionToServer() {
    }

}
