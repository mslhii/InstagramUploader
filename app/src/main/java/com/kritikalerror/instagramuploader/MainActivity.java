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

    private ImageButton captureButton;
    private Context mContext;
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
        mContext = this;
        initialize();
        mPath = "";
    }

    public void onResume() {
        super.onResume();
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


        captureButton = (ImageButton) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(captureListener);
    }

    /**
     * Listener to detect button press
     */
    View.OnClickListener captureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
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

    @Override
    protected void onPause() {
        super.onPause();
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