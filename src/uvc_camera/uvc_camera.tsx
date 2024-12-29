import {
  requireNativeComponent,
  Text,
  View,
  findNodeHandle,
  UIManager,
  StyleSheet,
  ToastAndroid,
} from 'react-native';
import React, {useCallback, useRef} from 'react';
import UsbDeviceModule from '../native/UVCDeviceModule';
import {TaskQueue, useDevices} from './help';
import {useDeviceEvent} from './help';

const isDev = __DEV__;

const taskQueue = new TaskQueue();

const ComponentName = 'UVCCameraView';
const UVCCameraView = requireNativeComponent(ComponentName);
const Commands = UIManager.getViewManagerConfig(ComponentName)?.Commands;

const BaseUVCCamera = ({deviceId}: {deviceId: number}) => {
  const viewRef = useRef(null);

  const handleAttached = useCallback(async () => {
    try {
      // 获取权限，排队执行
      const isGranted = await taskQueue.addTask(() =>
        UsbDeviceModule.requestPermission(deviceId),
      );

      // 权限请求成功后，设置设备ID
      if (isGranted) {
        const node = findNodeHandle(viewRef.current);
        if (node) {
          ToastAndroid.show('设备已连接' + deviceId, ToastAndroid.SHORT);
          UIManager.dispatchViewManagerCommand(node, Commands.setDeviceId, [
            deviceId,
          ]);
        }
      }
    } catch (error) {
      console.error('Failed to request device permission:', error);
    }
  }, [deviceId]);

  const {state} = useDeviceEvent({
    deviceId,
    onAttached: handleAttached,
  });

  return (
    <View style={styles.full}>
      <UVCCameraView ref={viewRef} />
      {isDev && <Text style={styles.rightTop}>{state}</Text>}
    </View>
  );
};

const UVCCamera = ({deviceId}: {deviceId?: number}) => {
  return (
    <View style={styles.full}>
      {deviceId ? (
        <BaseUVCCamera
          key={deviceId}
          deviceId={deviceId}
          style={{width: 320, height: 240}}
        />
      ) : (
        <Text style={styles.text}>等待连接</Text>
      )}
      {isDev && <Text style={styles.leftTop}>deviceId:{deviceId}</Text>}
    </View>
  );
};

const UVCCameraWithIndex = ({index}: {index: number}) => {
  const {devices} = useDevices();

  return (
    <View style={styles.full}>
      <UVCCamera deviceId={devices[index]?.deviceId} />
    </View>
  );
};

const styles = StyleSheet.create({
  full: {
    width: '100%',
    height: '100%',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    position: 'relative',
  },
  text: {
    color: 'white',
  },
  leftTop: {
    position: 'absolute',
    left: 5,
    top: 5,
    color: 'red',
  },
  rightTop: {
    position: 'absolute',
    right: 5,
    top: 5,
    color: 'red',
  },
});

export {UVCCamera, UVCCameraWithIndex};
