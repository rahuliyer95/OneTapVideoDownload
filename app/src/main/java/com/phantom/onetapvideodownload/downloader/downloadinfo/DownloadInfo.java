package com.phantom.onetapvideodownload.downloader.downloadinfo;

import android.content.Context;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.phantom.onetapvideodownload.Global;
import com.phantom.onetapvideodownload.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class DownloadInfo {
    public enum Status {
        Stopped(0), Completed(1), Downloading(2), NetworkProblem(3), NetworkNotAvailable(4),
        WriteFailed(5);

        int status;
        Status(int s) {
            status = s;
        }

        int getStatus() {
            return status;
        }
    }

    public abstract String getUrl();
    public abstract String getFilename();
    public abstract String getDownloadLocation();
    public abstract long getDatabaseId();
    public abstract void setDatabaseId(long databaseId);
    public abstract Status getStatus();
    public abstract void setStatus(Status status);
    public abstract long getContentLength();
    public abstract void setContentLength(long contentLength);
    public abstract long getDownloadedLength();
    public abstract void setDownloadedLength(long downloadedLength);
    public abstract void addDownloadedLength(long additionValue);
    public abstract Integer getProgress();
    public abstract void writeToDatabase();
    public abstract void removeDatabaseEntry();
    public abstract Collection<String> getOptions();
    public abstract MaterialDialog.ListCallback getOptionCallback();
    public abstract String getPackageName();
    public abstract void setPackageName(String packageName);

    List<String> getGenericOptions(Context context, Status status) {
        List<String> options = new ArrayList<>();
        switch (status) {
            case Completed:
                options.add(context.getResources().getString(R.string.open));
                options.add(context.getResources().getString(R.string.share));
                options.add(context.getResources().getString(R.string.remove_from_list));
                options.add(context.getResources().getString(R.string.delete_from_storage));
                options.add(context.getResources().getString(R.string.details));
                break;
            case Stopped:
                options.add(context.getResources().getString(R.string.resume));
                options.add(context.getResources().getString(R.string.remove_from_list));
                options.add(context.getResources().getString(R.string.delete_from_storage));
                options.add(context.getResources().getString(R.string.details));
                break;
            case Downloading:
                options.add(context.getResources().getString(R.string.pause));
                options.add(context.getResources().getString(R.string.details));
                break;
        }

        return options;
    }

    boolean handleGenericOptionClicks(final Context context, int resourceId) {
        switch (resourceId) {
            case R.string.open:
                Global.startOpenIntent(context, getDownloadLocation());
                return true;
            case R.string.share:
                Global.startFileShareIntent(context, getDownloadLocation());
                return true;
            case R.string.delete_from_storage:
                new MaterialDialog.Builder(context)
                        .title(R.string.delete_confirmation)
                        .content(R.string.delete_confirmation_content)
                        .positiveText(R.string.yes)
                        .negativeText(R.string.no)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Global.deleteFile(context, getDownloadLocation());
                                removeDatabaseEntry();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return true;
            case R.string.remove_from_list:
                new MaterialDialog.Builder(context)
                        .title(R.string.remove_from_list_confirmation)
                        .content(R.string.remove_from_list_confirmation_content)
                        .positiveText(R.string.yes)
                        .negativeText(R.string.no)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                removeDatabaseEntry();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return true;
            default:
                return false;
        }
    }
}
