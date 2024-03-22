import React, { useState, useEffect } from 'react';
import {
  Platform,
  View,
  Text,
  Image,
  Dimensions,
  PixelRatio,
  Appearance,
} from 'react-native';
import { NativeEventEmitter, NativeModules } from 'react-native';
import AppButton from './components/button';
import FloorPlan from './components/floorPlan';
import { AppStyles } from './styles';
import Loader from './components/loader';
import Dialog_Box from './components/dialog';
import requestPermissionsForAndroid from './services/permissions';

const MIST_SDK_TOKEN = process.env.MIST_SDK_TOKEN;

export default function App() {
  const MistSDK = NativeModules.RNMistsdk;
  const MistSDKEvents = new NativeEventEmitter(MistSDK);

  const [mapUrl, setMapUrl] = useState('');
  const [buttonText, setButtonText] = useState('Start');
  const [loaderText, setLoaderText] = useState('Starting MistSDK..');
  const [showLoader, setShowLoader] = useState(false);
  const [isSdkActive, setIsSdkActive] = useState(false);
  const [xPosition, setXPosition] = useState(0);
  const [yPosition, setYPosition] = useState(0);
  const [mapPPM, setMapPPM] = useState(0);
  const [mapName, setMapName] = useState('Map Name');
  const [mapImageHeight, setMapImageHeight] = useState(0);
  const [mapImageWidth, setMapImageWidth] = useState(0);
  const [blueDotX, setBlueDotX] = useState(0);
  const [blueDotY, setBlueDotY] = useState(0);
  const [scalingFactor, setScalingFactor] = useState(0);
  const [dialogState, setDialogState] = useState(false);
  const [dialogTitle, setDialogTitle] = useState('');
  const [dialogDescription, setDialogDescription] = useState('');
  const pixelRatio = PixelRatio.get(); // Used for converting px into dpx and vise versa

  useEffect(() => {
    if (Platform.OS === 'android') {
      requestPermissionsForAndroid();
      Appearance.setColorScheme('light')
    }
  }, []);

  useEffect(() => {
    if (mapUrl != '') {
      Image.getSize(mapUrl, (width, height) => {
        // calculate image width and height
        const screenWidth = Dimensions.get('window').width;
        const scalingFactor = ((screenWidth * pixelRatio) / width) * 0.9; // Scale image upto 90% of screen size/width
        setMapImageHeight((height / pixelRatio) * scalingFactor);
        setMapImageWidth((width / pixelRatio) * scalingFactor);
        setScalingFactor(scalingFactor);
      });
    }
  }, [mapUrl]);

  useEffect(() => {
    setBlueDotX((xPosition / pixelRatio) * scalingFactor);
    setBlueDotY((yPosition / pixelRatio) * scalingFactor);
  }, [xPosition, yPosition]);

  const toggleDialogState = () => {
    setDialogState(!dialogState);
  };

  MistSDKEvents.addListener('onMapUpdate', map => {
    // console.log(map);
    setMapUrl(map?.url);
    setMapPPM(map?.ppm);
    setMapName(map?.name);
    setShowLoader(false);
    setButtonText('Stop');
  });

  MistSDKEvents.addListener('onRelativeLocationUpdate', relativeLocation => {
    // console.log(relativeLocation);
    setXPosition(relativeLocation?.x);
    setYPosition(relativeLocation?.y);
  });

  // MapsListDelegate
  MistSDKEvents.addListener('onReceivedAllMaps', maps => {
    // console.log(maps);
  });

  // VirtualBeaconsDelegate
  MistSDKEvents.addListener('onUpdateVirtualBeaconList', virtualBeacons => {
    // console.log(virtualBeacons);
  });

  MistSDKEvents.addListener('onRangeVirtualBeacon', virtualBeacon => {
    setDialogTitle(
      'Entered in the range of virtual Beacon:' + virtualBeacon?.name,
    );
    setDialogDescription('Message:' + virtualBeacon?.message);
    setDialogState(true);
    // console.log(virtualBeacon);
  });

  // ZonesDelegate
  MistSDKEvents.addListener('onEnterZone', zone => {
    setDialogTitle('Notification');
    setDialogDescription('Entered the Zone: ' + zone?.name);
    setDialogState(true);
    // console.log(zone);
  });

  MistSDKEvents.addListener('onExitZone', zone => {
    setDialogTitle('Notification');
    setDialogDescription('Exited the Zone: ' + zone?.name);
    setDialogState(true);
    // console.log(zone);
  });

  // Start Button CallBack
  const onPress = () => {
    if (!isSdkActive) {
      setShowLoader(true);
      if (!MIST_SDK_TOKEN) {
        console.log('Please set MIST_SDK_TOKEN as env variable');
        setShowLoader(false);
        return;
      } else {
        MistSDK.startWithToken(MIST_SDK_TOKEN);
        setIsSdkActive(true);
        setButtonText('Cancel');
      }
    } else {
      MistSDK.stop();
      setButtonText('Start');
      setIsSdkActive(false);
      setMapUrl('');
      setShowLoader(false);
    }
  };

  return (
    <View style={AppStyles.container}>
      <View style={AppStyles.dialogView}>
        {dialogState ? (
          <Dialog_Box
            dialogState={dialogState}
            data={{ title: dialogTitle, description: dialogDescription }}
            onPress={toggleDialogState}
          />
        ) : null}
      </View>
      {mapUrl == '' || !isSdkActive || showLoader ? null : (
        <>
          <View>
            <Text style={AppStyles.mapNameLabel}>{mapName}</Text>
            <FloorPlan
              showBlueDot={true}
              imageUrl={mapUrl}
              mapWidth={mapImageWidth}
              mapHeight={mapImageHeight}
              blueDotX={blueDotX}
              blueDotY={blueDotY}
            />
            <Text style={AppStyles.ppmLabel}>PPM: {mapPPM}</Text>
          </View>

          {(blueDotX && blueDotY) ? (
            <Text style={AppStyles.sdkConsole}>
              Blue Dot Position: {'\n'}
              <Text style={{ fontSize: 12 }}>
                x = {(xPosition / mapPPM).toFixed(2)}m {'\n'}
                y = {(yPosition / mapPPM).toFixed(2)}m {'\n'}
              </Text>
            </Text>
          ) : null}
        </>
      )}
      {showLoader ? <Loader loaderText={loaderText} /> : null}
      <AppButton
        styles={AppStyles.startButton}
        title={buttonText}
        onPress={onPress}
      />
    </View>
  );
}
