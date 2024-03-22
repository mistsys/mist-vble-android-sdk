import {ActivityIndicator, View, Text} from 'react-native';
import {styles} from './styles';

interface loaderProps {
  loaderText?: string;
}
const Loader = (props: loaderProps) => (
  <View style={styles.loader}>
    <ActivityIndicator size="large" color="#0000ff" />
    <Text style={styles.loaderText}>{props?.loaderText}</Text>
  </View>
);

export default Loader;
