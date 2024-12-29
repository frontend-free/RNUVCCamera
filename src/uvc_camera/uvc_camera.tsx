import {
  requireNativeComponent,
  Text,
  View,
  findNodeHandle,
  UIManager,
  StyleSheet,
  ToastAndroid,
} from 'react-native';
import React, {useCallback, useLayoutEffect, useRef, useState} from 'react';
import {UVCDeviceModule} from './uvc_device_module';
import {TaskQueue, useDevices} from './help';
import {useDeviceEvent} from './help';

const isDev = __DEV__;

const taskQueue = new TaskQueue();

const ComponentName = 'UVCCameraView';
const UVCCameraView = requireNativeComponent(ComponentName);
const Commands = UIManager.getViewManagerConfig(ComponentName)?.Commands;

const BaseUVCCamera = ({deviceId}: {deviceId: number}) => {
  const viewRef = useRef<View>(null);
  const cameraViewRef = useRef(null);

  const handleAttached = useCallback(async () => {
    try {
      // 获取权限，排队执行
      const isGranted = await taskQueue.addTask(() =>
        UVCDeviceModule.requestPermission(deviceId),
      );

      // 权限请求成功后，设置设备ID，即可显示摄像头预览
      if (isGranted) {
        const node = findNodeHandle(cameraViewRef.current);
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

  // 获取预览大小
  const [viewSize, setViewSize] = useState({width: 0, height: 0});
  useLayoutEffect(() => {
    viewRef.current?.measure((ox, oy, width, height) => {
      setViewSize({width, height});
    });
  }, []);

  return (
    <View ref={viewRef} style={styles.full}>
      <UVCCameraView
        ref={cameraViewRef}
        // @ts-ignore
        style={{width: viewSize.width, height: viewSize.height}}
      />
      {isDev && <Text style={styles.rightTop}>{state}</Text>}
    </View>
  );
};

const UVCCamera = ({deviceId}: {deviceId?: number}) => {
  return (
    <View style={styles.full}>
      {deviceId ? (
        <BaseUVCCamera key={deviceId} deviceId={deviceId} />
      ) : (
        <Text style={styles.text}>等待连接</Text>
      )}
      {isDev && <Text style={styles.leftTop}>deviceId:{deviceId}</Text>}
    </View>
  );
};

/** 通过位置来调用，更便捷 */
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
