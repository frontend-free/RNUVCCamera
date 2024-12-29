import {
  requireNativeComponent,
  Text,
  View,
  ViewProps,
  findNodeHandle,
  UIManager,
} from 'react-native';
import React, {useCallback, useEffect, useRef, useState} from 'react';
import UsbDeviceModule from '../native/UsbDeviceModule';

// 全局权限请求池
interface PermissionRequest {
  deviceId: number;
  resolve: (value: boolean) => void;
  reject: (error: any) => void;
}

class PermissionRequestPool {
  private static instance: PermissionRequestPool;
  private requestQueue: PermissionRequest[] = [];
  private isProcessing = false;

  static getInstance() {
    if (!PermissionRequestPool.instance) {
      PermissionRequestPool.instance = new PermissionRequestPool();
    }
    return PermissionRequestPool.instance;
  }

  async addRequest(deviceId: number): Promise<boolean> {
    return new Promise((resolve, reject) => {
      this.requestQueue.push({deviceId, resolve, reject});
      this.processNextRequest();
    });
  }

  private async processNextRequest() {
    if (this.isProcessing || this.requestQueue.length === 0) return;

    this.isProcessing = true;
    const request = this.requestQueue.shift()!;

    try {
      const deviceList = await UsbDeviceModule.getDeviceList();
      const device = deviceList.find(x => x.deviceId === request.deviceId);
      if (device) {
        const granted = await UsbDeviceModule.requestPermission(
          device.deviceId,
        );
        request.resolve(granted);
      } else {
        request.resolve(false);
      }
    } catch (error) {
      request.reject(error);
    } finally {
      this.isProcessing = false;
      this.processNextRequest();
    }
  }
}

interface USBCameraProps extends ViewProps {
  deviceId?: string;
}

const ComponentName = 'RNUSBCameraView';

const RNUSBCameraView = requireNativeComponent(ComponentName);
const Commands = UIManager.getViewManagerConfig(ComponentName)?.Commands;

export const USBCamera: React.FC<USBCameraProps> = props => {
  const {deviceId} = props;
  const viewRef = useRef(null);
  const [isConnected, setIsConnected] = useState(false);
  const [state, setState] = useState<any>(-1);

  const setDeviceIdToNative = useCallback((id: number) => {
    const node = findNodeHandle(viewRef.current);
    if (node) {
      UIManager.dispatchViewManagerCommand(node, Commands.setDeviceId, [id]);
    }
  }, []);

  useEffect(() => {
    if (viewRef.current && isConnected) {
      setDeviceIdToNative(Number(deviceId));
    }
  }, [deviceId, isConnected, setDeviceIdToNative]);

  const requestDevicePermission = useCallback(async (deviceId: number) => {
    try {
      const granted = await PermissionRequestPool.getInstance().addRequest(
        deviceId,
      );
      setIsConnected(granted);
    } catch (error) {
      console.error('Failed to request device permission:', error);
    }
  }, []);

  useEffect(() => {
    if (!deviceId) {
      //这里很重要，下面的回调可能来不及
      setIsConnected(false);
      return;
    }

    const attachSubscription = UsbDeviceModule.addDeviceAttachedListener(
      device => {
        if (device.deviceId === Number(deviceId)) {
          requestDevicePermission(device.deviceId);
          setState(0);
        }
      },
    );

    const detachSubscription = UsbDeviceModule.addDeviceDetachedListener(
      device => {
        if (device.deviceId === Number(deviceId)) {
          setIsConnected(false);
          setState(1);
        }
      },
    );

    // 设备连接事件
    const connectedSubscription = UsbDeviceModule.addDeviceConnectedListener(
      device => {
        if (device.deviceId === Number(deviceId)) {
          setIsConnected(true);
          setState(2);
        }
      },
    );

    // 设备断开事件
    const disconnectedSubscription =
      UsbDeviceModule.addDeviceDisconnectedListener(device => {
        if (device.deviceId === Number(deviceId)) {
          setIsConnected(false);
          setState(3);
        }
      });

    // 设备权限被拒绝事件
    const permissionDeniedSubscription =
      UsbDeviceModule.addDevicePermissionDeniedListener(device => {
        if (device.deviceId === Number(deviceId)) {
          setIsConnected(false);
          setState(4);
        }
      });

    return () => {
      attachSubscription.remove();
      detachSubscription.remove();
      connectedSubscription.remove();
      disconnectedSubscription.remove();
      permissionDeniedSubscription.remove();
    };
  }, [deviceId, requestDevicePermission]);

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
        {deviceId} {state}
      </Text>
    </View>
  );
};
