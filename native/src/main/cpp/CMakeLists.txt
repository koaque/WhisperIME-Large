# CMakeLists.txt for WhisperIME Native Module
cmake_minimum_required(VERSION 3.22.1)

project("whisperimenat")

# Add whisper.cpp integration here when ready
# For now, create a basic JNI wrapper

add_library(whisperimenat SHARED
        whisper_jni.cpp
)

target_link_libraries(whisperimenat
        android
        log
)