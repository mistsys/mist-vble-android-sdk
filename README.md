# Android MistSDK      

## Overview
Mist SDK will provide you the indoor bluedot experience using Mist’s 16 vBLE antenna array Access point. Using this SDK you will know where the user is and can provide proximity related notification using Mist patented vBeacon technology.

## Beta Release:    

Mist vBLE Android SDK is also distributed as .aar file. [[Download Mist SDK]](https://github.com/mistsys/mist-vble-android-sdk/tree/beta/Library) 

Add following into App/build.gradle

```
implementation(name:'mistsdk-framework', ext:'aar')
implementation(name:'mist-mobile', ext:'aar')
```
 
## Release Notes:       
* Support to enroll Org on AWS East Environment.
 
  
## Integrating Mist SDK
To integrate Mist vBLE Android SDK in your app, please follow the instructions available at [Integration Guide in Wiki section](https://github.com/mistsys/mist-vble-android-sdk/wiki)

## Sample Apps:
For more detail, you can see the implementation in the sample app [here](https://github.com/mistsys/mist-vble-android-sdk/tree/beta/DemoApp).


## Contact Us
For more information, please visit [www.mist.com](https://www.mist.com/). For questions or assistance, please email us at support@mist.com.
