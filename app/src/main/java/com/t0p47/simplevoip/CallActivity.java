package com.t0p47.simplevoip;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;

/**
 * Created by 02 on 30.06.2017.
 */

public class CallActivity extends ActionBarActivity {

    private static final String APP_KEY = "6eb7b6bb-f716-466e-810b-ab4e38029e72";
    private static final String APP_SECRET = "/Aub5GfDBEasVHuj59rJ2Q==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";

    private Call call;
    private TextView callState;
    private SinchClient sinchClient;
    private Button button;
    private String callerId;
    private String recipientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call);

        Intent intent = getIntent();
        callerId = intent.getStringExtra("callerId");
        recipientId = intent.getStringExtra("recipientId");

        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId("current-user-id")
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .build();

        sinchClient.setSupportCalling(true);
        sinchClient.startListeningOnActiveConnection();
        sinchClient.start();

        sinchClient.getCallClient().addCallClientListener(new SinchCallClientListener());

        button = (Button) findViewById(R.id.button);
        callState = (TextView) findViewById(R.id.callState);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(call == null){
                    call = sinchClient.getCallClient().callUser("call-recipient-id");
                    call.addCallListener(new SinchCallListener());
                    button.setText("Hang Up");
                }else{
                    call.hangup();
                }
            }
        });
    }

    private class SinchCallListener implements CallListener{

        @Override
        public void onCallProgressing(Call call) {
            //Call is ringing
            callState.setText("Ringing");
        }

        @Override
        public void onCallEstablished(Call call) {
            //Incoming call was picked up
            callState.setText("connected");
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallEnded(Call call) {
            //Call ended by either party
            call = null;
            button.setText("Call");
            callState.setText("");
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {
            //Don't worry about this right now
        }
    }

    private class SinchCallClientListener implements CallClientListener{

        @Override
        public void onIncomingCall(CallClient callClient, Call incomingCall) {
            //Pick up the call!
            call = incomingCall;
            call.answer();
            call.addCallListener(new SinchCallListener());
            button.setText("Hang Up");
        }
    }

}
