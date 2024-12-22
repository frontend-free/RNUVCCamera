import { requireNativeComponent, ViewProps } from 'react-native';

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

const RNUSBCameraView = requireNativeComponent('RNUSBCameraView');

export const USBCamera: React.FC<USBCameraProps> = (props) => {
  return <RNUSBCameraView {...props} />;
}; 