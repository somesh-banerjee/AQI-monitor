package com.somesh.aqimonitor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final double PIC_WIDTH = 425;

    WebView wb;
    AutoCompleteTextView place;
    TextView resulttv,aqicat,aqicatt;
    Button btn;
    ConstraintLayout layout;

    String places[] = {"IIIT Naya Raipur","Virtual Place 1"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (Button) findViewById(R.id.button);
        wb = (WebView) findViewById(R.id.web);
        place = (AutoCompleteTextView) findViewById(R.id.place);
        resulttv = (TextView) findViewById(R.id.result);
        aqicat = (TextView) findViewById(R.id.Aqicat);
        aqicatt = (TextView) findViewById(R.id.aqicatt);

        wb.getSettings().setJavaScriptEnabled(true);
        wb.setInitialScale(getScale());
        wb.setBackgroundColor(Color.TRANSPARENT);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.select_dialog_item, places);
        //Set the number of characters the user must type before the drop down list is shown
        place.setThreshold(0);
        //Set the adapter
        place.setAdapter(adapter);

        layout =(ConstraintLayout) findViewById(R.id.bgmain);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = place.getText().toString();
                wb.setVisibility(View.VISIBLE);
                if(str.equalsIgnoreCase("IIIT NR") || str.equalsIgnoreCase("iiitnr") || str.equalsIgnoreCase("iiit naya raipur") || str.equalsIgnoreCase("iiit-nr")) {
                    wb.loadUrl("https://thingspeak.com/channels/1007906/charts/1?bgcolor=%23ffffff&color=%23d62020&dynamic=true&results=60&type=spline");
                    String url = "https://api.thingspeak.com/channels/1007906/fields/1.json?results=1";
                    new JsonTask().execute(url);
                } else if (str.equalsIgnoreCase("Virtual Place 1")|| str.equalsIgnoreCase("vp1")){
                    wb.loadUrl("https://thingspeak.com/channels/1087032/charts/1?bgcolor=%23ffffff&color=%23d62020&dynamic=true&results=60&type=spline&update=15");
                    String url = "https://api.thingspeak.com/channels/1087032/fields/1.json?results=1";
                    new JsonTask().execute(url);
                } else {
                    Toast.makeText(MainActivity.this,"Location doesn't exist on Database",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private int getScale(){
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        Double val = new Double(width)/new Double(PIC_WIDTH);
        val = val * 100d;
        return val.intValue();
    }

    private void changeBg(long aqi) {
        if (aqi <= 50) {
            layout.setBackgroundResource(R.drawable.bggreen);
        } else if (aqi <= 100) {
            layout.setBackgroundResource(R.drawable.bgyellow);
        } else if (aqi <= 200) {
            layout.setBackgroundResource(R.drawable.bgorange);
        } else if (aqi <= 300) {
            layout.setBackgroundResource(R.drawable.bgred);
        } else if (aqi <= 400) {
            layout.setBackgroundResource(R.drawable.bgpurple);
        } else {
            layout.setBackgroundResource(R.drawable.bgmaroon);
        }
        setCat(aqi);
    }

    private  void setCat(long aqi){
        aqicatt.setVisibility(View.VISIBLE);
        aqicat.setVisibility(View.VISIBLE);
        if (aqi <= 50) {
            aqicatt.setText("Good(0-50)");
        } else if (aqi <= 100) {
            aqicatt.setText("Satisfactory (51–100)");
        } else if (aqi <= 200) {
            aqicatt.setText("Moderately polluted (101–200)");
        } else if (aqi <= 300) {
            aqicatt.setText("Poor (201–300)");
        } else if (aqi <= 400) {
            aqicatt.setText("Very poor (301–400)");
        } else {
            aqicatt.setText("Severe (401+)");
        }

    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            String aqi = null;
            long AQI = -1;
            try {
                JSONObject json = new JSONObject(String.valueOf(result));
                JSONArray jarr = json.getJSONArray("feeds");
                JSONObject obj = jarr.getJSONObject(0);
                aqi = obj.getString("field1");
                String str = stripNonDigits(aqi);
                AQI = Long.parseLong(str);
                //Toast.makeText(MainActivity.this,String.valueOf(AQI),Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                aqi = e.getMessage();
            }
            changeBg(calAQI(AQI));
            resulttv.setText(String.valueOf(calAQI(AQI)));

        }
    }

    private float calCOppm(long aqi){
        double rs,ratio;
        rs = 10*(6204.5 - aqi)/aqi;
        ratio = rs/76.63;
        return Float.valueOf(String.valueOf(ratio));
    }

    private long calAQI(long Aqi){
        long res;
        float aqi = calCOppm(Aqi);
        if(aqi<=2.0)
            res = (long) (50*aqi);
        else if(aqi<=10)
            res= (long) ((100/8)*aqi);
        else if(aqi<=17)
            res= (long) ((100/7)*aqi);
        else if(aqi<=34)
            res= (long) ((100/17)*aqi);
        else
            res = (long) (aqi*11.79);
        return res;
    }

    public static String stripNonDigits(
            final CharSequence input ){
        final StringBuilder sb = new StringBuilder(
                input.length() );
        for(int i = 0; i < input.length(); i++){
            final char c = input.charAt(i);
            if(c > 47 && c < 58){
                sb.append(c);
            }
        }
        return sb.toString();
    }
}