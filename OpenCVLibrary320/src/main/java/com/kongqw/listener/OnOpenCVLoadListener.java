package com.kongqw.listener;

/**
 * Created by kongqingwei on 2017/5/15.
 * OnOpenCVLoadListener
 */

public interface OnOpenCVLoadListener {

    // OpenCV loaded successfully
    void onOpenCVLoadSuccess();

    // OpenCV failed to load
    void onOpenCVLoadFail();

    // OpenCVManager is not installed
    void onNotInstallOpenCVManager();
}
