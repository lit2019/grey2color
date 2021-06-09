package com.example.grey2color;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.MediaRouteButton;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    boolean isShowingResult=true;

    static ExampleRunnable exampleRunnable;

    MaterialButton btnAdd;
    static MaterialButton btnColorize;
    Bitmap image;
    static Uri imagePath;
    ImageView btnRemoveCustomPellet;
    static ImageView photoView;
    TextView btnAddPellet;
    RecyclerView pelletRecyclerView;
    String[] pelletPaths;
    static ProgressBar progressBar;
    private BottomSheetBehavior mBottomSheetBehavior;
    PelletRecViewAdapter pelletRecViewAdapter;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    ImageView fadePelletRecyclerView;
    boolean isUsingCustomPellet = false;
    RelativeLayout layoutCustomPellet;
    private FrameLayout frameLayout;
    private ImageView imgCustomPellet;
    private Bitmap customPellet;
    private static boolean isColoring = false;
    private Bitmap bitmap;
    private int height,width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        pelletRecyclerView = findViewById(R.id.samplePellets);
        btnColorize = findViewById(R.id.btnColor);
        photoView = findViewById(R.id.photoView);
        progressBar = findViewById(R.id.progress_bar_sample);
        frameLayout = findViewById(R.id.fragmentLayoutSample);
        btnAdd = findViewById(R.id.btnAdd);

        imgCustomPellet = findViewById(R.id.imgCustomPellet);
        layoutCustomPellet = findViewById(R.id.layoutCustomPellet);
        btnRemoveCustomPellet = findViewById(R.id.btnRemoveCustomPellet);
        fadePelletRecyclerView = findViewById(R.id.layoutHidePellet);
        btnAddPellet = findViewById(R.id.btnUploadPellet);

        setOnClickForBtnAdd();
        setTextForBtnColorize();

        View bottomSheet = findViewById(R.id.bottom_sheet);
        ImageView buttonExpand = findViewById(R.id.arrow);

        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        buttonExpand.setImageResource(R.drawable.ic_round_keyboard_arrow_down_24);

        buttonExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    buttonExpand.setImageResource(R.drawable.ic_round_keyboard_arrow_down_24);

                }
                if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    buttonExpand.setImageResource(R.drawable.ic_round_keyboard_arrow_up_24);
                }
            }
        });


        mBottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        buttonExpand.setImageResource(R.drawable.ic_round_keyboard_arrow_up_24);
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        buttonExpand.setImageResource(R.drawable.ic_round_keyboard_arrow_down_24);
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        buttonExpand.setImageResource(R.drawable.ic_round_keyboard_arrow_up_24);

                        break;

                }
            }

            @Override
            public void onSlide(@androidx.annotation.NonNull View bottomSheet, float slideOffset) {

            }
        });
        btnColorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imagePath==null){
                    Toast.makeText(MainActivity.this,"add and image",Toast.LENGTH_LONG).show();
                    return;
                }

                startColoring();
            }
        });

        btnColorize.setClickable(true);

        btnAddPellet.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                useCustomPellet();
            }
        });
        
        boolean isUsingCustomPellet = false;
        progressBar.setVisibility(View.GONE);
        addAdapterToPelletRecyclerView();
        removeCustomPellet();



        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
    }

    private void setOnClickForBtnAdd() {

        if(imagePath==null){
            btnAdd.setIcon(getDrawable(R.drawable.ic_twotone_add_circle_24));
            btnAdd.setText("Add");

            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pickImage();
                }
            });

        }else{
            btnAdd.setIcon(getDrawable(R.drawable.ic_twotone_remove_circle_24));
            btnAdd.setText("Remove");

            btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imagePath = null;
                    isColoring = false;
                    stopThread();
                    photoView.setImageResource(R.color.black);
                    setOnClickForBtnAdd();
                }
            });
        }

        btnColorize.setText("Color");
        btnColorize.setClickable(true);
    }



    void setTextForBtnColorize(){
        if (isColoring){
            btnColorize.setText("Please wait");
            btnColorize.setClickable(false);
        }else {
            btnColorize.setText("Colorize");
            btnColorize.setClickable(true);


        }


    }

    private void startColoring() {
            exampleRunnable = new ExampleRunnable();
            new Thread(exampleRunnable).start();
    }


    private void removeCustomPellet() {
        isUsingCustomPellet = false;
        layoutCustomPellet.setVisibility(View.GONE);
        fadePelletRecyclerView.setVisibility(View.GONE);
    }
    private void useCustomPellet() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        startActivityForResult(intent,4);


    }

    private void addAdapterToPelletRecyclerView() {
        pelletRecViewAdapter = new PelletRecViewAdapter(MainActivity.this);
        AssetManager assetManager = MainActivity.this.getAssets();
        try {
            pelletPaths = assetManager.list("pellets");
            for(int i =0;i<pelletPaths.length;i++){
                pelletPaths[i] = "pellets/"+pelletPaths[i];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        pelletRecViewAdapter.setpellets(pelletPaths);

        pelletRecyclerView.setAdapter(pelletRecViewAdapter);
        System.out.println(pelletRecViewAdapter.getItemCount());
        pelletRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL,false));
    }



    private void pickImage() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        startActivityForResult(intent,0);//SELECT_PICTURES is simply a global int used to check the calling intent in onActivityResult

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


//        TODO : for multiple Imageset
        if(resultCode==RESULT_OK && (requestCode==0)){
            if(data.getData() != null) {
                imagePath = data.getData();

                try {
                    InputStream inputStream = getContentResolver().openInputStream(data.getData());
                    image = BitmapFactory.decodeStream(inputStream);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Glide.with(MainActivity.this)
                        .asBitmap()
                        .load(image)
                        .into(photoView);


                System.out.println("here");
                System.out.println(imagePath.toString());

                setOnClickForBtnAdd();



            }
        }

        if (resultCode == RESULT_OK && requestCode == 4) {
            if (data != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(data.getData());
                    customPellet = BitmapFactory.decodeStream(inputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Glide.with(MainActivity.this)
                        .asBitmap()
                        .load(customPellet)
                        .into(imgCustomPellet);

                layoutCustomPellet.setVisibility(View.VISIBLE);
                fadePelletRecyclerView.setVisibility(View.VISIBLE);

                btnRemoveCustomPellet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeCustomPellet();
                    }
                });

                isUsingCustomPellet = true;
            }


        }








    }

    class ExampleRunnable implements Runnable {


        public boolean isShowingResult() {
            return isShowingResult;
        }

        public void setShowingResult(boolean showingResult) {
            isShowingResult = showingResult;
        }

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    preExecute();
                }
            });

            InputStream inputStream;
            Bitmap longBitmap = Bitmap.createBitmap(512,512*2, Bitmap.Config.ARGB_8888);

            System.out.println("lenght:");


            File pageFile;
            OutputStream outputStream;
            Interpreter tflite = null;
            InputStream essentialInputStream = null;
            TensorImage inputImageBuffer;
            TensorBuffer outputImageBuffer;
            Bitmap pelletBitmap = Bitmap.createBitmap(512,512, Bitmap.Config.ARGB_8888);
            int[] pixels = new int[512*512];

            try {
                if(isUsingCustomPellet==false){
                    inputStream = getAssets().open("pellets/" + new DecimalFormat("00").format(pelletRecViewAdapter.getCheckedItemPosition()) + ".png");
                    pelletBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(inputStream), 512, 512, false);
                }else {
                    pelletBitmap = Bitmap.createScaledBitmap(customPellet, 512, 512, false);

                }
                pelletBitmap.getPixels(pixels,0,512,0,0,512,512);
                longBitmap.setPixels(pixels,0,512,0,512,512,512);

                inputStream = MainActivity.this.getContentResolver().openInputStream(imagePath);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                height = bitmap.getHeight();
                width = bitmap.getWidth();

                if(height>width){
                    height = (height*512)/width;
                    width = 512;
                }else {
                    width = (width*512)/height;
                    height = 512;
                }

                Bitmap imageBimap = Bitmap.createBitmap(512,512, Bitmap.Config.ARGB_8888);

                resize(bitmap,imageBimap);

                imageBimap.getPixels(pixels,0,512,0,0,512,512);
                longBitmap.setPixels(pixels,0,512,0,0,512,512);

            } catch (IOException e) {
                e.printStackTrace();
            }

//            TODO remember to remove it
//            try {
//                essentialInputStream = TrySampleActivity.this.getAssets().open("tflite_model_dir/essential.json");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            JsonArray array = (JsonArray) JsonParser.parseReader(new InputStreamReader(essentialInputStream));
//            int[] essentials = new int[512*256];
//
//            Gson gson = new Gson();
//            essentials = gson.fromJson(array,essentials.getClass());
//            longBitmap.setPixels(essentials,0,512,0,1024,512,256);



            try {
                tflite = new Interpreter( loadModelFile().asReadOnlyBuffer());
            } catch (IOException e) {
                e.printStackTrace();
            }

//            TODO:here we will initialise the inut and output tensors
            DataType imageDataType = tflite.getInputTensor(0).dataType();
            inputImageBuffer = new TensorImage(imageDataType);
            inputImageBuffer.load(longBitmap);
            outputImageBuffer = TensorBuffer.createFixedSize(tflite.getOutputTensor(0).shape(),tflite.getOutputTensor(0).dataType());

            inputImageBuffer.load(longBitmap);

//                    TODO: running the tflite model
            tflite.run(inputImageBuffer.getBuffer().rewind(), outputImageBuffer.getBuffer().rewind());

//                    TODO: getting the output

            pixels = outputImageBuffer.getIntArray();

            System.out.println("pixels");
            System.out.println(pixels.toString());





            bitmap = Bitmap.createBitmap(512,512, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels,0,512,0,0,512,512);

            Bitmap temp = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
            resize(bitmap,temp);
            bitmap = temp.copy(Bitmap.Config.ARGB_8888,true);



            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(isShowingResult){
                        postExecute();
                    }

                }
            });
        }

        private MappedByteBuffer loadModelFile() throws IOException {
            AssetFileDescriptor fileDescriptor = MainActivity.this.getAssets().openFd("tflite_model_dir/model.tflite");
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLenght = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLenght);
        }
    }

    private void postExecute() {
        progressBar.setVisibility(View.GONE);
        isColoring = false;

        photoView.setImageBitmap(bitmap);

        btnColorize.setText("Color");
        btnColorize.setClickable(true);
    }

    void stopThread() {
        isShowingResult = false;
        progressBar.setVisibility(View.GONE);
        isColoring = false;
        setTextForBtnColorize();

    }

    private void preExecute() {
        isShowingResult = true;
        progressBar.setVisibility(View.VISIBLE);
        isColoring = true;
        setOnClickForBtnAdd();
        setTextForBtnColorize();
    }




    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

    private native void resize(Bitmap bitmapIn,Bitmap bitmapOut);

}