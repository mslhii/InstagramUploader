package com.kritikalerror.instagramuploader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.desmond.squarecamera.CameraActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private Camera.PictureCallback mPicture;
    private ImageButton captureButton, switchCamera;
    private Context myContext;
    private LinearLayout cameraPreview;
    private boolean cameraFront = false;
    private static String mPath;

    private static final int REQUEST_CAMERA = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;
        initialize();
        mPath = "";
    }

    /**
     * Check to see if we have a front facing camera
     * @return
     */
    private int findFrontFacingCamera() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    /**
     * Helper function to find back camera
     * @return
     */
    private int findBackFacingCamera() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    public void onResume() {
        super.onResume();
        /*
        if (!hasCamera(myContext)) {
            Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null) {
            if (findFrontFacingCamera() < 0) {
                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                switchCamera.setVisibility(View.GONE);
            }
            mCamera = Camera.open(findBackFacingCamera());
            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);
        }
        */
    }

    /**
     * Initialize preview surface
     */
    public void initialize() {
        /*
        cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);
        mPreview = new CameraPreview(myContext, mCamera);
        cameraPreview.addView(mPreview);

        captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(captureListener);

        switchCamera = (Button) findViewById(R.id.button_ChangeCamera);
        switchCamera.setOnClickListener(switchCameraListener);
        */

        // Add ads
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("YOUR_DEVICE_HASH")
                .build();
        mAdView.loadAd(adRequest);


        captureButton = (ImageButton) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(captureListener);
    }

    /**
     * Listener to detect button press
     */
    View.OnClickListener captureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Take picture
            //mCamera.takePicture(null, null, mPicture);
            // Start CameraActivity
            Intent startCustomCameraIntent = new Intent(MainActivity.this, CameraActivity.class);
            startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);
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
                //Bitmap newPic = addBorder(originalPic, 2);
                Bitmap banner = BitmapFactory.decodeResource(getResources(),
                        R.drawable.banner);
                Bitmap overlay = BitmapFactory.decodeResource(getResources(),
                        R.drawable.shotiphoneoverlay);
                Bitmap firstPic = imageOverlay(originalPic, banner);
                Bitmap newPic = imageOverlay(firstPic, overlay);
                saveBitmapToJPG(newPic);
            }
            else {
                Log.e("FILEOPEN", "Cannot open file!");
                Toast.makeText(getApplicationContext(),
                        "Cannot open file!", Toast.LENGTH_SHORT).show();
            }

            uploadToInstagram();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void saveBitmapToJPG(Bitmap bmp) {
        //create a file to write bitmap data
        //String oldPath = mPath;
        //mPath =  mPath.replace(".jpg", "") + "_EDITED.jpg";
        //File file = new File(mPath);
        File file = getOutputMediaFile(false);

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

    View.OnClickListener switchCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int camerasNumber = Camera.getNumberOfCameras();
            if (camerasNumber > 1) {
                releaseCamera();
                chooseCamera();
            } else {
                Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    };

    public void chooseCamera() {
        if (cameraFront) {
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                mCamera = Camera.open(cameraId);
                mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                mCamera = Camera.open(cameraId);
                mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //releaseCamera();
    }

    private boolean hasCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    private Camera.PictureCallback getPictureCallback() {
        Camera.PictureCallback picture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                File pictureFile = getOutputMediaFile(true);
                //File editedPictureFile = getOutputMediaFile(false);

                if (pictureFile == null /*|| editedPictureFile == null*/) {
                    return;
                }
                try {
                    // Upload original picture
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                    //Toast toast = Toast.makeText(myContext, "Picture saved: " + pictureFile.getName(), Toast.LENGTH_LONG);
                    //toast.show();

                    // Edit and upload new picture
//                    FileOutputStream editfos = new FileOutputStream(editedPictureFile);
//                    Bitmap originalPic = BitmapFactory.decodeByteArray(data, 0, data.length);
//                    //imageOverlay(originalPic, originalPic);
//                    Bitmap newPic = addBorder(originalPic, 2);
//                    byte[] newData = convertBitmapToByteArray(newPic);
//                    editfos.write(newData);
//                    editfos.close();

                    // Upload to Instagram immediately
                    Toast.makeText(MainActivity.this, "Picture saved to: " + mPath, Toast.LENGTH_LONG).show();
                    uploadToInstagram();

                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }

                mPreview.refreshCamera(mCamera);
            }
        };
        return picture;
    }

    /**
     * Save picture to folder
     * @return
     */
    private static File getOutputMediaFile(boolean isOriginal) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath(),
                "ShotOniPhone6");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        String imageHeader = "IMG_";
        if(!isOriginal)
        {
            imageHeader = "EDIT_IMG_";
        }
        mPath = mediaStorageDir.getPath() + File.separator + imageHeader + timeStamp + ".jpg";
        mediaFile = new File(mPath);

        return mediaFile;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    //file:///storage/emulated/0/Pictures/SquareCamera/IMG_20150912_185608.jpg


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
                /*
                try {
                    Log.e("INSTAUPLOADD", mPath);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(),
                            mPath, "Title", "Description")));
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                */

                Log.e("INSTAUPLOADD", mPath);
                //Uri uri = Uri.fromFile(media);
                Uri uri = Uri.parse("file://" + mPath);

                // Add the URI to the Intent.
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("image/*");

                startActivity(shareIntent);
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

    private void createInstagramIntent(String type, String mediaPath){
        if (!mPath.equals("")) {
            // Create the new Intent using the 'Send' action.
            Intent share = new Intent(Intent.ACTION_SEND);

            // Set the MIME type
            share.setType(type);

            // Create the URI from the media
            File media = new File(mediaPath);
            //Uri uri = Uri.fromFile(media);
            Uri uri = Uri.parse(mediaPath);

            // Add the URI to the Intent.
            share.putExtra(Intent.EXTRA_STREAM, uri);

            // Broadcast the Intent.
            startActivity(Intent.createChooser(share, "Share to"));
        }
    }

    /**
     * Add border overlay to picture
     * This is the ShotOniPhone 6 border
     */
    private Bitmap addBorder(Bitmap bmp, int borderSize)
    {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2,
                bmp.getHeight() + borderSize * 2,
                bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return bmpWithBorder;
    }

    /**
     * Make picture square before saving
     * We actually don't need this?
     */
    private void cropPictureToSquare()
    {
        //
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
        //canvas.drawBitmap(secondBitmap, 0, 0, null);
        //canvas.drawBitmap(secondBitmap,
        //        canvas.getWidth() - firstBitmap.getWidth(),
        //        canvas.getHeight() - firstBitmap.getHeight(), null);
        canvas.drawBitmap(secondBitmap,
                (firstBitmap.getWidth() / 2) - (secondBitmap.getWidth() / 2),
                firstBitmap.getHeight() - (secondBitmap.getHeight()), null);
        return bmOverlay;
    }

    public static byte[] convertBitmapToByteArray(Bitmap bmp) {
        //int bytes = b.getWidth()*b.getHeight()*4; for 64 bit images
        int bytes = bmp.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
        bmp.copyPixelsToBuffer(buffer); //Move the byte data to the buffer

        return buffer.array();
    }

    /**
     *
     * @param filePath
     * @return
     */
    public static byte[] convertPicToByteArray(String filePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, blob);
        return blob.toByteArray();
    }
}

/*
09-14 00:25:56.231  30066-30066/com.kritikalerror.instagramuploader E/URI﹕ file:///storage/emulated/0/Pictures/SquareCamera/IMG_20150914_002556.jpg
09-14 00:25:56.581  30066-30066/com.kritikalerror.instagramuploader E/INSTAUPLOAD﹕ Uploading to Instagram!
09-14 00:25:56.581  30066-30066/com.kritikalerror.instagramuploader E/INSTAUPLOADD﹕ /storage/emulated/0/ShotOniPhone6/EDIT_IMG_20150914_002556.jpg
 */