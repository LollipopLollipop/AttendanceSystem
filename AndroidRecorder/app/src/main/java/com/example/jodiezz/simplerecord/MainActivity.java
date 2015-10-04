package com.example.jodiezz.simplerecord;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends FragmentActivity implements AddNewMemberFragment.AddNewMemberListener{
    public final static String NEW_USER_NAME = "com.example.jodiezz.simplerecord.NEW_USER_NAME";
    DialogFragment newFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void addNewMember(View view){
        newFragment = new AddNewMemberFragment();
        //newFragment.show(getSupportFragmentManager(), "newuser");
        FragmentManager fragmentManager = getFragmentManager();
        newFragment.show(fragmentManager,"new user");
    }

    public void takeAttendance(View view) {
        Intent intent = new Intent(this, RecordActivity.class);
        intent.putExtra("Calling Activity", false);
        startActivity(intent);
    }
    public void proceedToRecord(String nameRecorded) {
        Intent intent = new Intent(this, RecordActivity.class);
        //LayoutInflater layoutInflater = getLayoutInflater();
        //layoutInflater.inflate(R.layout.activity_add_new_user,R.layout.activity_main);
        //EditText firstName = (EditText) findViewById(R.id.first_name);
        //EditText lastName = (EditText) findViewById(R.id.last_name);
        //String message = firstName.getText().toString()+lastName.getText().toString();
        System.out.println("name is " + nameRecorded);
        intent.putExtra(NEW_USER_NAME, nameRecorded);
        intent.putExtra("Calling Activity", true);//1 refers to new user activity
        startActivity(intent);
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String nameRecorded) {
        // User touched the dialog's positive button
        System.out.println("positive click detected");
        proceedToRecord(nameRecorded);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        newFragment.dismiss();

    }

}
