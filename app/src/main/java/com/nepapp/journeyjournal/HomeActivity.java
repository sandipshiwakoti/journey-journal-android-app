package com.nepapp.journeyjournal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.theme.overlay.MaterialThemeOverlay;
import com.google.gson.Gson;
import com.nepapp.journeyjournal.adapter.RecyclerViewAdapter;
import com.nepapp.journeyjournal.api.ApiInterface;
import com.nepapp.journeyjournal.api.RetrofitClient;
import com.nepapp.journeyjournal.models.JournalEntry;
import com.nepapp.journeyjournal.models.JournalResponse;
import com.nepapp.journeyjournal.utils.JWTUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private String token;
    private TextView textUserFullname;
    private TextView textEntryTitle;
    private ImageView imgEntry;
    private RecyclerView recyclerViewEntries;
    private RecyclerViewAdapter recyclerViewAdapter;
    private CircularProgressIndicator progressIndicator;
    private MaterialToolbar topAppBar;
    private List<JournalEntry> entries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sharedPreferences = getSharedPreferences("LoginSharedPreference", Context.MODE_PRIVATE);
        textUserFullname = findViewById(R.id.textUserFullname);

        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        getSupportActionBar().setTitle(null);

        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });


        progressIndicator = findViewById(R.id.progressIndicator);
        progressIndicator.setVisibility(View.VISIBLE);

        recyclerViewEntries = findViewById(R.id.recycleViewEntries);
        recyclerViewEntries.setHasFixedSize(true);
        recyclerViewEntries.setLayoutManager(new LinearLayoutManager(this));

        imgEntry = findViewById(R.id.imgEntry);

        token = sharedPreferences.getString("access-token", "");

        // If authentication token doesn't exist, navigate to Main Screen
        if (token.isEmpty()) {
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            Toast.makeText(HomeActivity.this, "Login authentication required", Toast.LENGTH_SHORT).show();
        }

        try {
            // Decode JWT token to obtain payload- fullname and email
            JSONObject payload = JWTUtils.decoded(token);
            String fullname = payload.getString("fullname");
            String email = payload.getString("email");
            textUserFullname.setText(fullname + " 👋");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Send API request for getting list of all entries
        loadEntryList();

    }

    private void loadEntryList() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        Call<JournalResponse> call = apiInterface.getJournalEntries("Bearer " + token);
        call.enqueue(new Callback<JournalResponse>() {
            @Override
            public void onResponse(Call<JournalResponse> call, Response<JournalResponse> response) {
                progressIndicator.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Log.e("Journal Entry Log", "OnSuccess: " + new Gson().toJson(response.body()));
                    entries = new ArrayList<>();
                    entries = response.body().getData();
                    recyclerViewAdapter = new RecyclerViewAdapter(HomeActivity.this, entries);
                    recyclerViewEntries.setAdapter(recyclerViewAdapter);
                }
            }

            @Override
            public void onFailure(Call<JournalResponse> call, Throwable t) {
                progressIndicator.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Something went wrong. Try again!",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // Navigate to AddJournalEntry Screen
    public void btnNavigateAddJournalEntry(View view) {
        overridePendingTransition(0, 0);
        startActivity(new Intent(HomeActivity.this, AddJournalEntryActivity.class));
        overridePendingTransition(0, 0);
    }

    // Navigate to AppInfo Screen
    public void btnNavigateAppInfo(View view) {
        overridePendingTransition(0, 0);
        startActivity(new Intent(HomeActivity.this, AppInfoActivity.class));
        overridePendingTransition(0, 0);
    }

    // Navigate to Account Screen
    public void btnNavigateAccount(View view) {
        overridePendingTransition(0, 0);
        startActivity(new Intent(HomeActivity.this, AccountActivity.class));
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

    // Manage actions for top menu options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.mi_logout:
                sharedPreferences.edit().remove("access-token").commit();

                Intent intentLogout = new Intent(HomeActivity.this, MainActivity.class);
                intentLogout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intentLogout);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

