package com.example.wastespotapp;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        linearLayout = findViewById(R.id.linearLayout);

//        final String receivedUserName = ParseUser.getCurrentUser().getUsername();
        final String receivedUserName = (String) ParseUser.getCurrentUser().get("riderOrDriver");
        FancyToast.makeText(this, receivedUserName, FancyToast.LENGTH_SHORT, FancyToast.INFO, false).show();

        setTitle( "History");

        ParseQuery<ParseObject> parseQuery = new ParseQuery<ParseObject>("Request");
        parseQuery.whereEqualTo("riderOrDriver", receivedUserName);
        parseQuery.orderByDescending("createdAt");

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Loading...");
        dialog.show();

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects.size() > 0 && e == null) {

                    for (ParseObject post : objects) {

                        final String anything = String.valueOf(post.get("image_des"));
                        final String date = String.valueOf(post.get("Date"));
                        final ParseGeoPoint location = (ParseGeoPoint) post.get("location");


                        final TextView information = new TextView(HistoryActivity.this);
                        information.setText("Location : "+location.getLatitude()+", "+location.getLongitude()+"\n"+"\n"+"Date : "+date+"\n"+"\n"+"Additional Info : "+anything);

                        ParseFile postPicture = (ParseFile) post.get("picture");
                        postPicture.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {

                                if (data != null && e == null) {

                                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                    ImageView postImageView = new ImageView(HistoryActivity.this);
                                    LinearLayout.LayoutParams imageView_params =
                                            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                                    imageView_params.setMargins(5, 5, 5, 5);
                                    postImageView.setLayoutParams(imageView_params);
                                    postImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                    postImageView.setImageBitmap(bitmap);

                                    LinearLayout.LayoutParams des_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    des_params.setMargins(5, 5, 5, 15);

                                    information.setLayoutParams(des_params);
                                    information.setGravity(Gravity.LEFT);
                                    information.setBackgroundColor(Color.WHITE);

                                    information.setBackgroundResource(R.drawable.back);
                                    information.setTextColor(Color.BLACK);
                                    information.setTextSize(15f);
                                    information.setPadding(10,10, 10, 10 );

                                    linearLayout.addView(postImageView);
                                    linearLayout.addView(information);
                                }
                            }
                        });
                    }
                } else {

                    FancyToast.makeText(HistoryActivity.this, receivedUserName + " doesn't have any history!", FancyToast.LENGTH_SHORT, FancyToast.CONFUSING, false).show();
                }

                dialog.dismiss();
            }
        });
    }
}