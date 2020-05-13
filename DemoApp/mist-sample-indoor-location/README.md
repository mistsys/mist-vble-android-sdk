# Indoor Location Sample

This application depicts the indoor location feature targeted for the mobile client, with a bluedot, using the MIST Core SDK. 

## Prerequisites

For running this application you need SDK Token, which can be obtained from the Mist Portal (Organization —> Mobile SDK)

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
    This fragment is the initial view attached to MainActivity and have fab icon which will launch AddTokenDialogFragment

#### Utility/POJO(model) classes
    SharedPrefUtils : Utility for Shared preference
    Utils : general utility class
    OrgData : POJO to save the org info got from enrollment 
    
### Main callbacks leveraged for this sample app
    onRelativeLocationUpdated : for location details
    onDRSnappedLocationUpdated : for DR snapped path from MSTPoint object
    onMapUpdated : for map details

### More details

For more details please visit https://github.com/mistsys/mist-vble-android-sdk/wiki
