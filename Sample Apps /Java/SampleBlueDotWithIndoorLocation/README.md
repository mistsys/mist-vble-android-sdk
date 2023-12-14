# Sample Bluedot With Indoor Location  app

This application depicts the indoor location feature targeted for the mobile client, with a bluedot, using the MIST Core SDK. 
When app is not in foreground it runs the Mist SDK as a foreground service.

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

#### MapFragment
    This is the fragment which is used to render the map/bluedot with the info received from the Mist SDK.


#### MistSdkManager
     This class initializes the location sdk by providing location callbacks and org secret 


#### MainActivity
    This class start the MapFragment & also starts the foreground service when activity is destoyed. 

#### LocationForegroundService
    This class extend service class and provide implementation of methods for starting location sdk 
    inside foreground service.

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


### SDK classes used for this sample app
    MistMap
       - getUrl For downloading floormap 
       - getId  For getting map id
       - getPpm For positioning bluedot 

### More details

For more details please visit https://github.com/mistsys/mist-vble-android-sdk/wiki
