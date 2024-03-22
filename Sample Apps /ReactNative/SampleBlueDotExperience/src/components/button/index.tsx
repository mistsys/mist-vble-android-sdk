import React from 'react';
import {
  View,
  Button,
  StyleProp,
  TextStyle,
  GestureResponderEvent,
} from 'react-native';

interface buttonProps {
  styles?: StyleProp<TextStyle>;
  title: string;
  onPress?: (event: GestureResponderEvent) => void;
}

export default function AppButton(props: buttonProps) {
  return (
    <View style={props?.styles}>
      <Button title={props.title} onPress={props.onPress} />
    </View>
  );
}
