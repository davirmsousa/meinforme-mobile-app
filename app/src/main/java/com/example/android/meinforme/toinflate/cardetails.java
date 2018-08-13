package com.example.android.meinforme.toinflate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.meinforme.R;
import com.example.android.meinforme.internet_connection.Conexao;
import com.example.android.meinforme.popup;
import com.example.android.meinforme.useful_classes.preferences_manipulator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

public class cardetails extends Activity{
    // Declaração de objetos do xml
    String url = "http://meinforme.pe.hu/apk/ride/ride_model.php", parametros = "";
    android.support.v7.widget.Toolbar customTooBar;
    LinearLayout linearContent, linearRoutes;
    CoordinatorLayout mainLayout;
    ImageView loadGif;
    int attempts = 0;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_cardetails);

        if(preferences_manipulator.getInt(getApplicationContext(), "ridedetails_popoup", 0) == 0){
            preferences_manipulator.putInt(getApplicationContext(),"ridedetails_popoup", 1);
            startActivity(new Intent(this, popup.class).putExtra("messageNum", 3));
        }

        // Vinculação dos objetos xml
        // Layouts
        linearRoutes = findViewById(R.id.layoutRoutesCarDetails);
        mainLayout = findViewById(R.id.mainCarDetailsLayout);
        linearContent = findViewById(R.id.mainContentView);
        // Toobar
        customTooBar = findViewById(R.id.customTooBar);
        // gif loader
        loadGif = findViewById(R.id.imageLoadCarDetails);
        Glide.with(this).load(R.drawable.load3).asGif().into(loadGif);


        // Ativar o botao de retorno do menu
        customTooBar.setNavigationIcon(R.drawable.back_icon);
        customTooBar.setNavigationOnClickListener(backListener);

        url = "http://10.0.2.2:8080/my%20portable%20files/MEINFORME/Meinforme-Apk/ride/ride_model.php";

        // Verifica se existe algum putExtra
        Bundle extras = getIntent().getExtras();
        if(null != extras && extras.getString("ownerId") != null){
            // Ver se tem conexao com a internet
            if (Conexao.isConnected(getApplicationContext())){
                // Pegar os detalhes da carona
                parametros = "action=5&carOwnerId=" +  Objects.requireNonNull(extras.getString("ownerId"));
                new requestData().execute(url);
            }else{
                hideGif();
                Snackbar.make(mainLayout, getString (R.string.internet_connection), Snackbar.LENGTH_SHORT).show();
            }
        }else{
            hideGif();
            Snackbar.make(mainLayout, getString (R.string.car_not_found), Snackbar.LENGTH_SHORT).show();
        }
    }

    // Função para centralizar o display da mensagem de erro
    private void messageError(){
        Snackbar.make(mainLayout, getString (R.string.data_error), Snackbar.LENGTH_LONG).setAction("Repetir", new View.OnClickListener(){
            @Override
            public void onClick(View view){
                new requestData().execute(url);
            }}).show();
    }

    // Função para pegar os dados de requestData e fazer alguma coisa
    private void processData(JSONObject returned){
        try {
            String status = returned.getString("status");
            switch (status){
                case "car_not_found": // Details
                    Snackbar.make(mainLayout, getString (R.string.car_not_found), Snackbar.LENGTH_SHORT).show();
                    break;
                case "car_found": // Details
                    // Dados do responsavel
                    JSONObject ownerData = returned.getJSONObject("ownerData");
                    insertTextInView(R.id.responsibleTelRideDetails, "Telefone: " + ownerData.getString("tel"));
                    insertTextInView(R.id.responsibleNameRideDetails, ownerData.getString("name"));
                    insertTextInView(R.id.responsibleMailRideDetails, "Email: " + ownerData.getString("email"));
                    // Dados do aluno
                    JSONObject studentData = returned.getJSONObject("StudentData");
                    insertTextInView(R.id.studentName, "Nome: " + studentData.getString("studentName"));
                    insertTextInView(R.id.studentClass, "Turma: " + studentData.getString("studentClass"));
                    // Dados do carro
                    JSONObject carData = returned.getJSONObject("carDetails");
                    insertTextInView(R.id.carMark, "Marca: " + carData.getString("mark"));
                    insertTextInView(R.id.carModel, "Modelo: " + carData.getString("model"));
                    insertTextInView(R.id.carColor, "Cor: " + carData.getString("color"));
                    insertTextInView(R.id.carPlate, "Placa: " + carData.getString("plate"));
                    insertTextInView(R.id.carVacancies, "Vagas disponíveis: " + carData.getString("vacancies"));
                    // Rotas
                    JSONObject routes = returned.getJSONObject("routes");
                    for(int i = 0; i < routes.length(); i++){
                        addRoute(routes.getString("" + (i + 1)));
                    }
                    // Rodape
                    String[] cuttedName = ownerData.getString("name").split(" ");
                    insertTextInView(R.id.footerMessage, String.format(getResources().getString(R.string.car_datails_footer_information), cuttedName[0] + " " + cuttedName[1]));
                    break;
                default: // Usuario nao encontrado
                    Snackbar.make(mainLayout, getString (R.string.somethigis_wrong), Snackbar.LENGTH_SHORT).show();
                    break;
            }
        }catch (JSONException e){
            messageError();
        }finally {
            hideGif();
        }
    }

    // Funcao para regar um id e atribui um texto
    private void insertTextInView(int id, String text){
        TextView tempTv = findViewById(id);
        tempTv.setText(text);
    }

    // Funcao para adicionar um item de rota na tela
    private void addRoute(String text){
        TextView txtView = new TextView(getBaseContext());
        txtView.setText(text);

        linearRoutes.addView(txtView);

        // getPixelValue(this, (int) getResources().getDimension(R.dimen.item_route_size))
        // int padding = getPixelValue(this, (int) getResources().getDimension(R.dimen.item_route_default_padding));
        txtView.setTextColor(getResources().getColor(R.color.pureBlack));
        txtView.setTextSize((int) getResources().getDimension(R.dimen.item_route_size));
        int padding = (int) getResources().getDimension(R.dimen.item_route_default_padding);
        txtView.setPadding((int) getResources().getDimension(R.dimen.item_route_right_padding),padding,padding,padding);
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
                    Snackbar.make(mainLayout, getString (R.string.attempts_limit), Snackbar.LENGTH_LONG).show();
                }
            }finally {
                hideGif();
            }
        }

    }

    // Funcao para esconder o gif
    private void hideGif(){
        loadGif.setVisibility(View.GONE);
        linearContent.setVisibility(View.VISIBLE);
    }

    // Funcao para quando o usuario clicar no botao de voltar
    private View.OnClickListener backListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
