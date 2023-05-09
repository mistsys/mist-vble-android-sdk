# App Sample App WakeUp

In this APP we listen beacons using alt beacon library and start Mist core SDK in background using foreground service 
whenever we detect any configured beacon. Mist core SDK provides indoor location details.


## Prerequisites


For running this application you need SDK Token, which can be obtained from the Mist Portal (Organization —> Mobile SDK)
We also need to make sure that App wakeup functionality is enabled in Mist Portal 

(Organization —> Site Configuration -> select your site -> Bluetooth based Location Services -> Enable App Wakeup )

Get the orgId from Mist Portal and set it in MainApplication class for defining regions for alt beacon monitoring.

Provide these values in Constants.java file 

### Permission required for this app

android.permission.INTERNET
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


#### SDKCallbackHandler
    This class provides the implementation for location sdk callback function

#### MainApplication
    This class starts ibeacon monitoring.


### Main callbacks leveraged for this sample app
    onRelativeLocationUpdated : for location details
    onMapUpdated : for map details
    onError :  for showing error returned by location sdk.

### More details

For more details please visit https://github.com/mistsys/mist-vble-android-sdk/wiki
