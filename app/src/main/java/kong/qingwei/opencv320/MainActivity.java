package kong.qingwei.opencv320;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.kongqw.permissionslibrary.PermissionsManager;

public class MainActivity extends BaseActivity {

    private PermissionsManager mPermissionsManager;

    private ImageView img_detectCarNo;

    // Permission to verify
    private final String[] PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    // Identification request code
    private final int REQUEST_CODE_DETECTION = 0;
    // Tracking request code
    private final int REQUEST_CODE_TRACK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img_detectCarNo = findViewById(R.id.id_image_detectPupil);

        img_detectCarNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mPermissionsManager.checkPermissions(REQUEST_CODE_DETECTION, PERMISSIONS);

            }
        });

        // Dynamic permission check
        mPermissionsManager = new PermissionsManager(this) {

            @Override
            public void authorized(int requestCode) {
                //
                //Permission
                switch (requestCode) {
                    case REQUEST_CODE_DETECTION:
                        startActivity(new Intent(MainActivity.this, ObjectDetectingActivity.class));
                        break;
                    case REQUEST_CODE_TRACK:
                        startActivity(new Intent(MainActivity.this, ObjectTrackingActivity.class));
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void noAuthorization(int requestCode, String[] lacksPermissions) {
                // Lack of necessary permissions
                showPermissionDialog();
            }

            @Override
            public void ignore(int requestCode) {
                // Android 6.0 The following systems are not verified
                authorized(requestCode);
            }
        };
    }

    /**
     * Review permission
     *
     * @param requestCode  requestCode
     * @param permissions  permissions
     * @param grantResults grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //
        //After the user makes a selection, he reviews the permissions and determines whether the application has been approved.
        mPermissionsManager.recheckPermissions(requestCode, permissions, grantResults);
    }



    /**
     * Target tracking
     *
     * @param view view
     */
    /*public void onTracking(View view) {
        // Check permission
        mPermissionsManager.checkPermissions(REQUEST_CODE_TRACK, PERMISSIONS);
    }*/
}
