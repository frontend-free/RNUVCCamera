import {NativeModules, NativeEventEmitter} from 'react-native';

interface UVCDevice {
  deviceId: number;
  deviceName: string;
  productId: number;
  vendorId: number;
}

const eventEmitter = new NativeEventEmitter(NativeModules.UVCDeviceModule);

const UVCDeviceModule = {
  getDeviceList: async (): Promise<UVCDevice[]> => {
    return NativeModules.UVCDeviceModule.getDeviceList();
  },

  requestPermission: async (deviceId: number): Promise<boolean> => {
    return NativeModules.UVCDeviceModule.requestPermission(deviceId);
  },

  hasPermission: async (deviceId: number): Promise<boolean> => {
    return NativeModules.UVCDeviceModule.hasPermission(deviceId);
  },

  onDeviceAttached: (callback: (device: UVCDevice) => void) => {
    return eventEmitter.addListener('onDeviceAttached', callback);
  },

  onDeviceDetached: (
    callback: (device: Pick<UVCDevice, 'deviceId'>) => void,
  ) => {
    return eventEmitter.addListener('onDeviceDetached', callback);
  },

  onDeviceConnected: (
    callback: (device: Pick<UVCDevice, 'deviceId'>) => void,
  ) => {
    return eventEmitter.addListener('onDeviceConnected', callback);
  },

  onDeviceDisconnected: (
    callback: (device: Pick<UVCDevice, 'deviceId'>) => void,
  ) => {
    return eventEmitter.addListener('onDeviceDisconnected', callback);
  },

  onDevicePermissionDenied: (
    callback: (device: Pick<UVCDevice, 'deviceId'>) => void,
  ) => {
    return eventEmitter.addListener('onDevicePermissionDenied', callback);
  },
};

export {UVCDeviceModule};
export type {UVCDevice};
