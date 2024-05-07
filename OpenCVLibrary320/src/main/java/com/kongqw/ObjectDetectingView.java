package com.kongqw;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.preference.PreferenceManager;
import android.se.omapi.Session;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

/**
 * Created by kqw on 2016/7/13.
 * RobotCameraView
 */
public class ObjectDetectingView extends BaseCameraView {

    private final Object Context;
    private Mat hierarchy;
    public int radius;

    private static final String TAG = "ObjectDetectingView";
    private ArrayList<ObjectDetector> mObjectDetects;

    private MatOfRect mObject;

    @Override
    public void onOpenCVLoadSuccess() {
        Log.i(TAG, "onOpenCVLoadSuccess: ");

        mObject = new MatOfRect();

        mObjectDetects = new ArrayList<>();
    }

    @Override
    public void onOpenCVLoadFail() {
        Log.i(TAG, "onOpenCVLoadFail: ");
    }

    public ObjectDetectingView(Context context, AttributeSet attrs) {

        super(context, attrs);
        Context = context;

    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        //
        //Child thread (non-UI thread)
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        for (ObjectDetector detector : mObjectDetects) {
            //
            //Detection target
            Rect[] object = detector.detectObject(mGray, mObject);
            for (Rect rect : object) {
                Imgproc.rectangle(mRgba, rect.tl(), rect.br(), detector.getRectColor(), 3);

                //find the pupil inside the eye rect
                detectPupil(rect);
            }
        }

        return mRgba;
    }

    /**
     *
     * Add detector
     *
     * @param detector Detector
     */
    public synchronized void addDetector(ObjectDetector detector) {
        if (!mObjectDetects.contains(detector)) {
            mObjectDetects.add(detector);
        }
    }

    /**
     * Remove detector
     *
     * @param detector
     * Detector
     */
    public synchronized void removeDetector(ObjectDetector detector) {
        if (mObjectDetects.contains(detector)) {
            mObjectDetects.remove(detector);
        }
    }


    public void detectPupil(Rect eyeRect) {
        hierarchy = new Mat();

        Mat img = mRgba.submat(eyeRect);
        Mat img_hue = new Mat();

        Mat circles = new Mat();

        // / Convert it to hue, convert to range color, and blur to remove false
        // circles
        Imgproc.cvtColor(img, img_hue, Imgproc.COLOR_RGB2HSV);// COLOR_BGR2HSV);

        Core.inRange(img_hue, new Scalar(0, 0, 0), new Scalar(255, 255, 32), img_hue);

        Imgproc.erode(img_hue, img_hue, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3)));

        Imgproc.dilate(img_hue, img_hue, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(6, 6)));

        Imgproc.Canny(img_hue, img_hue, 170, 220);
        Imgproc.GaussianBlur(img_hue, img_hue, new Size(9, 9), 2, 2);
        // Apply Hough Transform to find the circles
        Imgproc.HoughCircles(img_hue, circles, Imgproc.CV_HOUGH_GRADIENT, 3, img_hue.rows(), 200, 75, 10, 25);

        if (circles.cols() > 0)
            for (int i = 0; i < circles.cols(); i++) {
                double vCircle[] = circles.get(0, i);

                if (vCircle == null)
                    break;

                Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
                radius = (int) Math.round(vCircle[2]);


                Log.e(TAG, "Radius: "+radius);
                Log.e(TAG, "Point: "+pt);
//                Toast.makeText(getContext(), "Pupil: "+radius, Toast.LENGTH_SHORT).show();

                // draw the found circle
                Core.circle(img, pt, radius, new Scalar(0, 255, 0), 2);
//                Core.circle(img, pt, 3, new Scalar(0, 0, 255), 2);
            }
    }

    public String CallViaOtherClassFunction()
    {

        String a = String.valueOf(radius);
        return a;

    }

    /*public class Session {

        public SharedPreferences prefs;

        public Session(Context context) {
            // TODO Auto-generated constructor stub
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
        }

        public void setRadius(String setRadius) {
            prefs.edit().putString("radius", String.valueOf(radius)).commit();
        }

        public String getRadius() {
            String radius = prefs.getString("radius", "");
            return radius;
        }
    }*/




}
