package com.amg.appsflotantes;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FloatService extends Service {
    Binder binder = new FloatBinder();
    SharedPreferences mPreferences;
    PackageManager packageManager;
    List<ImageView> viewList = new ArrayList();
    WindowManager windowManager;

    /* access modifiers changed from: package-private */
    public class IconHolder {
        public Drawable defaultIcon;
        public String packageName;
        public Drawable statusIcon;
        public int statusId = 0;
        public ImageView view;
        public float x_pos;
        public float y_pos;

        IconHolder(ImageView view2, Drawable baseIcon, String packageName2) {
            this.view = view2;
            this.defaultIcon = baseIcon;
            this.statusIcon = baseIcon;
            this.packageName = new String(packageName2);
        }
    }

    public class FloatBinder extends Binder {
        public FloatBinder() {
        }

        /* access modifiers changed from: package-private */
        public FloatService getService() {
            return FloatService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    public void onCreate() {
        super.onCreate();
        this.windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        this.packageManager = getPackageManager();
        this.mPreferences = getSharedPreferences("appfloat.prefs", 0);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.d("AppFloat", "Service started with null intent");
            floatSavedApps();
            return START_STICKY;
        }
        Log.d("AppFloat", "Service started with intent");
        Bundle bundle = intent.getExtras();
        floatApp(bundle.getString("appPackage"), bundle.getInt("resId"));
        return START_STICKY;
    }

    private Set<String> getPrefPackageList() {
        return this.mPreferences.getStringSet("packageNames", new HashSet<>());
    }

    private void addToPrefPackageList(String packageName) {
        if (packageName != null) {
            Set<String> newPackageNameList = new HashSet<>(getPrefPackageList());
            newPackageNameList.add(packageName);
            SharedPreferences.Editor editor = this.mPreferences.edit();
            editor.putStringSet("packageNames", newPackageNameList);
            editor.commit();
            Log.d("AppFloat", newPackageNameList.size() + " package names in saved preferences");
        }
    }

    private void addIconToScreen(final String packageName, int resourceId, int x, int y) {
        int max_icons = 10;
        try {
            max_icons = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("number", "10"));
        } catch (Exception e) {
        }
        if (this.viewList.size() < max_icons) {
            final ImageView iconView = new ImageView(this);
            this.viewList.add(iconView);
            Drawable draw = getIcon(packageName, resourceId);
            iconView.setImageDrawable(draw);
            iconView.setTag(new IconHolder(iconView, draw, packageName));
            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(-2, -2);
            setWindowParams(params, x, y);
            try {
                if (this.windowManager != null) {
                    this.windowManager.addView(iconView, params);
                }
            } catch (Exception e2) {
                Toast.makeText(this, getString(R.string.msj), Toast.LENGTH_LONG).show();
            }
            ViewConfiguration.get(iconView.getContext());
            final int mLongPressTimeOut = ViewConfiguration.getLongPressTimeout();
            final int mTapTimeOut = ViewConfiguration.getTapTimeout();
            iconView.setOnTouchListener(new View.OnTouchListener() {
                /* class com.amg.appsflotantes.FloatService.AnonymousClass1 */
                private float initialTouchX;
                private float initialTouchY;
                private int initialX;
                private int initialY;
                private WindowManager.LayoutParams paramsF = params;

                /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case 0:
                            Log.d("AppFloat", "Action Down");
                            this.initialX = this.paramsF.x;
                            this.initialY = this.paramsF.y;
                            this.initialTouchX = event.getRawX();
                            this.initialTouchY = event.getRawY();
                            break;
                        case 1:
                            Log.d("AppFloat", "Action Up");
                            Log.d("AppFloat", "DistanceX: " + Math.abs(this.initialTouchX - event.getRawX()));
                            Log.d("AppFloat", "DistanceY: " + Math.abs(this.initialTouchY - event.getRawY()));
                            Log.d("AppFloat", "elapsed gesture time: " + (event.getEventTime() - event.getDownTime()));
                            if (Math.abs(this.initialTouchX - event.getRawX()) <= 20.0f && Math.abs(this.initialTouchY - event.getRawY()) <= 20.0f) {
                                if (event.getEventTime() - event.getDownTime() < ((long) mTapTimeOut)) {
                                    Log.d("AppFloat", "Click Detected");
                                    FloatService.this.startAppActivity(packageName);
                                } else if (event.getEventTime() - event.getDownTime() >= ((long) mLongPressTimeOut)) {
                                    Log.d("AppFloat", "Long Click Detected");
                                }
                            }
                            Log.d("AppFloat", "Action Default");
                            break;
                        case 2:
                            Log.d("AppFloat", "Action Move");
                            this.paramsF.x = this.initialX + ((int) (event.getRawX() - this.initialTouchX));
                            this.paramsF.y = this.initialY + ((int) (event.getRawY() - this.initialTouchY));
                            FloatService.this.windowManager.updateViewLayout(iconView, this.paramsF);
                            break;
                        default:
                            Log.d("AppFloat", "Action Default");
                            break;
                    }
                    return false;
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startAppActivity(String packageName) {
        Intent intent = this.packageManager.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            startActivity(intent);
        }
    }

    private void setWindowParams(WindowManager.LayoutParams params, int x, int y) {
        params.height = (int) getResources().getDimension(R.dimen.icon_size);
        params.width = (int) getResources().getDimension(R.dimen.icon_size);
        if (Build.VERSION.SDK_INT >= 26) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = -3;
        params.gravity = Gravity.START;
        if (x < 0 || y < 0) {
            params.y = dpToPx(60) * this.viewList.size();
            return;
        }
        params.x = x;
        params.y = y;
    }

    private int dpToPx(int dp) {
        return (int) ((((float) dp) * getResources().getDisplayMetrics().density) + 0.5f);
    }

    private Drawable getIcon(String packageName, int resourceId) {
        Drawable icon = null;
        if (resourceId == 0) {
            Log.v("AppFloat", "Resource ID not found, attempting to load package icon");
            try {
                icon = getPackageIcon(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Log.v("AppFloat", "Loading image using Resource ID");
            icon = getPackageImage(packageName, resourceId);
        }
        if (icon != null) {
            return icon;
        }
        Log.v("AppFloat", "Using default image for icon");
        return getResources().getDrawable(R.mipmap.ic_launcher_foreground);
    }

    private Drawable getPackageImage(String packageName, int resourceId) {
        return this.packageManager.getDrawable(packageName, resourceId, null);
    }

    private Drawable getPackageIcon(String appPackage) throws PackageManager.NameNotFoundException {
        try {
            return this.packageManager.getApplicationIcon(appPackage);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Drawable mergeBitmap(Drawable base, Drawable status) {
        Bitmap bitmap = Bitmap.createBitmap(base.getIntrinsicWidth(), base.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        int intrinsicHeight = base.getIntrinsicHeight() - status.getIntrinsicHeight();
        base.setBounds(0, 0, base.getIntrinsicWidth(), base.getIntrinsicHeight());
        status.setBounds(0, 70, 30, 100);
        base.draw(c);
        status.draw(c);
        return new BitmapDrawable(getResources(), bitmap);
    }

    public void floatApp(String packageName, int resourceId) {
        addIconToScreen(packageName, resourceId, -1, -1);
    }

    public void floatSavedApps() {
        for (String packageName : getPrefPackageList()) {
            addIconToScreen(packageName, 0, -1, -1);
        }
    }

    public void removeIconsFromScreen() {
        for (ImageView view : this.viewList) {
            this.windowManager.removeView(view);
        }
        this.viewList.clear();
    }

    public void saveIconsToPref() {
        for (ImageView view : this.viewList) {
            IconHolder holder = (IconHolder) view.getTag();
            addToPrefPackageList(holder.packageName);
            holder.x_pos = view.getX();
            holder.y_pos = view.getY();
        }
    }

    public void clearPrefPackageList() {
        SharedPreferences.Editor editor = this.mPreferences.edit();
        editor.remove("packageNames");
        editor.commit();
    }

    public void onDestroy() {
        super.onDestroy();
    }
}
