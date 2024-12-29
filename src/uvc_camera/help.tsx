import {useState, useEffect, useCallback, useMemo} from 'react';
import {UVCDeviceModule} from './uvc_device_module';
import {UVCDevice} from './uvc_device_module';

/** 任务调度 */
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

/** 获取设备列表 */
function useDevices() {
  const [devices, setDevices] = useState<UVCDevice[]>([]);

  // 稳定的
  const getDevices = useCallback(async () => {
    try {
      const list = await UVCDeviceModule.getDeviceList();
      setDevices(list);
    } catch (error) {
      console.error('Failed to get device list:', error);
    }
  }, []);

  useEffect(() => {
    getDevices();

    const attached = UVCDeviceModule.onDeviceAttached(device => {
      console.log('device attached:', device);
      getDevices();
    });

    const detached = UVCDeviceModule.onDeviceDetached(device => {
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

/** 设备事件监听 */
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
    const attached = UVCDeviceModule.onDeviceAttached(async device => {
      if (device.deviceId === deviceId) {
        setState('attached');

        onAttached?.();
      }
    });

    // 设备断开事件
    const detached = UVCDeviceModule.onDeviceDetached(device => {
      if (device.deviceId === deviceId) {
        setState('detached');

        onDetached?.();
      }
    });

    // 设备连接事件
    const connected = UVCDeviceModule.onDeviceConnected(device => {
      if (device.deviceId === deviceId) {
        setState('connected');

        onConnected?.();
      }
    });

    // 设备断开事件
    const disconnected = UVCDeviceModule.onDeviceDisconnected(device => {
      if (device.deviceId === deviceId) {
        setState('disconnected');

        onDisconnected?.();
      }
    });

    // 设备权限被拒绝事件
    const permissionDenied = UVCDeviceModule.onDevicePermissionDenied(
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
