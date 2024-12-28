import {
  requireNativeComponent,
  Text,
  View,
  ViewProps,
  findNodeHandle,
  UIManager,
} from 'react-native';
import React, {useCallback, useEffect, useRef, useState} from 'react';
import UsbDeviceModule, {UsbDevice} from "../native/UsbDeviceModule";

interface USBCameraProps extends ViewProps {
  deviceId?: string;
  resolution?: {
    width: number;
    height: number;
  };
  onDeviceConnected?: (event: Pick<UsbDevice, 'deviceId'>) => void;
  onDeviceDisconnected?: (event: Pick<UsbDevice, 'deviceId'>) => void;
  onPreviewStarted?: (event: any) => void;
  onPreviewStopped?: (event: any) => void;
}

const ComponentName = 'RNUSBCameraView';

const RNUSBCameraView = requireNativeComponent(ComponentName);
const Commands = UIManager.getViewManagerConfig(ComponentName)?.Commands;

export const USBCamera: React.FC<USBCameraProps> = props => {
  const {deviceId, resolution, onDeviceConnected, onDeviceDisconnected} = props;
  const viewRef = useRef(null);
  const [isConnected, setIsConnected] = useState(false);

  const setDeviceIdToNative = useCallback((id: number) => {
    const node = findNodeHandle(viewRef.current);
    if (node) {
      UIManager.dispatchViewManagerCommand(
        node,
        Commands.setDeviceId,
        [id]
      );
    }
  }, []);

  useEffect(() => {
    if (viewRef.current && isConnected) {
      setDeviceIdToNative(Number(deviceId));
    }
  }, [deviceId, resolution, isConnected, setDeviceIdToNative]);

  useEffect(() => {
    if (!deviceId) return;

    // 检查初始权限状态
    const checkInitialPermission = async () => {
      const hasPermission = await UsbDeviceModule.hasPermission(Number(deviceId));
      if (hasPermission) {
        setIsConnected(true);
      }
    };
    checkInitialPermission();

    // 设备连接事件
    const connectedSubscription = UsbDeviceModule.addDeviceConnectedListener(
      (device) => {
        if (device.deviceId === Number(deviceId)) {
          setIsConnected(true);
          onDeviceConnected?.(device);
        }
      }
    );

    // 设备断开事件
    const disconnectedSubscription = UsbDeviceModule.addDeviceDisconnectedListener(
      (device) => {
        if (device.deviceId === Number(deviceId)) {
          setIsConnected(false);
          onDeviceDisconnected?.(device);
        }
      }
    );

    // 设备权限被拒绝事件
    const permissionDeniedSubscription = UsbDeviceModule.addDevicePermissionDeniedListener(
      (device) => {
        if (device.deviceId === Number(deviceId)) {
          setIsConnected(false);
        }
      }
    );

    return () => {
      connectedSubscription.remove();
      disconnectedSubscription.remove();
      permissionDeniedSubscription.remove();
    };
  }, [deviceId, onDeviceConnected, onDeviceDisconnected]);

  // 自动请求权限
  useEffect(() => {
    if (!deviceId || isConnected) return;

    const requestDevicePermission = async () => {
      try {
        const deviceList = await UsbDeviceModule.getDeviceList();
        const device = deviceList.find(x => x.deviceId === Number(deviceId));
        if (device) {
          const granted = await UsbDeviceModule.requestPermission(device.deviceId);
          if (!granted) {
            setIsConnected(false);
          }
        }
      } catch (error) {
        console.error('Failed to request device permission:', error);
      }
    };

    const timer = setInterval(requestDevicePermission, 2000);
    return () => clearInterval(timer);
  }, [deviceId, isConnected]);

  return (
    <View
      style={[props.style, {justifyContent: 'center', alignItems: 'center'}]}>
      {isConnected ? (
        <RNUSBCameraView ref={viewRef} style={props.style} />
      ) : (
        <Text style={{position: 'absolute', color: 'white'}}>等待连接</Text>
      )}
      <Text
        style={{
          position: 'absolute',
          left: 5,
          top: 5,
          color: 'red',
          fontWeight: '500',
        }}>
        {deviceId}
      </Text>
    </View>
  );
};
