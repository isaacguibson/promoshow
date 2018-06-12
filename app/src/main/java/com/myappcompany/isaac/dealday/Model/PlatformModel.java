package com.myappcompany.isaac.dealday.Model;

/**
 * Created by isaac on 15/04/18.
 */

public class PlatformModel {

    private String name;
    private String confName;
    private boolean checked;

    public PlatformModel(String name, String confName, boolean checked) {
        this.name = name;
        this.confName = confName;
        this.checked = checked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConfName() {
        return confName;
    }

    public void setConfName(String confName) {
        this.confName = confName;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
