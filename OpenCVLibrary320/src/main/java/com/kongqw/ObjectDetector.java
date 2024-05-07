package com.kongqw;

import android.content.Context;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by kongqingwei on 2017/5/17.
 * ObjectDetector
 */

public class ObjectDetector {

    private CascadeClassifier mCascadeClassifier;
    private int mMinNeighbors;
    private float mRelativeObjectWidth;
    private float mRelativeObjectHeight;
    private Scalar mRectColor;

    /**
     * Construction method
     *
     * @param context              Context
     * @param id                   Cascading classifier ID
     * @param minNeighbors        Confirm the target in several consecutive frames
     * @param relativeObjectWidth  Minimum width screen ratio
     * @param relativeObjectHeight Minimum height screen ratio
     * @param rectColor            Brush color
     */
    public ObjectDetector(Context context, int id, int minNeighbors, float relativeObjectWidth, float relativeObjectHeight, Scalar rectColor) {
        context = context.getApplicationContext();
        mCascadeClassifier = createDetector(context, id);
        mMinNeighbors = minNeighbors;
        mRelativeObjectWidth = relativeObjectWidth;
        mRelativeObjectHeight = relativeObjectHeight;
        mRectColor = rectColor;
    }

    /**
     * Create detector
     *
     * @param context Context
     * @param id      Cascading classifier ID
     * @return Detector
     */
    private CascadeClassifier createDetector(Context context, int id) {
        CascadeClassifier javaDetector;
        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = context.getResources().openRawResource(id);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, id + ".xml");
            os = new FileOutputStream(cascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            javaDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());
            if (javaDetector.empty()) {
                javaDetector = null;
            }

            boolean delete = cascadeDir.delete();
            return javaDetector;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (null != is) {
                    is.close();
                }
                if (null != os) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Target Detection
     *
     * @param gray   Grayscale image
     * @param object Container for identifying results
     * @return Detected target location set
     */
    public Rect[] detectObject(Mat gray, MatOfRect object) {
        // Use Java face detection
        mCascadeClassifier.detectMultiScale(
                gray, // Grayscale image to check
                object, // Detected face
                1.1, // Indicates the scale factor of the search window in two successive scans. The default is 1.1, which means that each search window is expanded by 10%.
                mMinNeighbors, // The default is 3 control error detection, which means that the face is detected by overlapping several times by default, and the face is considered to exist.
                Objdetect.CASCADE_SCALE_IMAGE,
                getSize(gray, mRelativeObjectWidth, mRelativeObjectHeight), // The smallest possible size of the target
                gray.size()); // Maximum possible size of the target

        return object.toArray();
    }

    /**
     *
     * Get size according to screen ratio
     *
     * @param gray                 gray
     * @param relativeObjectWidth  Minimum width screen ratio
     * @param relativeObjectHeight Minimum height screen ratio
     * @return size
     */
    private Size getSize(Mat gray, float relativeObjectWidth, float relativeObjectHeight) {
        Size size = gray.size();
        int cameraWidth = gray.cols();
        int cameraHeight = gray.rows();
        int width = Math.round(cameraWidth * relativeObjectWidth);
        int height = Math.round(cameraHeight * relativeObjectHeight);
        size.width = 0 >= width ? 0 : (cameraWidth < width ? cameraWidth : width); // width [0, cameraWidth]
        size.height = 0 >= height ? 0 : (cameraHeight < height ? cameraHeight : height); // height [0, cameraHeight]
        return size;
    }

    /**
     *
     * Get the brush color
     *
     * @return colour
     */
    public Scalar getRectColor() {
        return mRectColor;
    }
}
