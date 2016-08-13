package com.faezmurshidiadnan.whatisthisimage;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.makeramen.roundedimageview.RoundedImageView;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    int REQUEST_CAMERA = 0;
    private int PICK_IMAGE_REQUEST = 1;
    private Bitmap bitmap;
    private RoundedImageView ivProfile;
    private FloatingActionButton fab;
    private Uri ur;
    CustomProgressDialog progressDialog;

    //CLOUDVISION API
    private String CLOUD_VISION_API_KEY;
    public static final String FILE_NAME = "temp.jpg";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-RobotoRegular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );


        CLOUD_VISION_API_KEY=getResources().getString(R.string.API_KEY);




        ivProfile = (RoundedImageView) findViewById(R.id.iv_profile);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    callCloudVision(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));

    }
//
//    private void uploadToCLoudVision(Uri ur) {
//        if (ur != null) {
//            try {
//                // scale the image to 800px to save on bandwidth
//                bitmap = scaleBitmapDown(MediaStore.Images.Media.getBitmap(getContentResolver(), ur), 1200);
//                //else{
//                callCloudVision(bitmap);
//                ivProfile.setImageBitmap(bitmap);
//                // }
//
//
//
//            } catch (IOException e) {
//
//                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
//            }
//        } else {  //URI=NULL
//
//            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
//        }
//
//    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Switch text to loading
        // mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        final ProgressDialog loading = ProgressDialog.show(this, "Scanning...", "Please wait...", false, false);
        new AsyncTask<Object, Void, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
//                progressDialog=new CustomProgressDialog(getBaseContext(),"Updating Screen...");
//                progressDialog.show();



            }

            @Override
            protected String doInBackground(Object... params) {

                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(new
                            VisionRequestInitializer(CLOUD_VISION_API_KEY));
                    Vision vision = builder.build();


                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        com.google.api.services.vision.v1.model.Image base64EncodedImage = new com.google.api.services.vision.v1.model.Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        int nh = (int) ( bitmap.getHeight() * (512.0 / bitmap.getWidth()) );
                        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
                        scaled.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("LABEL_DETECTION");//"LABEL_DETECTION"
                            labelDetection.setMaxResults(10);
                            add(labelDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    //Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    // updateFirebase(convertResponseToString(response));
                    return convertResponseToString(response);

                } catch (GoogleJsonResponseException e) {
                    return "Cloud Vision API request failed. Check logs for details.";
                } catch (IOException e) {

                    return "Cloud Vision API request failed. Check logs for details.";
                }

            }
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                //only updateFirebase if there is no error:
                loading.dismiss();
                switch (result){
                    case "Cloud Vision API request failed. Check logs for details.":
                        //error


                        onCloudVisionError();  //displays error message to user
                        break;
                    default:
                        //Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
                        Intent i=new Intent(MainActivity.this,DetailsActivity.class);
                        i.putExtra("det", result);
                        startActivity(i);
                        break;
                }


            }
        }.execute();
    }

    private void onCloudVisionError() {
        Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {

        List<EntityAnnotation> labellabels = response.getResponses().get(0).getLabelAnnotations();
        String message = getEntityAnnotations(labellabels);
        return message;
    }

    private String getEntityAnnotations(List<EntityAnnotation> mlabels) {

        String tempString = "";
        if (mlabels != null) {
            for (EntityAnnotation label : mlabels) {
                tempString += String.format("%.3f: %s", label.getScore(), label.getDescription());
                tempString += "\n";
            }
        } else {
            tempString += "nothing";
        }

        return tempString;
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private void selectFromLibrary() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }

    private void takePhoto() {
        // Camera permissions is already available, show the camera preview.
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            }
        }
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectFromLibrary();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST){
                beginCrop(data.getData());

            }else if (requestCode == REQUEST_CAMERA){

                beginCrop(data.getData());

            }else if (requestCode == Crop.REQUEST_CROP) {
                handleCrop(resultCode, data);
                //upload
                //uploadToCLoudVision(data.getData());

            }
        }
    }

    private void handleCrop(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            System.out.print(resultCode);
            Uri imageUri = Crop.getOutput(data);
            try {
                ImageUtils.normalizeImageForUri(getApplication(), imageUri);
                //bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), imageUri);
                // scale the image to 800px to save on bandwidth
                bitmap = scaleBitmapDown(MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri), 1200);
                //else{
                //callCloudVision(bitmap);
                ivProfile.setImageBitmap(bitmap);


            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(getBaseContext(), Crop.getError(data).getMessage(), Toast.LENGTH_SHORT).show();
        }

        fab.setVisibility(View.VISIBLE);

    }

    private void beginCrop(Uri data) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped.jpg"));
        Crop.of(data, destination).asSquare().start(MainActivity.this, Crop.REQUEST_CROP);


    }


    private boolean isTakePictureAllow() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            }

        }
        else if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
            }
        }
        else if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void add(View view) {
        new BottomSheet.Builder( MainActivity.this).title(getString(R.string.edit_profile)).sheet(R.menu.menu_edit_profile_sheet).listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case R.id.action_photo_capture:
                        if(isTakePictureAllow())
                            takePhoto();
                        break;
                    case R.id.action_photo_gallery:
                        if(isTakePictureAllow())
                            selectFromLibrary();
                        break;
                }
            }
        }).show();
    }
}
