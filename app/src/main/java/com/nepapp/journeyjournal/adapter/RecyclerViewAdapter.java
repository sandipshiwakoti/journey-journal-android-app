package com.nepapp.journeyjournal.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.nepapp.journeyjournal.AccountActivity;
import com.nepapp.journeyjournal.BuildConfig;
import com.nepapp.journeyjournal.EditJournalEntryActivity;
import com.nepapp.journeyjournal.HomeActivity;
import com.nepapp.journeyjournal.LoginActivity;
import com.nepapp.journeyjournal.R;
import com.nepapp.journeyjournal.api.ApiInterface;
import com.nepapp.journeyjournal.api.RetrofitClient;
import com.nepapp.journeyjournal.models.JournalEntry;
import com.nepapp.journeyjournal.models.SuccessResponse;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Url;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<JournalEntry> journalEntries;

    public RecyclerViewAdapter(Context context, List<JournalEntry> journalEntries) {
        this.context = context;
        this.journalEntries = journalEntries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.component_card_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JournalEntry journal = journalEntries.get(position);
        holder.textEntryTitle.setText(journal.getTitle());
        holder.textEntryDescription.setText(journal.getDescription());

        TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(journal.getCreatedAt());
        Instant i = Instant.from(ta);
        Date d = Date.from(i);
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy h:mm aa");
        String strDate= formatter.format(d);
        holder.textEntryDate.setText(strDate);


        String image = journal.getImage();
        if (image == null) {
            image = "https://res.cloudinary.com/nepal-cloud/image/upload/v1644651363/doineedit/noimage_mpo6ko.jpg";
        }
        Picasso.get().load(image).into(holder.imgEntry);
    }

    @Override
    public int getItemCount() {
        return journalEntries.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View
            .OnClickListener {
        public TextView textEntryTitle, textEntryDescription, textEntryDate;
        public ImageView imgEntry;
        public ImageView btnCardMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            textEntryTitle = itemView.findViewById(R.id.textEntryTitle);
            textEntryDescription = itemView.findViewById(R.id.textEntryDescription);
            imgEntry = itemView.findViewById(R.id.imgEntry);
            textEntryDate = itemView.findViewById(R.id.textEntryDate);
            btnCardMenu = itemView.findViewById(R.id.btnCardMenu);

            btnCardMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = ViewHolder.this.getAdapterPosition();
                    JournalEntry journal = journalEntries.get(position);
                    String title = journal.getTitle();
                    String description = journal.getDescription();
                    String imageUrl = journal.getImage();

                    BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
                    bottomSheetDialog.setContentView(R.layout.bottom_sheet_layout);
                    LinearLayout btnShareEntry = bottomSheetDialog.findViewById(R.id.btnShareEntry);
                    LinearLayout btnEditEntry = bottomSheetDialog.findViewById(R.id.btnEditEntry);
                    LinearLayout btnDeleteEntry = bottomSheetDialog.findViewById(R.id.btnDeleteEntry);

                    btnShareEntry.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            bottomSheetDialog.dismiss();
                            if(!TextUtils.isEmpty(imageUrl)){
                                Picasso.get().load(imageUrl).into(new Target() {
                                    @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        Intent i = new Intent(Intent.ACTION_SEND);
                                        i.setType("image/*");
                                        i.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap));
                                        i.putExtra(Intent.EXTRA_TEXT, title + "\n" + description);
                                        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        context.startActivity(Intent.createChooser(i, "Share"));
                                    }

                                    @Override
                                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                                    }
                                    @Override public void onPrepareLoad(Drawable placeHolderDrawable) { }
                                });
                            }
                            else {
                                Intent i = new Intent(Intent.ACTION_SEND);
                                i.setType("text/plain");
                                i.putExtra(Intent.EXTRA_TEXT, title);
                                context.startActivity(Intent.createChooser(i, "Share"));
                            }

                        }

                        public Uri getLocalBitmapUri(Bitmap bmp) {
                            Uri bmpUri = null;
                            try {
                                File file =  new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
                                FileOutputStream out = new FileOutputStream(file);
                                bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
                                out.close();
                                bmpUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider",file);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return bmpUri;
                        }
                    });

                    btnEditEntry.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            bottomSheetDialog.dismiss();
                            editEntry();
                        }
                    });

                    btnDeleteEntry.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            bottomSheetDialog.dismiss();
                            removeEntry();
                        }
                    });
                    bottomSheetDialog.show();
                }
            });
        }

        @Override
        public void onClick(View view) {

        }

        public void editEntry(){
            int position = this.getAdapterPosition();
            JournalEntry journal = journalEntries.get(position);
            String id = journal.getId();
            String title = journal.getTitle();
            String image = journal.getImage();
            String description = journal.getDescription();

            // Send data to EditJournalEntry Screen
            Intent intent = new Intent(context, EditJournalEntryActivity.class);
            intent.setAction("");
            intent.putExtra("id", id);
            intent.putExtra("title", title);
            intent.putExtra("image", image);
            intent.putExtra("description", description);
            context.startActivity(intent);
        }

        public void removeEntry() {
            int position = this.getAdapterPosition();
            JournalEntry journal = journalEntries.get(position);
            String id = journal.getId();

            SharedPreferences sharedPreferences = context.getSharedPreferences("LoginSharedPreference", Context.MODE_PRIVATE);
            String token = sharedPreferences.getString("access-token", "");

            // Send API request for deleting entry
            ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
            Call<SuccessResponse> call = apiInterface.deleteJournalEntry("Bearer " + token, id);
            call.enqueue(new Callback<SuccessResponse>() {
                @Override
                public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                    if (response.isSuccessful()) {
                        Log.e("OnSuccessDelete: ", "OnSuccess: " + new Gson().toJson(response.body()));
                        Intent intent = new Intent(context, HomeActivity.class);
                        context.startActivity(intent);
                        Toast.makeText(context, "Entry deleted successfully!",
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
                            Toast.makeText(context, jObjError.getString("message"),
                                    Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<SuccessResponse> call, Throwable t) {
                    Log.e("OnErrorDelete", t.getMessage());
                    Toast.makeText(context, "Error in deletion", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
