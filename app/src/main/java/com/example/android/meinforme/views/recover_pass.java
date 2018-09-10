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
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.meinforme.R;
import com.example.android.meinforme.internet_connection.Conexao;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

/*
 * Created by Iniciação Científica Júnior - Núcleo de Inovações Tecnológicas.
 */

public class recover_pass extends AppCompatActivity {
    // Declaração de objetos do xml
    String url = "http://seuinforme.com.br/meinforme-apk/user/user_model.php", parametros = "";
    TextView viewEmailRecover, viewPassRecover, viewSuccessMessage;
    TextInputLayout widgetEmail, widgetPass;
    RelativeLayout mainLinearLayout;
    Button btnSendNewPass;
    int attempts = 0;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_recover_pass);

        // Vinculação dos objetos xml ao Java por ID
        viewSuccessMessage = findViewById(R.id.successMessage);
        mainLinearLayout = findViewById(R.id.mainRecoverLayout);
        viewPassRecover = findViewById(R.id.newPasswordRecover);
        btnSendNewPass = findViewById(R.id.btnRecoverPassword);
        viewEmailRecover = findViewById(R.id.emailRecoverPass);
        widgetEmail = findViewById(R.id.widgetEmailUser);
        widgetPass = findViewById(R.id.widgetPassUser);

        //url = "http://10.0.2.2:8080/my%20portable%20files/MEINFORME/meinforme-apk/user/user_model.php";

        // Para qualquer alteração nos inputs eu retiro a mensagem de erro
        viewEmailRecover.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s){widgetEmail.setError(null);}
            public void beforeTextChanged(CharSequence s, int start, int count, int after){widgetEmail.setError(null);}
            public void onTextChanged(CharSequence s, int start, int before, int count){widgetEmail.setError(null);}
        });
        viewPassRecover.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s){widgetPass.setError(null);}
            public void beforeTextChanged(CharSequence s, int start, int count, int after){widgetPass.setError(null);}
            public void onTextChanged(CharSequence s, int start, int before, int count){widgetPass.setError(null);}
        });

        // Ativar o botao de retorno do menu
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        // Mudar o titulo da activity
        getSupportActionBar().setTitle(getString(R.string.recover_pass_activity_title));
    }

    // Funcao chamada quando clicar no botao
    public void updateForgotPassword(View v){
        btnSendNewPass.setEnabled(false);
        viewSuccessMessage.setVisibility(View.INVISIBLE);
        String email = viewEmailRecover.getText().toString();
        String pass = viewPassRecover.getText().toString();

        //Verificação da conexão com a internet
        if(Conexao.isConnected(getApplicationContext())){
            // Verifica se o usuário esqueceu de digitar algum dos campos
            if(email.isEmpty() && pass.isEmpty()){
                viewEmailRecover.requestFocus();
                Snackbar.make(mainLinearLayout, getString (R.string.fill_all_field), Snackbar.LENGTH_SHORT).show();
            } else if(email.isEmpty()){
                viewEmailRecover.requestFocus();
                widgetEmail.setError(getString (R.string.required_input));
            } else if(pass.isEmpty()){
                viewPassRecover.requestFocus();
                widgetPass.setError(getString (R.string.required_input));
            } else{
                widgetEmail.setError(null);widgetPass.setError(null);

                parametros = "action=3&email=" + email + "&newpass=" + pass;
                new requestData().execute(url);
            }
        } else{ // Se não houver conexão com a internet
            Snackbar.make(mainLinearLayout, getString (R.string.internet_connection), Snackbar.LENGTH_LONG).show();
        }
        btnSendNewPass.setEnabled(true);
    }

    // Funcao para tratamento do retorno
    private void processData(JSONObject returned){
        try {
            String status = returned.getString("uploadStatus");
            switch (status){
                case "user_erro"://se o usuario digitou algum dado errado
                    Snackbar.make(mainLinearLayout, "Você digitou algo errado, por favor, tente novamente.", Snackbar.LENGTH_LONG).show();
                    break;
                case "update_ok"://se o update foi concluido
                    viewSuccessMessage.setVisibility(View.VISIBLE);
                    break;
                case "access_error"://se o update foi concluido
                    Snackbar.make(mainLinearLayout, "Você não tem permissão para realizar esta ação.", Snackbar.LENGTH_LONG).show();
                    break;
                case "validation_error"://se o update foi concluido
                    Snackbar.make(mainLinearLayout, "Você precisa validar esta conta antes de trocar a senha.", Snackbar.LENGTH_LONG).show();
                    break;
                default:
                    Snackbar.make(mainLinearLayout, getString (R.string.somethigis_wrong), Snackbar.LENGTH_LONG).show();
                    break;
            }
        }catch (JSONException e) {
            if(attempts < 2){
                attempts++;
                messageError();
            }else{
                Snackbar.make(mainLinearLayout, getString (R.string.attempts_limit), Snackbar.LENGTH_LONG).show();
            }
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
            try{
                // Crio o objeto JSON para recuperar o retorno
                JSONObject jsonObject = new JSONObject(resultado);
                processData(jsonObject);
            }catch (JSONException e) {
                if(attempts < 2){
                    attempts++;
                    messageError();
                }else{
                    Snackbar.make(mainLinearLayout, getString (R.string.attempts_limit), Snackbar.LENGTH_LONG).show();
                }
            }

        }
    }

    // Função para centralizar o display da mensagem de erro
    private void messageError(){
        Snackbar.make(mainLinearLayout, getString (R.string.data_error), Snackbar.LENGTH_LONG).setAction("Repetir", new View.OnClickListener(){
            @Override
            public void onClick(View view){
                new requestData().execute(url);
            }}).show();
    }

    // Quando o usuario clicar em algum botao fisico do celular
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Se o botao for de voltar
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            // Abre a activity de view_login
            startActivity(new Intent(this, login.class));
            finish();
            return true;
        }else {
            return super.onKeyDown(keyCode, event);
        }

    }
}
