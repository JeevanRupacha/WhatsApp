package com.jeevan.whatsapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Constraints;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jeevan.whatsapp.Data.FeedDataEntry;
import com.jeevan.whatsapp.MainActivity;
import com.jeevan.whatsapp.R;
import com.jeevan.whatsapp.WhatsAppMVVC.WhatsAppDataModel;
import com.jeevan.whatsapp.WorkManagerHandler.UploadImageWorkManager;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SettingActivity";
    private static final int GALLERY_PICKER_ID = 1;


    //Fields variables
    private CircleImageView circleProfileImage;
    private EditText username, profileBio;
    private Button updateButton;

    //firebase setup
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private StorageReference profileImageStorageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

    //loading bar
    private ProgressDialog loading;

    //Android MVVC
    private WhatsAppDataModel whatsAppDataModel;

    //uploading profile image in database
    private CropImage.ActivityResult result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        setUpToolbar();
        initializeFields();

        loadViewModel();


        updateButton.setOnClickListener(this);
        circleProfileImage.setOnClickListener(this);
    }

    private void loadViewModel() {
       whatsAppDataModel.getSingleUserDocument(auth.getCurrentUser().getUid())
       .observe(this, new Observer<Map>() {
           @Override
           public void onChanged(Map map) {
               updateFields(map);
           }
       });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadViewModel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //starts the gallery to pick images
        if(requestCode == GALLERY_PICKER_ID && resultCode == RESULT_OK && data != null)
        {
            Uri imageUri = data.getData();

            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }


        //crop image library activity check
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
             result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                UploadImageAsyncTask uploadImageAsyncTask = new UploadImageAsyncTask();
                uploadImageAsyncTask.execute();

//                Constraints.Builder constraintsBuilder = new Constraints.Builder();
//                constraintsBuilder.setRequiredNetworkType(NetworkType.CONNECTED).build();
//
//                WorkRequest uploadWorkRequest =
//                        new OneTimeWorkRequest.Builder(UploadImageWorkManager.class)
//                                .setConstraints(constraintsBuilder)
//                                .build();
//                WorkManager
//                        .getInstance(this)
//                        .enqueue(uploadWorkRequest);
            }
        }
    }

    public final void uploadProfileImage()
    {
        Uri resultUri = result.getUri();
        final StorageReference filePath = profileImageStorageRef.child(auth.getCurrentUser().getUid() +".jpg");

        filePath.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Log.d(TAG, "onComplete: Success to delete the image");
                }else{
                    Log.d(TAG, "onComplete: Failure to delete the image");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: " + e);
            }
        });

        Bitmap bmp = null;
        try {
            bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assert bmp != null;
        bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
        byte[] imageData = baos.toByteArray();
        //uploading the image
        final UploadTask uploadTask2 = filePath.putBytes(imageData);

        uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Map<String, String> profilePic = new HashMap<>();

                        //url.substring(0,url.indexOf("&"))
                        profilePic.put("profileImageSrc", String.valueOf(uri));

                        //upload image uri into users imageProfileLink
                        DocumentReference docRef = db.collection("Users")
                                .document(auth.getCurrentUser().getUid());


                        docRef.set(profilePic,SetOptions.merge())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {

                                            Log.d(TAG, "onComplete: Success upload image uri");
                                        }else{
                                            Log.d(TAG, "onComplete: fail to upload image uri");
                                        }
                                    }
                                });

//                        if(loading != null)
//                        {
//                            loading.dismiss();
//                        }

                    }
                });
            }
        });


    }

    private void updateUI() {
        //updates the editable fields like user name user bio etc

    }



    private void updateFields(Map user)
    {

        //update Fields
        String usernameText = String.valueOf(user.get(FeedDataEntry.USERNAME));
        String profileBioText = String.valueOf(user.get(FeedDataEntry.PROFILE_BIO));
        String profileImageSrc = String.valueOf(user.get(FeedDataEntry.PROFILE_IMAGE_CIRCLE_SOURCE));
        username.setText(usernameText);

        profileBio.setText(profileBioText);

        if(profileImageSrc != null)
        {
            Picasso.get().load(profileImageSrc).into(circleProfileImage, new Callback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "onSuccess: profile image");
                }

                @Override
                public void onError(Exception e) {
                    Log.d(TAG, "onError: profile image " + e);
                }
            });
        }
    }



    private void initializeFields()
    {
        circleProfileImage = findViewById(R.id.profile_image_settings);
        username = findViewById(R.id.edit_username);
        profileBio = findViewById(R.id.edit_profile_bio);
        updateButton = findViewById(R.id.settings_update_button);

        //android jetPack viewModel live data component architecture
        whatsAppDataModel = new ViewModelProvider(this).get(WhatsAppDataModel.class);
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        toolbar.setTitle(R.string.setting_titles);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(SettingActivity.this, MainActivity.class));
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.settings_update_button : updateUserProfile();
            break;
            case R.id.profile_image_settings: selectProfileImage();
            break;
            default:break;
        }
    }

    private void selectProfileImage() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_PICKER_ID);
    }

    private void updateUserProfile() {

        String usernameMap = username.getText().toString();
        String profileBioMap = profileBio.getText().toString();

        HashMap<String, String> data = new HashMap<>();

        data.put("userID",auth.getCurrentUser().getUid());
        data.put("username",usernameMap);
        data.put("profileBio",profileBioMap);

        db.collection("Users")
                .document(auth.getCurrentUser().getUid())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SettingActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onSuccess: Success to add document");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Fail to update user data in Users collections error= " +e);
                    }
                });
    }


    private void loadingBar()
    {
        loading = new ProgressDialog(this);
        loading.setCancelable(false);
        loading.setMessage("Uploading File...");
        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loading.setTitle("Wait..");
        loading.show();
    }


    public class UploadImageAsyncTask extends AsyncTask<Void, Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            uploadProfileImage();
            return null;
        }
    }


}