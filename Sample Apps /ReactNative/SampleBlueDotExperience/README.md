# React Native Sample App Installation

# Introduction
The indoor location service and outdoor location service differ significantly. This is because most outdoor location services rely on GPS and other Global Navigation Satellite Systems (GNSS), which are obstructed by buildings' roofs and walls in the case of indoor location services. Consequently, GPS is not a suitable solution for our case study in terms of accuracy. To attain indoor location services, we utilize MistSDK, which employs BLE technology for indoor positioning, wayfinding systems, proximity notification, and other features.

<U>Important features offered by our Sample App:<U>
* Indoor Location
  * Providing the coordinates in the map for your app to draw the blue dot location of the device.
* Virtual Beacon Notification
  * A beacon, which is not physically present, but a virtual one. A virtual beacon that is added on the web portal and known by your app through the SDK. It provides notifications when the device is near the location of the virtual beacon.
* Virtual Zone Notification
  * Zones are defined on the Mist portal as areas within a map. The SDK provides notifications on going into a zone.

 
# System Requirements
1. Software Requirements

   * Android Studio: 4.0 or later (For Android)
   * Xcode: 12 or later (For iOS)
   * Access to the [Mist Account](https://manage.mist.com/)
   * Mobile SDK secret
  
# Installation
1. To get started with the React native sample app, you should setup the react native environment for [android](https://reactnative.dev/docs/environment-setup?guide=native&platform=android) and for [iOS](https://reactnative.dev/docs/environment-setup?guide=native&platform=ios)
2. Clone the repo.
3. Add the Mist SDK token in the .env file.
4. To install the npm package, inside your project directory, run:
```
# using npm
$npm install

   or

# using yarn
$yarn
```
5. To start the React native sample app by run the following command from your project folder.
```
# using npm
$npm start

    or

# using yarn
$yarn start
```
6. Select 'a' for ```Android``` and 'i' for ```iOS``` to compile for the respective platform.
