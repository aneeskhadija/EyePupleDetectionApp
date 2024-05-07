package kong.qingwei.opencv320;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.kongqw.permissionslibrary.PermissionsManager;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     *
     * Show dialog box with missing permissions
     */
    protected void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Request permission");
        builder.setMessage("Android 6.0+ Dynamic request camera permissions");
        builder.setPositiveButton("Go to set permissions", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                PermissionsManager.startAppSettings(getApplicationContext());
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * Show dialog box without OpenCV Manager installed
     */
    protected void showInstallDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("You have not installed OpenCV Manager yet");
        builder.setMessage("Do you download and install?");
        builder.setPositiveButton("go download", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "go download", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/kongqw/FaceDetectLibrary/tree/opencv3.2.0/OpenCVManager")));
            }
        });
        builder.setNegativeButton("drop out", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.create().show();
    }
}
