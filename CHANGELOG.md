# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Coming Soon
- iOS support
- Web platform support
- TensorFlow Lite model optimization
- Custom model support

## [2.0.0] - 2025-05-04

### Breaking Changes
- **VisionCamera v5 required**: Upgraded from VisionCamera v4 to v5. The plugin architecture changed from JSI FrameProcessorPlugin to Nitro HybridObject.
- **Reanimated v4 required**: Updated peer dependency from Reanimated v3 to v4.
- **Worklets package changed**: Replaced `react-native-worklets-core` with `react-native-worklets` (Software Mansion's official worklets package).
- **Babel plugin changed**: Update `babel.config.js` from `react-native-reanimated/plugin` to `react-native-worklets/plugin`.
- **Frame Processor API changed**: Consumers must now use `useFrameOutput` instead of `useFrameProcessor`, and `frame.dispose()` instead of `frame.release()`.
- **JSI bridge removed**: The old `FaceAntiSpoofJSI.cpp` and `FaceAntiSpoofFrameProcessor.kt` (v4 plugin) have been removed. The plugin is now a Nitro HybridObject.
- **Native module simplified**: Removed `install()` bridge method. Use `NitroModules.createHybridObject('FaceAntiSpoofPlugin')` to access the plugin directly.

### Added
- Nitro Module spec (`src/specs/FaceAntiSpoof.nitro.ts`) for type-safe native plugin interface.
- `HybridFaceAntiSpoofPlugin.kt`: New Nitro HybridObject implementation for VisionCamera v5.
- `FaceAntiSpoofManager.kt`: Shared singleton manager so the Nitro plugin and bridge module share one TFLite model instance.

### Changed
- `android/build.gradle`: Removed `react-native-worklets-core` dependency, added `react-native-nitro-modules`.
- `CMakeLists.txt`: Removed old `faceantispoof` JSI library target; `fastyuv` C++ library is retained.
- `index.js`: Uses `NitroModules.createHybridObject('FaceAntiSpoofPlugin')` instead of `VisionCameraProxy.initFrameProcessorPlugin()`.
- `README.md`: Updated all examples and requirements for v5 ecosystem.

### Migration Guide (1.x -> 2.0.0)
1. Upgrade dependencies:
   ```bash
   npm install react-native-vision-camera@^5.0.0 react-native-reanimated@^4.0.0 react-native-worklets@^0.8.0 react-native-vision-camera-worklets
   ```
2. Update `babel.config.js`:
   ```js
   plugins: ['react-native-worklets/plugin']
   ```
3. Replace `useFrameProcessor` with `useFrameOutput` in your camera component.
4. Replace `frame.release()` with `frame.dispose()` inside the worklet.
5. Replace `runOnJS` from Reanimated with `scheduleOnRN` from `react-native-worklets`.

## [1.0.18] - 2024-03-24

### Added
- Comprehensive README with complete API documentation
- Advanced usage examples with full feature set
- TypeScript type definitions for all public APIs
- CHANGELOG.md for version tracking
- Contributing guidelines
- Performance optimization tips
- Troubleshooting guide

### Changed
- Updated package.json with better metadata
- Improved npm package visibility
- Enhanced documentation structure
- Better error handling examples

### Fixed
- Better error messages for initialization failures
- Improved frame release handling

## [1.0.17] - 2024-03-20

### Added
- Blink detection for enhanced liveness verification
- Face centering validation
- Customizable anti-spoofing thresholds

### Improved
- Performance optimization with batched updates
- Memory management for frame processing
- GPU acceleration support

## [1.0.16] - 2024-03-15

### Added
- Face stability tracking
- Consecutive live frame detection
- Laplacian score calculation

### Fixed
- Frame release memory leak
- State reset on component unmount

## [1.0.0] - 2024-03-01

### Added
- Initial release
- Face anti-spoofing detection
- TensorFlow Lite integration with GPU acceleration
- YUV frame processing optimization
- Real-time frame processor
- Confidence scoring
- Print attack detection
- Display attack detection
- Mask attack detection
- Replay attack detection

### Features
- High-performance real-time detection
- Multi-model ensemble for better accuracy
- Optimized for React Native Vision Camera
- Support for Android platform

---

## Development Versions

### Version History

#### v1.0.18 (Latest)
- Full npm publishing support
- Complete documentation
- TypeScript support
- Better examples

#### v1.0.0 - v1.0.17
- Initial development
- Core functionality
- Bug fixes and optimizations

## Versioning

We use [Semantic Versioning](https://semver.org/):
- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes and improvements

## Future Roadmap

### Phase 2 (Next Release)
- [ ] iOS native implementation
- [ ] Improved accuracy metrics
- [ ] Custom model support
- [ ] Advanced analytics

### Phase 3 (Later)
- [ ] Web platform support
- [ ] Server-side verification
- [ ] Cross-platform examples
- [ ] Integration templates

## Migration Guides

### Upgrading from 1.0.0 to 1.0.18
- Install peer dependencies if not already installed
- No breaking API changes
- Update example code for enhanced features

### Future Breaking Changes
None planned for the 1.0.x series.

## Support & Issues

- Report bugs: [GitHub Issues](https://github.com/dpraful/react-native-vision-camera-spoof-detector/issues)
- Feature requests: [GitHub Discussions](https://github.com/dpraful/react-native-vision-camera-spoof-detector/discussions)

## Contributing

Please see [CONTRIBUTING.md](./CONTRIBUTING.md) for details on:
- How to submit issues
- How to submit pull requests
- Development setup
- Code standards

## License

JESCON TECHNOLOGIES PVT LTD License - See [LICENSE](./LICENSE) file for details

---

Last Updated: March 24, 2024
Maintained by: JESCON TECHNOLOGIES PVT LTD
