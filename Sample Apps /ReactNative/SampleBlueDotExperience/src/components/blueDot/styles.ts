import { StyleSheet } from 'react-native';

export const blueDotStyles = StyleSheet.create({
    outerCircle: {
        position: 'absolute',
        width: 25,
        height: 25,
        borderRadius: 50,
        backgroundColor: 'rgba(18, 151, 254, 0.13)',
        justifyContent: 'center',
        alignItems: 'center',
    },
    innerCircle: {
        width: 7,
        height: 7,
        borderRadius: 25,
        backgroundColor: 'rgba(18, 151, 254, 1)',
    },
});