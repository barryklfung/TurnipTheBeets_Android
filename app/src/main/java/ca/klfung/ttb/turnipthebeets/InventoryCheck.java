package ca.klfung.ttb.turnipthebeets;

import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import android.widget.SearchView;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
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

    HashMap<String, EditText> cur_values = new HashMap<String, EditText>();

    final List<String> list_item = new ArrayList<String>();
    final List<String> list_mass = new ArrayList<String>();
    boolean showDialog = false;

    HashMap<String, TableRow> ingred_table = new HashMap<String, TableRow>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_check);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        get_inventory(list_item, list_mass);

        FloatingActionButton fab_add = (FloatingActionButton) findViewById(R.id.fab_add);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog temp = onCreateAddDialog(view, list_item, list_mass);
                temp.show();
            }
        });

        FloatingActionButton fab_push = (FloatingActionButton) findViewById(R.id.fab_push);
        fab_push.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog = true;
                Dialog temp = onCreatePushDialog(view);
                if ( showDialog ){
                    temp.show();
                }
                else{
                    Snackbar.make(view, "No changes from the database!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        SearchView sv = (SearchView) findViewById(R.id.sv_inventory);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                for (HashMap.Entry<String, TableRow> entry : ingred_table.entrySet()) {
                    if (entry.getKey().toLowerCase().contains(newText.toLowerCase())){
                        entry.getValue().setVisibility(View.VISIBLE);
                    }
                    else {
                        entry.getValue().setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TableLayout t1=(TableLayout)findViewById(R.id.tbl_inventory);
        int size = list_item.size();

        for (int i = 0; i < size; i++) {
            addNewItem(list_item.get(i), list_mass.get(i), t1);
        }
    }

    public void get_inventory(List<String> list_item, List<String> list_mass) {

        try {

            URL url = new URL("https://turnipthebeets.herokuapp.com/inventory");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {

                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                String result = "";
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
    }

    public void push_inventory(List<String> change_item, List<Double> change_mass) {

        for (int i = 0; i < change_item.size(); i++) {
            try {

                URL url = new URL("https://turnipthebeets.herokuapp.com/inventory/");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {

                    urlConnection.setRequestMethod("POST");
                    urlConnection.addRequestProperty("Content-Type", "application/json");
                    urlConnection.addRequestProperty("Accept", "application/json");
                    urlConnection.setDoOutput(true);

                    urlConnection.connect();
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("item", change_item.get(i));
                    jsonObj.put("mass", change_mass.get(i));

                    OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
                    wr.write(jsonObj.toString());
                    wr.flush();

                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                    String result = s.hasNext() ? s.next() : "";

                } catch (Exception e) {
                } finally {
                    urlConnection.disconnect();
                }
            }catch(Exception e)
            {
            }
        }
    }


    public void addNewItem(String ingredient, String mass, TableLayout t1) {
        EditText txt = cur_values.get(ingredient.toLowerCase());
        mass = String.format( "%.2f", Float.parseFloat(mass));
        if (txt == null) {
            TableRow tr1 = new TableRow(this);
            TableRow.LayoutParams params = new LayoutParams();
            params.span = 2;
            tr1.setLayoutParams(params);
            TextView ing_name = new TextView(this);
            ing_name.setText(ingredient.substring(0, 1).toUpperCase() + ingredient.substring(1));
            EditText amount = new EditText(this);
            cur_values.put(ingredient.toLowerCase(), amount);
            amount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            amount.setText(mass);
            amount.setGravity(0x05);
            amount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        String m = v.getText().toString();
                        m = String.format( "%.2f", Float.parseFloat(m));
                        v.setText(m);

                        return true;
                    }
                    return false;
                }
            });
            ingred_table.put(ingredient.toLowerCase(), tr1);
            tr1.addView(ing_name);
            tr1.addView(amount);
            t1.addView(tr1, new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        }
        else{
            txt.setText(mass);
        }
    }

    public Dialog onCreatePushDialog(View view){

        final List<String> change_item = new ArrayList<String>();
        final List<Double> change_mass = new ArrayList<Double>();
        final List<String> cur_mass = new ArrayList<String>();
        Double epsilon = 0.001;

        for (int i=0; i < list_item.size(); i++)
        {
            EditText res = cur_values.get(list_item.get(i));
            if (res == null && !(Integer.parseInt(list_mass.get(i)) == 0) ){
                change_item.add(list_item.get(i));
                change_mass.add(-Double.parseDouble(list_mass.get(i)));
                cur_mass.add("0");
            }
            else if (!(res == null))
            {
                Double cur = Double.parseDouble(res.getText().toString());
                Double prev = Double.parseDouble(list_mass.get(i));
                if (Math.abs(cur - prev) > epsilon){
                    change_item.add(list_item.get(i));
                    change_mass.add(cur-prev);
                    cur_mass.add(res.getText().toString());
                }
            }
        }

        if ( change_item.size() == 0 ){
            showDialog = false;
        }

        LayoutInflater inflater = this.getLayoutInflater();
        View inflatedView = inflater.inflate(R.layout.push_inventory_dialog, null);
        final TableLayout tbl1 = (TableLayout) inflatedView.findViewById(R.id.tbl_change);

        for (int i = 0; i < change_item.size(); i++) {
            TableRow tr1 = new TableRow(this);
            TableRow.LayoutParams params = new LayoutParams();
            params.span = 2;
            tr1.setLayoutParams(params);
            TextView txt_name = new TextView(this);
            String ingredient = change_item.get(i);
            txt_name.setText(ingredient.substring(0, 1).toUpperCase() + ingredient.substring(1));
            TextView txt_amount = new TextView(this);
            txt_amount.setText(cur_mass.get(i));
            tr1.addView(txt_name);
            tr1.addView(txt_amount);
            tbl1.addView(tr1, new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        }

        final View vF = view;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(inflatedView)
                // Add action buttons
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        push_inventory(change_item, change_mass);
                        Snackbar.make(vF, "Database Updated!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Snackbar.make(vF, "Changes Canceled!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                    }
                });
        return builder.create();
    }


    public Dialog onCreateAddDialog(View view, List<String> list_item, List<String> list_mass) {
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
