package cn.wpj.tvlauncher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.wpj.tvlauncher.entity.AppInfo;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final String SHARED_PREFERENCES_FILE_NAME = "tv_launcher_shared_preferences";
    private static final String SHARED_PREFERENCES_APP_LIST_KEY = "tv_launcher_shared_preferences_app_list";

    private static SharedPreferences mSharedPreferences;

    private RecyclerView mRecyclerView;
    private MyAdapter myAdapter;
    private TextView tvTip;
    private List<AppInfo> datas;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideStatusBar();
        mSharedPreferences = getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        EventBus.getDefault().register(this);

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //全屏并且隐藏状态栏
    private void hideStatusBar() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    public void initView() {
        datas = getAppList(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        tvTip = (TextView) findViewById(R.id.tv_tip);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 5);
        mRecyclerView.setLayoutManager(layoutManager);

        myAdapter = new MyAdapter(datas, this, new MyAdapter.Listener() {
            @Override
            public void onClick(int position) {
                if (tvTip.getVisibility() == View.VISIBLE) {
                    Uri uri = Uri.fromParts("package", datas.get(position).getPackageName(), null);
                    Intent intent = new Intent(Intent.ACTION_DELETE, uri);
                    startActivity(intent);
//                    CommandUtil.exec("adb shell pm uninstall --user 0 " + datas.get(position).getPackageName());
                } else {
                    startActivity(datas.get(position).getIntent());
                }
            }

            @Override
            public void onLongClick(int position) {
                AppInfo appInfo = datas.get(position);
                datas.remove(appInfo);
                datas.add(0, appInfo);
                myAdapter.notifyDataSetChanged();
                //本地记录排序
                String appStr = mSharedPreferences.getString(SHARED_PREFERENCES_APP_LIST_KEY, "");
                Log.i(TAG, "本地应用顺序：" + appStr);
                appStr = appStr.replaceAll(appInfo.getPackageName(), "");
                appStr = appInfo.getPackageName() + "," + appStr;
                appStr = appStr.replaceAll(",,", "");
                Log.i(TAG, "新应用顺序：" + appStr);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString(SHARED_PREFERENCES_APP_LIST_KEY, appStr);
                editor.commit();
                Log.i(TAG, "本地应用顺序：" + mSharedPreferences.getString(SHARED_PREFERENCES_APP_LIST_KEY, ""));
            }
        });
        mRecyclerView.setAdapter(myAdapter);

        mRecyclerView.setFocusable(true);
        mRecyclerView.setFocusableInTouchMode(true);
    }

    public List<AppInfo> getAppList(Context context) {
        List<AppInfo> list = new ArrayList<>();
        String appStr = mSharedPreferences.getString(SHARED_PREFERENCES_APP_LIST_KEY, "");
        Log.i(TAG, "本地应用顺序：" + appStr);
        //本地存储的app有序列表
        List<String> appList = Arrays.asList(appStr.split(","));
        //记录app顺序
        StringBuilder sb = new StringBuilder();
        //记录新增的app顺序
        StringBuilder newSb = new StringBuilder();
        //新增app列表
        List<AppInfo> newAppList = new ArrayList<>();

        PackageManager pm = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activities = pm.queryIntentActivities(mainIntent, 0);
        //循环本地应用列表，按序排列
        for (int i = 0; i < appList.size(); i++) {
            //过滤本应用
            if (TextUtils.equals(appList.get(i), context.getPackageName())) {
                continue;
            }
            for (ResolveInfo info : activities) {
                if (TextUtils.equals(appList.get(i), info.activityInfo.packageName)) {
                    //添加到全局应用列表
                    list.add(info2AppInfo(info, pm));
                    //记录app顺序
                    sb.append(info.activityInfo.packageName).append(",");
                    continue;
                }
            }
        }
        //循环系统应用列表，将本地没有的存储起来
        for (ResolveInfo info : activities) {
            //过滤当前应用
            if (TextUtils.equals(info.activityInfo.packageName, context.getPackageName())) {
                continue;
            }
            if (!appStr.contains(info.activityInfo.packageName)) {
                //添加到新增应用列表
                newAppList.add(info2AppInfo(info, pm));
                //记录新增app顺序
                newSb.append(info.activityInfo.packageName).append(",");
            }
        }
        //将新增应用列表添加到全局应用列表最后
        list.addAll(newAppList);
        //将新增应用顺序添加到最后
        if (newSb.length() > 1) {
            String str = newSb.toString();
            Log.i(TAG, "新增应用：" + str);
            sb.append(str);
        }
        if (sb.length() > 1) {
            //去掉最后一个分隔符','
            String str = sb.substring(0, sb.length() - 1);
            Log.i(TAG, "新应用顺序：" + str);
            //保存到本地
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(SHARED_PREFERENCES_APP_LIST_KEY, str);
            editor.commit();
        }
        return list;
    }

    private static AppInfo info2AppInfo(ResolveInfo info, PackageManager pm) {
        AppInfo mInfo = new AppInfo();
        mInfo.setIco(info.loadIcon(pm));
        mInfo.setName(info.loadLabel(pm).toString());
        mInfo.setPackageName(info.activityInfo.packageName);
        // 为应用程序的启动Activity 准备Intent
        Intent launchIntent = new Intent();
        launchIntent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
        mInfo.setIntent(launchIntent);
        return mInfo;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (tvTip.getVisibility() == View.VISIBLE) {
                tvTip.setVisibility(View.GONE);
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            tvTip.setVisibility(tvTip.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleClearEvent(String str) {
        if (TextUtils.equals(str, "app_list_update")) {
            datas.clear();
            datas.addAll(getAppList(this));
            myAdapter.notifyDataSetChanged();
        }
    }

}
