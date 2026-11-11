cmake_minimum_required(VERSION 3.22.1)
project(zyon_virtual_pc VERSION 16.0.0 LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

if(ANDROID)
    add_definitions(-DANDROID -DZYON_ANDROID)
endif()

include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}/include
    ${CMAKE_CURRENT_SOURCE_DIR}
)

file(GLOB_RECURSE ZYON_SOURCES
    "${CMAKE_CURRENT_SOURCE_DIR}/application/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/vm_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/vm_instance/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/qemu_backend/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/qmp_client/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/cpu_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/memory_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/machine_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/chipset_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/firmware_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/boot_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/disk_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/storage_backend/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/image_converter/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/snapshot_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/backup_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/display_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/graphics_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/input_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/keyboard_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/mouse_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/network_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/audio_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/usb_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/peripheral_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/shared_folders/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/clipboard_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/power_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/security_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/performance_monitor/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/logging/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/diagnostics/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/automation/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/plugin_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/configuration_manager/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/api_server/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/user_interface/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/android_bridge/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/native_renderer/*.cpp"
    "${CMAKE_CURRENT_SOURCE_DIR}/jni_bridge.cpp"
)

add_library(zyon_core SHARED ${ZYON_SOURCES})

target_include_directories(zyon_core PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/include)

find_library(log-lib log)
find_library(android-lib android)
find_library(EGL-lib EGL)
find_library(GLESv3-lib GLESv3)
find_library(OpenSLES-lib OpenSLES)
find_library(jnigraphics-lib jnigraphics)
find_library(dl-lib dl)

target_link_libraries(zyon_core
    ${log-lib}
    ${android-lib}
    ${EGL-lib}
    ${GLESv3-lib}
    ${OpenSLES-lib}
    ${jnigraphics-lib}
    ${dl-lib}
)
