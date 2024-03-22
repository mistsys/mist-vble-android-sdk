import {Permission, PermissionsAndroid} from 'react-native';

const askForPermissions: Permission[] = [
  PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
  PermissionsAndroid.PERMISSIONS.BLUETOOTH_SCAN,
  PermissionsAndroid.PERMISSIONS.BLUETOOTH_CONNECT,
];

const requestPermissionsForAndroid = async (
  permissions: Permission[] = askForPermissions,
) => {
  try {
    const results: {[key: string]: string} = {};
    for (const permission of permissions) {
      const granted = await PermissionsAndroid.request(permission);
      results[permission] = granted;
    }
    return results;
  } catch (err) {
    console.warn(err);
    return {};
  }
};

export default requestPermissionsForAndroid;
