import React from 'react';
import {View} from 'react-native';
import {blueDotStyles} from './styles';

interface blueDotProps {
  x?: number;
  y?: number;
}

const BlueDot = (props: blueDotProps) => {
  const xOffset = blueDotStyles?.outerCircle?.width / 2; // offset the bluedot so that its center is exactly at the right coordinate irrespective of the bluedot dimensions
  const yOffset = blueDotStyles?.outerCircle?.height / 2;
  const x = (props.x! | 0) - xOffset;
  const y = (props.y! | 0) - yOffset;

  return (
    <View style={[blueDotStyles.outerCircle, {top: y, left: x}]}>
      <View style={blueDotStyles.innerCircle} />
    </View>
  );
};

export default BlueDot;
