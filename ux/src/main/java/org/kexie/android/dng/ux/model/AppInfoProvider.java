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

public final class AppInfoProvider
{
    private AppInfoProvider()
    {
        throw new AssertionError();
    }


    public static List<AppInfo> getLaunchApps(Context context)
    {
        PackageManager localPackageManager = context.getPackageManager();
        Intent localIntent = new Intent("android.intent.action.MAIN");
        localIntent.addCategory("android.intent.category.LAUNCHER");
        List<ResolveInfo> localList = localPackageManager.queryIntentActivities(localIntent, 0);
        ArrayList<AppInfo> localArrayList = null;
        Iterator<ResolveInfo> localIterator = null;
        if (localList != null)
        {
            localArrayList = new ArrayList<>();
            localIterator = localList.iterator();
            while (true)
            {
                if (!localIterator.hasNext())
                {
                    break;
                }
                ResolveInfo localResolveInfo = (ResolveInfo) localIterator.next();
                AppInfo localAppBean = new AppInfo();
                localAppBean.setIcon(localResolveInfo.activityInfo.loadIcon(localPackageManager));
                localAppBean.setName(localResolveInfo.activityInfo.loadLabel(localPackageManager).toString());
                localAppBean.setPackageName(localResolveInfo.activityInfo.packageName);
                localAppBean.setDataDir(localResolveInfo.activityInfo.applicationInfo.publicSourceDir);
                localAppBean.setLauncherName(localResolveInfo.activityInfo.name);
                String pkgName = localResolveInfo.activityInfo.packageName;
                PackageInfo mPackageInfo;
                try
                {
                    mPackageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
                    if ((mPackageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
                    {
                        //系统预装
                        localAppBean.setSysApp(true);
                    }
                } catch (PackageManager.NameNotFoundException e)
                {
                    e.printStackTrace();
                }

                String noSeeApk = localAppBean.getPackageName();
                // 屏蔽自己
                if (!noSeeApk.equals(context.getPackageName()))
                {
                    localArrayList.add(localAppBean);
                }
            }
        }
        return localArrayList;
    }

    public static List<AppInfo> getUninstallApps(Context context)
    {
        PackageManager localPackageManager = context.getPackageManager();
        Intent localIntent = new Intent("android.intent.action.MAIN");
        localIntent.addCategory("android.intent.category.LAUNCHER");
        List<ResolveInfo> localList = localPackageManager.queryIntentActivities(localIntent, 0);
        ArrayList<AppInfo> localArrayList = null;
        Iterator<ResolveInfo> localIterator = null;
        if (localList != null)
        {
            localArrayList = new ArrayList<>();
            localIterator = localList.iterator();
            while (true)
            {
                if (!localIterator.hasNext())
                    break;
                ResolveInfo localResolveInfo = localIterator.next();
                AppInfo localAppBean = new AppInfo();
                localAppBean.setIcon(localResolveInfo.activityInfo.loadIcon(localPackageManager));
                localAppBean.setName(localResolveInfo.activityInfo.loadLabel(localPackageManager).toString());
                localAppBean.setPackageName(localResolveInfo.activityInfo.packageName);
                localAppBean.setDataDir(localResolveInfo.activityInfo.applicationInfo.publicSourceDir);
                String pkgName = localResolveInfo.activityInfo.packageName;
                PackageInfo mPackageInfo;
                try
                {
                    mPackageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
                    if ((mPackageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
                    {//系统预装
                        localAppBean.setSysApp(true);
                    } else
                    {
                        localArrayList.add(localAppBean);
                    }
                } catch (PackageManager.NameNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return localArrayList;
    }
}
