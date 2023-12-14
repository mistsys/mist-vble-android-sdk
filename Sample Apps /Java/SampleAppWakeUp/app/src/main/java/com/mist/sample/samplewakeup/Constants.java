package com.mist.sample.samplewakeup;

public class Constants {
    /**
     * Provide SDK token in ORG_SECRET,
     * we can get org secret from MIST UI (Organization —> Mobile SDK)
     */
    public static final String ORG_SECRET = "";
    /**
     * Provide orgId for alt beacon scanning here
     */

    public static final String ORG_ID = "";
    public static final long NO_VBLE_TIMEOUT_MS = 5 * 60 * 1000;
    public static final long NO_VBLE_FAIL_COUNT_LIMIT = 200;
    public static final long BEACON_SCAN_INTERVAL_LOCATION_SDK_RUNNING_MS = 10 * 60 * 1000; /* In ms*/
    public static final long BEACON_SCAN_INTERVAL_LOCATION_SDK_NOT_RUNNING_MS = 100; /* In ms*/
    public static final long BEACON_PER_SCAN_DURATION = 10000;
}
