package com.example.em;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Color;
import android.widget.TextView;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class MainActivity<activityInference> extends AppCompatActivity {

    private Button enhance_btn;
    private static final int MAX_SIZE = 512;

    private ImageView imageview;
    private TextView txt_clickbtn;
    // private static final String IMAGE_DIRECTORY = "/zoom";
    private int GALLERY = 1, CAMERA = 2;
    private ActivityInference activityInference;
    private Bitmap bmp_imageview;
    private Bitmap bmp_processed_image;
    private static final int REQUEST_WRITE_PERMISSION = 786;
    int original_height;
    int original_width;
    int diff = 0;
    int image =  R.drawable.turtle;




  void ActivityInterface() {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();


        enhance_btn = (Button) findViewById(R.id.btn_enhance);
        imageview = (ImageView) findViewById(R.id.iv);
        txt_clickbtn = (TextView) findViewById(R.id.usage);

        Bitmap bmp_2 = decodeSampledBitmapFromResource(getResources(), image, 512, 512);
        bmp_imageview = scaleDown(bmp_2, 512, false);




        imageview.setImageBitmap(bmp_imageview);
        int i = 0;



        //to select image
        Button imgraw = findViewById(R.id.btn_gallery);
        imgraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPictureDialog();
                Toast.makeText(getApplicationContext(), "Button Clicked!", Toast.LENGTH_SHORT).show();
            }
        });


        class ActivityInference extends com.example.em.ActivityInference {
            public ActivityInference(Context context) throws Exception {
                super(context);
            }
        }

        enhance_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Bitmap input_image = decodeSampledBitmapFromResource(getResources(), R.drawable.jacosidad, 512, 512);
                long tStart = System.currentTimeMillis();

                // Get input stream from the ImageView
                InputStream imageStream = null;
                Drawable drawable = imageview.getDrawable();
                if (drawable instanceof BitmapDrawable) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    imageStream = new ByteArrayInputStream(stream.toByteArray());
                }

                Bitmap input_image = BitmapFactory.decodeStream(imageStream);

                if (input_image != null) {
                    float[] processed_image_values = processImage(input_image);

                    /**
                     * Image enhancement:
                     *      Output: int array containing RGB pixels (enhanced)
                     * */
                    // to check enhance image is no null


                    if (activityInference != null) {
                        int[] enhanced = activityInference.enhanceImage(processed_image_values);
                        // rest of the code
                    } else {
                        Log.e("Error", "activityInference is null");
                    }


                    /**
                     * Crop enhanced image
                     * */

                    int[] enhanced = new int[original_height * original_width * 3];

                    int[] colors;
                    if (original_height < MAX_SIZE) {
                        int[] crop = new int[original_height * original_width * 3];
                        int cont = diff * original_width * 3;
                        for (int i = 0; i < crop.length; i++) {
                            crop[i] = enhanced[cont + i];
                        }
                        colors = new int[original_height * original_width];
                        for (int ci = 0; ci < colors.length; ci++) {
                            colors[ci] = (255 & 0xff) << 24 | (crop[3 * ci + 2] & 0xff) << 16 | (crop[3 * ci + 1] & 0xff) << 8 | (crop[3 * ci] & 0xff);
                        }
                    } else {
                        colors = new int[MAX_SIZE * MAX_SIZE];
                        for (int ci = 0; ci < colors.length; ci++) {
                            int index = 3 * ci;
                            if (index + 2 < enhanced.length) {
                                colors[ci] = (255 & 0xff) << 24 | (enhanced[index + 2] & 0xff) << 16 | (enhanced[index + 1] & 0xff) << 8 | (enhanced[index] & 0xff);
                            } else {
                                Toast.makeText(MainActivity.this, "ci else executed", Toast.LENGTH_SHORT).show();
                                // Handle index out of bounds error
                                // This could include throwing an exception, logging an error message, or taking some other action
                            }
                        }
                    }



                        /**
                         * Group RGB pixels to get a Bitmap again
                         * */
        /*int[] colors = new int[512 * 512];
        for (int ci = 0; ci < colors.length; ci++)
        {
            colors[ci] = (255 & 0xff) << 24 | (enhanced[3*ci+2] & 0xff) << 16 | (enhanced[3*ci+1] & 0xff) << 8 | (enhanced[3*ci] & 0xff);
        }*/

                    //Bitmap bmp_enhanced = bmp_imageview;
                    Bitmap aux1 = Bitmap.createScaledBitmap(bmp_imageview, bmp_imageview.getWidth(), bmp_imageview.getHeight(), false);
                    aux1.setPixels(colors, 0, aux1.getWidth(), 0, 0, aux1.getWidth(), aux1.getHeight());


                    /**
                     * Set view to show enhanced image
                     * */
                    imageview.setImageBitmap(aux1);
                    long tEnd = System.currentTimeMillis();
                    float tDelta = tEnd - tStart;
                    Log.i("Runtime", "Tiempo de ejecucion: " + tDelta + "ms");

                    saveImage(aux1, "enhanced_image");
                }
                else {
                    Log.e("Error", "Input image is null");
                }
            }
        });



    }



    public float[] processImage(Bitmap input_image){

        if (input_image == null) {
            throw new IllegalArgumentException("Input image is null");
        }

        Bitmap scaledBitmap = scaleDown(input_image, 512, false);

        if (scaledBitmap == null) {
            throw new RuntimeException("Scaled bitmap is null");
        }

        original_height = scaledBitmap.getHeight();
        original_width= scaledBitmap.getWidth();

        if(original_height < MAX_SIZE){
            diff = 512 - original_height;
            Bitmap new_height = addPaddingTopForBitmap(scaledBitmap,diff);
            bmp_processed_image = Bitmap.createScaledBitmap(new_height,new_height.getWidth(),new_height.getHeight(),false);

        }else if(original_width < MAX_SIZE){
            diff = 512 - original_width;
            Bitmap new_width = addPaddingLeftForBitmap(scaledBitmap,diff);
            bmp_processed_image = Bitmap.createScaledBitmap(new_width,new_width.getWidth(),new_width.getHeight(),false);

        }else{
            bmp_processed_image = Bitmap.createScaledBitmap(scaledBitmap,512,512,false);
        }

        if (bmp_processed_image == null) {
            throw new RuntimeException("Processed bitmap is null");
        }

        int i2 = 0;
        /**
         * Process input image: Image --> Bitmap --> Int array (input_values)
         * */
        int[] input_values = new int[512 * 512];
        //bmp_processed_image = Bitmap.createScaledBitmap(input_image,512,512,false);
        bmp_processed_image.getPixels(input_values,0,bmp_processed_image.getWidth(), 0,0, bmp_processed_image.getWidth(),bmp_processed_image.getHeight());


        /**
         * Extract RGB from input_values array.
         * Cast values to float to feed the model (floatValues)
         * */
        float[] floatValues = new float[512 * 512 * 3];
        for (int i = 0; i < input_values.length; ++i) {
            final int val = input_values[i];

            floatValues[i * 3 + 0] = ((val & 0xFF));
            floatValues[i * 3 + 1] = (((val >> 8) & 0xFF));
            floatValues[i * 3 + 2] = (((val >> 16) & 0xFF));
        }

        return floatValues;
    }


    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        if (realImage == null) {
            return null;
        }

        float ratio = Math.min((float) maxImageSize / realImage.getWidth(), (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width, height, filter);
        return newBitmap;
    }


    public Bitmap addPaddingBottomForBitmap(Bitmap bitmap, int paddingBottom) {
        Bitmap outputBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight() + paddingBottom, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        return outputBitmap;
    }

    public Bitmap addPaddingTopForBitmap(Bitmap bitmap, int paddingTop) {
        Bitmap outputBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight() + paddingTop, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, paddingTop, null);
        return outputBitmap;
    }

    public Bitmap addPaddingLeftForBitmap(Bitmap bitmap, int paddingLeft) {
        Bitmap outputBitmap = Bitmap.createBitmap(bitmap.getWidth() + paddingLeft, bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, paddingLeft, 0, null);
        return outputBitmap;
    }

    public Bitmap addPaddingRightForBitmap(Bitmap bitmap, int paddingRight) {
        Bitmap outputBitmap = Bitmap.createBitmap(bitmap.getWidth() + paddingRight, bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        return outputBitmap;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    private void saveImage(Bitmap finalBitmap, String name) {

        String root = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/enhanced_images");
        myDir.mkdirs();
        Random generator = new Random();

        int n = 10000;
        n = generator.nextInt(n);
        String fname = "image_"+ name +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            // sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
            //
            //     Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }








    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
        } else {
           // applySuperResolution();
        }
    }

    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Choose");
        String[] pictureDialogItems = {
                "From Gallery",
                "Open Cemera"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallery();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    private void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    // Check that the Uri is valid
                    if (contentURI != null) {
                        // Query the content provider to get the file path
                        String[] projection = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(contentURI, projection, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                            String filePath = cursor.getString(column_index);
                            cursor.close();
                            // Check that the file exists
                            File file = new File(filePath);
                            if (file.exists()) {
                                // Save the file to disk
                                File newFile = saveImageFileToDisk(file);
                                // Load the bitmap from the saved file
                                Bitmap bitmap = BitmapFactory.decodeFile(newFile.getAbsolutePath());
                                if (bitmap != null) {
                                    bmp_imageview = bitmap;
                                    imageview.setImageBitmap(bmp_imageview);
                                } else {
                                    Toast.makeText(MainActivity.this, "Failed to load image!", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "File not found!", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "File not found!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Invalid Uri!", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "No image selected!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private File saveImageFileToDisk(File file) throws IOException {
        // Create a new file in the app's cache directory
        File newFile = new File(getCacheDir(), "temp.jpg");
        // Copy the contents of the original file to the new file
        FileInputStream inputStream = new FileInputStream(file);
        FileOutputStream outputStream = new FileOutputStream(newFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        // Close the input and output streams
        inputStream.close();
        outputStream.close();
        return newFile;
    }


    /*public void applySuperResolution() {
//        // http://aqibsaeed.github.io/2017-05-02-deploying-tensorflow-model-andorid-device-human-activity-recognition/
//        int[] imagenOriginal = new int[500*500*3];
//        int[] resizedArray = new int [1000*1000*3];
//
//        // Convierto el Bitmap con la imagen en un array de int
//        imagen.getPixels(imagenOriginal, 0, 500, 0, 0, 500, 500);
//        //Hago el escalado
//        Bitmap resized = Bitmap.createScaledBitmap(imagen, 1000, 1000, true);
//        resized.getPixels(resizedArray, 0, 1000, 0, 0, 1000, 1000);
//
//        int[] result1 = new int[3];
//        int [] y = new int [500 * 500];
//        int [] u = new int [1000*1000];
//        int [] v = new int [1000*1000];
//        // De la imagen original me quedo con el canal y
//        for (int i = 0; i < imagenOriginal.length; i++){
//            result1 = convertRGB2YUV(imagenOriginal[i]);
//            y[i] = result1[0];
//        }
//        // De la imagen escalada me quedo con los canales u y v
//        for (int i = 0; i < resizedArray.length; i++){
//            result1 = convertRGB2YUV(resizedArray[i]);
//            u[i] = result1[1];
//            v[i] = result1[2];
//        }
//        // Aplico el modelo al canal y
//        int[] results = activityInference.getActivityProb(y);
//
//        // Junto los canales
//        int[] result3 = new int [1000*1000*3];
//        for (int i = 0; i < 1000; i++){
//            result3[i] = results[i];
//            result3[i+1] = u[i];
//            result3[i+2] = v[i];
//
//        }
//
//        // Lo convierto a bitmap y lo guardo
//        Bitmap bitmap_result = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
//        bitmap_result.setPixels(result3, 0, 1000, 0, 0, 1000, 1000);
//        saveImage(bitmap_result);

        // BLANCO Y NEGRO

        int[] imagenOriginal = new int[500 * 500];
        imagen.getPixels(imagenOriginal, 0, 500, 0, 0, 500, 500);
        Bitmap resized = Bitmap.createScaledBitmap(imagen, 1000, 1000, true);

        long tInicio = System.currentTimeMillis();

        int[] results = activityInference.getActivityProb(imagenOriginal);

        long tFinal = System.currentTimeMillis();
        long tDiferencia = tFinal - tInicio;
        Log.i("Runtime", "Tiempo de ejecucion: " + tDiferencia + "ms");

        Bitmap bitmap_result = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888);
        bitmap_result.setPixels(results, 0, 1000, 0, 0, 1000, 1000);
        Bitmap scaled_image = overlay(bitmap_result, resized);

        saveImage(bitmap_result, "residual");
        saveImage(scaled_image, "result");
        Toast.makeText(MainActivity.this, "¡Imagen escalada en " + tDiferencia+ " ms!", Toast.LENGTH_SHORT).show();

    }*/

    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, 0, 0, null);
        return bmOverlay;
    }




}
