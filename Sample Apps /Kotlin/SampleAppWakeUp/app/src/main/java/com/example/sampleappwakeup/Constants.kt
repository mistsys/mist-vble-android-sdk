package com.example.sampleappwakeup

class Constants {
    /**
     * Provide SDK token in ORG_SECRET,
     * we can get org secret from MIST UI (Organization —> Mobile SDK)
     */
    val orgSecret : String = ""
    /**
     * Provide orgId for alt beacon scanning here
     */
    val orgId : String = ""

    val noVBLETimeMs : Long = 5 * 60 * 1000

    val noVBLEFailCountLimit : Long = 200

    val beaconScanIntervalLocationSdkRunningMs : Long = 10 * 60 * 1000   /* In ms*/

    val beaconScanIntervalLocationSdkNotRunningMs : Long = 100   /* In ms*/

    val beaconPerScanDuration : Long = 10000
}