package com.kongqw;

import android.graphics.Bitmap;
import android.util.Log;

import com.kongqw.listener.OnCalcBackProjectListener;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Created by kongqingwei on 2017/4/26.
 * ObjectTracker
 */
public class ObjectTracker {

    private static final String TAG = "ObjectTracker";
    private OnCalcBackProjectListener mOnCalcBackProjectListener;
    private Mat hsv, hue, mask, prob;
    private Rect trackRect;
    private RotatedRect rotatedRect;
    private Mat hist;
    private List<Mat> hsvList, hueList;
    private MatOfFloat ranges;

    public ObjectTracker() {
        hist = new Mat();
        trackRect = new Rect();
        rotatedRect = new RotatedRect();
        hsvList = new Vector<>();
        hueList = new Vector<>();

        ranges = new MatOfFloat(0f, 256f);
    }

    public Bitmap createTrackedObject(Mat rgba, Rect region) {

        hsv = new Mat(rgba.size(), CvType.CV_8UC3);
        mask = new Mat(rgba.size(), CvType.CV_8UC1);
        hue = new Mat(rgba.size(), CvType.CV_8UC1);
        prob = new Mat(rgba.size(), CvType.CV_8UC1);

        //Convert rgb camera frames into hsv space
        rgba2Hsv(rgba);

        updateHueImage();

        Mat tempMask = mask.submat(region);

        // MatOfFloat ranges = new MatOfFloat(0f, 256f);
        // MatOfInt histSize = new MatOfInt(25);
        MatOfInt histSize = new MatOfInt(255);

        List<Mat> images = Collections.singletonList(hueList.get(0).submat(region));
        Imgproc.calcHist(images, new MatOfInt(0), tempMask, hist, histSize, ranges);

        Bitmap bitmap = Bitmap.createBitmap(hue.width(), hue.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(hue, bitmap);

        // Normalize the array of hist matrix, normalized to 0~255
        Core.normalize(hist, hist, 0, 255, Core.NORM_MINMAX);
        trackRect = region;

        return bitmap;
    }

    private void rgba2Hsv(Mat rgba) {

        Imgproc.cvtColor(rgba, hsv, Imgproc.COLOR_RGB2HSV);

        //The function of the inRange function is to check whether the size of each element of the input array is between 2 given values. There can be multiple channels. The mask saves the minimum value of 0 channel, which is the h component.
        //Here we use 3 channels of hsv, compare h, 0~180, s, smin~256, v, min(vmin, vmax), max(vmin, vmax). If all 3 channels are in the corresponding range, then
        //The value of the point corresponding to the mask is all 1 (0xff), otherwise it is 0 (0x00).
        int vMin = 65, vMax = 256, sMin = 55;
        Core.inRange(
                hsv,
                new Scalar(0, sMin, Math.min(vMin, vMax)),
                new Scalar(180, 256, Math.max(vMin, vMax)),
                mask
        );
    }

    private void updateHueImage() {
        hsvList.clear();
        hsvList.add(hsv);

        // Hue is initialized to the same depth as the hsv size. The measure of hue is expressed by angle. The difference between red, green and blue is 120 degrees, and the inverse color is 180 degrees.
        hue.create(hsv.size(), hsv.depth());

        hueList.clear();
        hueList.add(hue);
        MatOfInt from_to = new MatOfInt(0, 0);

        // Copy the number of the first channel (that is, the hue) of hsv to hue, 0 index array
        Core.mixChannels(hsvList, hueList, from_to);
    }

    public RotatedRect objectTracking(Mat mRgba) {

        rgba2Hsv(mRgba);

        updateHueImage();
        // Calculate the back projection of the histogram.
        // Imgproc.calcBackProject(hueList, new MatOfInt(0), hist, prob, ranges, 255);
        Imgproc.calcBackProject(hueList, new MatOfInt(0), hist, prob, ranges, 1.0);

        // Calculates the bitwise join of two arrays (dst = src1 & src2) to compute the bitwise join of each of the two arrays or arrays and scalar elements.
        Core.bitwise_and(prob, mask, prob, new Mat());

        // Tracking target
        rotatedRect = Video.CamShift(prob, trackRect, new TermCriteria(TermCriteria.EPS, 10, 1));

        if (null != mOnCalcBackProjectListener) {
            mOnCalcBackProjectListener.onCalcBackProject(prob);
        }

        // Take this final goal as the target of the next tracking
        trackRect = rotatedRect.boundingRect();

        Imgproc.rectangle(prob, trackRect.tl(), trackRect.br(), new Scalar(255, 255, 0, 255), 6);

        Log.i(TAG, "objectTracking: width : " + trackRect.width + "  height : " + trackRect.height + "angle : " + rotatedRect.angle);
        return rotatedRect;
    }

    /**
     * Add a histogram back projection listener
     *
     * @param listener listener
     */
    public void setOnCalcBackProjectListener(OnCalcBackProjectListener listener) {
        mOnCalcBackProjectListener = listener;
    }
}
