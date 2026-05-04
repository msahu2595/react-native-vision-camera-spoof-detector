cmake_minimum_required(VERSION 3.18)
project(FaceAntiSpoofDetector)

set(SOURCE_FILES
    FastYuvJni.cpp
    fast_yuv.cpp
    JNIOnLoad.cpp
)

add_library(FaceAntiSpoofDetector SHARED ${SOURCE_FILES})

# Enable NEON SIMD optimizations for ARM architectures
if(CMAKE_SYSTEM_PROCESSOR MATCHES "armv7" OR CMAKE_SYSTEM_PROCESSOR MATCHES "aarch64")
    # ARM NEON optimization flags
    target_compile_options(FaceAntiSpoofDetector PRIVATE
        -fPIC
        -O3
    )

    # ARM-specific NEON enablement (clang doesn't support -march=native for cross-compilation)
    if(CMAKE_SYSTEM_PROCESSOR MATCHES "armv7")
        # ARMv7 with NEON (32-bit ARM)
        target_compile_options(FaceAntiSpoofDetector PRIVATE -march=armv7-a -mfpu=neon -mfloat-abi=softfp)
    elseif(CMAKE_SYSTEM_PROCESSOR MATCHES "aarch64")
        # ARMv8 with full SIMD support (64-bit ARM)
        target_compile_options(FaceAntiSpoofDetector PRIVATE -march=armv8-a)
    endif()
endif()

# Always add libyuv include dir so headers like libyuv/convert.h are found.
target_include_directories(FaceAntiSpoofDetector PRIVATE ${CMAKE_SOURCE_DIR}/libyuv/include)

# Prefer using libyuv subproject if available.
if (EXISTS ${CMAKE_SOURCE_DIR}/libyuv/CMakeLists.txt)
    add_subdirectory(libyuv)
    # Link against the libyuv target produced by that CMake. The lib creates
    # targets named 'yuv' (static) and 'yuv_shared' (shared) whose output
    # library name is 'libyuv'. Prefer the shared target when available.
    if (TARGET yuv_shared)
        target_link_libraries(FaceAntiSpoofDetector PRIVATE yuv_shared)
    elseif (TARGET yuv)
        target_link_libraries(FaceAntiSpoofDetector PRIVATE yuv)
    endif()
endif()

# Support Android 15+ 16KB page size
# https://developer.android.com/guide/practices/page-sizes
target_link_options(FaceAntiSpoofDetector PRIVATE "-Wl,-z,max-page-size=16384")

# Android log
find_library(log-lib log)
target_link_libraries(FaceAntiSpoofDetector PRIVATE ${log-lib})

# Nitrogen autolinking - includes generated C++ sources, headers, and links Nitro/FBJNI/ReactAndroid
get_filename_component(NITROGEN_DIR "${CMAKE_SOURCE_DIR}/../../../nitrogen/generated/android" ABSOLUTE)
if (EXISTS ${NITROGEN_DIR}/FaceAntiSpoofDetector+autolinking.cmake)
    include(${NITROGEN_DIR}/FaceAntiSpoofDetector+autolinking.cmake)
else()
    message(WARNING "Nitrogen autolinking cmake not found at ${NITROGEN_DIR}/FaceAntiSpoofDetector+autolinking.cmake - run 'npx nitrogen' first")
endif()


