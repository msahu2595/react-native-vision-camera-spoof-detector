import { NativeModules } from 'react-native';
import { NitroModules } from 'react-native-nitro-modules';

const { FaceAntiSpoof } = NativeModules;

// Create the Nitro HybridObject plugin instance (VisionCamera v5)
let _faceAntiSpoofPlugin = null;
try {
  _faceAntiSpoofPlugin = NitroModules.createHybridObject('FaceAntiSpoofPlugin');
} catch (e) {
  console.warn('[FaceAntiSpoof] Failed to create Nitro plugin:', e);
}

// Worklet called by VisionCamera v5 useFrameOutput
export const faceAntiSpoofFrameProcessor = function (frame) {
  'worklet';
  try {
    if (!_faceAntiSpoofPlugin) {
      console.warn('[FaceAntiSpoof] Plugin not initialized');
      return null;
    }
    
    if (typeof _faceAntiSpoofPlugin.call !== 'function') {
      console.warn('[FaceAntiSpoof] Plugin call method not available');
      return null;
    }
    
    if (!frame) {
      console.warn('[FaceAntiSpoof] Frame is null or undefined');
      return null;
    }
    
    const result = _faceAntiSpoofPlugin.call(frame);
    
    // Validate result structure
    if (result && typeof result === 'object') {
      return result;
    }
    
    return null;
  } catch (err) {
    console.error('[FaceAntiSpoof] Error in frame processor:', err);
    return null;
  }
};

export default FaceAntiSpoof;

// Utility functions
export const isFaceAntiSpoofAvailable = () => {
  return !!FaceAntiSpoof;
};

export const initializeFaceAntiSpoof = async () => {
  try {
    if (!FaceAntiSpoof) {
      throw new Error('FaceAntiSpoof module not available on this platform');
    }

    // Initialize the Nitro plugin (v5)
    if (_faceAntiSpoofPlugin && typeof _faceAntiSpoofPlugin.initialize === 'function') {
      _faceAntiSpoofPlugin.initialize();
    }

    const result = await FaceAntiSpoof.initialize();
    const status = await FaceAntiSpoof.checkModelStatus();

    if (!result || !status) {
      throw new Error('Initialization returned empty result');
    }

    const isSuccess = result && status.pluginAvailable;
    console.log('[FaceAntiSpoof] Initialization result:', isSuccess);
    return isSuccess;
  } catch (error) {
    console.error('[FaceAntiSpoof] Initialization failed:', error);
    throw error;
  }
};
