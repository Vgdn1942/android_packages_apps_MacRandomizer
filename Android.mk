LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_PACKAGE_NAME := MacRandomizer
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_PROGUARD_ENABLED := disabled
LOCAL_DEX_PREOPT := false

include $(BUILD_PACKAGE)

