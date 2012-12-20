TARGET_PLATFORM = android-4
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := axeldroid
LOCAL_SRC_FILES := axel.c  conf.c  conn.c  ftp.c  http.c  search.c  tcp.c  text.c com_axeldroid_Axel.cpp
LOCAL_LDLIBS :=  -L$(SYSROOT)/usr/lib -llog

include $(BUILD_SHARED_LIBRARY)
