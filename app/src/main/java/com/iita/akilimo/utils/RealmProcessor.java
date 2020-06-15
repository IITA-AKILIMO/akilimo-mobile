package com.iita.akilimo.utils;

import com.iita.akilimo.entities.ProfileInfo;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class RealmProcessor {
    Realm realm;

    public RealmProcessor() {
        realm = Realm.getDefaultInstance();
    }

    public void saveProfileInfo(ProfileInfo profileInfo) {
        realm.beginTransaction();
        realm.beginTransaction();
        realm.copyToRealm(profileInfo);
        realm.commitTransaction();
    }

    public ProfileInfo getProfileInfo() {
        ProfileInfo profileInfo = realm
                .where(ProfileInfo.class)
                .findFirst();

        return profileInfo;
    }
}
