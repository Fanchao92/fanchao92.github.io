package com.example.fanchaozhou.optout4;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    private final String resName = "/a4/fanchao";
    private final String paraName = "UserName";
    private final int inputBufSize = 512;
    private TextView httpIndicator;

    public void sendUserInfo(View view){

        EditText editIP = (EditText) findViewById(R.id.editIP);
        EditText editPort = (EditText) findViewById(R.id.editPort);
        EditText editName = (EditText) findViewById(R.id.editName);
        String serverIP = editIP.getText().toString();
        String serverPort = editPort.getText().toString();
        String userName = editName.getText().toString();
        Switch sw = (Switch)findViewById(R.id.switch1);

        if(sw.isChecked()){
            //POST
            new SendPost().execute("http://"+serverIP+":"+serverPort+resName, paraName, userName);
        }else{
            //GET
            try{
                userName = URLEncoder.encode(userName, "UTF-8");
                new SendGet().execute("http://"+serverIP+":"+serverPort+resName, paraName, userName);
            }catch(Exception e){
                httpIndicator.setText(e.toString());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        httpIndicator = (TextView)findViewById(R.id.textView2);
        Switch sw = (Switch) findViewById(R.id.switch1);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ((TextView)findViewById(R.id.textView)).setText("POST"); // POST
                } else {
                    ((TextView)findViewById(R.id.textView)).setText("GET"); // GET
                }
            }
        });
    }

    public class SendPost extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            StringBuffer buffer = new StringBuffer();

            try{
                URL url = new URL(params[ 0 ]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setDoInput(true);

                OutputStreamWriter output = new OutputStreamWriter(connection.getOutputStream());
                output.write(params[ 1 ]+"="+params[ 2 ]);
                output.flush();

                InputStream input = connection.getInputStream();
                byte[] inputBytes = new byte[ inputBufSize ];
                while (input.read(inputBytes) != -1) {
                    buffer.append(new String(inputBytes));
                }

                output.close();
                input.close();
                connection.disconnect();

                return "SUCCESSFUL POST";

            }catch(Exception e){
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String indicator) {
            super.onPostExecute(indicator);

            httpIndicator.setText(indicator);
        }
    }

    public class SendGet extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {
            StringBuffer buffer = new StringBuffer();

            try{
                URL url = new URL(params[ 0 ]+"?"+params[ 1 ]+"="+params[ 2 ]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.connect();

                InputStream input = connection.getInputStream();
                byte[] inputBytes = new byte[ inputBufSize ];
                while (input.read(inputBytes) != -1) {
                    buffer.append(new String(inputBytes));
                }

                input.close();
                connection.disconnect();

                return "SUCCESSFUL GET";
            }catch(Exception e){
                return e.toString();
            }
        }


        @Override
        protected void onPostExecute(String indicator) {
            super.onPostExecute(indicator);

            httpIndicator.setText(indicator);
        }
    }
}
