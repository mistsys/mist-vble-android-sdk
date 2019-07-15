# Background Sample App Code

This application demonstrates how to handle the SDK in the background state using Mist SDK.

## Prerequisites

For running this application you need the SDK Token, which can be obtained from the Mist Portal (Organization —> Mobile SDK)

### Major classes and its usage

#### MistManager
    The manager class manages all the crucial interactions between Mist SDK and App

#### MapFragment
    This is the fragment which is used to render the map/bluedot with the info received from the Mist SDK.
    This class also check for permission provided and the availability of network/bluetooth/location.
    MapFragment is responsible to start the SDK from the Token info it got from the AddTokenDialogFragment

#### HomeFragment
    This fragment is the initial view attached to MainActivity

#### MSTSDKBackgroundService
    The background service that will be scheduled and unscheduled when MapFragment goes to background via onStop and foreground via onStart respectively.

#### Utility/POJO(model) classes
    SharedPrefUtils: Utility for Shared preference
    Utils: general utility class
    OrgData: POJO to save the org info got from enrollment
    
### Main callbacks leveraged for this sample app
    onRelativeLocationUpdated : for location details
    onMapUpdated : for map details

### More details

For more details please visit https://github.com/mistsys/mist-vble-android-sdk/wiki/Background-mode
