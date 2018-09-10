package com.example.android.meinforme.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.example.android.meinforme.R;
import com.example.android.meinforme.internet_connection.Conexao;
import com.example.android.meinforme.useful_classes.preferences_manipulator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/*
 * Created by Iniciação Científica Júnior - Núcleo de Inovações Tecnológicas.
 */

public class main extends AppCompatActivity implements Runnable  {
    String url = "http://seuinforme.com.br/meinforme-apk/user/user_model.php", parametros = "";
    RelativeLayout mainLayout;
    ImageView loadGif;
    // Tentativas para se conectar [maximo de 2]
    int attempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_main);

        // Gif de carregamento
        loadGif = findViewById(R.id.loadgif);
        Glide.with(this).load(R.drawable.load2).asGif().into(loadGif);

        mainLayout = findViewById(R.id.mainLayoutSplashScreen);

        //url = "http://10.0.2.2:8080/my%20portable%20files/MEINFORME/meinforme-apk/user/user_model.php";

        // Um delay para a logo aparecer por mais tempo
        Handler handler = new Handler();
        handler.postDelayed(this, 3000);
    }

    public void run(){
        if(preferences_manipulator.getBoolean(getApplicationContext(), "isLogged", false)){// Ver se o usuário está logado
            // Ver se ele completou a validação, se for estudante
            if((preferences_manipulator.getInt(getApplicationContext(), "validated", 0) == 0) && (preferences_manipulator.getString(getApplicationContext(), "type", "").equals("student"))){
                redirect(new Intent(this, validate.class));
            }else{
                // Ver se tem conexao com a internet
                if (Conexao.isConnected(getApplicationContext())) {
                    String identifier = preferences_manipulator.getString(getApplicationContext(), "identifier", "");
                    String schoolcode = preferences_manipulator.getString(getApplicationContext(), "schoolcode", "");
                    String type = preferences_manipulator.getString(getApplicationContext(), "type", "");
                    parametros = "action=2&type=" + type + "&identifier=" + identifier + "&schoolCode=" + schoolcode;
                    new requestData().execute(url);
                } else {
                    redirect(new Intent(this, notices.class));
                }
            }
        }else{
            redirect(new Intent(this, login.class));
        }
    }

    // Função para centralizar o display da mensagem de erro
    private void messageError(){
        loadGif.setVisibility(View.INVISIBLE);
        Snackbar.make(mainLayout, getString (R.string.data_error), Snackbar.LENGTH_LONG).setAction("Repetir", new View.OnClickListener(){
            @Override
            public void onClick(View view){
                loadGif.setVisibility(View.VISIBLE);
                new requestData().execute(url);
            }}).show();
    }

    // Função para pegar os dados de requestData e fazer alguma coisa
    private void processData(JSONObject returned){
        try {
            String status = returned.getString("statusLogin");
            Intent intent;
            switch (status){
                case "login_ok":
                    intent = new Intent(this, notices.class);
                    break;
                case "access_denied":
                    preferences_manipulator.deletAllPreferences(getApplicationContext());
                    intent = new Intent(this, login.class).putExtra("message", getString(R.string.access_denied_string));
                    break;
                default:
                    preferences_manipulator.deletAllPreferences(getApplicationContext());
                    intent = new Intent(this, login.class).putExtra("message", "Sua conta não foi encontrada.");
                    break;
            }
            redirect(intent);
        }catch (JSONException e){
            messageError();
        }
    }

    // Função que faz a requisição dos dados
    @SuppressLint("StaticFieldLeak")
    private class requestData extends AsyncTask<String, Void, String> {
        // Envia a url e os parâmentros para a função Conexao
        @Override
        protected String doInBackground(String... urls){
            try {
                return Conexao.postDados(urls[0], parametros);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        // Irá pegar o resultado retornado pela função Conexao para fazer o tratamento
        @Override
        protected void onPostExecute(String result){
            try {
                JSONObject jsonObject = new JSONObject(result);
                processData(jsonObject);
            }catch (JSONException e){
                if(attempts < 2){
                    attempts++;
                    messageError();
                }else{
                    loadGif.setVisibility(View.INVISIBLE);
                    Snackbar.make(mainLayout, getString (R.string.attempts_limit), Snackbar.LENGTH_LONG).show();
                }
            }
        }

    }

    // Função para centralizar o redirecionamento
    private void redirect(Intent intent){
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause(){
        super.onPause();
        finish();
    }
}
