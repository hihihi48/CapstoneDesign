package com.example.weatherinfo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URL;


public class SignupActivity extends AppCompatActivity {

    private static String IP_ADDRESS = "211.58.211.39";
    private static String TAG = "signup";

    private EditText mEditTextID;
    private EditText mEditTextName;
    private TextView mTextViewResult;
    private RadioGroup radioGroup;
    private int gender = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mEditTextID = (EditText)findViewById(R.id.et_id);
        mEditTextName = (EditText)findViewById(R.id.et_name);
        radioGroup = (RadioGroup) findViewById(R.id.RadioGroup);
        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);


        mTextViewResult = (TextView)findViewById(R.id.textView_result);

        mTextViewResult.setMovementMethod(new ScrollingMovementMethod());


        Button buttonInsert = (Button)findViewById(R.id.btn_register);
        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gender != 0) {
                    String ID = mEditTextID.getText().toString();
                    String Password = mEditTextName.getText().toString();


                    InsertData task = new InsertData();
                    task.execute("http://" + IP_ADDRESS + "/signup.php", ID,Password, String.valueOf(gender));

                    mEditTextID.setText("");
                    mEditTextName.setText("");

                    SharedPreferences preferences = getSharedPreferences("app_preferences", MODE_PRIVATE);;
                    //Editor를 preferences에 쓰겠다고 연결
                    SharedPreferences.Editor editor = preferences.edit();
                    //putString(KEY,VALUE)
                    editor.putString("signup","OK");
                    editor.putInt("gender", gender);
                    //항상 commit & apply 를 해주어야 저장이 된다.
                    editor.commit();

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            }
        });

    }

    RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            if(i == R.id.radioButton){
                gender = 1;
            }
            else if(i == R.id.radioButton2){
                gender = 2;
            }
        }
    };



    class InsertData extends AsyncTask<String, Void, String>{
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(SignupActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            mTextViewResult.setText(result);
            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {

            String id = (String)params[1];
            String name = (String)params[2];
            String gender_id = (String)params[3];



            String serverURL = (String)params[0];
            String postParameters = "id=" + id + "&name=" + name + "&gender_id=" + gender_id;


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString();


            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }

        }
    }

}

