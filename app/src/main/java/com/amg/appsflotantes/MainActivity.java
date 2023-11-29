package com.amg.appsflotantes;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    List<String> appList;
    private ServiceConnection mConnection = new ServiceConnection() {
        /* class com.amg.appsflotantes.MainActivity.AnonymousClass3 */

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("AppFloater", "Connected to service");
            MainActivity.this.mService = ((FloatService.FloatBinder) service).getService();
            MainActivity.this.mbound = true;
            if (MainActivity.this.mPreferences.getBoolean("pref_save", false)) {
                Log.d("AppFloater", "Floating saved preference apps");
                MainActivity.this.mService.floatSavedApps();
                return;
            }
            Log.d("AppFloater", "Not floating saved preference apps");
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d("AppFloat", "Disconnected from service");
            MainActivity.this.mbound = false;
        }
    };
    SharedPreferences mPreferences;
    FloatService mService;
    boolean mbound = false;
    Set<String> packageNameSet;

    /* access modifiers changed from: protected */
    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AdView adView = findViewById(R.id.adView);
        adView.loadAd(new AdRequest.Builder().build());
        bindService(new Intent(this, FloatService.class), this.mConnection, BIND_AUTO_CREATE);
        ((Button) findViewById(R.id.habilitar)).setOnClickListener(new View.OnClickListener() {
            /* class com.amg.appsflotantes.MainActivity.AnonymousClass1 */

            public void onClick(View v) {
                MainActivity.this.startActivityForResult(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + MainActivity.this.getPackageName())), 0);
            }
        });
        PackageManager pm = getPackageManager();
        this.packageNameSet = new HashSet();
        final ArrayList<ApplicationInfo> acceptablePackages = new ArrayList<>();
        ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getRunningAppProcesses();
        for (PackageInfo packageInfo : pm.getInstalledPackages(PackageManager.GET_META_DATA)) {
            if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                acceptablePackages.add(packageInfo.applicationInfo);
            }
        }
        Collections.sort(acceptablePackages, new ApplicationInfoSorter(pm));
        myArrayAdapter adapter = new myArrayAdapter(this, acceptablePackages);
        ListView listview = (ListView) findViewById(R.id.listView);
        listview.setAdapter((ListAdapter) adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /* class com.amg.appsflotantes.MainActivity.AnonymousClass2 */

            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (!MainActivity.this.packageNameSet.contains(((ApplicationInfo) acceptablePackages.get(position)).packageName)) {
                    MainActivity.this.floatApp(((ApplicationInfo) acceptablePackages.get(position)).packageName);
                    MainActivity.this.packageNameSet.add(((ApplicationInfo) acceptablePackages.get(position)).packageName);
                }
            }
        });
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    /* access modifiers changed from: protected */
    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity
    public void onStop() {
        super.onStop();
        boolean savedApps = this.mPreferences.getBoolean("pref_save", false);
        if (!this.mbound) {
            Log.d("AppFloater", "onStop called, but service isn't bound");
        } else if (savedApps) {
            Log.d("AppFloater", "Clearing pref list");
            this.mService.clearPrefPackageList();
            Log.d("AppFloater", "Saving icons to prefs");
            this.mService.saveIconsToPref();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.FragmentActivity
    public void onDestroy() {
        super.onDestroy();
        Log.d("AppFloater", "Activity onDestroy called");
        if (this.mbound) {
            Log.d("AppFloater", "Unbinding from service");
            unbindService(this.mConnection);
            this.mbound = false;
        }
        try {
            this.mService.removeIconsFromScreen();
        }
        catch (Exception e){}
    }

    /*private boolean isMyServiceRunning(Class<?> serviceClass) {
        for (ActivityManager.RunningServiceInfo service : ((ActivityManager) getSystemService("activity")).getRunningServices(ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i("Service status", "Not running");
        return false;
    }*/

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void floatApp(String packageName) {
        floatApp(packageName, 0);
    }

    private void floatApp(String packageName, int resourceId) {
        if (this.mbound) {
            this.mService.floatApp(packageName, resourceId);
        }
    }

    private byte[] encodeResourceToByteArray() {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_foreground);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if(R.id.detener == id) {/*{ENCODED_INT: 2131624097}*/
            if (this.mbound) {
                Log.d("AppFloater", "Calling remove icons on service");
                this.mService.removeIconsFromScreen();
                this.packageNameSet.clear();
                return true;
            }
            Log.d("AppFloater", "Service isn't bound, can't call remove icons");
            return true;
        }
        else if(id == R.id.ajustes) { /*{ENCODED_INT: 2131624098}*/
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        else if(id == R.id.compartir) { /*{ENCODED_INT: 2131624100}*/
            Intent sharingIntent = new Intent("android.intent.action.SEND");
            sharingIntent.setType("text/plain");
            String shareBodyText = "https://play.google.com/store/apps/details?id=" + getPackageName();
            sharingIntent.putExtra("android.intent.extra.SUBJECT", "Share");
            sharingIntent.putExtra("android.intent.extra.TEXT", shareBodyText);
            startActivity(Intent.createChooser(sharingIntent, "Shearing Option"));
            return true;
        }
        else if (id == R.id.help){
            AlertDialog.Builder title = new AlertDialog.Builder(MainActivity.this).setTitle(MainActivity.this.getString(R.string.help));
            title.setMessage(MainActivity.this.getString(R.string.inicial)).setPositiveButton("OK", new DialogInterface.OnClickListener() { // from class: com.amg.compressaudio.MainActivity.CompressAudios.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).show();
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }
}
