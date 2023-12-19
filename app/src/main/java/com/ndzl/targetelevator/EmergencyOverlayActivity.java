package com.ndzl.targetelevator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class EmergencyOverlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_emergency_overlay);


        setShowWhenLocked(true);
        setTurnScreenOn(true);

       // addOverlayView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //TEST IN THE EMERGENCY BUTTON SCOPE
        //KEYS ARE CAPTURE ONLY WHEN THIS ACTIVITY IN THE FOREGROUND
        Toast.makeText(this, "EmergencyOverlayActivity Key Pressed "+keyCode, Toast.LENGTH_SHORT).show();
        return super.onKeyDown(keyCode, event);
    }


    private WindowManager wm;
    private Button button;
    private void addOverlayView() {

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        button = new Button(this);
        //button.setBackgroundResource(R.drawable.ic_button1);
        button.setText("ALERT MESSAGE HERE!");
        button.setAlpha(1);
        button.setBackgroundColor(Color.BLUE);
        //button.setOnClickListener(this);

        int type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

/*        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = 0;
        params.y = 0;
        */


        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);


        wm.addView(button, params);
    }
}