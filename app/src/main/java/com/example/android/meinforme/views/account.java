package com.example.android.meinforme.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.meinforme.R;
import com.example.android.meinforme.internet_connection.Conexao;
import com.example.android.meinforme.popup;
import com.example.android.meinforme.useful_classes.phone_mask;
import com.example.android.meinforme.useful_classes.preferences_manipulator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/*
 * Created by Iniciação Científica Júnior - Núcleo de Inovações Tecnológicas.
 */

public class account extends AppCompatActivity {
    // Declaração de objetos do xml
    String url = "http://seuinforme.com.br/meinforme-apk/account/account_model.php", parametros = "";
    LinearLayout menuIcon, contentListLayout, contentLayout;
    CoordinatorLayout mainLayout;
    TextView viewUserName;
    TextView view1NewPass;
    ImageView loadGif;
    int attempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_account);

        if(preferences_manipulator.getInt(getApplicationContext(), "account_popoup", 0) == 0 &&
                !preferences_manipulator.getString(getApplicationContext(), "type", "").equals("student")){
            preferences_manipulator.putInt(getApplicationContext(),"account_popoup", 1);
            startActivity(new Intent(this, popup.class).putExtra("messageNum", 4));
        }

        // Vinculação dos objetos xml
        viewUserName = findViewById(R.id.userName);
        menuIcon = findViewById(R.id.LinearMenuIcon);
        loadGif = findViewById(R.id.imageLoadNotice);
        view1NewPass = findViewById(R.id.newPassUser);
        contentLayout = findViewById(R.id.contentLayout);
        mainLayout = findViewById(R.id.mainLayoutAccount);
        contentListLayout = findViewById(R.id.contentListLayout);
        Glide.with(this).load(R.drawable.load4).asGif().into(loadGif);

        //url = "http://10.0.2.2:8080/my%20portable%20files/MEINFORME/meinforme-apk/account/account_model.php";

        // Inflar o menu de estudante ou de responsavel
        int menu = R.layout.menu_student;
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        if(preferences_manipulator.getString(getApplicationContext(), "type", "").equals("responsible"))
            menu = R.layout.menu_responsible;
        assert inflater != null;
        View childLayout = inflater.inflate(menu, (ViewGroup) findViewById(R.id.LinearListIcon));
        menuIcon.addView(childLayout);

        // Mudar a imagem da Activity atual
        ImageView currentIconActivity = findViewById(R.id.iconAccount);currentIconActivity.setOnClickListener(null);
        currentIconActivity.setImageDrawable(getResources().getDrawable(R.drawable.account_o));

        // Verificação da conexão com a internet
        if(Conexao.isConnected(getApplicationContext())){

            // Pegar a matricula do aluno ou id do responsavel
            String useridentifier = preferences_manipulator.getString(getApplicationContext(), "identifier", "");

            // Montar parametros diferenciados para cada tipo de usuario
            if(preferences_manipulator.getString(getApplicationContext(), "type", "student").equals("student")){parametros = "action=1&registry=";
            }else{parametros = "action=2&identifier=";}

            // Concatenar a matricula do aluno ou id do responsavel
            parametros += useridentifier;
            new requestData().execute(url);
        }else{ // Se não houver conexão com a internet
            // Deixa o gif invisivel
            loadGif.setVisibility(View.GONE);
            Snackbar.make(mainLayout, getString (R.string.internet_connection), Snackbar.LENGTH_SHORT).show();
        }
    }

    // Responsavel por inserir um item nao editavel na tela
    private void InsertItemNotEditable(String title, String body){
        // Inflar o layout item
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        @SuppressLint("InflateParams") View newLayout = inflater.inflate(R.layout.toinflate_itemnoteditable, null);
        contentListLayout.addView(newLayout);

        // Mudar o texto das views filhas
        TextView viewTitle = newLayout.findViewById(R.id.listItemTitle); viewTitle.setText(title);
        TextView viewBody = newLayout.findViewById(R.id.listItemBody); viewBody.setText(body);
    }

    // Responsavel por inserir um item nao editavel na tela
    private void InsertItemEditable(String text){
        // Inflar o layout item
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        @SuppressLint("InflateParams") View newLayout = inflater.inflate(R.layout.toinflate_itemeditable, null);
        contentListLayout.addView(newLayout);

        // Mudar o texto das views filhas
        TextView viewTitle = newLayout.findViewById(R.id.listItemTitle); viewTitle.setText(getString(R.string.tel_hint));
        EditText viewBody = newLayout.findViewById(R.id.listItemEdit); viewBody.setText(text);
        // Mascara de telefone
        viewBody.addTextChangedListener(phone_mask.insert("(##)#####-####", viewBody));
    }

    // Responsavel por tratar o retorno de requestData para o usuario estudante
    private void processDataStudent(JSONObject returned){
        try {
            String status = returned.getString("status");

            switch (status){
                case "user_found": // Do something
                    viewUserName.setText(returned.getString("name"));
                    InsertItemNotEditable("Matrícula", preferences_manipulator.getString(getBaseContext(), "identifier", "Not Found"));
                    InsertItemNotEditable("Turma", returned.getString("class"));
                    InsertItemNotEditable("Seriado", returned.getString("serie") + "° ano");
                    InsertItemNotEditable("E-mail", returned.getString("email"));
                    InsertItemNotEditable("Vinculado à escola", returned.getString("schoolName"));
                    InsertItemNotEditable("Fale Com a Gente enviados", returned.getString("amountOfTalkWithUs"));
                    InsertItemNotEditable("Responsáveis Vinculados a esta conta", returned.getString("amountOfResponsible"));
                    break;
                case "update_ok": // Do something
                    view1NewPass.setText(null);
                    Snackbar.make(mainLayout, R.string.success_password_update, Snackbar.LENGTH_SHORT).show();
                    break;
                case "update_error": // Do something
                    Snackbar.make(mainLayout, R.string.somethigis_wrong, Snackbar.LENGTH_SHORT).show();
                    break;
                case "empty_pass": // Do something
                    Snackbar.make(mainLayout, R.string.fill_the_new_pass_field, Snackbar.LENGTH_SHORT).show();
                    break;
                default: // Se algo deu arrado
                    Snackbar.make(mainLayout, R.string.user_not_found, Snackbar.LENGTH_SHORT).show();
                    break;
            }
        }catch (JSONException e){
            messageError();
        }
    }

    // Responsavel por tratar o retorno de requestData para o usuario responsavel
    private void processDataResponsible(JSONObject returned){
        try {
            String status = returned.getString("status");

            switch (status){
                case "user_found": // Do something
                    viewUserName.setText(returned.getString("name"));
                    InsertItemNotEditable("E-mail", returned.getString("email"));
                    InsertItemNotEditable("Nome do usuário vinculado", returned.getString("studentName"));
                    InsertItemNotEditable("Matrícula do usuário vinculado", returned.getString("studentRegistry"));
                    InsertItemNotEditable("Turma do usuário vinculado", returned.getString("class"));
                    InsertItemNotEditable("Seriado do usuário vinculado", returned.getString("serie") + "° ano");
                    InsertItemNotEditable("Vinculado à escola", returned.getString("schoolName"));
                    InsertItemEditable(returned.getString("tel"));
                    preferences_manipulator.putString(getApplicationContext(), "tel", returned.getString("tel"));
                    break;
                case "update_ok": // Do something
                    view1NewPass.setText(null);
                    Snackbar.make(mainLayout, R.string.success_data_update, Snackbar.LENGTH_SHORT).show();
                    break;
                case "update_error": // Do something
                    Snackbar.make(mainLayout, R.string.somethigis_wrong, Snackbar.LENGTH_SHORT).show();
                    break;
                case "invalid_tel": // Do something
                    showAlertDialogTel();
                    break;
                case "tel_exists": // Do something
                    Snackbar.make(mainLayout, R.string.tel_exists, Snackbar.LENGTH_SHORT).show();
                    break;
                default: // Se algo deu arrado
                    Snackbar.make(mainLayout, R.string.user_not_found, Snackbar.LENGTH_SHORT).show();
                    break;
            }
        }catch (JSONException e){
            messageError();
        }
    }

    // Função para centralizar o display da mensagem de erro
    private void messageError(){
        Snackbar.make(mainLayout, getString (R.string.data_error), Snackbar.LENGTH_INDEFINITE).setAction("Repetir", new View.OnClickListener(){
            @Override
            public void onClick(View view){
                contentLayout.setVisibility(View.INVISIBLE);
                loadGif.setVisibility(View.VISIBLE);
                new requestData().execute(url);
            }}).show();
    }

    // Funcao para salvar senha, se for aluno, ou telefone e senha, se for responsavel
    public void saveChanges(View view) {
        if(Conexao.isConnected(getApplicationContext())){

            // Pegar a senha
            String newPass = view1NewPass.getText().toString();

            // Montar os parametros
            if(preferences_manipulator.getString(getApplicationContext(), "type", "student").equals("student")){
                parametros = "action=3&registry=" + preferences_manipulator.getString(getApplicationContext(), "identifier", "");
            }else{
                // Pegar a senha
                TextView view1Newtel = findViewById(R.id.listItemEdit);
                String newTel = view1Newtel.getText().toString();
                if(newTel.length() != 14){
                    showAlertDialogTel();
                    return;
                }else{
                    if((newTel.equals(preferences_manipulator.getString(getApplicationContext(), "tel", ""))) && (newPass.isEmpty())){
                        Snackbar.make(mainLayout, getString (R.string.fill_the_new_tel_field), Snackbar.LENGTH_SHORT).show();
                        return;
                    }else{
                        preferences_manipulator.putString(getApplicationContext(), "tel", newTel);
                        parametros = "action=4&identifier=" + preferences_manipulator.getString(getApplicationContext(), "identifier", "") + "&newtel=" + newTel;
                    }
                }
            }

            // Concatenar a senha
            parametros += "&newPass=" + newPass;
            new requestData().execute(url);
        }else{ // Se não houver conexão com a internet
            Snackbar.make(mainLayout, getString (R.string.internet_connection), Snackbar.LENGTH_SHORT).show();
        }
    }

    // Função que faz a requisição dos dados para a função Conexao (outra classe Java)
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
                contentLayout.setVisibility(View.VISIBLE);
                if(preferences_manipulator.getString(getApplicationContext(), "type", "student").equals("student")){processDataStudent(jsonObject);
                }else{processDataResponsible(jsonObject);}
            }catch (JSONException e) {
                if(attempts < 2){
                    attempts++;
                    messageError();
                }else{
                    Snackbar.make(mainLayout, getString (R.string.attempts_limit), Snackbar.LENGTH_LONG).show();
                }
            }finally {
                // Torna o gif invisível
                loadGif.setVisibility(View.GONE);
            }
        }
    }

    // Mostra o AlertDialog do telefone
    private void showAlertDialogTel(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_tel_title)
                .setMessage(R.string.dialog_tel_body)
                .setNeutralButton("Ok", null)
                .show();
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
        finish();
    }

    // Funcao de logoff
    public void logoff(View v){
        preferences_manipulator.deletAllPreferences(getBaseContext());
        startActivity(new Intent(this, login.class));
        finish();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferences_manipulator.deletkey(getApplicationContext(), "tel");
    }
}
