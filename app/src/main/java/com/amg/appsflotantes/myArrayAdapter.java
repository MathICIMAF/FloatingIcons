package com.amg.appsflotantes;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class myArrayAdapter extends ArrayAdapter {
    private final Context context;
    private final PackageManager packageManager;
    private final List<ApplicationInfo> values;

    public myArrayAdapter(Context context2, List<ApplicationInfo> values2) {
        super(context2, -1, values2);
        this.context = context2;
        this.packageManager = context2.getPackageManager();
        this.values = values2;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View packageView = convertView;
        if (packageView == null) {
            packageView = LayoutInflater.from(this.context).inflate(R.layout.package_view, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.iconImageView = (ImageView) packageView.findViewById(R.id.iconImageView);
            viewHolder.nameTextView = (TextView) packageView.findViewById(R.id.nameTextView);
            viewHolder.packageNameTextView = (TextView) packageView.findViewById(R.id.packageNameTextView);
            packageView.setTag(viewHolder);
        }
        ViewHolder viewHolder2 = (ViewHolder) packageView.getTag();
        viewHolder2.iconImageView.setImageDrawable(this.values.get(position).loadIcon(this.packageManager));
        viewHolder2.nameTextView.setText(this.values.get(position).loadLabel(this.packageManager));
        viewHolder2.packageNameTextView.setText(this.values.get(position).packageName);
        return packageView;
    }

    static class ViewHolder {
        public ImageView iconImageView;
        public TextView nameTextView;
        public TextView packageNameTextView;

        ViewHolder() {
        }
    }
}
