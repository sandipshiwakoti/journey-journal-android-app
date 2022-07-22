package com.nepapp.journeyjournal;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.nepapp.journeyjournal.api.ApiInterface;
import com.nepapp.journeyjournal.api.RetrofitClient;
import com.nepapp.journeyjournal.models.SuccessResponse;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddJournalEntryActivity extends AppCompatActivity {
    private MaterialToolbar topAppBar;
    private TextView textEntryTitle, textEntryDescription;
    private TextInputLayout textEntryTitleLayout, textEntryDescriptionLayout;
    private Button btnChooseEntryImage;
    private Button btnAddEntry;
    private ImageView imgEntryPreview;
    private CircularProgressIndicator progressIndicator;
    private SharedPreferences sharedPreferences;
    private String token;
    private String imageFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journal_entry);

        topAppBar = findViewById(R.id.topAppBar);
        btnAddEntry = findViewById(R.id.btnAddEntry);
        btnChooseEntryImage = findViewById(R.id.btnChooseEntryImage);
        imgEntryPreview = findViewById(R.id.imgEntryPreview);
        textEntryTitle = findViewById(R.id.textEntryTitle);
        textEntryDescription = findViewById(R.id.textEntryDescription);
        textEntryTitleLayout = findViewById(R.id.textEntryTitleLayout);
        textEntryDescriptionLayout = findViewById(R.id.textEntryDescriptionLayout);
        progressIndicator = findViewById(R.id.progressIndicator);

        sharedPreferences = getSharedPreferences("LoginSharedPreference", Context.MODE_PRIVATE);

        token = sharedPreferences.getString("access-token", "");

        if (token.isEmpty()) {
            startActivity(new Intent(AddJournalEntryActivity.this, MainActivity.class));
            Toast.makeText(AddJournalEntryActivity.this, "Login authentication required", Toast.LENGTH_SHORT).show();
        }

//        topAppBar.setOverflowIcon(getDrawable(R.drawable.ic_vertical_bar));
        setSupportActionBar(topAppBar);
        getSupportActionBar().setTitle(null);

        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        imgEntryPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Capture image and display image preview
                ImagePicker.Companion.with(AddJournalEntryActivity.this)
                        .crop()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start();
            }
        });

        btnChooseEntryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Capture image and display image preview
                ImagePicker.Companion.with(AddJournalEntryActivity.this)
                        .crop()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start();
            }
        });

        btnAddEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = textEntryTitle.getText().toString();
                String description = textEntryDescription.getText().toString();
                if (checkValidation()) {
                    progressIndicator.setVisibility(View.VISIBLE);
                    RequestBody titleRequest =
                            RequestBody.create(MediaType.parse("multipart/form-data"), title);
                    RequestBody descriptionRequest =
                            RequestBody.create(MediaType.parse("multipart/form-data"), description);

                    MultipartBody.Part imageRequest = null;

                    if(imageFilePath!=null){
                        File file = new File(imageFilePath);
                        RequestBody requestFile =
                                RequestBody.create(MediaType.parse("multipart/form-data"), file);
                        imageRequest = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
                    }

                    // Send API request for adding entry
                    ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
                    Call<SuccessResponse> call = apiInterface.addJournalEntry("Bearer " + token, titleRequest,descriptionRequest,  imageRequest);
                    call.enqueue(new Callback<SuccessResponse>() {
                        @Override
                        public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                            progressIndicator.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Log.e("AddEntryLog", "OnSuccess: " + new Gson().toJson(response.body()));

                                Intent intent = new Intent(AddJournalEntryActivity.this, HomeActivity.class);
                                finish();
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                Toast.makeText(AddJournalEntryActivity.this, "Journey entry added successfully!",
                                        Toast.LENGTH_LONG).show();

                            } else {
                                String data = null;
                                try {
                                    data = response.errorBody().string();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    JSONObject jObjError = new JSONObject(data);
                                    Toast.makeText(AddJournalEntryActivity.this, jObjError.getString("message"),
                                            Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(AddJournalEntryActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<SuccessResponse> call, Throwable t) {
                            progressIndicator.setVisibility(View.GONE);
                            Log.e("AddEntryLog", t.getMessage());
                            Toast.makeText(AddJournalEntryActivity.this, "Error in adding", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }


    // Navigate to Home Screen
    public void btnNavigateHome(View view) {
        startActivity(new Intent(AddJournalEntryActivity.this, HomeActivity.class));
    }

    // Navigate to AppInfo Screen
    public void btnNavigateAppInfo(View view) {
        overridePendingTransition(0, 0);
        startActivity(new Intent(AddJournalEntryActivity.this, AppInfoActivity.class));
        overridePendingTransition(0, 0);
    }

    // Navigate to Account Screen
    public void btnNavigateAccount(View view) {
        overridePendingTransition(0, 0);
        startActivity(new Intent(AddJournalEntryActivity.this, AccountActivity.class));
        overridePendingTransition(0, 0);
    }

    // Exit the application
    public void btnExitApp(View view) {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    // Display top menu option icon
    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_menu, menu);

        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.mi_logout:
                sharedPreferences.edit().remove("access-token").commit();
                Intent intentLogout = new Intent(AddJournalEntryActivity.this, MainActivity.class);
                intentLogout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentLogout);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = data.getData();
        imageFilePath = uri.getPath();
        Log.e("ImageLog", uri.getPath().toString());
        imgEntryPreview.setImageURI(uri);
    }

    // Check validation
    private boolean checkValidation(){
        boolean isValid = true;

        if(textEntryTitle.length() == 0){
            textEntryTitleLayout.setError(getString(R.string.textBlankJourneyTitle));
            isValid = false;
        }
        if(textEntryDescription.length() == 0){
            textEntryDescriptionLayout.setError(getString(R.string.textBlankJourneyDescription));
            isValid = false;
        }
        return isValid;
    }

}