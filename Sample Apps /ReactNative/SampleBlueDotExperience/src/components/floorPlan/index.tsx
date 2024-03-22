import React, {useState} from 'react';
import {Image, ImageStyle, StyleProp, View} from 'react-native';
import BlueDot from '../blueDot';

interface floorPlanProps {
  imageUrl: string;
  mapWidth: number;
  mapHeight: number;
  styles?: StyleProp<ImageStyle>;
  showBlueDot: boolean;
  blueDotX?: number;
  blueDotY?: number;
}

const FloorPlan = (props: floorPlanProps) => {
  const [hasImageLoaded, setHasImageLoaded] = useState(false);

  return (
    <View>
      <Image
        style={props?.styles}
        source={{
          uri: props?.imageUrl,
        }}
        alt="map image placeholder"
        height={props?.mapHeight}
        width={props?.mapWidth}
        onLoad={() => setHasImageLoaded(true)}
      />
      {!props?.showBlueDot || !hasImageLoaded ? null : (
        <BlueDot x={props?.blueDotX} y={props?.blueDotY} />
      )}
    </View>
  );
};

export default FloorPlan;
