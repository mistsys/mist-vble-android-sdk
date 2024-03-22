import {StyleSheet} from 'react-native';

export const AppStyles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  sdkConsole: {
    fontSize: 14,
    textAlign: 'center',
    marginTop: 60,
    marginBottom: 0,
  },
  mapNameLabel: {
    fontSize: 12,
    fontWeight: 'bold',
    alignSelf: 'center',
    marginBottom: 20,
    marginTop: -100,
  },
  ppmLabel: {fontSize: 12, alignSelf: 'flex-end'},
  dialogView: {position: 'absolute'},
  startButton: {position: 'absolute', bottom: 100},
});
