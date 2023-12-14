# App Sample Indoor Location Reporting

In this App we create background services which uses Mist core SDK to provide indoor location details. 

## Prerequisites

For running this application you need SDK Token, which can be obtained from the Mist Portal (Organization —> Mobile SDK)
Provide this value in Constants.java

### Permission required for this app

android.permission.INTERNET
android.permission.RECEIVE_BOOT_COMPLETED
android.permission.ACCESS_BACKGROUND_LOCATION
android.permission.FOREGROUND_SERVICE
android.permission.ACCESS_FINE_LOCATION
android.permission.ACCESS_COARSE_LOCATION
android.permission.BLUETOOTH
android.permission.BLUETOOTH_ADMIN
android.permission.BLUETOOTH_SCAN
android.permission.BLUETOOTH_CONNECT


### Major classes and its usage


#### MistSdkManager
     This class initializes the location sdk by providing location callbacks and org secret 


#### MainActivity
    This class start the locaton service in background

#### LocationForegroundService
    This class extend service class and provide implementation of methods for starting location sdk 
    inside foreground service.

#### LocationJobService
     This class extend JobService class and provide implementation of methods for starting location sdk 
     inside  scheduled job.

#### SdkBroadcasterReceiver
    This class provides the handling the broadcast events.

#### ServiceInitializer
    This class provides utility functions for starting services.

#### SDKCallbackHandler
    This class provides the implementation for location sdk callback function



### Main callbacks leveraged for this sample app
    onRelativeLocationUpdated : for location details
    onMapUpdated : for map details
    onError :  for showing error returned by location sdk.

### More details

For more details please visit https://github.com/mistsys/mist-vble-android-sdk/wiki
