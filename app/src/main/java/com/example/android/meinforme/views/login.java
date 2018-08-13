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
import java.util.Objects;

/*
 * Created by Iniciação Científica Júnior - Núcleo de Inovações Tecnológicas.
 */

public class login extends AppCompatActivity{
    // Declaração de objetos do xml
    String url = "http://meinforme.pe.hu/apk/user/user_model.php", parametros = "";
    TextInputLayout widgetEmail, widgetPass;
    TextView viewPassUser, viewEmailUser;
    RelativeLayout mainLoginLayout;
    int attempts = 0;
    Button btnAccess;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_login);

        // Vinculação dos objetos xml ao Java por ID
        btnAccess = findViewById(R.id.loginUser);
        viewEmailUser = findViewById(R.id.emailUser);
        widgetPass = findViewById(R.id.widgetPassUser);
        viewPassUser = findViewById(R.id.passwordUser);
        widgetEmail = findViewById(R.id.widgetEmailUser);
        mainLoginLayout = findViewById(R.id.mainLoginLayout);

        // Para qualquer alteração nos inputs eu retiro a mensagem de erro
        viewPassUser.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s){widgetPass.setError(null);}
            public void beforeTextChanged(CharSequence s, int start, int count, int after){widgetPass.setError(null);}
            public void onTextChanged(CharSequence s, int start, int before, int count){widgetPass.setError(null);}
        });
        viewEmailUser.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s){widgetEmail.setError(null);}
            public void beforeTextChanged(CharSequence s, int start, int count, int after){widgetEmail.setError(null);}
            public void onTextChanged(CharSequence s, int start, int before, int count){widgetEmail.setError(null);}
        });

        url = "http://10.0.2.2:8080/my%20portable%20files/MEINFORME/Meinforme-Apk/user/user_model.php";

        // Verifica se existe algum putExtra
        Bundle extras = getIntent().getExtras();
        if(null != extras && extras.getString("message") != null){Snackbar.make(mainLoginLayout, Objects.requireNonNull(extras.getString("message")), Snackbar.LENGTH_LONG).show();}

        // Quando clicar no botão para logar
        btnAccess.setOnClickListener(btnAccessListener);
    }

    // Função para centralizar o display da mensagem de erro
    private void messageError(){
        Snackbar.make(mainLoginLayout, getString (R.string.data_error), Snackbar.LENGTH_LONG).setAction("Repetir", new View.OnClickListener(){
            @Override
            public void onClick(View view){
                new requestData().execute(url);
            }}).show();
    }

    // Responsável pelo tratamento dos dados do user responsavel
    private void processDataResponsible(JSONObject returned){
        try {
            String status = returned.getString("statusLogin");
            switch (status){
                case "login_ok": // Se o view_login for aceito
                    // Se a aluno que ele é responsavel foi enconrado no banco
                    if(returned.getString("studentFound").equals("found")){
                        // Escrevendo no objeto SharedPreferences
                        preferences_manipulator.putString(getApplicationContext(), "schoolcode", returned.getString("schoolCode"));
                        preferences_manipulator.putString(getApplicationContext(), "identifier", returned.getString("id"));
                        preferences_manipulator.putString(getApplicationContext(), "class", returned.getString("class"));
                        preferences_manipulator.putString(getApplicationContext(), "type", returned.getString("type"));
                        preferences_manipulator.putBoolean(getApplicationContext(), "isLogged", true);

                        // Chamar a tela de informativos
                        redirect(new Intent(this, notices.class));
                    }else{
                        Snackbar.make(mainLoginLayout, "Sua conta não está vinculada a um aluno existente.", Snackbar.LENGTH_LONG).show();
                    }
                    break;
                case "login_error": // Se os dados estiverem incorretos
                    Snackbar.make(mainLoginLayout, getString(R.string.email_or_pass_wrong), Snackbar.LENGTH_LONG).show();
                    break;
                case "access_denied": // Se ele nao estiver ativo
                    Snackbar.make(mainLoginLayout, getString(R.string.access_denied_string), Snackbar.LENGTH_LONG).show();
                    break;
                default:
                    Snackbar.make(mainLoginLayout, getString(R.string.somethigis_wrong), Snackbar.LENGTH_LONG).show();
                    break;
            }
        }catch (JSONException e){
            messageError();
        }
    }

    // Responsável por preencher as preferencias do user estudante
    private void fillStudentData(JSONObject data) throws JSONException {
        preferences_manipulator.putString(getApplicationContext(), "schoolcode", data.getString("schoolCode"));
        preferences_manipulator.putString(getApplicationContext(), "identifier", data.getString("registry"));
        preferences_manipulator.putString(getApplicationContext(), "class", data.getString("class"));
        preferences_manipulator.putString(getApplicationContext(), "type", data.getString("type"));
        preferences_manipulator.putBoolean(getApplicationContext(), "isLogged", true);
        preferences_manipulator.putInt(getApplicationContext(), "validated", data.getInt("validated"));
    }

    // Responsável pelo tratamento dos dados do user estudante
    private void processDataStudent(JSONObject returned){
        try {
            String status = returned.getString("statusLogin");
            switch (status){
                case "login_ok": // Se o view_login for aceito
                    // Escrevendo no objeto SharedPreferences
                    fillStudentData(returned);

                    // Chamar a tela de informativos
                    redirect(new Intent(this, notices.class));
                    break;
                case "login_error": // Se os dados estiverem incorretos
                    Snackbar.make(mainLoginLayout, getString(R.string.email_or_pass_wrong), Snackbar.LENGTH_LONG).show();
                    break;
                case "need_to_valid": // Se o view_login for aceito mas ele nao se validou
                    // Escrevendo no objeto SharedPreferences
                    fillStudentData(returned);

                    // Chama a tela de validacao de conta
                    redirect(new Intent(this, validate.class));
                    break;
                case "access_denied": // Se ele nao estiver ativo
                    Snackbar.make(mainLoginLayout, getString(R.string.access_denied_string), Snackbar.LENGTH_LONG).show();
                    break;
                default:
                    Snackbar.make(mainLoginLayout, getString(R.string.somethigis_wrong), Snackbar.LENGTH_LONG).show();
                    break;
            }
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
                // Crio um objeto JSON para recuperar o status do view_login
                JSONObject jsonObject = new JSONObject(result);
                if(jsonObject.getString("type").equals("student")){
                    processDataStudent(jsonObject);
                }else{
                    processDataResponsible(jsonObject);
                }
            } catch (JSONException e) {
                if(attempts < 2){
                    attempts++;
                    messageError();
                }else{
                    Snackbar.make(mainLoginLayout, getString (R.string.attempts_limit), Snackbar.LENGTH_LONG).show();
                }
            }
        }

    }

    // Função para centralizar o redirecionamento
    private void redirect(Intent intent){
        startActivity(intent);
        finish();
    }

    // Funcao executada ao clicar nos labels do rodape
    public void footerLabelRedirect(View v){
        Intent intent = null;
        switch (v.getId()){
            case R.id.forgotPassword: // Abre a tela de cadastro de usuarios
                intent = new Intent(this, recover_pass.class );
                break;
            case R.id.registerUser: // Abre a tela de recuperação de senha
                intent = new Intent (getApplicationContext(), register_user.class);
                break;
        }
        startActivity(intent);
    }

    // Funcao do botao de login
    private View.OnClickListener btnAccessListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btnAccess.setEnabled(false);
            if(Conexao.isConnected(getApplicationContext())){
                // Pega o que o usuário escreveu para comparar com os dados do banco
                String password = viewPassUser.getText().toString();
                String email = viewEmailUser.getText().toString();

                // Verifica se o usuário esqueceu de digitar algum dos dois campos
                if(password.isEmpty() && email.isEmpty()){
                    viewEmailUser.requestFocus();
                    Snackbar.make(mainLoginLayout, getString (R.string.fill_all_field), Snackbar.LENGTH_SHORT).show();
                }else if(password.isEmpty()){
                    viewPassUser.requestFocus();
                    widgetPass.setError(getString (R.string.required_input));
                }else if(email.isEmpty()){
                    viewEmailUser.requestFocus();
                    widgetEmail.setError(getString (R.string.required_input));
                }else{
                    widgetEmail.setError(null);widgetPass.setError(null);

                    // Verificação dos dados digitados
                    parametros = "action=1&email=" + email + "&password=" + password;
                    new requestData().execute(url);
                }
            } else{ // Se não houver conexão com a internet
                Snackbar.make(mainLoginLayout, getString (R.string.internet_connection), Snackbar.LENGTH_SHORT).show();
            }
            btnAccess.setEnabled(true);
        }
    };
}
