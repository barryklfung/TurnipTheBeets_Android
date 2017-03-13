package ca.klfung.ttb.turnipthebeets;

import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.text.InputType;
import android.app.Dialog;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.content.DialogInterface;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ca.klfung.ttb.turnipthebeets.R;

public class InventoryCheck extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_check);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog temp = onCreateDialog(view);
                temp.show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String result = "";
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        List<String> list_item = new ArrayList<String>();
        List<String> list_mass = new ArrayList<String>();
        try {

            URL url = new URL("https://turnipthebeets.herokuapp.com/inventory");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {

                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                result = s.hasNext() ? s.next() : "";

                JSONObject jObject = new JSONObject(result);

                JSONArray jArray = jObject.getJSONArray("inventory");
                for (int i=0; i < jArray.length(); i++)
                {
                    try {
                        JSONObject oneObject = jArray.getJSONObject(i);
                        // Pulling items from the array
                        String oneObjectsItem = oneObject.getString("item");
                        list_item.add(oneObjectsItem);
                        String oneObjectsItem2 = oneObject.getString("mass");
                        list_mass.add(oneObjectsItem2);
                    } catch (JSONException e) {
                        // Oops
                    }
                }

            }catch(Exception e) {
            }
            finally {
                urlConnection.disconnect();
            }
        }catch(Exception e)
        {
        }

        TableLayout t1=(TableLayout)findViewById(R.id.tbl_inventory);
        int size = list_item.size();

        for (int i = 0; i < size; i++) {
            addNewItem(list_item.get(i), list_mass.get(i), t1);
        }
    }

    public void addNewItem(String ingredient, String mass, TableLayout t1) {
        TableRow tr1 = new TableRow(this);
        TableRow.LayoutParams params = new LayoutParams();
        params.span = 2;
        tr1.setLayoutParams(params);
        TextView ing_name = new TextView(this);
        ing_name.setText(ingredient);
        EditText amount = new EditText(this);
        amount.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        amount.setText(mass);
        amount.setGravity(0x05);
        tr1.addView(ing_name);
        tr1.addView(amount);
        t1.addView(tr1, new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
    }

    public Dialog onCreateDialog(View view) {
        List<String> list = new ArrayList<String>();
        String[] ingred_name = { "Apple", "Egg", "Steak", "Orange", "Broccoli", "Cucumber", "Tomato", "Potato", "Banana", "Chicken", "Pork", "Lamb", "Carrot", "Celery", "Onion", "Garlic", "Mushroom", "Spinach", "Cabbage"};
        int size = ingred_name.length;

        LayoutInflater inflater = this.getLayoutInflater();
        View inflatedView = inflater.inflate(R.layout.add_inventory_dialog, null);
        for (int i = 0; i < size; i++) {
            list.add(ingred_name[i]);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner spn=(Spinner)inflatedView.findViewById(R.id.spn_item);
        final EditText et_amount=(EditText)inflatedView.findViewById(R.id.txt_amount);
        final TableLayout t_inv=(TableLayout)findViewById(R.id.tbl_inventory);
        et_amount.setText("5");

        spn.setAdapter(dataAdapter);

        final View vF = view;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflatedView)
                // Add action buttons
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        addNewItem(spn.getSelectedItem().toString(), et_amount.getText().toString(), t_inv);
                        Snackbar.make(vF, "Item Added!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Snackbar.make(vF, "No Item Added!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                    }
                });
        return builder.create();
    }
}
