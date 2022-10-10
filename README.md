# Android MistSDK      

## Overview
Mist SDK will provide you the indoor bluedot experience using Mist’s 16 vBLE antenna array Access point. Using this SDK you will know where the user is and can provide proximity related notification using Mist patented vBeacon technology.

## Latest Release:
Add the following dependency with the latest version of the Mist SDK in the build.gradle file:
```gradle
implementation 'com.mist:core-sdk:2.3.0'
```
OR     

Mist vBLE Android SDK is also distributed as .aar file. [[Download Mist SDK]](https://github.com/mistsys/mist-vble-android-sdk/tree/master/Library) 
 
 
## Release Notes:       
**New:**
Release Note:
1. Introducing token rotation to maintain security and limit the effects of any compromised tokens. It involves exchanging one expiring access token for a new one.     
* Secret token which is used to initialize the MSTCentralManager will be auto-refreshed after every 30 days.     
* App Developer MUST implement the Error Handling callback to re-enroll the device once the existing secret token has expired.     
2. Enhancement to the SDK on BLE scanning mechanism to prevent the connection retries to cloud when device is not in BLE Proximity.     
3. SDK can experience error anywhere in the lifecycle due to numerous reasons on the network. To increase the reliability and reduce the operational cost, we have added backoff where SDK will pause for given amount of time before before retrying the connection again.      

  
## Integrating Mist SDK
To integrate Mist vBLE Android SDK in your app, please follow the instructions available at [Integration Guide in Wiki section](https://github.com/mistsys/mist-vble-android-sdk/wiki)

## Sample Apps:
For more detail, you can see the implementation in the sample app [here](https://github.com/mistsys/mist-vble-android-sdk/tree/master/DemoApp).


## Contact Us
For more information, please visit [www.mist.com](https://www.mist.com/). For questions or assistance, please email us at support@mist.com.
