# Wakeup Sample

This application depicts the wakeup feature targeted for the mobile client, using the Google Nearby API ( https://developers.google.com/nearby/messages/android/get-started ).
You can start MIST Core SDK in killed state after getting wakeup callback.

## Prerequisites

For running this application you need SDK Token, which can be obtained from the Mist Portal (Organization —> Mobile SDK)

You have to replace "YOUR_APP_KEY" in the AndroidManifest file with your API Key of the Google Project.

### Major classes and its usage

#### MistManager
    This is the manager class which is doing all the interaction between Mist core SDK and App

#### MapFragment
    This is the fragment which is used to render the map/bluedot with the info received from the Mist SDK.
    This class also check for permission provided and the availability of network/bluetooth/location.
    MapFragment is responsible to start the SDK from the Token info it got from the AddTokenDialogFragment

#### AddTokenDialogFragment
    This is a dialog fragment to get the SDK token from the User on click of fab icon

#### HomeFragment
    This fragment is the initial view attached to MainActivity and have fab icon which will launch 
    AddTokenDialogFragment
    
#### MISTSDKBackgroundService
    This is a job service which can be scheduled/unscheduled as per need, like when got the onFound 
    of the beacon registered for wakeup
    
#### NearByJobIntentService
    This is a job intent service which will be enqued for work when NearByBroadCastReceiver's onReceive is triggered   
        
#### NearByBroadCastReceiver
    This is a broadcast receiver  which is past as pendingIntent to Nearby.Messages.subscribe and 
    it will be the one which will enque the work of NearByJobIntentService
    
    
#### Utility/POJO(model) classes
    SharedPrefUtils : Utility for Shared preference
    Utils : general utility class
    OrgData : POJO to save the org info got from enrollment 
    
### Main callbacks leveraged for this sample app
    onRelativeLocationUpdated : for location details
    onMapUpdated : for map details

### More details

For more details please visit https://github.com/mistsys/mist-vble-android-sdk/wiki#app-wake-up and https://github.com/mistsys/mist-vble-android-sdk/wiki#background-mode
