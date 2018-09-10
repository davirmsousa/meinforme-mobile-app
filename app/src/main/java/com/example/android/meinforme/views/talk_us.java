package com.example.android.meinforme.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.android.meinforme.R;
import com.example.android.meinforme.internet_connection.Conexao;
import com.example.android.meinforme.useful_classes.preferences_manipulator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;

/*
 * Created by Iniciação Científica Júnior - Núcleo de Inovações Tecnológicas.
 */

public class talk_us extends AppCompatActivity {
    //Declaração de objetos do xml
    String schoolCode, studentMessage, studentRegistry, formType, isAnonymous, url = "http://seuinforme.com.br/meinforme-apk/talkUs/talkUs_model.php", parametros = "";
    @SuppressLint("SimpleDateFormat") String sendDate = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
    TextInputLayout widgetStudentMessage;
    CoordinatorLayout mainLayout;
    TextView ViewStudentMessage;
    RadioButton radioFormType;
    CheckBox checkAnonnymous;
    LinearLayout menuIcon;
    int attempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_talk_us);

        // Vinculação dos objetos xml ao Java por ID
        widgetStudentMessage = findViewById(R.id.widgetStudentMessage);
        ViewStudentMessage = findViewById(R.id.studentMessage);
        checkAnonnymous = findViewById(R.id.annonymous);
        menuIcon = findViewById(R.id.LinearMenuIcon);
        mainLayout = findViewById(R.id.MainLayoutTU);

        //url = "http://10.0.2.2:8080/my%20portable%20files/MEINFORME/meinforme-apk/talkUs/talkUs_model.php";

        // Inflar o menu de estudante ou de responsavel
        int menu = R.layout.menu_student;
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        if(preferences_manipulator.getString(getApplicationContext(), "type", "").equals("responsible"))
            menu = R.layout.menu_responsible;
        assert inflater != null;
        View childLayout = inflater.inflate(menu, (ViewGroup) findViewById(R.id.LinearListIcon));
        menuIcon.addView(childLayout);

        // Mudar a imagem da Activity atual
        ImageView currentIconActivity = findViewById(R.id.iconTalkUs);currentIconActivity.setOnClickListener(null);
        currentIconActivity.setImageDrawable(getResources().getDrawable(R.drawable.speaker_o));

        // Recuperar dados do aluno
        studentRegistry = preferences_manipulator.getString(getApplicationContext(), "identifier", "");
        schoolCode = preferences_manipulator.getString(getApplicationContext(), "schoolcode", "");

        // Para qualquer alteração nos inputs eu retiro a mensagem de erro
        ViewStudentMessage.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s){widgetStudentMessage.setError(null);}
            public void beforeTextChanged(CharSequence s, int start, int count, int after){widgetStudentMessage.setError(null);}
            public void onTextChanged(CharSequence s, int start, int before, int count){widgetStudentMessage.setError(null);}
        });
    }

    // Função para centralizar o display da mensagem de erro
    private void messageError(){
        Snackbar.make(mainLayout, getString (R.string.data_error), Snackbar.LENGTH_LONG).setAction("Repetir", new View.OnClickListener(){
            @Override
            public void onClick(View view){
                new requestData().execute(url);
            }}).show();
    }

    // responsavel por tratar o retorno de requestData
    private void processData(JSONObject returned){
        try {
            String status = returned.getString("status");

            switch (status){
                case "success_submitting":// Se o fale com a gente foi cadastrado com sucesso
                    Snackbar.make(mainLayout, "Sua mensagem foi enviada.", Snackbar.LENGTH_SHORT).show();

                    // Anular os insputs
                    ViewStudentMessage.setText(""); checkAnonnymous.setChecked(false); radioFormType.setChecked(false);
                    break;
                default:
                    Snackbar.make(mainLayout, getString (R.string.somethigis_wrong), Snackbar.LENGTH_SHORT).show();
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
        protected void onPostExecute(String resultado){
            try{
                // Crio o objeto JSON para recuperar o retorno
                JSONObject jsonObject = new JSONObject(resultado);
                processData(jsonObject);
            }catch (JSONException e){
                if(attempts < 2){
                    attempts++;
                    messageError();
                }else{
                    Snackbar.make(mainLayout, getString (R.string.attempts_limit), Snackbar.LENGTH_LONG).show();
                }
            }
        }

    }

    // Função de envio do fale com a gente
    public void SendForm(View v) {
       // Verificação da conexão com a internet
        if(Conexao.isConnected(getApplicationContext())){
            // Pega o que o usuário escreveu para enviar pro banco
            studentMessage = ViewStudentMessage.getText().toString();
            boolean isCheckedAnonnymous = checkAnonnymous.isChecked();

            if(isCheckedAnonnymous){// Se ele prefirir o anonimato
                isAnonymous = "1";
            }else{// Se ele não selecionar o campo "anonimo"
                isAnonymous = "0";
            }

            if(formType == null && studentMessage.isEmpty()){
                Snackbar.make(mainLayout, getString (R.string.fill_all_field), Snackbar.LENGTH_SHORT).show();
            }else if(formType == null){
                Snackbar.make(mainLayout, "Escolha um dos três tipos de formulário.", Snackbar.LENGTH_SHORT).show();
            }else if(studentMessage.isEmpty() || studentMessage.equals(" ")){
                ViewStudentMessage.requestFocus();
                widgetStudentMessage.setError(getString (R.string.required_input));
            }else{
                // Monta os parâmetros
                parametros = "action=1&anonymous=" + isAnonymous + "&registry=" + studentRegistry + "&formType=" + formType
                        + "&message=" + studentMessage + "&schoolCode=" + schoolCode + "&sendDate=" + sendDate;
                new requestData().execute(url);
            }
        } else{// Se não houver conexão com a internet
            Snackbar.make(mainLayout, getString (R.string.internet_connection), Snackbar.LENGTH_SHORT).show();
        }
    }

    // Atribui um valor à variável 'formType'
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch(view.getId()){
            case R.id.praise:
                if (checked){
                    radioFormType = findViewById(R.id.praise);
                    formType = "elo";
                    break;
                }
            case R.id.complaint:
                if (checked){
                    radioFormType = findViewById(R.id.complaint);
                    formType = "rec";
                    break;
                }
            case R.id.doubt:
                if (checked){
                    radioFormType = findViewById(R.id.doubt);
                    formType = "duv";
                    break;
                }
        }
    }

    // Redirecionamento do menu superior de icones
    public void menuIconRedirect(View v){
        Intent intent = null;
        switch (v.getId()){
            case R.id.iconNews:intent = new Intent(this, notices.class);break;
            case R.id.iconTalkUs:intent = new Intent(this, talk_us.class);break;
            case R.id.iconRide:intent = new Intent(this, ride.class);break;
            case R.id.iconAccount:intent = new Intent(this, account.class);break;
        }
        startActivity(intent);

    }

    // Quando o usuario clicar em algum botao fisico do celular
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Se o botao for de voltar
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            // Abre a activity de avisos
            startActivity(new Intent(this, notices.class));
            finish();
            return true;
        }else {
            return super.onKeyDown(keyCode, event);
        }

    }
}
