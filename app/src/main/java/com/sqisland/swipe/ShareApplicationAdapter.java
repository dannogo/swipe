package com.sqisland.swipe;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oleh on 12/1/15.
 */
public class ShareApplicationAdapter extends RecyclerView.Adapter<ShareApplicationAdapter.ShareApplicationViewHolder> {

    private LayoutInflater inflater;
    private List<ResolveInfo> list;
    private Context context;
    PackageManager packageManager;
    Intent sendIntent;

    public ShareApplicationAdapter(Context context) {
        packageManager = context.getPackageManager();

        sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
//        sendIntent.putExtra(Intent.EXTRA_TEXT, "https://www.google.com");
        sendIntent.setType("text/plain");
        list =  packageManager.queryIntentActivities(sendIntent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);


        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public ShareApplicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.share_applications_row, parent, false);
        ShareApplicationViewHolder holder = new ShareApplicationViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ShareApplicationViewHolder holder, int position) {
        holder.icon.setImageDrawable(list.get(position).loadIcon(packageManager));
        holder.label.setText(list.get(position).loadLabel(packageManager));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ShareApplicationViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener {

        private ImageView icon;
        private TextView label;
        public ShareApplicationViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.appIcon);
            label = (TextView) itemView.findViewById(R.id.appLabel);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == itemView.getId()){

                Resources resources = context.getResources();
                String packageName = list.get(getAdapterPosition()).activityInfo.packageName;
                Log.w("LOG", "packageName: "+packageName);
                if (packageName.contains("android.email")){

                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.subjectForMailApps));
                    emailIntent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.largeTextForMailApps) + "https://play.google.com/store/apps/details?id="/* + context.getPackageName()*/);
                    emailIntent.setType("message/rfc822");
                    context.startActivity(emailIntent);

                }else if (packageName.contains("android.gm")){
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SENDTO);
                    intent.setComponent(new ComponentName(list.get(getAdapterPosition()).activityInfo.packageName,
                            list.get(getAdapterPosition()).activityInfo.name));
                    intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(resources.getString(R.string.largeTextForMailApps)) + " https://play.google.com/store/apps/details?id=");
                    intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.subjectForMailApps));
                    intent.setType("message/rfc822");
                    context.startActivity(intent);
                }else if (packageName.contains("facebook.katana")){
                    ShareLinkContent content = new ShareLinkContent.Builder()
                            .setContentUrl(Uri.parse("http://google.com.ua/"))
                            .build();
                    ShareDialog.show(((ShareActivity)context), content);
                }else{
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setComponent(new ComponentName(list.get(getAdapterPosition()).activityInfo.packageName,
                            list.get(getAdapterPosition()).activityInfo.name));
                    intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(resources.getString(R.string.largeTextForMailApps)) + " https://play.google.com/store/apps/details?id=");
                    intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.subjectForMailApps));
                    context.startActivity(intent);
                }



            }
        }
    }
}
