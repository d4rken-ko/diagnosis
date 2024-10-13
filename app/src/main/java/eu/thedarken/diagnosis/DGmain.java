package eu.thedarken.diagnosis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class DGmain extends SherlockFragmentActivity {
    private final static int DB_DELETE_VERSION = 26;
    private final static int BUSYBOX_DELETE_VERSION = 26;
    public static String versName = "";
    public static int versCode = 0;
    public static String BUSYBOX = "";
    public static String BUSYBOX_VERSION = "";
    public static File db;
    private static Context mContext;
    private final String TAG = "eu.thedarken.diagnosis.DGmain";
    private Intent service;
    private SharedPreferences settings;
    private SharedPreferences.Editor prefEditor;

    public static boolean checkPro(Context useContext) {
        Context diagnosispro = null;
        try {
            diagnosispro = useContext.createPackageContext("eu.thedarken.diagnosis.pro", 0);
        } catch (NameNotFoundException e) {
            return false;
        }
        if (diagnosispro != null) {
            if (useContext.getPackageManager().checkSignatures(useContext.getPackageName(), diagnosispro.getPackageName()) == PackageManager.SIGNATURE_MATCH) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.main);

        service = new Intent(mContext, DGoverlay.class);
        settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        prefEditor = settings.edit();

        PreferenceManager.setDefaultValues(mContext, R.xml.preferences, false);

        BUSYBOX = mContext.getFilesDir() + "/busybox";

        new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/eu.thedarken.diagnosis/databases/").mkdirs();
        db = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/eu.thedarken.diagnosis/databases/database.db");

        prefEditor.putString("BUSYBOX", BUSYBOX);
        prefEditor.commit();


        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().removeAllTabs();

        Tab tab = getSupportActionBar().newTab().setText("info").setTabListener(new TabListener<DGinfo>(DGmain.this, "Info", DGinfo.class));
        getSupportActionBar().addTab(tab);

        tab = getSupportActionBar().newTab().setText("stats").setTabListener(new TabListener<DGstats>(DGmain.this, "Stats", DGstats.class));
        getSupportActionBar().addTab(tab);

        tab = getSupportActionBar().newTab().setText("apps").setTabListener(new TabListener<DGapps>(DGmain.this, "apps", DGapps.class));
        getSupportActionBar().addTab(tab);

        if (savedInstanceState != null) {
            getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt("tabState"));
        }

        new setupTask(this).execute();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tabState", getSupportActionBar().getSelectedTab().getPosition());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DGmain.checkPro(mContext)) {
            getSupportActionBar().setTitle(mContext.getString(R.string.pro));
        } else {
            getSupportActionBar().setTitle("");
        }

        if (settings.getInt("news.shown", 0) < versCode) {
            prefEditor.putInt("news.shown", versCode);
            prefEditor.commit();
            MiscDialogFragments.showDialog(getSupportFragmentManager(), MiscDialogFragments.NEWS);
        }
    }

    private Boolean CopyAssets() {
        if (settings.getInt("busyboxversion", 0) < BUSYBOX_DELETE_VERSION) {
            new File(mContext.getFilesDir() + "/busybox").delete();
            prefEditor.putInt("busyboxversion", BUSYBOX_DELETE_VERSION);
            prefEditor.commit();
        }
        if (!new File(mContext.getFilesDir() + "/busybox").exists()) {
            AssetManager am = mContext.getAssets();
            try {
                String fileName = "busybox";
                InputStream in = am.open("busybox");
                FileOutputStream f;
                f = mContext.openFileOutput(fileName, Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
                byte[] buffer = new byte[1024];
                int len1 = 0;
                while ((len1 = in.read(buffer)) > 0) {
                    f.write(buffer, 0, len1);
                }
                f.close();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "BusyBox 생성에 실패했습니다.");
                return false;
            }
            Log.d(TAG, "BusyBox가 성공적으로 생성되었습니다.");
            setNonRootBusyBox();
            return true;
        } else {
            Log.d(TAG, "BusyBox를 찾았습니다.");
        }
        return true;
    }

    private void setNonRootBusyBox() {
        Cmd c = new Cmd();
        c.addCommand("chmod 777 " + DGmain.BUSYBOX + "\n");
        c.execute();
        if (c.getExitCode() == 0) {
            Log.d(TAG, "루트가 아닌 BusyBox에 대한 권한이 성공적으로 설정되었습니다.");
        } else {
            Log.d(TAG, "루팅되지 않은 BusyBox에 대한 권한을 설정하려고 하는 중 오류가 발생했습니다.");
        }
    }

    private String getBusyboxVersion() {
        Cmd c = new Cmd();
        c.addCommand(DGmain.BUSYBOX + " | " + DGmain.BUSYBOX + " head -n1" + "\n");
        c.setTimeout(15000);
        c.execute();
        if (c.getOutput().size() > 0 && c.getOutput().get(0).length() > 21) {
            String vers = c.getOutput().get(0);
            vers = (String) vers.subSequence(0, 22);
            Log.d(TAG, "BusyBox 버전: " + vers);
            return vers;
        } else {
            return "";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        if (DGoverlay.isRunning) {
            menu.findItem(R.id.starttracking).setTitle(mContext.getString(R.string.stop_tracking));
        } else {
            menu.findItem(R.id.starttracking).setTitle(mContext.getString(R.string.start_tracking));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                MiscDialogFragments.showDialog(getSupportFragmentManager(), MiscDialogFragments.NEWS);
                break;
            case R.id.starttracking:
                new serviceTask(this).execute();
                break;
            case R.id.settings:
                Intent startPreferencesActivity = new Intent(this, DGsettings.class);
                this.startActivity(startPreferencesActivity);
                break;
            case R.id.changelog:
                ChangelogDialogFragment changelog = ChangelogDialogFragment.newInstance();
                changelog.showDialog(getSupportFragmentManager());
                break;
            case R.id.about:
                AboutDialogFragment about = AboutDialogFragment.newInstance();
                about.showDialog(getSupportFragmentManager());
                break;
        }
        return true;
    }

    public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private Fragment mFragment;

        /**
         * 새 탭이 생성될 때마다 사용되는 생성자입니다.
         *
         * @param activity 조각을 인스턴스화하는 데 사용되는 호스트 활동
         * @param tag      프래그먼트의 식별자 태그
         * @param clz      프래그먼트의 클래스는 프래그먼트를 인스턴스화하는 데 사용됩니다.
         */
        public TabListener(Activity activity, String tag, Class<T> clz) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
        }

		/* 다음은 각 ActionBar.TabListener 콜백입니다. */

        public void onTabSelected(Tab tab, FragmentTransaction ignoredft) {
            FragmentManager fragMgr = ((FragmentActivity) mActivity).getSupportFragmentManager();
            FragmentTransaction ft = fragMgr.beginTransaction();

            // 조각이 이미 초기화되었는지 확인하세요
            if (mFragment == null) {
                // If not, instantiate and add it to the activity
                mFragment = Fragment.instantiate(mActivity, mClass.getName());
                ft.add(android.R.id.content, mFragment, mTag);
            } else {
                // 존재한다면 그것을 보여주기 위해 첨부하기만 하면 됩니다.
                ft.attach(mFragment);
            }
            try {
                ft.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                // Detach the fragment, because another one is being attached
                ft.detach(mFragment);
            }
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            // 사용자가 이미 선택된 탭을 선택했습니다. 보통 아무것도 하지 않습니다.
        }
    }

    private class setupTask extends AsyncTask<String, Void, Boolean> {
        private Activity mActivity;
        private ProgDialog dialog;

        public setupTask(Activity a) {
            mActivity = a;
        }

        protected void onPreExecute() {
            dialog = new ProgDialog(mActivity);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.show();
            dialog.updateMessage(mActivity.getString(R.string.loading));
        }

        @Override
        protected void onPostExecute(final Boolean ok) {
            try {
                versCode = mContext.getPackageManager().getPackageInfo("eu.thedarken.diagnosis", 0).versionCode;
                versName = mContext.getPackageManager().getPackageInfo("eu.thedarken.diagnosis", 0).versionName;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
                versCode = 0;
                versName = "";
            }
            Log.d(TAG, "버전 이름: " + DGmain.versName);
            Log.d(TAG, "버전 코드: " + DGmain.versCode);

            dialog.dismiss();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            dialog.updateMessage(mActivity.getString(R.string.copying_busybox));

            CopyAssets();

            dialog.updateMessage(mActivity.getString(R.string.getting_busybox_version));

            BUSYBOX_VERSION = getBusyboxVersion();
            if (BUSYBOX_VERSION.length() == 0) {
                dialog.updateMessage(mActivity.getString(R.string.startup_error));
                MiscDialogFragments.showDialog(getSupportFragmentManager(), MiscDialogFragments.BUSYBOX_ERROR);
            }

            dialog.updateMessage(mActivity.getString(R.string.checking_database));

            if (settings.getInt("dbversion", 0) < DB_DELETE_VERSION && db.exists()) {
                if (db.delete()) {
                    dialog.updateMessage(mActivity.getString(R.string.db_deletion_successfull));
                    Log.d(TAG, mActivity.getString(R.string.db_deletion_successfull));
                    // DGdatabase db_object =
                    // DGdatabase.getInstance(mContext.getApplicationContext());
                    // db_object.init();
                    prefEditor.putInt("dbversion", DB_DELETE_VERSION);
                    prefEditor.commit();
                } else {
                    dialog.updateMessage(mActivity.getString(R.string.could_not_delete_db));
                    Log.d(TAG, mActivity.getString(R.string.could_not_delete_db));
                    MiscDialogFragments.showDialog(getSupportFragmentManager(), MiscDialogFragments.REINSTALL);
                }

                MiscDialogFragments.showDialog(getSupportFragmentManager(), MiscDialogFragments.DATABASE_REMOVAL);
            } else {
                prefEditor.putInt("dbversion", DB_DELETE_VERSION);
                prefEditor.commit();
            }

            Styles s = new Styles(mContext);
            s.initLines();

            return true;
        }
    }

    private class serviceTask extends AsyncTask<String, Void, Boolean> {
        private Activity mActivity;
        private ProgDialog dialog;

        public serviceTask(Activity a) {
            mActivity = a;
        }

        protected void onPreExecute() {
            dialog = new ProgDialog(mActivity);
            dialog.setProgressStyle(DGoverlay.isRunning ? ProgressDialog.STYLE_SPINNER : ProgressDialog.STYLE_HORIZONTAL);
            dialog.updateMessage(DGoverlay.isRunning ? mActivity.getString(R.string.stopping_service) : mActivity.getString(R.string.starting_service));
            dialog.show();

        }

        @Override
        protected void onPostExecute(final Boolean ok) {
            invalidateOptionsMenu();
            dialog.dismiss();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                if (DGoverlay.isRunning) {
                    DGoverlay.haltoverlay = true;
                    while (DGoverlay.isRunning)
                        Thread.sleep(25);
                } else {

                    dialog.updateMessage(mActivity.getString(R.string.cleaning_old_database_entries));
                    DGdatabase db_object = DGdatabase.getInstance(mContext.getApplicationContext());

                    dialog.setMax(db_object.getTableSize());
                    db_object.clean((long) (settings.getInt("database.agelimit", 48) * 3600000), dialog);

                    getApplication().startService(service);
                    while (!DGoverlay.isRunning)
                        Thread.sleep(25);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }
}
