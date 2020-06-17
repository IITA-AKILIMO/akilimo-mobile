package com.iita.akilimo.utils;

import com.iita.akilimo.entities.ProfileInfo;

import io.realm.Realm;

public class RealmProcessor {
    Realm realm;

    public RealmProcessor() {
        realm = Realm.getDefaultInstance();
    }

    public void saveProfileInfo(ProfileInfo profileInfo) {
        realm.beginTransaction();
        realm.copyToRealm(profileInfo);
        realm.commitTransaction();
    }

    public void updateProfileInfo() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

            }
        });
    }

    public ProfileInfo getProfileInfo() {
        ProfileInfo profileInfo = realm
                .where(ProfileInfo.class)
                .findFirst();
        return profileInfo;
    }
}
