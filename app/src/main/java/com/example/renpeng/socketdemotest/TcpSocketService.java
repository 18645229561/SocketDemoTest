package com.example.renpeng.socketdemotest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * Created by renpeng on 16/11/27.
 */
public class TcpSocketService extends Service{

    private boolean mIsServiceDestory = false;

    private String[] mDefineMessage = new String[]{
            "你好啊,","请问你叫森么","今天天气增么样","are you ok"
    };


    @Override
    public void onCreate() {
        new Thread(new TcpServer()).start();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mIsServiceDestory = true;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class TcpServer implements Runnable{

        @Override
        public void run() {
            ServerSocket serverSocket = null;

            try {
                serverSocket = new ServerSocket(8688);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            while (!mIsServiceDestory){
                try {
                    final Socket client = serverSocket.accept();

                    new Thread(){
                        @Override
                        public void run() {
                            try {
                                responseClient(client);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void responseClient(Socket client) throws IOException{
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())),true);

        out.println("欢迎来到聊天室");

        while (!mIsServiceDestory){
            String str = in.readLine();
            if(str == null){
                break;
            }
            int i = new Random().nextInt(mDefineMessage.length);
            String msg = mDefineMessage[i];

            out.println(msg);
        }

        in.close();
        out.close();
        client.close();
    }


}
