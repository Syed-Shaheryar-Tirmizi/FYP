package com.syed.map_crimes;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class AboutUsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about_us, container, false);

        ImageButton insta = root.findViewById(R.id.insta);
        ImageButton fb = root.findViewById(R.id.fb);
        ImageButton twitter = root.findViewById(R.id.twitter);
        ImageButton call = root.findViewById(R.id.call);
        ImageButton gmail = root.findViewById(R.id.gmail);

        insta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("http://instagram.com/_u/iam_fk7");
                Intent i= new Intent(Intent.ACTION_VIEW,uri);
                i.setPackage("com.instagram.android");

                try {
                    startActivity(i);
                } catch (ActivityNotFoundException e) {

                    Toast.makeText(getContext(), "Please Install Instagram app", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    Uri uri = Uri.parse("fb://profile/100003888752760");
                    Intent i= new Intent(Intent.ACTION_VIEW,uri);
                    startActivity(i);
                }
                catch (Exception e)
                {
                    Toast.makeText(getContext(), "Please Install facebook app", Toast.LENGTH_SHORT).show();
                }

            }
        });

        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=SyedShaheryar_"));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Please Install Twitter app", Toast.LENGTH_SHORT).show();
                }
            }
        });

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:+923044208796"));
                    startActivity(callIntent);
                } catch (ActivityNotFoundException activityException) {
                    Toast.makeText(getContext(), "Please Install Phone app", Toast.LENGTH_SHORT).show();
                }
            }
        });

        gmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Intent intent = new Intent (Intent.ACTION_VIEW , Uri.parse("mailto:" + "itxfk7@gmail.com"));
                    intent.putExtra(Intent.EXTRA_SUBJECT, "your_subject");
                    intent.putExtra(Intent.EXTRA_TEXT, "your_text");
                    startActivity(intent);
                }catch(ActivityNotFoundException e){
                    Toast.makeText(getContext(), "Please Install Email app", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return root;
    }

}