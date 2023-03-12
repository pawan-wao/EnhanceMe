package com.example.em;

import android.content.Context;
import android.content.res.AssetManager;
import org.tensorflow.lite.Interpreter;
import android.graphics.Bitmap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import android.content.res.AssetFileDescriptor;
import android.os.ParcelFileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ActivityInference {

    private static ActivityInference activityInferenceInstance;
    private final Interpreter interpreter;
    private AssetManager assetManager;


    private static final String MODEL_FILE=  "file:///android_asset/frozen_LPGAN_736_FLOAT.pb";
    private static final String INPUT_NODE = "Placeholder";
    private static final String[] OUTPUT_NODES = {"netG-736/netG-736_var_scope/netG-736_var_scopeA/netG-736_3/Add"};
    private static final String OUTPUT_NODE = "netG-736/netG-736_var_scope/netG-736_var_scopeA/netG-736_3/Add";
    private static final long[] INPUT_SIZE = {1,512,512,3};
    private static final int OUTPUT_SIZE = 512;
    private static final int CHANNELS = 3;

    public ActivityInference(Context context) throws Exception {
        assetManager = context.getAssets();
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4); // change this number to adjust the number of threads to use for inference
        interpreter = new Interpreter(loadModelFile(assetManager, MODEL_FILE), options);
    }

    public static synchronized ActivityInference getInstance(Context context) throws Exception {
        if (activityInferenceInstance == null) {
            activityInferenceInstance = new ActivityInference(context);
        }
        return activityInferenceInstance;
    }

    private ByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    public int[] getActivityProb(int[] input_signal) {
        // Convert the input array to a float array
        float[] input = new float[(int) (INPUT_SIZE[1] * INPUT_SIZE[2] * INPUT_SIZE[3])];
        for (int i = 0; i < input_signal.length; i++){
            input[i] = (float) input_signal[i];
        }

        // Run inference
        float[] output = new float[OUTPUT_SIZE * OUTPUT_SIZE * CHANNELS];
        interpreter.run(convertInput(input), output);

        // Convert the output array to an integer array
        int[] result = new int[OUTPUT_SIZE * OUTPUT_SIZE * CHANNELS];
        for (int i = 0; i < output.length; i++){
            float cast = output[i] * 255 + 0.5f;
            if (cast < 0) {
                cast = 0;
            } else if (cast > 255) {
                cast = 255;
            }
            result[i] = (int) cast;
        }
        return result;
    }

    private ByteBuffer convertInput(float[] input) {
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect((int) (4 * INPUT_SIZE[1] * INPUT_SIZE[2] * INPUT_SIZE[3]));
        inputBuffer.order(ByteOrder.nativeOrder());
        for (int i = 0; i < INPUT_SIZE[1]; ++i) {
            for (int j = 0; j < INPUT_SIZE[2]; ++j) {
                int pixelValue = (int) input[(int) ((i * INPUT_SIZE[2] + j) * INPUT_SIZE[3])];
                inputBuffer.putFloat((((pixelValue >> 16) & 0xFF) - 128.0f) / 128.0f);
                inputBuffer.putFloat((((pixelValue >> 8) & 0xFF) - 128.0f) / 128.0f);
                inputBuffer.putFloat(((pixelValue & 0xFF) - 128.0f) / 128.0f);
            }
        }
        return inputBuffer;
    }


    public int[] enhanceImage(float[] input_signal) {
        float[] outputValues_float = new float[OUTPUT_SIZE * OUTPUT_SIZE * CHANNELS];
        int[] outputValues_int = new int[OUTPUT_SIZE * OUTPUT_SIZE * CHANNELS];

        interpreter.run(convertInput(input_signal), outputValues_float);

        float cast = 0;
        for (int i = 0; i < outputValues_float.length; i++) {
            cast = outputValues_float[i] * 255 + 0.5f;
            if (cast < 0) {
                cast = 0;
            } else {
                if (cast > 255) {
                    cast = 255;
                }
            }
            outputValues_int[i] = (int) cast;
        }

        return outputValues_int;
    }


    public void saveImage(Bitmap finalBitmap, String name) {
        String root = "/sdcard/Mobile-DCSCN-master/app/results"; // change the root directory
        File myDir = new File(root);

        String fname = "image_" + name + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}