package com.photatos.dalin.mlkit.ghost.network.entity;

import java.util.Arrays;
import java.util.List;

import com.photatos.dalin.mlkit.ghost.model.entity.Setting;

@SuppressWarnings("unused")
public class SettingsList {

    public List<Setting> settings;

    public static SettingsList from(Setting... settings) {
        SettingsList settingsList = new SettingsList();
        settingsList.settings = Arrays.asList(settings);
        return settingsList;
    }

}
