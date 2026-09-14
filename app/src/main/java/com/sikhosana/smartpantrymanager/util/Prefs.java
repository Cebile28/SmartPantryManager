package com.sikhosana.smartpantrymanager.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Stores the user's settings.
 *
 * SharedPreferences is used rather than the SQLite database because these are
 * a handful of single values rather than records: there is nothing to query,
 * sort or relate. Android writes them to a small XML file belonging to the
 * app, so they survive the app being closed just as the database does.
 */
public final class Prefs {

    private static final String FILE_NAME = "pantry_settings";

    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts_enabled";
    private static final String KEY_WARNING_DAYS  = "expiry_warning_days";
    private static final String KEY_PREFERRED_UNIT = "preferred_unit";

    public static final int DEFAULT_WARNING_DAYS = 7;
    public static final String DEFAULT_UNIT = "g";

    private Prefs() { }

    private static SharedPreferences get(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    // ------------------------------------------------ expiring-soon alerts

    public static boolean areExpiryAlertsEnabled(Context context) {
        return get(context).getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public static void setExpiryAlertsEnabled(Context context, boolean enabled) {
        get(context).edit().putBoolean(KEY_EXPIRY_ALERTS, enabled).apply();
    }

    /** How many days ahead counts as "expiring soon". */
    public static int getWarningDays(Context context) {
        return get(context).getInt(KEY_WARNING_DAYS, DEFAULT_WARNING_DAYS);
    }

    public static void setWarningDays(Context context, int days) {
        get(context).edit().putInt(KEY_WARNING_DAYS, days).apply();
    }

    // --------------------------------------------------- units preference

    /** The unit pre-selected when adding a new pantry item. */
    public static String getPreferredUnit(Context context) {
        return get(context).getString(KEY_PREFERRED_UNIT, DEFAULT_UNIT);
    }

    public static void setPreferredUnit(Context context, String unit) {
        get(context).edit().putString(KEY_PREFERRED_UNIT, unit).apply();
    }
}