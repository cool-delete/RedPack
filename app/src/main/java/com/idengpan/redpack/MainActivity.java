package com.idengpan.redpack;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView tv;

    private static final String SERVICE_NAME = ".HongbaoService";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.setVisibility(View.GONE);

        tv = (TextView)findViewById(R.id.tv);
        final Button btn = (Button)findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               // btn.animate().alphaBy(1).alpha(0.1f).setDuration(1500).start();
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivityForResult(intent, 0);
            }
        });


        String accessibilityEnabledString = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        Log.d("idengpan","accessibilityEnabledString = " + accessibilityEnabledString);//com.idengpan.redpack/com.idengpan.redpack.RobotService
        if(!accessibilityEnabledString.contains(SERVICE_NAME)){//com.idengpan.redpack/.RobotService
            tv.setText(Html.fromHtml("<font color = gray>服务未开启</font>"));
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("特别提示");
            builder.setMessage("辅助服务未开启，是否前往设置开启，以便能自动抢红包？");
            builder.setPositiveButton("前往设置", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivityForResult(intent, 0);
                }
            });

            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    tv.setText("服务未开启，自动抢红包功能将不可用");
                    Toast.makeText(MainActivity.this, "服务未开启，自动抢红包功能将不可用", Toast.LENGTH_SHORT).show();
                }
            });

            if(false){
                Settings.Secure.putString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "com.idengpan.redpack/com.idengpan.redpack.RobotService");
                Log.e("idengpan","自动开启辅助服务");
            }

            builder.create().show();
        }else{
            tv.setText(Html.fromHtml("<font color = green>服务已开启</font>"));
            Toast.makeText(this,"辅助服务已开启，可以自动抢红包啦！",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0){
            String accessibilityEnabledString = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            Log.d("idengpan","accessibilityEnabledString = " + accessibilityEnabledString);//com.idengpan.redpack/com.idengpan.redpack.RobotService
            if(accessibilityEnabledString.contains(SERVICE_NAME)){
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                tv.setText(Html.fromHtml("<font color = green>服务已开启</font>"));
                Toast.makeText(this,"设置成功，开始自动抢红包啦！",Toast.LENGTH_SHORT).show();
            }else{
                tv.setText(Html.fromHtml("<font color = gray>服务未开启</font>"));
                Toast.makeText(MainActivity.this, "服务未开启，自动抢红包功能将不可用", Toast.LENGTH_SHORT).show();
                if(false){//Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
                    Settings.Secure.putString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "com.idengpan.redpack/com.idengpan.redpack.RobotService");
                    Log.e("idengpan", "自动开启辅助服务");
                    Toast.makeText(MainActivity.this, "已自动为你开启辅助服务", Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //return true;
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, 0);
        }

        return super.onOptionsItemSelected(item);
    }
}
