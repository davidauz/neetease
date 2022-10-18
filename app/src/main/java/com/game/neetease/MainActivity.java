package com.game.neetease;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    TextView m_log_tw;
    private String m_source_path
    ,   m_dest_path
    ;
    public static final String[] strNeedPermissions=
    {   Manifest.permission.WRITE_EXTERNAL_STORAGE
    ,   Manifest.permission.READ_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText et_sourcePath = findViewById(R.id.et_source_path)
        ,   et_destPath = findViewById(R.id.et_dest_path)
        ;

        m_log_tw = findViewById(R.id.log_tw);
        m_log_tw.setMovementMethod(new ScrollingMovementMethod());
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
//        settings.registerOnSharedPreferenceChangeListener(this);
        m_source_path = settings.getString("source_path", "/storage/emulated/0/netease/cloudmusic/Cache/Music1");
        m_dest_path = settings.getString("dest_path", "/storage/emulated/0/download");

        et_sourcePath.setText(m_source_path);
        et_destPath.setText(m_dest_path);

        et_sourcePath.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange (View v, boolean hasFocus) {
                if( ! hasFocus ) { // lost focus
                    String strValue=et_sourcePath.getText().toString();
                    if( ! strValue.equals(m_source_path)){
                        settings.edit().putString("source_path", strValue)
                        .apply();
                    }
                }
            }
        });

        et_destPath.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange (View v, boolean hasFocus) {
                if( ! hasFocus ) { // lost focus
                    String strValue=et_destPath.getText().toString();
                    if( ! strValue.equals(m_dest_path)){
                        settings.edit().putString("dest_path", strValue)
                        .apply();
                    }
                }
            }
        });

        for( String str : strNeedPermissions)
            if (ContextCompat.checkSelfPermission(this, str)!= PackageManager.PERMISSION_GRANTED)
                m_log_tw.append("permission `"+str+"` MISSING!\n");

        File dir = new File(m_source_path);
        File[] list_of_files = dir.listFiles();
        int n_audio_files=0;
        if (null == list_of_files)
            m_log_tw.append("No files in cache");
        else {
            int n_total_files = list_of_files.length;
            for (File aFile : list_of_files)
                if(aFile.getName().endsWith(".uc!"))
                    n_audio_files++;

            m_log_tw.append("Found "+n_total_files+" files, ");
            m_log_tw.append(""+n_audio_files +" possible audio files.\n");
        }
    }



    public void do_stuff(View view) {
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle reply = msg.getData();
                String strR=(String)reply.getSerializable(convertThread.UI_MESSAGE);
                m_log_tw.append(strR);
            }
        };
        convertThread ct = new convertThread(new Messenger(handler));
        ct.startServer(this.getApplicationContext());
    }

    public void onExit(View view) {
        finish();
        System.exit(0);
    }
}
