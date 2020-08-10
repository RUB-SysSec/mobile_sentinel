package com.bedrankarakoc.mobilesentinel;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class PacketViewerActivity extends AppCompatActivity {

    private static String content;
    private static String packetName;
    private String searchItem;
    private String replaceString;
    private int startIndex;
    private TextView textView;
    private TextView actionBarTitle;
    private ImageButton searchButton;
    private Button nextSearchItemButton;
    private Context mContext;
    private ScrollView scrollView;
    private JSONObject contentJson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_packet_viewer);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.packet_viewer_abs);
        actionBarTitle = (TextView) findViewById(R.id.packetViewerActionBarTitle);
        Bundle extras = getIntent().getExtras();
        textView = (TextView) findViewById(R.id.textView2);
        searchButton = (ImageButton) findViewById(R.id.packetSearchButton);
        nextSearchItemButton = (Button) findViewById(R.id.nextSearchItemButton);
        nextSearchItemButton.setVisibility(View.INVISIBLE);
        textView.setMovementMethod(new ScrollingMovementMethod());
        scrollView = (ScrollView) findViewById(R.id.packetScrollView);
        mContext = getApplicationContext();
        searchButtonListener();
        nextSearchItemButtonListener();


        if (extras != null) {
            content = extras.getString("content");
            packetName = extras.getString("packetName");


        }

        try {
            actionBarTitle.setText(packetName);
            contentJson = new JSONObject(content);
            textView.setText(contentJson.toString(4));

        } catch (Throwable tx) {
            tx.printStackTrace();
            textView.setText("Malformed packet");
        }


    }

    private void showAddItemDialog(Context c) {
        final EditText searchEditText = new EditText(c);
        searchEditText.setHint("Tap to search string");

        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Search String (Case-Sensitive !)")
                .setView(searchEditText)
                .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        try {

                            nextSearchItemButton.setVisibility(View.VISIBLE);
                            replaceString = contentJson.toString(4);
                            searchItem = String.valueOf(searchEditText.getText());
                            startIndex = replaceString.indexOf(searchItem);
                            if (startIndex < 0) {
                                Toast.makeText(getApplicationContext(), "String not found", Toast.LENGTH_LONG).show();
                            } else {
                                nextSearchItemButton.setVisibility(View.VISIBLE);

                                int textLine = textView.getLayout().getLineForOffset(startIndex);
                                SpannableString spannableString = new SpannableString(contentJson.toString(4));
                                ForegroundColorSpan fcsRed = new ForegroundColorSpan(Color.GREEN);
                                RelativeSizeSpan relativeSizeSpan = new RelativeSizeSpan(1.5f);
                                spannableString.setSpan(relativeSizeSpan, startIndex, startIndex + searchItem.length(), 0);
                                spannableString.setSpan(fcsRed, startIndex, startIndex + searchItem.length(), Spanned.SPAN_COMPOSING);
                                textView.setText(spannableString);
                                scrollView.scrollTo(0, textView.getLayout().getLineTop(textLine));

                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    public void searchButtonListener() {
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddItemDialog(PacketViewerActivity.this);
            }
        });
    }

    public void nextSearchItemButtonListener() {
        nextSearchItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (startIndex > 0) {
                    try {
                        replaceString = contentJson.toString(4);
                        startIndex = replaceString.indexOf(searchItem, startIndex + searchItem.length());
                        int textLine = textView.getLayout().getLineForOffset(startIndex);
                        SpannableString spannableString = null;
                        spannableString = new SpannableString(contentJson.toString(4));
                        ForegroundColorSpan fcsRed = new ForegroundColorSpan(Color.GREEN);
                        RelativeSizeSpan relativeSizeSpan = new RelativeSizeSpan(1.5f);
                        if (startIndex > 0) {
                            spannableString.setSpan(relativeSizeSpan, startIndex, startIndex + searchItem.length(), 0);
                            spannableString.setSpan(fcsRed, startIndex, startIndex + searchItem.length(), Spanned.SPAN_COMPOSING);
                            textView.setText(spannableString);
                            scrollView.scrollTo(0, textView.getLayout().getLineTop(textLine));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
                
            }
        });
    }
}
