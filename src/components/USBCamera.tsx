import {
  requireNativeComponent,
  Text,
  View,
  ViewProps,
  findNodeHandle,
  UIManager,
} from 'react-native';
import React, {useCallback, useEffect, useMemo, useRef, useState} from 'react';
import UsbDeviceModule from "../native/UsbDeviceModule.ts";

interface USBCameraProps extends ViewProps {
  deviceId?: string;
  resolution?: {
    width: number;
    height: number;
  };
  onDeviceConnected?: (event: any) => void;
  onDeviceDisconnected?: (event: any) => void;
  onPreviewStarted?: (event: any) => void;
  onPreviewStopped?: (event: any) => void;
}

const ComponentName = 'RNUSBCameraView';

const RNUSBCameraView = requireNativeComponent(ComponentName);
const Commands = UIManager.getViewManagerConfig(ComponentName)?.Commands;

export const USBCamera: React.FC<USBCameraProps> = props => {
  const {deviceId, resolution} = props;
  const viewRef = useRef(null);
  const [isConnected, setIsConnected] = useState(false);
  const intervalRef = useRef<any>(null);
  const isConnectedRef = useRef(false);

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

  const setResolutionToNative = useCallback((res: {width: number; height: number}) => {
    const node = findNodeHandle(viewRef.current);
    if (node) {
      UIManager.dispatchViewManagerCommand(
        node,
        Commands.setResolution,
        [res]
      );
    }
  }, []);

  useEffect(() => {
    if (viewRef.current && isConnected) {
      setDeviceIdToNative(Number(deviceId));
    }
  }, [deviceId, resolution, isConnected]);

  const findAndRequestPermission = useCallback(async deviceId => {
    const deviceList = await UsbDeviceModule.getDeviceList();
    const device = deviceList.find(x => x.deviceId == Number(deviceId));
    if (!device) {
      setIsConnected(false);
    }
    const result = await UsbDeviceModule.requestPermission(
      Number(deviceId),
    );
    setIsConnected(result);
  }, []);

  useEffect(() => {
    intervalRef.current = setInterval(() => {
      if (deviceId && !isConnectedRef.current) {
        findAndRequestPermission(deviceId);
      }
    }, 2000);
    return () => {
      clearInterval(intervalRef.current);
    };
  }, [deviceId]);

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
