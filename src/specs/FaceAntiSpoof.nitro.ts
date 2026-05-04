import type { HybridObject } from 'react-native-nitro-modules'
import type { Frame } from 'react-native-vision-camera'

export interface FaceAntiSpoofResult {
  neuralNetworkScore: number
  laplacianScore: number
  combinedScore: number
  confidence: number
  isLive: boolean
  label: string
  error?: string
}

export interface FaceAntiSpoofPlugin extends HybridObject<{ ios: 'swift', android: 'kotlin' }> {
  /**
   * Process a camera frame for face anti-spoofing detection.
   * Returns the latest available result (may be from a previous frame
   * since processing is offloaded to a background thread).
   */
  call(frame: Frame): FaceAntiSpoofResult

  /**
   * Initialize the anti-spoofing model with the app's AssetManager.
   * Should be called before the first call() if the plugin
   * was not auto-initialized.
   */
  initialize(): boolean

  /**
   * Check whether the model is loaded and ready.
   */
  isReady(): boolean

  /**
   * Clean up native resources (executor, TFLite interpreter, etc.).
   */
  cleanup(): void
}
