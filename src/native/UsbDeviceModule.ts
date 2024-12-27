import {NativeModules, NativeEventEmitter} from 'react-native';

const {UsbDeviceModule} = NativeModules;

export interface UsbDevice {
  deviceId: number;
  deviceName: string;
  productId: number;
  vendorId: number;
}

interface UsbDeviceModuleInterface {
  getDeviceList(): Promise<UsbDevice[]>;
  requestPermission(deviceId: number): Promise<boolean>;
  hasPermission(deviceId: number): Promise<boolean>;
  addDeviceAttachedListener(callback: (device: UsbDevice) => void): any;
  addDeviceDetachedListener(callback: (device: Pick<UsbDevice, 'deviceId'>) => void): any;
  addDeviceConnectedListener(callback: (device: Pick<UsbDevice, 'deviceId'>) => void): any;
  addDeviceDisconnectedListener(callback: (device: Pick<UsbDevice, 'deviceId'>) => void): any;
  addDevicePermissionDeniedListener(callback: (device: Pick<UsbDevice, 'deviceId'>) => void): any;
}

const eventEmitter = new NativeEventEmitter(UsbDeviceModule);

export default {
  ...UsbDeviceModule,

  getDeviceList: async (): Promise<UsbDevice[]> => {
    return UsbDeviceModule.getDeviceList();
  },

  requestPermission: async (deviceId: number): Promise<boolean> => {
    return UsbDeviceModule.requestPermission(deviceId);
  },

  hasPermission: async (deviceId: number): Promise<boolean> => {
    return UsbDeviceModule.hasPermission(deviceId);
  },

  addDeviceAttachedListener: (callback: (device: UsbDevice) => void) => {
    return eventEmitter.addListener('onDeviceAttached', callback);
  },

  addDeviceDetachedListener: (callback: (device: Pick<UsbDevice, 'deviceId'>) => void) => {
    return eventEmitter.addListener('onDeviceDetached', callback);
  },

  addDeviceConnectedListener: (callback: (device: Pick<UsbDevice, 'deviceId'>) => void) => {
    return eventEmitter.addListener('onDeviceConnected', callback);
  },

  addDeviceDisconnectedListener: (callback: (device: Pick<UsbDevice, 'deviceId'>) => void) => {
    return eventEmitter.addListener('onDeviceDisconnected', callback);
  },

  addDevicePermissionDeniedListener: (callback: (device: Pick<UsbDevice, 'deviceId'>) => void) => {
    return eventEmitter.addListener('onDevicePermissionDenied', callback);
  },
} as UsbDeviceModuleInterface;
