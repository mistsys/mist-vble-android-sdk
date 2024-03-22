import React from 'react';
import {View} from 'react-native';
import Dialog from 'react-native-dialog';
import {dialogStyles} from './styles';

const Dialog_Box = (props: any) => {
  return (
    <View style={dialogStyles.container}>
      <Dialog.Container
        visible={props.dialogState}
        onBackdropPress={props.onPress}>
        <Dialog.Title style={{textAlign: 'center'}}>
          {props.data?.title}
        </Dialog.Title>
        <Dialog.Description style={{textAlign: 'center'}}>
          {props.data?.description}
        </Dialog.Description>
        <Dialog.Button
          style={{color: 'blue'}}
          bold={true}
          label="Dismiss"
          onPress={props.onPress}
        />
      </Dialog.Container>
    </View>
  );
};

export default Dialog_Box;
