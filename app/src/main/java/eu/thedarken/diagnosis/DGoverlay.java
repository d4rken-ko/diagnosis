package eu.thedarken.diagnosis;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import eu.thedarken.diagnosis.InfoClass.AppInfo;
import eu.thedarken.diagnosis.InfoClass.FreqInfo;
import eu.thedarken.diagnosis.InfoClass.CpuInfo;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.TrafficStats;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

public class DGoverlay extends Service {
    private final static int NOTIFICATION_ID = 88;
    public static int INTERVALL = 5000;
    public static boolean isPaused = false;
    public static boolean haltoverlay = false;
    public static boolean screenON = true;
    public static boolean isRunning = false;
    public static float ALERT_BARRIER_CUSTOM = 70;
    public static boolean use_fahrenheit = false;
    public static int default_color_normal = 0xff06ff00;
    public static int default_color_alert = 0xffffff00;
    public static int default_color_bg = 0x70000000;
    private static ArrayList<Line> lines = new ArrayList<Line>();
    private static boolean reset = true;
    private static ArrayList<ExternalSD> external_sds = new ArrayList<ExternalSD>();
    private final String TAG = "eu.thedarken.diagnosis.DGoverlay";
    private SharedPreferences settings;
    private Context mContext;
    private Overlay mOverlay;
    private WindowManager wm;
    private Handler mHandler;
    private boolean clearScreen = false;
    private DGdata data;
    private BroadcastReceiver screenOnReciever;
    private float ALERT_BARRIER_MEDIUM = 40;
    private float ALERT_BARRIER_LOW = 1;
    private int overlay_width;
    private int overlay_height;
    @SuppressWarnings("unchecked")
    private Runnable update = new Runnable() {
        public void run() {
            data.update();
            if (screenON) {
                if (reset) {
                    DisplayMetrics outMetrics = new DisplayMetrics();
                    wm.getDefaultDisplay().getMetrics(outMetrics);
                    overlay_width = outMetrics.widthPixels;
                    overlay_height = outMetrics.heightPixels;
                    WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                            overlay_width,
                            overlay_height,
                            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                            PixelFormat.TRANSLUCENT);
                    params.gravity = Gravity.LEFT | Gravity.TOP;

                    wm.updateViewLayout(mOverlay, params);

                    use_fahrenheit = settings.getBoolean("layout.usefahrenheit", false);


                    lines.clear();
                    Line init;
                    int line;

                    line = 0;
                    init = new Line();
                    init.layout = (ArrayList<Integer>) ObjectSerializer.deserialize(settings.getString("layout.line" + line, ""));
                    if (init.layout == null) init.layout = new ArrayList<Integer>();
                    init.alignright = settings.getBoolean("overlay.align.right.line" + line, false);
                    init.setFont(Integer.parseInt(settings.getString("overlay.font.type.line" + line, "0")));
                    init.x_pos = Integer.parseInt(settings.getString("overlay.x_pos.line" + line, "1"));
                    init.y_pos = Integer.parseInt(settings.getString("overlay.y_pos.line" + line, "45"));
                    init.textstyle.setTextSize(settings.getInt("overlay.font.size.line" + line, 15));
                    init.textstyle.setColor(settings.getInt("overlay.color.normal.line" + line, default_color_normal));
                    init.bgstyle.setColor(settings.getInt("overlay.color.background.line" + line, default_color_bg));
                    init.drawbackground = settings.getBoolean("overlay.drawbackground.line" + line, false);
                    lines.add(init);

                    line = 1;
                    init = new Line();
                    init.layout = (ArrayList<Integer>) ObjectSerializer.deserialize(settings.getString("layout.line" + line, ""));
                    if (init.layout == null) init.layout = new ArrayList<Integer>();
                    init.alignright = settings.getBoolean("overlay.align.right.line" + line, false);
                    init.setFont(Integer.parseInt(settings.getString("overlay.font.type.line" + line, "0")));
                    init.x_pos = Integer.parseInt(settings.getString("overlay.x_pos.line" + line, "1"));
                    init.y_pos = Integer.parseInt(settings.getString("overlay.y_pos.line" + line, "60"));
                    init.textstyle.setTextSize(settings.getInt("overlay.font.size.line" + line, 15));
                    init.textstyle.setColor(settings.getInt("overlay.color.normal.line" + line, default_color_normal));
                    init.bgstyle.setColor(settings.getInt("overlay.color.background.line" + line, default_color_bg));
                    init.drawbackground = settings.getBoolean("overlay.drawbackground.line" + line, false);
                    lines.add(init);

                    line = 2;
                    init = new Line();
                    init.layout = (ArrayList<Integer>) ObjectSerializer.deserialize(settings.getString("layout.line" + line, ""));
                    if (init.layout == null) init.layout = new ArrayList<Integer>();
                    init.alignright = settings.getBoolean("overlay.align.right.line" + line, false);
                    init.setFont(Integer.parseInt(settings.getString("overlay.font.type.line" + line, "0")));
                    init.x_pos = Integer.parseInt(settings.getString("overlay.x_pos.line" + line, "1"));
                    init.y_pos = Integer.parseInt(settings.getString("overlay.y_pos.line" + line, "75"));
                    init.textstyle.setTextSize(settings.getInt("overlay.font.size.line" + line, 15));
                    init.textstyle.setColor(settings.getInt("overlay.color.normal.line" + line, default_color_normal));
                    init.bgstyle.setColor(settings.getInt("overlay.color.background.line" + line, default_color_bg));
                    init.drawbackground = settings.getBoolean("overlay.drawbackground.line" + line, false);
                    lines.add(init);

                    line = 3;
                    init = new Line();
                    init.layout = (ArrayList<Integer>) ObjectSerializer.deserialize(settings.getString("layout.line" + line, ""));
                    if (init.layout == null) init.layout = new ArrayList<Integer>();
                    init.alignright = settings.getBoolean("overlay.align.right.line" + line, false);
                    init.setFont(Integer.parseInt(settings.getString("overlay.font.type.line" + line, "0")));
                    init.x_pos = Integer.parseInt(settings.getString("overlay.x_pos.line" + line, "1"));
                    init.y_pos = Integer.parseInt(settings.getString("overlay.y_pos.line" + line, "90"));
                    init.textstyle.setTextSize(settings.getInt("overlay.font.size.line" + line, 15));
                    init.textstyle.setColor(settings.getInt("overlay.color.normal.line" + line, default_color_normal));
                    init.bgstyle.setColor(settings.getInt("overlay.color.background.line" + line, default_color_bg));
                    init.drawbackground = settings.getBoolean("overlay.drawbackground.line" + line, false);
                    lines.add(init);


                    Log.d(TAG, "재설정 완료");
                    setLine(0);
                    setLine(1);
                    setLine(2);
                    setLine(3);
                    reset = false;
                } else {
                    setLine(0);
                    setLine(1);
                    setLine(2);
                    setLine(3);
                }
                mOverlay.invalidate();
            } else {
                // 화면이 꺼져 있었습니다
            }
            if (haltoverlay) {
                haltoverlay = false;
                Log.d(TAG, "중단됨");
                reset = true;
                stopSelf();
            } else {
                mHandler.postDelayed(update, INTERVALL);
            }
        }
    };

    public static Line getLine(int position) {
        if (lines.size() <= position) {
            return null;
        }
        return lines.get(position);
    }

    public static ArrayList<Line> getLines() {
        if (lines == null) return new ArrayList<Line>();
        return lines;
    }

    public static ArrayList<ExternalSD> getExternalSDs() {
        return external_sds;
    }

    public static void initReset() {
        reset = true;
    }

    @Override
    public void onCreate() {
        mContext = this;
        settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        PreferenceManager.setDefaultValues(mContext, R.xml.preferences, false);

        data = new DGdata(mContext);
        mOverlay = new Overlay(mContext);
        Log.d(TAG, "오버레이 서비스 시작");
        super.onCreate();

        INTERVALL = (settings.getInt("general.intervall", 5) * 1000);

        findExternalSD();

        use_fahrenheit = settings.getBoolean("layout.usefahrenheit", false);

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        overlay_width = outMetrics.widthPixels;
        overlay_height = outMetrics.heightPixels;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                overlay_width,
                overlay_height,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;

        wm.addView(mOverlay, params);

        mHandler = new Handler();
        mHandler.postDelayed(update, 1);

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        screenOnReciever = new ScreenReceiver();
        this.registerReceiver(screenOnReciever, filter);

        isRunning = true;
        Toast.makeText(this.getApplicationContext(), mContext.getString(R.string.diagnosis_service_created), Toast.LENGTH_SHORT).show();

        Notification note = new Notification(R.drawable.note, mContext.getString(R.string.we_now_know_whats_going_on), System.currentTimeMillis());
        Intent i = new Intent(this, DGmain.class);

        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

        note.setLatestEventInfo(this, mContext.getString(R.string.diagnosis), mContext.getString(R.string.click_me_to_open_app), pi);
        note.flags |= Notification.FLAG_NO_CLEAR;
        note.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        note.flags |= Notification.FLAG_ONGOING_EVENT;
        if (settings.getBoolean("general.notification.enabled", true))
            this.startForeground(NOTIFICATION_ID, note);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "오버레이 서비스가 파괴되었습니다");
        wm.removeView(mOverlay);
        this.unregisterReceiver(screenOnReciever);
        data.close();
        isRunning = false;
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;

    }

    private void findExternalSD() {
        ArrayList<String> canidates = new ArrayList<String>();
        canidates.add("/mnt/sdcard-ext");
        canidates.add(Environment.getExternalStorageDirectory().getAbsolutePath() + "/external_sd");
        canidates.add("/mnt/emmc");
        canidates.add("/mnt/extSdCard");
        canidates.add("/emmc");
        canidates.add(Environment.getExternalStorageDirectory().getAbsolutePath() + "/_ExternalSD");
        canidates.add(Environment.getExternalStorageDirectory().getAbsolutePath() + "/sd");
        canidates.add(Environment.getExternalStorageDirectory().getAbsolutePath() + "/sdcard2");
        canidates.add("/mnt/sdcard2");
        canidates.add("/mnt/external1");
        canidates.add(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ext_sd");
//		canidates.add("/data/sdext2");
        canidates.add("/mnt/usb_storage");
        canidates.add("/mnt/sdcard/removable_sdcard");
        canidates.add("/Removable/MicroSD");

        for (String c : canidates) {
            File canidate = new File(c);
            if (canidate.exists() && canidate.canRead()) {
                ExternalSD e = new ExternalSD();
                e.isCovered = alreadyCovered(canidate);
                e.path = canidate;
                e.label = e.path.getName();
                external_sds.add(e);
            }
        }

        for (ExternalSD sd : getExternalSDs())
            Log.d(TAG, "외부 SD가 발견되었습니다 " + sd.path.getAbsolutePath().toString());
    }

    private boolean alreadyCovered(File f) {
        if (f.getAbsolutePath().toString().contains(Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/"))
            return true;
        for (ExternalSD sd : getExternalSDs()) {
            if (sd.path.getAbsolutePath().toString().contains(f.getAbsolutePath().toString()))
                return true;
        }
        return false;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "구성이 변경되어 다시 로드 중입니다...");
        reset = true;
    }

    private void setLine(int line) {
        StringBuilder toset = new StringBuilder();
        LinkedList<Integer> tobuild = new LinkedList<Integer>();
        tobuild.addAll(lines.get(line).layout);
        Integer item;
        while (!tobuild.isEmpty()) {
            item = tobuild.removeFirst();
            switch (item) {
                //<item>Select something</item>
                case 0:
                    toset.append("");
                    break;
                //<item>Apps >X%CPU</item>
                case 1:
                    toset.append(prepHighest(ALERT_BARRIER_CUSTOM));
                    if (toset.length() > 0) {
                        lines.get(line).textstyle.setColor(settings.getInt("overlay.color.alert.line" + line, default_color_alert));
                    } else {
                        lines.get(line).textstyle.setColor(settings.getInt("overlay.color.normal.line" + line, default_color_normal));
                    }
                    break;
                //<item>Apps >40%CPU</item>
                case 2:
                    toset.append(prepHighest(ALERT_BARRIER_MEDIUM));
                    if (toset.length() > 0) {
                        lines.get(line).textstyle.setColor(settings.getInt("overlay.color.alert.line" + line, default_color_alert));
                    } else {
                        lines.get(line).textstyle.setColor(settings.getInt("overlay.color.normal.line" + line, default_color_normal));
                    }
                    break;
                //<item>Apps >1% CPU</item>
                case 3:
                    toset.append(prepHighest(ALERT_BARRIER_LOW));
                    if (toset.length() > 0) {
                        lines.get(line).textstyle.setColor(settings.getInt("overlay.color.alert.line" + line, default_color_alert));
                    } else {
                        lines.get(line).textstyle.setColor(settings.getInt("overlay.color.normal.line" + line, default_color_normal));
                    }
                    break;
                //<item>Total Cpu use</item>
                case 4:
                    toset.append("CPU " + (int) CpuInfo.calcAvgCpu(data.getCpu().usage) + "%");
                    break;
                //<item>CPU 사용량 USR</item>
                case 5:
                    toset.append("user " + (int) CpuInfo.calcAvgCpu(data.getCpu().user) + "%");
                    break;
                //<item>CPU 사용량 SYS</item>
                case 6:
                    toset.append("sys " + (int) CpuInfo.calcAvgCpu(data.getCpu().system) + "%");
                    break;
                //<item>CPU 사용량 IDLE</item>
                case 7:
                    toset.append("idl " + (int) CpuInfo.calcAvgCpu(data.getCpu().idle) + "%");
                    break;
                //<item>CPU 사용량 IO</item>
                case 8:
                    toset.append("io " + (int) CpuInfo.calcAvgCpu(data.getCpu().io) + "%");
                    break;
                //<item>CPU 사용량 NICE</item>
                case 9:
                    toset.append("nic " + (int) CpuInfo.calcAvgCpu(data.getCpu().nice) + "%");
                    break;
                //<item>총 사용 가능한 메모리</item>
                case 10:
                    toset.append("MEM " + Formatter.formatFileSize(mContext, (long) (data.getMem().total_free)));
                    break;
                //<item>실제 자유 메모리</item>
                case 11:
                    toset.append("Free " + Formatter.formatFileSize(mContext, (long) (data.getMem().free)));
                    break;
                //<item>사용된 메모리</item>
                case 12:
                    toset.append("Used " + Formatter.formatFileSize(mContext, (long) (data.getMem().used)));
                    break;
                //<item>버퍼링된 메모리</item>
                case 13:
                    toset.append("Buff " + Formatter.formatFileSize(mContext, (long) (data.getMem().buff)));
                    break;
                //<item>캐시된 메모리</item>
                case 14:
                    toset.append("Cache " + Formatter.formatFileSize(mContext, (long) (data.getMem().cached)));
                    break;
                //<item>시스템 시간</item>
                case 15:
                    toset.append("Systime:" + System.currentTimeMillis());
                    break;
                //<item>다운로드</item>
                case 16: //다운로드
                    if (data.getNet().rate_down == TrafficStats.UNSUPPORTED) {
                        toset.append("없음");
                    } else {
                        toset.append("디엘에이 " + Formatter.formatFileSize(mContext, data.getNet().rate_down) + "/s");
                    }
                    break;
                //<item>업로드</item>
                case 17: //Upload
                    if (data.getNet().rate_up == TrafficStats.UNSUPPORTED) {
                        toset.append("없음");
                    } else {
                        toset.append("UL " + Formatter.formatFileSize(mContext, data.getNet().rate_up) + "/s");
                    }
                    break;
                //<item>시스템 부하</item>
                case 18: //Load
                    toset.append("L " + data.getLoad().first + " " + data.getLoad().second + " " + data.getLoad().third);
                    break;
                //<item>활성 앱 수</item>
                case 19: //Active apps
                    toset.append("Apps" + "(" + data.getCpu().act_apps_cur + ")");
                    break;
                //<item>배터리 수준</item>
                case 20: //Batt level
                    toset.append(data.getBatt().level + "%");
                    break;
                //<item>배터리 전압</item>
                case 21: //Batt voltage
                    toset.append(((float) data.getBatt().voltage / 1000) + "V");
                    break;
                //<item>배터리 온도</item>
                case 22: //Batt temp
                    toset.append(data.getBatt().formatTemp(data.getBatt().batt_temp_cur, use_fahrenheit));
                    break;
                //<item>시간</item>
                case 23: //Time
                    toset.append(new SimpleDateFormat("HH:mm:ss").format(new Date()));
                    break;
                //<item>날짜</item>
                case 24: //Date
                    toset.append(DateFormat.getDateInstance().format(new Date()));
                    break;
                //<item>디스크 읽기</item>
                case 25: //Disk read rate
                    toset.append("R " + Formatter.formatFileSize(mContext, data.getDisk().read_rate) + "/s");
                    break;
                //<item>디스크 쓰기</item>
                case 26: //Disk write rate
                    toset.append("W " + Formatter.formatFileSize(mContext, data.getDisk().write_rate) + "/s");
                    break;
                //<item>CPU 주파수</item>
                case 27:
                    toset.append((FreqInfo.calcAvgCoreFrequency(data.getFreq().cpu_frequency) / 1000) + "Mhz");
                    break;
                //<item>외부 공간의 자유</item>
                case 28:
                    toset.append("확장 " + Formatter.formatFileSize(mContext, (data.getSpace().extern_total - data.getSpace().extern_used)));
                    break;
                //<item>무료 SD 카드 공간</item>
                case 29:
                    toset.append("SD " + Formatter.formatFileSize(mContext, (data.getSpace().sdcard_total - data.getSpace().sdcard_used)));
                    break;
                //<item>시스템 공간 확보</item>
                case 30:
                    toset.append("Sys " + Formatter.formatFileSize(mContext, (data.getSpace().system_total - data.getSpace().system_used)));
                    break;
                //<item>Free internal space</item>
                case 31:
                    toset.append("Data " + Formatter.formatFileSize(mContext, (data.getSpace().data_total - data.getSpace().data_used)));
                    break;
                //<item>simsimi.kr로 핑</item>
                case 32:
                    long ping = data.getPing().ping;
                    if (ping > 500) {
                        toset.append("Ping >500ms");
                    } else {
                        toset.append("Ping " + ping + "ms");
                    }
                    break;
                //<item>Wi-Fi 신호 강도</item>
                case 33:
                    toset.append("Wi-Fi " + data.getWlan().formatSignal(data.getWlan().signal));
                    break;
                //<item>CELL 신호 강도</item>
                case 34:
                    toset.append("셀 " + data.getPhone().formatSignal(data.getPhone().gsm_signal));
                    break;
                case 35:
                    if (data.getWlan().ip == 0) {
                        toset.append("no ip");
                    } else {
                        toset.append(Formatter.formatIpAddress(data.getWlan().ip));
                    }
                    break;
                case 36:
                    if (data.getWlan().name.length() > 0) {
                        toset.append(data.getWlan().name);
                    } else {
                        toset.append("no wifi");
                    }
                    break;
                case 37:
                    if (data.getWlan().linkspeed > 0) {
                        toset.append(data.getWlan().linkspeed + "Mbps");
                    } else {
                        toset.append("no link");
                    }
                    break;
                case 38:
                    if (data.getNet().mobile_rate_down == TrafficStats.UNSUPPORTED) {
                        toset.append("N/A");
                    } else {
                        toset.append("cDL " + Formatter.formatFileSize(mContext, data.getNet().mobile_rate_down) + "/s");
                    }
                    break;
                case 39:
                    if (data.getNet().mobile_rate_up == TrafficStats.UNSUPPORTED) {
                        toset.append("N/A");
                    } else {
                        toset.append("cUL " + Formatter.formatFileSize(mContext, data.getNet().mobile_rate_up) + "/s");
                    }
                    break;
                //<item>Core1 주파수</item>
                case 40:
                    if (DGdata.CORES > 0)
                        toset.append("C1 " + (data.getFreq().cpu_frequency[0] / 1000) + "Mhz");
                    else
                        toset.append("C1 N/A");
                    break;
                //<item>Core2 주파수</item>
                case 41:
                    if (DGdata.CORES > 1)
                        toset.append("C2 " + (data.getFreq().cpu_frequency[1] / 1000) + "Mhz");
                    else
                        toset.append("C2 N/A");
                    break;
                //<item>Core3 주파수</item>
                case 42:
                    if (DGdata.CORES > 2)
                        toset.append("C3 " + (data.getFreq().cpu_frequency[2] / 1000) + "Mhz");
                    else
                        toset.append("C3 N/A");
                    break;

                //<item>Core#1 사용 [Pro]</item><!-- 44 -->
                case 44:
                    if (DGdata.CORES > 0)
                        toset.append("C1 " + (int) data.getCpu().usage[0] + "%");
                    else
                        toset.append("C1 N/A");
                    break;
                //<item>코어#1 USR [프로]</item><!-- 45 -->
                case 45:
                    if (DGdata.CORES > 0)
                        toset.append("C1[usr] " + (int) data.getCpu().user[0] + "%");
                    else
                        toset.append("C1[usr] N/A");
                    break;
                //<item>코어#1 SYS [프로]</item><!-- 46 -->
                case 46:
                    if (DGdata.CORES > 0)
                        toset.append("C1[sys] " + (int) data.getCpu().system[0] + "%");
                    else
                        toset.append("C1[sys] N/A");
                    break;
                //<item>코어#1 IDLE [프로]</item><!-- 47 -->
                case 47:
                    if (DGdata.CORES > 0)
                        toset.append("C1[idl] " + (int) data.getCpu().idle[0] + "%");
                    else
                        toset.append("C1[idl] N/A");
                    break;
                //<item>코어#1 IO [프로]</item><!-- 48 -->
                case 48:
                    if (DGdata.CORES > 0)
                        toset.append("C1[io] " + (int) data.getCpu().io[0] + "%");
                    else
                        toset.append("C1[io] N/A");
                    break;
                //<item>코어#1 나이스[프로]</item><!-- 49 -->
                case 49:
                    if (DGdata.CORES > 0)
                        toset.append("C1[nic] " + (int) data.getCpu().nice[0] + "%");
                    else
                        toset.append("C1[nic] N/A");
                    break;
                //<item>Core#2 사용 [Pro]</item><!-- 50 -->
                case 50:
                    if (DGdata.CORES > 1)
                        toset.append("C2 " + (int) data.getCpu().usage[1] + "%");
                    else
                        toset.append("C2 N/A");
                    break;
                //<item>코어#2 USR [프로]</item><!-- 51 -->
                case 51:
                    if (DGdata.CORES > 1)
                        toset.append("C2[usr] " + (int) data.getCpu().user[1] + "%");
                    else
                        toset.append("C2[usr] N/A");
                    break;
                //<item>코어#2 SYS [프로]</item><!-- 52 -->
                case 52:
                    if (DGdata.CORES > 1)
                        toset.append("C2[sys] " + (int) data.getCpu().system[1] + "%");
                    else
                        toset.append("C2[sys] N/A");
                    break;
                //<item>코어#2 IDLE [프로]</item><!-- 53 -->
                case 53:
                    if (DGdata.CORES > 1)
                        toset.append("C2[idl] " + (int) data.getCpu().idle[1] + "%");
                    else
                        toset.append("C2[idl] N/A");
                    break;
                //<item>코어#2 IO [프로]</item><!-- 54 -->
                case 54:
                    if (DGdata.CORES > 1)
                        toset.append("C2[io] " + (int) data.getCpu().io[1] + "%");
                    else
                        toset.append("C2[io] N/A");
                    break;
                //<item>코어#2 NICE [프로]</item><!-- 55 -->
                case 55:
                    if (DGdata.CORES > 1)
                        toset.append("C2[nic] " + (int) data.getCpu().nice[1] + "%");
                    else
                        toset.append("C2[nic] N/A");
                    break;
                //<item>Core#3 사용 [Pro]</item><!-- 56 -->
                case 56:
                    if (DGdata.CORES > 2)
                        toset.append("C3 " + (int) data.getCpu().usage[2] + "%");
                    else
                        toset.append("C3 N/A");
                    break;
                //<item>코어#3 USR [프로]</item><!-- 57 -->
                case 57:
                    if (DGdata.CORES > 2)
                        toset.append("C3[usr] " + (int) data.getCpu().user[2] + "%");
                    else
                        toset.append("C3[usr] N/A");
                    break;
                //<item>코어#3 SYS [프로]</item><!-- 58 -->
                case 58:
                    if (DGdata.CORES > 2)
                        toset.append("C3[sys] " + (int) data.getCpu().system[2] + "%");
                    else
                        toset.append("C3[sys] N/A");
                    break;
                //<item>코어#3 IDLE [프로]</item><!-- 59 -->
                case 59:
                    if (DGdata.CORES > 2)
                        toset.append("C3[idl] " + (int) data.getCpu().idle[2] + "%");
                    else
                        toset.append("C3[idl] N/A");
                    break;
                //<item>Core#3 IO [Pro]</item><!-- 60 -->
                case 60:
                    if (DGdata.CORES > 2)
                        toset.append("C3[io] " + (int) data.getCpu().io[2] + "%");
                    else
                        toset.append("C3[io] N/A");
                    break;
                //<item>코어#3 NICE [프로]</item><!-- 61 -->
                case 61:
                    if (DGdata.CORES > 2)
                        toset.append("C3[nic] " + (int) data.getCpu().nice[2] + "%");
                    else
                        toset.append("C3[nic] N/A");
                    break;
                //<item>Core#4 usage [Pro]</item><!-- 62 -->
                case 62:
                    if (DGdata.CORES > 3)
                        toset.append("C4 " + (int) data.getCpu().usage[3] + "%");
                    else
                        toset.append("C4 N/A");
                    break;
                //<item>Core#4 USR [Pro]</item><!-- 63 -->
                case 63:
                    if (DGdata.CORES > 3)
                        toset.append("C4[usr] " + (int) data.getCpu().user[3] + "%");
                    else
                        toset.append("C4[usr] N/A");
                    break;
                //<item>Core#4 SYS [Pro]</item><!-- 64 -->
                case 64:
                    if (DGdata.CORES > 3)
                        toset.append("C4[sys] " + (int) data.getCpu().system[3] + "%");
                    else
                        toset.append("C4[sys] N/A");
                    break;
                //<item>Core#4 IDLE [Pro]</item><!-- 65 -->
                case 65:
                    if (DGdata.CORES > 3)
                        toset.append("C4[idl] " + (int) data.getCpu().idle[3] + "%");
                    else
                        toset.append("C4[idl] N/A");
                    break;
                //<item>Core#4 IO [Pro]</item><!-- 66 -->
                case 66:
                    if (DGdata.CORES > 3)
                        toset.append("C4[io] " + (int) data.getCpu().io[3] + "%");
                    else
                        toset.append("C4[io] N/A");
                    break;
                //<item>Core#4 NICE [Pro]</item><!-- 67 -->
                case 67:
                    if (DGdata.CORES > 3)
                        toset.append("C4[nic] " + (int) data.getCpu().nice[3] + "%");
                    else
                        toset.append("C4[nic] N/A");
                    break;
                default:
                    toset.append("");
                    break;
            }
            if (!tobuild.isEmpty()) toset.append(settings.getString("overlay.divider", "|"));
        }

        lines.get(line).text = toset.toString();
    }

    String prepHighest(float barrier) {
        ArrayList<AppInfo> apps = data.getAppsByCpu(barrier);
        int cutoff = 15;
        if (apps.size() == 0) return "";
        StringBuilder ret = new StringBuilder();
        for (AppInfo a : apps) {
            if (a.cpu > barrier)
                if (settings.getBoolean("general.database.hidesystem", false)) {
                    if ((a.command.startsWith("[") && a.command.endsWith("]"))) {
                        continue;
                    }
                }
            if (a.command.length() >= cutoff) {
                ret.append(".." + a.command.substring(a.command.length() - cutoff) + "@" + a.cpu + "%" + " ");
            } else {
                ret.append(a.command + "@" + a.cpu + "%" + " ");
            }
        }
        return ret.toString();
    }

    public class Line {
        String text = new String();
        int x_pos = 0;
        int y_pos = 0;
        boolean alignright = false;
        Paint textstyle = null;
        ArrayList<Integer> layout = new ArrayList<Integer>();
        Rect bg = null;
        Paint bgstyle = null;
        boolean drawbackground = false;
        private int fonttype = 1;

        private Line() {
            textstyle = new Paint();
            textstyle.setAntiAlias(true);
            textstyle.setShadowLayer(5, 5, 5, 0xff000000);
            setFont(fonttype);
            this.x_pos = 1;
            int y_move = 45;
            this.y_pos = (int) y_move + (lines.size() * 15);

            bg = new Rect();
            bgstyle = new Paint();
            bgstyle.setColor(Color.BLACK);
            bgstyle.setStyle(Paint.Style.FILL);
        }

        public void setFont(int type) {
            switch (type) {
                case 0:
                    textstyle.setTypeface(Typeface.DEFAULT);
                    break;
                case 1:
                    textstyle.setTypeface(Typeface.DEFAULT_BOLD);
                    break;
                case 2:
                    textstyle.setTypeface(Typeface.MONOSPACE);
                    break;
                case 3:
                    textstyle.setTypeface(Typeface.SANS_SERIF);
                    break;
                case 4:
                    textstyle.setTypeface(Typeface.SERIF);
                    break;
            }
            fonttype = type;
        }
    }

    class ExternalSD {
        File path;
        boolean isCovered = false;
        String label = "";
    }

    class Overlay extends ViewGroup {
        public Overlay(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (clearScreen) {
                canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                clearScreen = false;
            } else {
                for (Line l : lines) {
                    int x = 0;
                    if (l.alignright) {
                        l.x_pos = overlay_width;
                        x = (int) (l.x_pos - l.textstyle.measureText(l.text));
                    } else {
                        x = l.x_pos;
                    }
                    if (l.y_pos > overlay_height) {
                        l.y_pos = overlay_height;
                    }

                    if (l.drawbackground) {
                        l.textstyle.getTextBounds(l.text, 0, l.text.length(), l.bg);
                        canvas.translate(x, l.y_pos);
                        canvas.drawRect(l.bg, l.bgstyle);
                        canvas.translate(-x, -l.y_pos);
                    }

                    canvas.drawText(l.text, x, l.y_pos, l.textstyle);
                }
            }
        }

        @Override
        protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {

        }
    }

    public class ScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                DGoverlay.screenON = false;
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                DGoverlay.screenON = true;
            }
        }

    }
}
