package org.kexie.android.dng.ux.model;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;


import org.kexie.android.dng.ux.model.entity.AppInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Mr.小世界 on 2018/11/27.
 */

public final class AppInfoProvider {
    private AppInfoProvider() {
        throw new AssertionError();
    }

    public static AppInfo getLaunchApp(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(packageName);
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);
        if (resolveInfos.size() < 1) {
            return null;
        }
        return toAppInfo(resolveInfos.get(0), packageManager);
    }

    public static List<AppInfo> getLaunchApps(Context context) {
        PackageManager localPackageManager = context.getPackageManager();
        Intent localIntent = new Intent(Intent.ACTION_MAIN);
        localIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> localList = localPackageManager.queryIntentActivities(localIntent, 0);
        ArrayList<AppInfo> localArrayList = null;
        Iterator<ResolveInfo> localIterator;
        if (localList != null) {
            localArrayList = new ArrayList<>();
            localIterator = localList.iterator();
            while (localIterator.hasNext()) {
                ResolveInfo localResolveInfo = localIterator.next();
                // 屏蔽自己
                if (localResolveInfo.activityInfo.packageName.equals(context.getPackageName())) {
                    continue;
                }
                AppInfo localAppBean = toAppInfo(localResolveInfo, localPackageManager);
                localArrayList.add(localAppBean);
            }
        }
        return localArrayList;
    }

    private static AppInfo toAppInfo(ResolveInfo localResolveInfo, PackageManager packageManager) {
        AppInfo localAppBean = new AppInfo();
        localAppBean.setIcon(localResolveInfo.activityInfo.loadIcon(packageManager));
        localAppBean.setName(localResolveInfo.activityInfo.loadLabel(packageManager).toString());
        localAppBean.setPackageName(localResolveInfo.activityInfo.packageName);
        localAppBean.setDataDir(localResolveInfo.activityInfo.applicationInfo.publicSourceDir);
        localAppBean.setLauncherName(localResolveInfo.activityInfo.name);
        String pkgName = localResolveInfo.activityInfo.packageName;
        PackageInfo mPackageInfo;
        try {
            mPackageInfo = packageManager.getPackageInfo(pkgName, 0);
            if ((mPackageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
                //系统预装
                localAppBean.setSysApp(true);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localAppBean;
    }
}
