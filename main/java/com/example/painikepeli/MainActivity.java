package com.example.painikepeli;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.ServerSocket;
import java.util.Random;

//MainActivity luokka tarjoaa toiminnallisuuden pelaajalle jossa painiketta voi painaa
public class MainActivity extends AppCompatActivity {
    //määritelty staattisia muuttujia ehkäistäkseen näppäilyvirheitä ja helpottaakseen
    //merkkijonojen muuttamista tarvittaessa
    private final static int REQUEST_CODE_1 =1;
    private final static String KEY_PREFS_SCORE = "score";
    private final static String KEY_PREFS_TOPRIZE = "toprize";
    private final static String KEY_PREFS_NAME = "name";
    private final static String KEY_PREFS = "prefs";
    //instanssimuuttujat on määritelty tässä jotta
    // niitä ei tarvitse määritellä useampaan kertaan
    private TextView pisteet_textView;
    private TextView nimi_textView;
    private TextView palkintoon_textView;
    private TextView voitto_textView;


    //ohjelman käynnistäessä ohjelma ajaa ensimmäisenä onCreate osion
    //joka asettaa käyttöliittymä näkymän käyttäjälle joka on määritelty
    //polussa /layout/activty_main.xml
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    //onStart ajetaan onCreaten jälkeen
    @SuppressLint("HandlerLeak")
    @Override
    protected void onStart(){
        super.onStart();

        //Liitetään aiemmin luodut muuttujat niiden käyttöliittymän
        //vastapareihin
        pisteet_textView= findViewById(R.id.pisteet_textView);
        nimi_textView= findViewById(R.id.nimi_textView);
        palkintoon_textView= findViewById(R.id.palkintoon_textView);
        voitto_textView= findViewById(R.id.voitto_textView);

        //luodaan painike muuttuja joka on linkitetty käyttöliittymän painikkeeseen
        //jos nimi on asetettu tarkoittaa se että tallennettuja muuttujia löytyy
        //tallennettuina laitteelle joten haetaan ne puhelimen muistista ja tuodaan
        //näkyviin display_prefs() funktiolla
        final Button painike = findViewById(R.id.painike_button);
        if (!getName().equals("")){
            display_prefs();
        }

        //handler joka käsittelee serversocketilta palaavan viestin
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //asettaa painikkeen taas toimivaksi
                painike.setEnabled(true);
                //tarkistaa saatiinko palvelimelta vastausta joka osattiin käsitellä oikein
                if (msg.what == 1) {
                    Bundle bundle = msg.getData();
                    if (bundle != null) {
                        //tallennetaan vastaus muuttujaan
                        String response = bundle.getString("KEY_RESPONSE_TEXT");
                        Log.d("handler response:", response);
                        try {
                            //tehdään merkkijonosta JSON olio koska palvelin palauttaa viestin JSONina
                            JSONObject json = new JSONObject(response);
                            //tarkistetaan palauttiko palvelin tiedon virheestä
                            if (json.getString("error").equals("true")) {
                                Toast.makeText(getApplicationContext(), "tapahtui virhe yritä uudestaan.", Toast.LENGTH_LONG).show();
                            } else {
                                //jos virhettä ei tapahtunut luetaan JSON oliosta palvelimen palauttamat
                                //pisteet ja painallusten määrä seuraavaan voittoon
                                setScore(json.getInt("pisteet"));
                                Log.d("pisteet", String.valueOf(json.getInt("pisteet")));
                                setToNextPrize(json.getInt("voittoon"));
                                //päivitetään näkymä
                                display_prefs();
                                //tarkistetaan jos palvelin ilmoitti pelaajan saaneen palkinnon
                                //ja ilmoiteteaan pelaajalle
                                Integer prize = json.getInt("voitto");
                                if (prize>0){
                                    Log.d("voitto:", String.valueOf(prize));
                                    youWonPrize(prize);

                                //jos voittoa ei kuitenkaan tullut piilotetaan tekstikenttä pelaajalta
                                }  else {
                                    voitto_textView.setVisibility(View.GONE);
                                }
                            }
                        } catch (JSONException ex) {
                            Log.e("JSON_ERROR", ex.getMessage(), ex);
                            Toast.makeText(getApplicationContext(),"jotain meni vikaan yritä uudestaan", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Log.d("Virhe", "ei vastausta palvelimelta");
                    Toast.makeText(getApplicationContext(),"jotain meni vikaan yritä uudestaan", Toast.LENGTH_LONG).show();
                }
            }
        };

        //asetetaan ohjelma kuuntelemaan painikkeen painallusta
        //kun painike on painettu ajetaan funktio onClick joka tarkastaa onko nimi asetettu
        //jos nimeä ei ole asetettu kutsutaan aktiviteettia jossa nimi asetetaan -> openSetNameActivity
        //jos nimi on asetettu ajetaan funktio painallus
        painike.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                painike.setEnabled(false);
                if(getName().equals("")){
                    openSetNameActivity();
                    painike.setEnabled(true);

                } else {
                    painallus(handler);
                }
            }


        });


    }

    //funktio avaa uuden aktiviteetin joka on määritelty omana luokkana ->setNameActivity
    public void openSetNameActivity() {
        Intent intent = new Intent(this, setNameActivity.class);
        intent.putExtra("name",getName());
        startActivityForResult(intent, REQUEST_CODE_1);
    }

    //tämä funktio ajetaan kun aktiviteetti setNameActivity on käyty loppuun eli nimi on asetettu
    //tietokantaan onnistuneesti. Funktio saa setNameActivy aktiviteetilta asetetun nimen ja
    //asettaa sen sovelluksen muistiin setName funktiossa sekä tallentaa pistemäärän uudelle pelaajalle
    //jonka jälkeen display_prefs() funktio päivittää näkymän
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent){
        super.onActivityResult(requestCode,resultCode,dataIntent);
        //tarkistetaan että tulos palautuu oikealta aktiviteetilta ja aktiviteetti on onnistunut
        switch (requestCode){
            case REQUEST_CODE_1:
                if(resultCode==RESULT_OK){
                    Log.d("passed name:",dataIntent.getStringExtra("name"));
                    setName(dataIntent.getStringExtra("name"));
                    setScore(20);
                    display_prefs();
                }
        }
    }

    //funktio hakee nimen sovelluksen tallennetuista muuttujista
    private String getName(){
        SharedPreferences prefs = getSharedPreferences(KEY_PREFS,MODE_PRIVATE);
        return prefs.getString(KEY_PREFS_NAME,"");
    }

    //funktio asettaa nimen sovelluksen tallennettuihin muuttujiin
    private void setName(String name){
        if (!name.equals("")) {
            save_prefs(KEY_PREFS_NAME, name);
        }
    }

    //funktio asettaa pisteet sovelluksen tallennettuihin muuttujiin
    private void setScore(Integer score){
        save_prefs(KEY_PREFS_SCORE, score);
    }

    //funktio tallentaa viimeisimmän tiedon vaadituista painalluksista seuraavaan palkintoon
    private void setToNextPrize(Integer value){
        save_prefs(KEY_PREFS_TOPRIZE, value);
    }

    //funktio tallentaa annetulla nimellä lukuarvon tallennettuihin muuttujiin
    private void save_prefs(String prefname, Integer value){
        SharedPreferences prefs = getSharedPreferences(KEY_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(prefname, value);
        editor.apply();
    }

    //funktio tallentaa annetulla nimellä merkkijonon tallennettuihin muuttujiin
    private void save_prefs(String prefname, String value){
        SharedPreferences prefs = getSharedPreferences("prefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(prefname, value);
        editor.apply();
    }

    //funktio hakee sovellukseen liitetystä puhelimen muistista
    //tallennetut muuttujat ja asettaa käyttöliittymän tulostamaan
    //tiedot pelaajan nähtäväksi
    //pelaajalle esitettävät merkkijonot on esitelty polussa /values/string.xml
    private void display_prefs(){
        SharedPreferences prefs = getSharedPreferences(KEY_PREFS,MODE_PRIVATE);
        String name=prefs.getString(KEY_PREFS_NAME,"");
        int toNextPrize=prefs.getInt(KEY_PREFS_TOPRIZE,0);
        int score=prefs.getInt(KEY_PREFS_SCORE,0);
        String text = getString(R.string.pelaajan_nimi,name);
        Log.d("text_name",text);
        nimi_textView.setText(text);
        Button painike=findViewById(R.id.painike_button);
        if (score < 1){
            text= getString(R.string.uusi_peli);
            painike.setText(text);
        }
        else {
            text = getString(R.string.painike_text);
            painike.setText(text);
        }
        text= getString(R.string.pisteet,score);
        Log.d("text_score",text);
        pisteet_textView.setText(text);

        if (toNextPrize!=0){
            text= getString(R.string.palkintoon,toNextPrize);
            palkintoon_textView.setText(text);
        }
    }

    //luodaan uusi olio jolle annetaan handler jotta voidaan käsitellä
    //toisesta langasta(Thread) saatu viesti
    //serversocket on luokka joka käsittelee kommunikoinnin palvelimen kanssa
    private void painallus(Handler handler){
        serversocket socket = new serversocket(handler);
        socket.buttonPushed(getName());
    }

    //funktio asettaa käyttöliittymässä pelaajalle näkyviin ilmoituksen palkinnosta
    private void youWonPrize(int prize){
        String text = getString(R.string.voitto_text, prize);
        voitto_textView.setText(text);
        voitto_textView.setTextColor(Color.rgb(255,128,0));
        voitto_textView.setVisibility(View.VISIBLE);
    }
}
