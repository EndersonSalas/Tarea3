package com.example.tarea3.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tarea3.R;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class MainFragment extends Fragment {

    public Double colon,pesoMex,bolivar,pesoChile,solPeru = 0.0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_main, container, false);
        final TextView editText = (TextView) view.findViewById(R.id.edit_view_quantity);
        enviarPeticionDatos(view);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    enviarPeticionDatos(view);
                }
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setEditTextCont(colon,solPeru,bolivar,pesoMex,pesoChile);
            }
        });
        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    editText.clearFocus();
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    private void  enviarPeticionDatos(View view){
        if (hasInternetAccess()) {
            Toast.makeText(view.getContext().getApplicationContext(), R.string.access_internet, Toast.LENGTH_SHORT).show();
            DownloadWebPageTask downloadAsyn = new DownloadWebPageTask();
            downloadAsyn.execute(new String[]{"http://www.apilayer.net/api/live?access_key=0e7154e8fa962781d88bea27b1feb9cb&currencies=CRC,PEN,COP,MXN,CLP&format=1"});
        } else {
            Toast.makeText(view.getContext().getApplicationContext(), R.string.no_access, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean hasInternetAccess() {
        ConnectivityManager cm = (ConnectivityManager) this.getContext().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void setEditTextCont(Double colon,Double solPeruano,Double bolivar,Double pesosMex,Double pesoChile) {
        TextView textViewColon = (TextView) this.getView().findViewById(R.id.text_cr);
        TextView textViewMxn = (TextView) this.getView().findViewById(R.id.text_mx);
        TextView textViewCop = (TextView) this.getView().findViewById(R.id.text_col);
        TextView textViewClp = (TextView) this.getView().findViewById(R.id.text_cl);
        TextView textViewPer = (TextView) this.getView().findViewById(R.id.text_pe);

        this.colon=colon;
        this.solPeru=solPeruano;
        this.bolivar=bolivar;
        this.pesoMex=pesosMex;
        this.pesoChile=pesoChile;
        textViewColon.setText(conversionMoneda(colon));
        textViewPer.setText(conversionMoneda(solPeruano));
        textViewCop.setText(conversionMoneda(bolivar));
        textViewMxn.setText(conversionMoneda(pesosMex));
        textViewClp.setText(conversionMoneda(pesoChile));

    }
    private String conversionMoneda(Double cambio) {
        EditText editText = (EditText) this.getView().findViewById(R.id.edit_view_quantity);
        Double datoEditText=0.0;
        if(!editText.getText().toString().isEmpty()){
            datoEditText=Double.parseDouble(editText.getText().toString());
        }
        Double result= datoEditText* cambio;
        return result.toString();
    }

    private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
        private ProgressDialog mProgress;
        private final String TAG = "DownloadWebPageTask";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress = new ProgressDialog(MainFragment.this.getContext());
            mProgress.setMessage("Downloading website content");
            mProgress.show();
        }

        @Override
        protected String doInBackground(String... url) {
            OkHttpClient client = new OkHttpClient();
            Request request = null;
            try {
                request = new Request.Builder().url(url[0]).build();
            } catch (Exception e) {
                Log.i(TAG, "Error in the URL");
                return "Download Failed";
            }
            Response response = null;
            try {
                response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    return response.body().string();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "Download failed";
        }

        @Override
        protected void onPostExecute(String result) {
            mProgress.dismiss();
            JSONObject response = null;
            try {
                response = new JSONObject(result);
                String success = response.getString("success");
                JSONObject currencies = response.getJSONObject("quotes");
                Double colon = currencies.getDouble("USDCRC");
                Double pesoMex = currencies.getDouble("USDMXN");
                Double bolivar = currencies.getDouble("USDCOP");
                Double pesoChile = currencies.getDouble("USDCLP");
                Double solPeru = currencies.getDouble("USDPEN");

                setEditTextCont(colon,solPeru,bolivar,pesoMex,pesoChile);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

}
