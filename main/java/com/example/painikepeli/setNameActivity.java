package com.example.painikepeli;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;

//luokka setNameActivity käsittelee nimen luomista pelaajalle
public class setNameActivity extends AppCompatActivity {

    //avataan käyttöliittymä uuteen aktiviteettiin pelaajalle
    // joka on määritelty polussa /layout/activity_set_name
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_name);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        final EditText getName_editText = findViewById(R.id.getName_editText);

        final Button setName_button = findViewById(R.id.setName_button);

        //käsittelee serversocket luokan mainthreadin kanssa rinnakkain ajetun threadin
        //lähettämän viestin eli vastauksen onnistuneesta tai epäonnistuneesta nimen lisäyksestä
        //tietokantaan
        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            @ Override
            public void handleMessage (Message msg) {
                if(msg.what == 1){
                    Bundle bundle = msg.getData();
                    if (bundle != null) {
                        String response = bundle.getString("KEY_RESPONSE_TEXT");
                        Log.d("handler response:",response);
                        try {
                            JSONObject json = new JSONObject(response);
                            Log.d("Testhandler",json.getString("error"));
                            if (json.getString("error").equals("true")){
                                Toast.makeText(getApplicationContext(),"nimi on jo käytössä",Toast.LENGTH_LONG).show();
                                setName_button.setEnabled(true);
                            } else {
                                Intent intent = new Intent();
                                intent.putExtra("name",json.getString("name"));
                                setResult(RESULT_OK,intent);
                                finish();
                            }
                        } catch(JSONException ex){
                            Log.e("JSON_ERROR",ex.getMessage(),ex);
                        }

                    }
                } else{
                    setName_button.setEnabled(true);
                }
            }
        };

        //kuutelee onko käyttäjä painanut "aseta nimi" painiketta
        //kun pelaaja painaa painiketta ajetaan funktio joka asettaa
        //painikkeen pois päältä ja lukee käyttäjän kirjoittaman nimen
        //joka annetaan funktiolle setNametodb joka asettaa nimen tietokantaan
        setName_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //asettaa painikkeen pois käytöstä ettei pelaaja paina painiketta uudelleen
                //ja aiheuta ei toivottua toimintaa ohjelmalta
                setName_button.setEnabled(false);
                final String name=getName_editText.getText().toString(); //lukee käyttäjän syötteen
                //jos käyttäjä asetti tyhjän nimen ei nimeä hyväksytä ja kehotetaan pelaajaa asettamaan nimi
                if (name.equals("")){
                    Toast.makeText(getApplicationContext(),"kirjoita nimi ennen nimen asettamista"+name,
                            Toast.LENGTH_LONG).show();
                    setName_button.setEnabled(true);
                } else {
                    setNametodb(name, handler);
                }
            }
        });



    }

    //funktio asettaa nimen tietokantaan serversocket olion avulla jolle annetaan
    //handler jotta voidaan käsitellä palvelimen palauttama vastaus nimen lisäämisestä tietokantaan
    private boolean setNametodb(String name, Handler handler){
        serversocket socket = new serversocket(handler);
        socket.setName(name);
        return true;

    }
}
