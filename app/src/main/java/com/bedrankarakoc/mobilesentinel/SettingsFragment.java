package com.bedrankarakoc.mobilesentinel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import org.json.JSONException;

public class SettingsFragment extends Fragment {

    private View view;
    private Button changeNumberButton;
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.settings_fragment, container, false);
        changeNumberButton = (Button) view.findViewById(R.id.changeNumberButton);
        mContext = getActivity();
        changeNumberButtonListener();
        return view;
    }



    public void changeNumberButtonListener() {
        changeNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText searchEditText = new EditText(mContext);
                searchEditText.setHint("Custom Number");
                searchEditText.setInputType(InputType.TYPE_CLASS_PHONE);
                AlertDialog dialog = new AlertDialog.Builder(mContext)
                        .setTitle("Add custom target call number")
                        .setView(searchEditText)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SHARED_PREFERENCES", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();

                                editor.putString("CALL_NUMBER", searchEditText.getText().toString());
                                editor.commit();

                            }
                        })
                        .setNegativeButton("Use Test Number", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SHARED_PREFERENCES", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();

                                editor.putString("CALL_NUMBER", "+4915792389038");
                                editor.commit();
                            }
                        })
                        .create();
                dialog.show();



            }
        });
    }
}
