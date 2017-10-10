package com.catcher;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class VerifySMSPopupService extends Service {
    //Constructor
    public VerifySMSPopupService() {}

    //Intent extra recieved static string
    public static final String AUTH_NUMBER = "auth_number";

    protected WindowManager windowManager;
    protected View popupView;

    WindowManager.LayoutParams  params;
    TextView tv_sms_popup;
    String auth_number;
    Button btn_copy;

    public void onCreate(){
        Log.d("POPUP SERVICE","CREATE SERVICE");
        super.onCreate();

        // Get window width using window manager
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        int width = (int) (display.getWidth() * 0.9);

        // Set window params
        params = new WindowManager.LayoutParams(
                width,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER | Gravity.BOTTOM;
        params.y = 230;

        // Get popup view and views component
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = layoutInflater.inflate(R.layout.sms_popup, null);
        tv_sms_popup = (TextView) popupView.findViewById(R.id.tv_sms_popup);
        btn_copy = (Button) popupView.findViewById(R.id.btn_copy);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.d("POPUP SERVICE","START SERVICE");
        // If get Intent Extras succesfully set text, touch listener and show up popup view
        setExtra(intent);
        if (!TextUtils.isEmpty(auth_number)) {
            tv_sms_popup.setText("인증번호 "+auth_number);
            setTouchListener(btn_copy);
        }

        // Add view
        windowManager.addView(popupView, params);

        // Service will be finished after 6.5sec
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable(){
            @Override
            public void run(){
                removePopup();
            }
        },6500);

        // This service no need to restart, just one way service
        return START_NOT_STICKY;
    }

    // Set TouchListener, verification code copy to clipboard
    public void setTouchListener(Button btn){
        btn.setOnTouchListener( new Button.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent ev){
                switch(ev.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Log.d("TOAST","PRESS");
                        copyToClipboard(getApplicationContext(), auth_number);
                        //Toast.makeText(VerifySMSPopupService.this, "눌림버튼", Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            }
        });
    }

    public void setExtra(Intent intent){
        if(intent == null){
            removePopup();
            return;
        }
        auth_number = intent.getStringExtra(AUTH_NUMBER);
    }

    @SuppressWarnings("deprecation")
    public void copyToClipboard(Context context, String text){
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Verification Code", text);
            clipboard.setPrimaryClip(clip);
        }
        Toast.makeText(context, "복사되었습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removePopup();
    }

    public void removePopup() {
        if (popupView != null && windowManager != null) windowManager.removeView(popupView);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
