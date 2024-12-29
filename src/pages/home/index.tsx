import React, {FC, useEffect, useRef} from 'react';
import {Button, ScrollView, StyleSheet, Text, ToastAndroid, View} from 'react-native';
import {USBCamera} from '../../components/USBCamera';
import {PERMISSIONS, requestMultiple} from "react-native-permissions";
import UsbDeviceManager, {UsbDevice} from '../../native/UVCDeviceModule.ts';


const Home: FC = () => {
  const [devices, setDevices] = React.useState<UsbDevice[]>([]);
  const [hasCameraPermission, setHasCameraPermission] = React.useState(false);
  const camera1 = useRef<any>(null);
  useEffect(() => {
    // 监听设备插入
    const attachSubscription = UsbDeviceManager.addDeviceAttachedListener(device => {
      console.log('设备插入:', device);
      getDevices();
    });

    // 监听设备拔出
    const detachSubscription = UsbDeviceManager.addDeviceDetachedListener(device => {
      console.log('设备拔出:', device);
      getDevices();
    });

    // 获取设备列表
    const getDevices = async () => {
      try {
        const devices = await UsbDeviceManager.getDeviceList();
        setDevices(devices)
        console.log('设备列表:', devices);
      } catch (error) {
        console.error('获取设备列表失败:', error);
      }
    };

    getDevices();

    return () => {
      attachSubscription.remove();
      detachSubscription.remove();
    };
  }, []);

  useEffect(() => {
    requestMultiple([
      PERMISSIONS.ANDROID.CAMERA,
      PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE,
    ]).then((status) => {
        if(status[PERMISSIONS.ANDROID.CAMERA] === 'granted'){
            setHasCameraPermission(true);
        }
      }
    );
  }, []);

  return (
    <ScrollView contentInsetAdjustmentBehavior="automatic">
      {/* 相机预览区域 */}
      <View style={styles.cameraContainer}>
        {/* 相机1 */}
        <View style={styles.cameraWrapper}>
          <Text style={styles.cameraTitle}>相机 1</Text>
          <USBCamera
            ref={camera1}
            style={styles.cameraView}
            deviceId={devices[0]?.deviceId || ''}
            resolution={{width: 640, height: 600}}
            onDeviceConnected={(event) => console.log('相机1已连接', event)}
            onDeviceDisconnected={(event) => console.log('相机1已断开', event)}
            onPreviewStarted={(event) => console.log('相机1开始预览', event)}
            onPreviewStopped={(event) => console.log('相机1停止预览', event)}
          />
        </View>
        <View style={styles.cameraWrapper}>
          <Text style={styles.cameraTitle}>相机 2</Text>
          <USBCamera
            style={styles.cameraView}
            deviceId={devices[1]?.deviceId || ''}
            resolution={{width: 640, height: 600}}
            onDeviceConnected={(event) => console.log('相机1已连接', event)}
            onDeviceDisconnected={(event) => console.log('相机1已断开', event)}
            onPreviewStarted={(event) => console.log('相机1开始预览', event)}
            onPreviewStopped={(event) => console.log('相机1停止预览', event)}
          />
        </View>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  deviceList: {
    padding: 16,
  },
  deviceItem: {
    padding: 10,
    marginVertical: 5,
    backgroundColor: '#f5f5f5',
    borderRadius: 5,
  },
  sectionTitle: {
    fontSize: 20,
    fontWeight: '600',
    marginBottom: 10,
  },
  cameraContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-around',
    padding: 16,
  },
  cameraWrapper: {
    alignItems: 'center',
    marginBottom: 20,
  },
  cameraTitle: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 10,
  },
  cameraView: {
    width: 320,
    height: 240,
    backgroundColor: '#000',
  },
});

export default Home;
