package com.amg.appsflotantes;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.Comparator;

public class ApplicationInfoSorter implements Comparator<ApplicationInfo> {
    private final PackageManager packageManager;

    public ApplicationInfoSorter(PackageManager packageManager2) {
        this.packageManager = packageManager2;
    }

    public int compare(ApplicationInfo a1, ApplicationInfo a2) {
        return a1.loadLabel(this.packageManager).toString().compareToIgnoreCase(a2.loadLabel(this.packageManager).toString());
    }
}
