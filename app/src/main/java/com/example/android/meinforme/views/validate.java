package com.example.android.meinforme.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.meinforme.R;
import com.example.android.meinforme.internet_connection.Conexao;
import com.example.android.meinforme.useful_classes.preferences_manipulator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/*
 * Created by Iniciação Científica Júnior - Núcleo de Inovações Tecnológicas.
 */

public class validate extends AppCompatActivity {
    // Declaração de objetos do xml
    String url = "http://seuinforme.com.br/meinforme-apk/user/user_model.php", parametros;
    TextView viewPassStudent, viewRegistryStud;
    TextInputLayout widgetRegistry, widgetPass;
    RelativeLayout mainValidateLayout;
    Button btnFinishValidation;
    int attempts = 0;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_validate);

        // Vinculacao das view com o java por id
        btnFinishValidation = findViewById(R.id.finishValidation);
        mainValidateLayout = findViewById(R.id.mainValidateLayout);
        widgetRegistry = findViewById(R.id.widgetRegistryUser);
        viewPassStudent = findViewById(R.id.newPassStudent);
        viewRegistryStud = findViewById(R.id.validRegistry);
        widgetPass = findViewById(R.id.widgetPassUser);

        //url = "http://10.0.2.2:8080/my%20portable%20files/MEINFORME/meinforme-apk/user/user_model.php";

        // Para qualquer alteração nos inputs eu retiro a mensagem de erro
        viewPassStudent.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s){widgetPass.setError(null);}
            public void beforeTextChanged(CharSequence s, int start, int count, int after){widgetPass.setError(null);}
            public void onTextChanged(CharSequence s, int start, int before, int count){widgetPass.setError(null);}
        });
        viewRegistryStud.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s){widgetRegistry.setError(null);}
            public void beforeTextChanged(CharSequence s, int start, int count, int after){widgetRegistry.setError(null);}
            public void onTextChanged(CharSequence s, int start, int before, int count){widgetRegistry.setError(null);}
        });

        // Evento para quando o botao de acesso for clicado
        btnFinishValidation.setOnClickListener(btnValidateListener);
    }

    private void processData(JSONObject returned){
        try {
            String status = returned.getString("successful");
            switch(status){
                case "validated":  // Se a validacao foi bem sucedida
                    // Chamar a tela de informativos
                    preferences_manipulator.putInt(getApplicationContext(), "validated", 1);
                    redirect(new Intent(this, notices.class));
                    break;
                default:
                    Snackbar.make(mainValidateLayout, getString (R.string.somethigis_wrong), Snackbar.LENGTH_LONG).show();
                    break;
            }
        }catch (JSONException e) {
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
        protected void onPostExecute(String resultado){
            try {
                JSONObject jsonObject = new JSONObject(resultado);
                processData(jsonObject);
            }catch (JSONException e) {
                if(attempts < 2){
                    attempts++;
                    messageError();
                }else{
                    Snackbar.make(mainValidateLayout, getString (R.string.attempts_limit), Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

    //Volta pra tela de view_login
    public void backToLogin (View v){
        preferences_manipulator.deletAllPreferences(getApplicationContext());
        redirect(new Intent(this, login.class ));
    }

    // Função para centralizar o display da mensagem de erro
    private void messageError(){
        Snackbar.make(mainValidateLayout, getString (R.string.data_error), Snackbar.LENGTH_LONG).setAction("Repetir", new View.OnClickListener(){
            @Override
            public void onClick(View view){
                new requestData().execute(url);
            }}).show();
    }

    // Função para centralizar o redirecionamento
    private void redirect(Intent intent){
        startActivity(intent);
        finish();
    }

    // Funcao do botao de login
    private View.OnClickListener btnValidateListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            btnFinishValidation.setEnabled(false);
            // Verificação da conexão com a internet
            if(Conexao.isConnected(getApplicationContext())){
                // Recupero os valores digitados
                String newPassword = viewPassStudent.getText().toString();
                String registryStudent = viewRegistryStud.getText().toString();

                // Verifico se todos estao preencidos
                if(newPassword.isEmpty() && registryStudent.isEmpty()){
                    viewRegistryStud.requestFocus();
                    Snackbar.make(mainValidateLayout, getString (R.string.fill_all_field), Snackbar.LENGTH_SHORT).show();
                }else if(newPassword.isEmpty()){
                    viewPassStudent.requestFocus();
                    widgetPass.setError(getString (R.string.required_input));
                }else if(registryStudent.isEmpty()){
                    viewRegistryStud.requestFocus();
                    widgetRegistry.setError(getString (R.string.required_input));
                }else{
                    widgetRegistry.setError(null);widgetPass.setError(null);

                    // Vejo se ele digitou a matrícula correta
                    if(registryStudent.equals(preferences_manipulator.getString(getApplicationContext(), "identifier", ""))){
                        // Recupero o códigoEscolaAluno
                        String schoolCode = preferences_manipulator.getString(getApplicationContext(), "schoolcode", "");

                        // Insiro os dados digitados no banco
                        parametros = "action=4&password=" + newPassword + "&registry=" + registryStudent + "&schoolCode=" + schoolCode;
                        new requestData().execute(url);
                    }else{
                        Snackbar.make(mainValidateLayout, "A matrícula digitada não foi encontrada.", Snackbar.LENGTH_LONG).show();
                    }
                }
            }else{
                Snackbar.make(mainValidateLayout, getString (R.string.internet_connection), Snackbar.LENGTH_LONG).show();
            }
            btnFinishValidation.setEnabled(true);
        }
    };
}
