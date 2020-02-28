package com.example.painikepeli;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


import javax.net.ssl.HttpsURLConnection;

//serversocket luokka käsittelee palvelinpyynnöt ja vastaukset sovellukselle
public class serversocket {
    private Handler handler;
    private final static String SET_NAME_URL = "https://painikepeliprojekti.herokuapp.com/setName.php";
    private final static String Button_PRESSED_URL = "https://painikepeliprojekti.herokuapp.com/painikepainettu.php";
    private final static String KEY_RESPONSE_TEXT = "KEY_RESPONSE_TEXT";

    //constructor joka määrittää olion instassimuuttujan handler
    //oliota luodessa
    //mahdollistaa eri serversocket olioille eri handler muuttujan
    public serversocket(Handler handler){
        this.handler = handler;
    }

    //funktio joka lähettää palvelimelle käskyjä ja lähettää palvelimen vastaukset handlerille
    //jotka tässä tapauksessa ovat MainActivity ja setNameActivity luokissa määritellyt handlerit
    protected void sendHttpsRequestThread(final String params, final String address) {
        //luodaan uusi lanka jota voidaan ajaa rinnakkain päälangan kanssa
        Thread thread = new Thread() {
            @Override
            public void run() {
                HttpURLConnection httpsConnection = null;

                try {
                    //yritetään luoda https yhteys palvelimeen alla olevilla parametreilla
                    URL url = new URL(address);
                    httpsConnection = (HttpURLConnection) url.openConnection();
                    httpsConnection.setRequestMethod("POST");
                    httpsConnection.setConnectTimeout(10000);
                    httpsConnection.setReadTimeout(10000);
                    httpsConnection.setDoOutput(true);
                    httpsConnection.connect();
                    Log.d("params", params);

                    //kirjoitetaan lähtevään streamiin halutut parametrit
                    //eli käytännössä lisätään parametrit https post pyyntöön joka lähetetään palvelimelle
                    OutputStream out = new BufferedOutputStream(httpsConnection.getOutputStream());
                    out.write(params.getBytes());
                    out.flush();
                    out.close();

                    //luetaan palvelimen vastauskoodi esim 200 ok tai 500 server error
                    int responseCode = httpsConnection.getResponseCode();
                    Log.d("RESPONSE CODE:",Integer.toString(responseCode));

                    //jos palvelin vastaa yhteyden onnistuneen luetaan palvelimen palauttama vastaus
                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        InputStream in = new BufferedInputStream(httpsConnection.getInputStream());
                        InputStreamReader reader = new InputStreamReader(in);
                        BufferedReader br = new BufferedReader(reader);
                        StringBuilder sb = new StringBuilder();

                        String line;
                        while((line = br.readLine()) != null){
                            sb.append(line + "\n");
                        }
                        br.close();
                        //debugaamista varten lokitetaan vastaus palvelimelta
                        Log.d("response",sb.toString());

                        //luodaan viesti joka voidaan lähettää handlerille
                        Message msg = new Message();
                        msg.what =1;
                        //lisätään viestiin palvelimen palauttama syöte
                        Bundle bundle = new Bundle();
                        bundle.putString("KEY_RESPONSE_TEXT",sb.toString());
                        msg.setData(bundle);

                        //lähetetään viesti handlerille
                        handler.sendMessage(msg);




                    }else {
                        handler.sendEmptyMessage(2);
                    }
                //virheen käsittelyä esimerkiksi rajapinnan ollessa varattu
                //tai epäsopivan url osoitteen takia
                //virheet lokitetaan
                } catch (MalformedURLException ex) {
                    Log.e("HTTP_URL_CONNECTION", ex.getMessage(), ex);
                    handler.sendEmptyMessage(2);
                } catch (IOException ex) {
                    Log.e("HTTP_URL_CONNECTION", ex.getMessage(), ex);
                    handler.sendEmptyMessage(2);
                } finally {
                    httpsConnection.disconnect();
                    Log.d("DISCONNECTED","TRUE");

                }

            }

        };
        thread.start();
    }

    //luokan ulkopuolelta kutsuttava funktio joka ajaa sendHttpsRequestThread funktion
    //toivotuilla parametreilla nimeä asetettaessa
    public void setName(String name) {
        sendHttpsRequestThread(name, SET_NAME_URL);
    }


    //luokan ulkopuolelta kutsuttava funktio joka ajaa sendHttpsRequestThread funktion
    //toivotuilla parametreilla kun pelaaja on painanut painiketta
    public void buttonPushed(String name) {
        sendHttpsRequestThread(name, Button_PRESSED_URL);
    }

}
