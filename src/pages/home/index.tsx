import React, {FC, useEffect} from 'react';
import {StyleSheet, Text, View} from 'react-native';
import {UVCCameraWithIndex} from '@yz1311/react-native-uvc-camera';
import {PERMISSIONS, requestMultiple} from 'react-native-permissions';

const Home: FC = () => {
  // 申请权限
  useEffect(() => {
    requestMultiple([
      PERMISSIONS.ANDROID.CAMERA,
      PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE,
    ]);
  }, []);

  return (
    <View>
      <Text>相机 1</Text>
      <View style={styles.cameraView}>
        <UVCCameraWithIndex index={0} />
      </View>
      <Text>相机 2</Text>
      <View style={styles.cameraView}>
        <UVCCameraWithIndex index={1} />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  cameraView: {
    width: '100%',
    height: 240,
    backgroundColor: '#000',
  },
});

export default Home;
