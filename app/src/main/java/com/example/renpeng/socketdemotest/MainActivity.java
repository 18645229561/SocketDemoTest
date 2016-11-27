package com.example.renpeng.socketdemotest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int MESSAGE_RECEIVE_NEW_MSG= 1;

    private static final int MESSAGE_SOCKET_CONNECTION = 2;

    private TextView mTextView;

    private EditText mEditText;

    private Button mButton;

    private Socket client;

    private PrintWriter mPrintWriter;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_RECEIVE_NEW_MSG:
                    mButton.setEnabled(true);
                    break;
                case MESSAGE_SOCKET_CONNECTION:
                    mTextView.setText(mTextView.getText() + (String)msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.text);
        mEditText = (EditText) findViewById(R.id.edit);
        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(this);
        Intent intent = new Intent(this,TcpSocketService.class);
        startService(intent);
        new Thread(){
            @Override
            public void run() {
                connectionTcpServer();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        if(client != null){
            try {
                client.shutdownInput();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if(v == mButton){
            final String msg = mEditText.getText().toString();
            if(!TextUtils.isEmpty(msg)){
                mPrintWriter.println(msg);
                mEditText.setText("");
                String time = formatTCPServer(System.currentTimeMillis());
                final String showMsg = "self" + time + ":" + msg + "\n";
                mTextView.setText(mTextView.getText() + "\n"+showMsg);
            }
        }
    }

    private String formatTCPServer(long time){
        return new SimpleDateFormat("(HH:mm:ss)").format(new Date(time));
    }

    private void connectionTcpServer(){
        Socket socket = null;
        while (socket == null){
            try {
                socket = new Socket("localhost",8688);
                client = socket;

                mPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
            } catch (IOException e) {
                SystemClock.sleep(2000);
                e.printStackTrace();
            }
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (!MainActivity.this.isFinishing()){
                String msg = br.readLine();
                if(msg !=null){
                    String time = formatTCPServer(System.currentTimeMillis());
                    final String showedMsg = "server "+time + ":" + msg;
                    mHandler.obtainMessage(MESSAGE_SOCKET_CONNECTION,showedMsg).sendToTarget();
                }
            }
            br.close();
            mPrintWriter.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
