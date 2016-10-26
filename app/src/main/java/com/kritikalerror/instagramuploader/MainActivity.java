package com.kritikalerror.instagramuploader;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.desmond.squarecamera.CameraActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private Context mContext;
    private static String mPath;

    private static final int REQUEST_CAMERA = 0;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 123;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContext = this;
        initializeWrapper();
        mPath = "";
    }

    public void onResume() {
        super.onResume();
    }

    private void showOKAlertMessage(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission))
                return false;
        }
        return true;
    }

    private boolean initializeWrapper() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.CAMERA))
            permissionsNeeded.add("Camera");
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("Write to External Storage");
        if (!addPermission(permissionsList, Manifest.permission.INTERNET))
            permissionsNeeded.add("Internet Access");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to: " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                showOKAlertMessage(message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                this.initialize();
                return true;
            }
            ActivityCompat.requestPermissions(MainActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            this.initialize();
            return true;
        }
        this.initialize();
        return true;
    }

    /**
     * Initialize preview surface
     */
    public void initialize() {
        // Add ads
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("YOUR_DEVICE_HASH")
                .build();
        mAdView.loadAd(adRequest);

        ImageButton captureButton = (ImageButton) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(captureListener);

        ImageButton galleryButton = (ImageButton) findViewById(R.id.button_gallery);
        galleryButton.setOnClickListener(galleryListener);
    }

    /**
     * Listeners to detect button press
     */
    View.OnClickListener captureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Start CameraActivity
            Intent startCustomCameraIntent = new Intent(MainActivity.this, CameraActivity.class);
            startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);
        }
    };

    View.OnClickListener galleryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Start CameraActivity
            Intent galleryIntent = new Intent(Intent.ACTION_VIEW,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivity(galleryIntent);
        }
    };

    // Receive Uri of saved square photo
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_CAMERA) {
            Uri photoUri = data.getData();
            mPath = photoUri.toString();
            Log.e("URI", mPath);
            Bitmap originalPic = BitmapFactory.decodeFile(mPath.replace("file://", ""));

            if (originalPic != null) {
                Toast.makeText(getApplicationContext(),
                        "Constructing poster from original pic...",
                        Toast.LENGTH_SHORT).show();
                Bitmap banner = BitmapFactory.decodeResource(getResources(),
                        R.drawable.banner);
                Bitmap overlay = BitmapFactory.decodeResource(getResources(),
                        R.drawable.shotiphoneoverlay);
                Bitmap firstPic = imageOverlay(originalPic, banner);
                Bitmap newPic = imageOverlay(firstPic, overlay);
                String oldPath = mPath;
                saveBitmapToJPG(newPic);
                if (oldPath.equals(mPath)) {
                    Toast.makeText(getApplicationContext()
                            , "Cannot create ShotOniPhone6 poster! Cannot create directory to save in"
                            , Toast.LENGTH_LONG).show();
                    return;
                }

                // Refresh gallery
                Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri fileContentUri = Uri.parse("file://" + mPath);
                mediaScannerIntent.setData(fileContentUri);
                getApplicationContext().sendBroadcast(mediaScannerIntent);
            }
            else {
                Log.e("FILEOPEN", "Cannot open file!");
                Toast.makeText(getApplicationContext(),
                        "Cannot open file!", Toast.LENGTH_LONG).show();
            }

            uploadToInstagram();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void saveBitmapToJPG(Bitmap bmp) {
        File file = getOutputMediaFile();

        if (file == null) {
            return;
        }

        //Convert bitmap to byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Save picture to folder
     * @return
     */
    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "ShotOniPhone6");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e("FAILFILE", "Cannot make directory!");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        String imageHeader = "EDIT_IMG_";
        mPath = mediaStorageDir.getPath() + File.separator + imageHeader + timeStamp + ".jpg";
        mediaFile = new File(mPath);

        return mediaFile;
    }

    /**
     * We can't actually upload to Instagram but we can give user choices
     */
    private void uploadToInstagram() {
        if (!mPath.equals("")) {
            Log.e("INSTAUPLOAD", "Uploading to Instagram!");
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
            if (intent != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setPackage("com.instagram.android");

                Log.e("INSTAUPLOADD", mPath);
                Uri uri = Uri.parse("file://" + mPath);

                // Add the URI to the Intent.
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("image/*");

                startActivity(shareIntent);
                //startActivity(Intent.createChooser(shareIntent, "Share to"));
            } else {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Instagram not found!")
                    .setMessage("Do you want to download Instagram from the Play Store?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setData(Uri.parse("market://details?id=" + "com.instagram.android"));
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "Going back to app!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            }
        }
        else
        {
            Log.e("INSTAFAIL", "mPath is: " + mPath);
        }
    }

    /**
     * Helper function to overlay main pic on top of iPhone 6 bg
     * @param firstBitmap
     * @param secondBitmap
     * @return
     */
    public static Bitmap imageOverlay(Bitmap firstBitmap, Bitmap secondBitmap) {
        Bitmap bmOverlay = Bitmap.createBitmap(firstBitmap.getWidth(), firstBitmap.getHeight(), firstBitmap.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(firstBitmap, new Matrix(), null);
        canvas.drawBitmap(secondBitmap,
                (firstBitmap.getWidth() / 2) - (secondBitmap.getWidth() / 2),
                firstBitmap.getHeight() - (secondBitmap.getHeight()), null);
        return bmOverlay;
    }
}