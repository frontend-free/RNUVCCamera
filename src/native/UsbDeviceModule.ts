import {NativeModules, NativeEventEmitter} from 'react-native';

const {UsbDeviceModule} = NativeModules;

interface UsbDeviceModuleInterface {
  getDeviceList(): Promise<any[]>;
  addDeviceAttachedListener(callback: (device: any) => void): any;
  addDeviceDetachedListener(callback: (device: any) => void): any;
}

const eventEmitter = new NativeEventEmitter(UsbDeviceModule);

export default {
  ...UsbDeviceModule,
  addDeviceAttachedListener: (callback: (device: any) => void) => {
    return eventEmitter.addListener('usbDeviceAttached', callback);
  },
  addDeviceDetachedListener: (callback: (device: any) => void) => {
    return eventEmitter.addListener('usbDeviceDetached', callback);
  },
} as UsbDeviceModuleInterface; 