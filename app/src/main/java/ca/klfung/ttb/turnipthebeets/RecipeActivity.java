package ca.klfung.ttb.turnipthebeets;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class RecipeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView txt_recp = (TextView)findViewById(R.id.txt_recp);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Refreshing Recipe List...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        String result = "";
        //Should have seperate thread for network operations (asyncronous)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {

            URL url = new URL("https://turnipthebeets.herokuapp.com/recommended_recipes");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                urlConnection.setRequestMethod("POST");
                urlConnection.addRequestProperty("Content-Type", "application/json");
                urlConnection.addRequestProperty("Accept", "application/json");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.connect();
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("servings", new Integer(1));

                OutputStreamWriter wr= new OutputStreamWriter(urlConnection.getOutputStream());
                wr.write(jsonObj.toString());
                wr.flush();

                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                result = s.hasNext() ? s.next() : "";


            }catch(Exception e) {
            }
            finally {
                urlConnection.disconnect();
            }
        }catch(Exception e)
        {
        }
        txt_recp.setText(result);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    void GetRecommendedRecipes(){
        /*
         Using a login key and numbers, refresh recipes.
         This should be run oncreate without any recipes
         */
    }
    void PretendGetRecokmmendedRecipes()
    {

    }
}
