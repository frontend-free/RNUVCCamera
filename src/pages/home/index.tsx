import React, {FC, useEffect, useRef} from 'react';
import {Header} from 'react-native/Libraries/NewAppScreen';
import {Button, ScrollView, StyleSheet, Text, ToastAndroid, View} from 'react-native';
import {USBCamera} from '../../components/USBCamera';
import {UsbSerialManager} from 'react-native-usb-serialport-for-android';
import {PERMISSIONS, request, requestMultiple} from "react-native-permissions";

interface UsbDevice {
  deviceId: string;
  vendorId: number;
  productId: number;
  serialNumber: string;
}

const Home: FC = () => {
  const [devices, setDevices] = React.useState<UsbDevice[]>([]);
  const camera1 = useRef<any>(null);


  useEffect(() => {
    let fetchDevices:any = null;
    let timer = 0;
    requestMultiple([
      PERMISSIONS.ANDROID.CAMERA,
      PERMISSIONS.ANDROID.WRITE_EXTERNAL_STORAGE,
    ]).then((status) => {

        // …
        fetchDevices = async () => {
          try {
            const deviceList = await UsbSerialManager.list();
            ToastAndroid.show('333333'+deviceList.length, 2000)
            setDevices(deviceList);
            //自动请求前两个设备的权限
            // if (deviceList.length >= 1) {
            //   await UsbSerialManager.tryRequestPermission(deviceList[0].deviceId);
            // }
            // if (deviceList.length >= 2) {
            //   await UsbSerialManager.tryRequestPermission(deviceList[1].deviceId);
            // }
          } catch (error) {
            console.error('获取设备列表失败:', error);
          }
          timer = setInterval(fetchDevices, 1000);
        }
      }
    );

    return () => clearInterval(timer);
  }, []);

  return (
    <ScrollView contentInsetAdjustmentBehavior="automatic">
      {/* 设备列表 */}
      <View style={styles.deviceList}>
        <Text style={styles.sectionTitle}>已连接的设备：</Text>
        {devices.map((device, index) => (
          <View key={index} style={styles.deviceItem}>
            <Text>设备 {index + 1}</Text>
            <Text>设备ID: {device.deviceId}</Text>
            <Text>厂商ID: {device.vendorId}</Text>
            <Text>产品ID: {device.productId}</Text>
            <Text>序列号: {device.serialNumber}</Text>
          </View>
        ))}
      </View>

      {/* 相机预览区域 */}
      <View style={styles.cameraContainer}>
        {/* 相机1 */}
        <View style={styles.cameraWrapper}>
          <Text style={styles.cameraTitle}>相机 1</Text>
          <USBCamera
            ref={camera1}
            style={styles.cameraView}
            deviceId={devices[0]?.deviceId}
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
            deviceId={devices[1]?.deviceId}
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
