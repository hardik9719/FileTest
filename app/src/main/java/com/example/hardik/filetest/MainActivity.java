package com.example.hardik.filetest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    String FinalLocation;
    TextView tvfilePath,tvreceivingLocation,tvConnectionInfo;
    Button btnreceivingLocation,btnselectFileToSend,btnCreateServer,btnConnectToSever;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvfilePath=findViewById(R.id.tvfilePath);
        tvreceivingLocation=findViewById(R.id.tvreceivingLocation);
        tvConnectionInfo=findViewById(R.id.tvConnectionInfo);

        btnreceivingLocation=findViewById(R.id.btnreceivingLocation);
        btnselectFileToSend=findViewById(R.id.btnselectFileToSend);
        btnCreateServer=findViewById(R.id.btnCreateServer);
        btnConnectToSever=findViewById(R.id.btnConnectToSever);



        btnreceivingLocation.setOnClickListener(this);
        btnselectFileToSend.setOnClickListener(this);
        btnCreateServer.setOnClickListener(this);
        btnConnectToSever.setOnClickListener(this);

        getReadPermssion();
        getWritePermssion();



    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnreceivingLocation:
                SelectDestinationDirectoy();
                break;
            case R.id.btnselectFileToSend:
                fileChooser();
                break;
            case R.id.btnCreateServer:
                Server server=new Server();
                server.execute(FinalLocation);
                break;
            case R.id.btnConnectToSever:
                Client client=new Client();
                client.execute(FinalLocation);
                break;
        }
    }

    //READ_EXTERNAL_STORAGE
    private void getReadPermssion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0x3);

            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0x3);
            }
        } else {
            Toast.makeText(getApplication(), "" + Manifest.permission.READ_EXTERNAL_STORAGE + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }
    private void getWritePermssion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x3);

            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0x3);
            }
        } else {
            Toast.makeText(getApplication(), "" + Manifest.permission.WRITE_EXTERNAL_STORAGE + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }

    private void SelectDestinationDirectoy() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(Intent.createChooser(i, "Choose directory"), 9999);
    }
    private void fileChooser() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    1234);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case 1234:// File select

               Uri fileUri = data.getData();
                String filePath=fileUri.getPath();
                filePath=filePath.substring(filePath.lastIndexOf(":")+1);
                FinalLocation=(Environment
                        .getExternalStorageDirectory()
                        .getAbsolutePath())+"/"+filePath+"/";
                tvfilePath.setText("File Path:"+FinalLocation);
                Log.d("Receiver", "Receiver: "+filePath);
               /* Sender s=new Sender();
                s.execute(Path);*/

                break;
            case 9999: //Directoy select
                if(resultCode==RESULT_OK){
                    Uri directoryUri= data.getData();
                    String receivingPath=directoryUri.getPath();
                    receivingPath=receivingPath.substring(receivingPath.lastIndexOf(":")+1);
                    FinalLocation=(Environment
                            .getExternalStorageDirectory()
                            .getAbsolutePath())+"/"+receivingPath+"/";
                    tvreceivingLocation.setText("Location:"+FinalLocation);



                }
                break;
        }

    }


   class Server extends AsyncTask<String,Void,String>{
        // If your create Server you need to create hotspot

       BufferedOutputStream bufferdOutputStream;
       OutputStream os;
       BufferedInputStream bufferedInputStream;
       Socket cl;
        @Override
        protected String doInBackground(String...  fileLocation) {
            Log.d("Server", "doInBackground:"+fileLocation[0]);
            String loc=fileLocation[0];
           try{
                ServerSocket serverSocket = new ServerSocket(8888);
           //     tvConnectionInfo.setText("Info: Waiting for Connection");
                Log.d("Sever","Waiting for Connection"+serverSocket);
               byte[] buffer = new byte[1024*4];
               byte[] totalData;
                cl= serverSocket.accept();
                Log.d("Server","Connected"+cl.toString());
                bufferdOutputStream=new BufferedOutputStream(cl.getOutputStream());


                File f1=new File(loc);
                DataOutputStream fileSize=new DataOutputStream(cl.getOutputStream());
                fileSize.writeInt((int) f1.length());
                //File f1 = new File("/home/hardik/Downloads/P/sample.jpg");
                bufferedInputStream = new BufferedInputStream(new FileInputStream(f1));
                while(bufferedInputStream.read(buffer)>0){
                    bufferdOutputStream.write(buffer);
                }

                bufferdOutputStream.close();
                bufferedInputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(bufferedInputStream!=null) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (cl!=null) {
                    try {
                        cl.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

       @Override
       protected void onPostExecute(String s) {
           super.onPostExecute(s);
           Toast.makeText(MainActivity.this, "Sent !!!!!!!", Toast.LENGTH_SHORT).show();
       }
   }

    private class Client extends AsyncTask<String,Void,Void> {
        // IF you  are client the you need to connect to hotspot

        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        int bytesRead=0;
        int current = 0;
        @Override
        protected Void doInBackground(String... strings) {
            Socket socket;
            try {
                socket = new Socket("192.168.43.1",8888);
             //   Toast.makeText(MainActivity.this, "Info"+socket.toString(), Toast.LENGTH_SHORT).show();
                Log.d("Client", "doInBackground:"+socket.toString());
                byte[] buffer = new byte[4096];
                byte[] totalData;
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                fileOutputStream = new FileOutputStream("/storage/emulated/0/123.png");
                bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                DataInputStream fileSize=new DataInputStream(socket.getInputStream());
                int filesize=fileSize.readInt();
                int read = 0;
                int totalRead = 0;
                int remaining = filesize;
                while((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                    Log.d("Client", "doInBackground:"+read);
                    totalRead += read;
                    remaining -= read;
                    System.out.println("read " + totalRead + " bytes.");
                    bufferedOutputStream.write(buffer, 0, read);
                }

                bufferedOutputStream.close();
                dis.close();


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(MainActivity.this, "Completed!!!!", Toast.LENGTH_SHORT).show();
            super.onPostExecute(aVoid);
        }
    }
}
