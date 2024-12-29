import {useState, useEffect, useCallback, useMemo} from 'react';
import UsbDeviceModule from '../native/UVCDeviceModule';
import UsbDeviceManager, {UsbDevice} from '../native/UVCDeviceModule';

class TaskQueue {
  queue: {
    task: () => Promise<any>;
    resolve: (value: any) => void;
    reject: (error: any) => void;
  }[] = [];
  running = false;

  constructor() {
    this.queue = [];
    this.running = false;
  }

  addTask(task: () => Promise<any>) {
    return new Promise((resolve, reject) => {
      this.queue.push({task, resolve, reject});
      this.runNext();
    });
  }

  runNext() {
    if (this.running || this.queue.length === 0) {
      return;
    }

    this.running = true;

    const {task, resolve, reject} = this.queue.shift()!;

    task()
      .then(resolve)
      .catch(reject)
      .finally(() => {
        this.running = false;
        this.runNext();
      });
  }
}

function useDevices() {
  const [devices, setDevices] = useState<UsbDevice[]>([]);

  // 稳定的
  const getDevices = useCallback(async () => {
    try {
      const list = await UsbDeviceManager.getDeviceList();
      setDevices(list);
    } catch (error) {
      console.error('Failed to get device list:', error);
    }
  }, []);

  useEffect(() => {
    getDevices();

    const attached = UsbDeviceManager.addDeviceAttachedListener(device => {
      console.log('device attached:', device);
      getDevices();
    });

    const detached = UsbDeviceManager.addDeviceDetachedListener(device => {
      console.log('device detached:', device);
      getDevices();
    });

    return () => {
      attached.remove();
      detached.remove();
    };
  }, [getDevices]);

  // 排个序，以免顺序变换
  const sortedDevices = useMemo(() => {
    return devices.sort((a, b) => a.deviceId - b.deviceId);
  }, [devices]);

  return {devices: sortedDevices};
}

function useDeviceEvent(params: {
  deviceId: number;

  onAttached?: () => void;
  onDetached?: () => void;
  onConnected?: () => void;
  onDisconnected?: () => void;
  onPermissionDenied?: () => void;
}) {
  const {
    deviceId,
    onAttached,
    onDetached,
    onConnected,
    onDisconnected,
    onPermissionDenied,
  } = params;
  const [state, setState] = useState<string>('');

  useEffect(() => {
    // 设备插入事件
    const attached = UsbDeviceModule.addDeviceAttachedListener(async device => {
      if (device.deviceId === deviceId) {
        setState('attached');

        onAttached?.();
      }
    });

    // 设备断开事件
    const detached = UsbDeviceModule.addDeviceDetachedListener(device => {
      if (device.deviceId === deviceId) {
        setState('detached');

        onDetached?.();
      }
    });

    // 设备连接事件
    const connected = UsbDeviceModule.addDeviceConnectedListener(device => {
      if (device.deviceId === deviceId) {
        setState('connected');

        onConnected?.();
      }
    });

    // 设备断开事件
    const disconnected = UsbDeviceModule.addDeviceDisconnectedListener(
      device => {
        if (device.deviceId === deviceId) {
          setState('disconnected');

          onDisconnected?.();
        }
      },
    );

    // 设备权限被拒绝事件
    const permissionDenied = UsbDeviceModule.addDevicePermissionDeniedListener(
      device => {
        if (device.deviceId === deviceId) {
          setState('permissionDenied');

          onPermissionDenied?.();
        }
      },
    );

    return () => {
      attached.remove();
      detached.remove();
      connected.remove();
      disconnected.remove();
      permissionDenied.remove();
    };
  }, [
    deviceId,
    onAttached,
    onDetached,
    onConnected,
    onDisconnected,
    onPermissionDenied,
  ]);

  return {state};
}

export {TaskQueue, useDeviceEvent, useDevices};
