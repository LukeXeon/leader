package org.kexie.android.dng.ux.model.entity;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

public class AppInfo
{
  private String dataDir;
  private Drawable icon;
  private String id;
  private String name;
  private String launcherName;
  private String packageName;
  private boolean sysApp = false;

  public String getDataDir()
  {
    return this.dataDir;
  }

  public Drawable getIcon()
  {
    return this.icon;
  }

  public String getId()
  {
    return this.id;
  }

  public String getName()
  {
    return this.name;
  }

  public String getPackageName()
  {
    return this.packageName;
  }

  public void setDataDir(String paramString)
  {
    this.dataDir = paramString;
  }

  public void setIcon(Drawable paramDrawable)
  {
    this.icon = paramDrawable;
  }

  public void setId(String paramString)
  {
    this.id = paramString;
  }

  public void setName(String paramString)
  {
    this.name = paramString;
  }

  public void setPackageName(String paramString)
  {
    this.packageName = paramString;
  }

  @NonNull
  public String toString()
  {
    return "AppInfo [packageName=" + this.packageName + ", name=" + this.name + ", dataDir=" + this.dataDir + "]";
  }

  public boolean isSysApp()
  {
    return sysApp;
  }

  public void setSysApp(boolean sysApp)
  {
    this.sysApp = sysApp;
  }

  public String getLauncherName()
  {
    return launcherName;
  }

  public void setLauncherName(String launcherName)
  {
    this.launcherName = launcherName;
  }
}

