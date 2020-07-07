package com.iita.akilimo;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Locales {
    private static final Locale LOCAL_TZ_SWA = new Locale("sw", "TZ");
    private static final Locale LOCAL_KE_SWA = new Locale("sw", "KE");
    private static final Locale LOCAL_NG_ENGLISH = new Locale("en", "NG");

    public static final List<Locale> APP_LOCALES = Arrays.asList(
            Locale.ENGLISH,
            Locales.LOCAL_TZ_SWA);
    public static final List<Locale> LOCALE_COUNTRIES = Arrays.asList(Locales.LOCAL_TZ_SWA, LOCAL_NG_ENGLISH);
}
