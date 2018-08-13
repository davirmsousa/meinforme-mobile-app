package com.example.android.meinforme.fragments;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.meinforme.R;
import com.example.android.meinforme.internet_connection.Conexao;
import com.example.android.meinforme.toinflate.cardetails;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

/*
 * Created by Iniciação Científica Júnior - Núcleo de Inovações Tecnológicas.
 */

public class ride_student extends Fragment {
    // Declaração de objetos do xml
    String url = "http://meinforme.pe.hu/apk/ride/ride_model.php", parametros = "";
    LinearLayout mainLayout, linearContent;
    LayoutInflater systemServiceInflater;
    ImageView loadGif;
    int attempts = 0;
    View fragment;

    /* quando mostrar as caronas, no codigo de inserilas adicione o setTag(...);
       ela funciona como um 'value' ou 'data-attribute' do html
       dai quando o usar clicar no elemento vc pega o valor com getTag(...)
     */
    public ride_student() {}

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragment = inflater.inflate(R.layout.fragment_ride_student, container, false);
        // Inflater
        systemServiceInflater = inflater;

        url = "http://10.0.2.2:8080/my%20portable%20files/MEINFORME/Meinforme-Apk/ride/ride_model.php";

        // Vinculação dos objetos xml
        // Layouts
        linearContent = fragment.findViewById(R.id.mainContentLayout);
        mainLayout = fragment.findViewById(R.id.mainFragmentLayout);
        // gif loader
        loadGif = fragment.findViewById(R.id.imageLoadRide);
        Glide.with(getActivity()).load(R.drawable.load4).asGif().into(loadGif);

        // Ver se tem conexao com a internet
        if (Conexao.isConnected(Objects.requireNonNull(getActivity()).getApplicationContext())){
            // Pegar as previas das caronas
            parametros = "action=4";
            new requestData().execute(url);
        }else{
            loadGif.setVisibility(View.GONE);
            linearContent.setVisibility(View.VISIBLE);
            Snackbar.make(mainLayout, getString (R.string.internet_connection), Snackbar.LENGTH_SHORT).show();
        }

        return fragment;
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
                case "nothing": // All
                    Snackbar.make(mainLayout, getString (R.string.nothing), Snackbar.LENGTH_SHORT).show();
                    break;
                case "successful": // Details
                    showRidePreview(returned);
                    break;
                default: // Usuario nao encontrado
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
                loadGif.setVisibility(View.GONE);
                linearContent.setVisibility(View.VISIBLE);
            }
        }
    }

    // Funcao para listar as caronas encontradas
    private void showRidePreview(JSONObject data){
        try {
            JSONObject allRideData = data.getJSONObject("ride");

            int l = allRideData.length();

            for(int i = 1; i <= l; i++){
                JSONObject ride = allRideData.getJSONObject("" + i);

                assert systemServiceInflater != null;
                @SuppressLint("InflateParams") View newLayout = systemServiceInflater.inflate(R.layout.toinflate_itemridemodel, null);
                linearContent.addView(newLayout);

                newLayout.setOnClickListener(getRouteListener);

                newLayout.setTag(ride.getString("OwnerId"));

                TextView tvName = newLayout.findViewById(R.id.responsibleName);
                tvName.setText(ride.getString("OwnerName"));

                JSONObject route = ride.getJSONObject("route");

                TextView tvR1 = newLayout.findViewById(R.id.route1);
                tvR1.setText(route.getString("1"));

                TextView tvR2 = newLayout.findViewById(R.id.route2);
                tvR2.setText(route.getString("2"));

                TextView tvR3 = newLayout.findViewById(R.id.route3);
                tvR3.setText(route.getString("3"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Funcao para pegar o id do responsavel e pegar mais detalhes no banco
    private View.OnClickListener getRouteListener = new View.OnClickListener() {
        @SuppressLint("NewApi")
        @Override
        public void onClick(View v) {
            // Pegar os detalhes da carona
            startActivity(
                    new Intent(Objects.requireNonNull(
                            getActivity()).getApplicationContext(), cardetails.class).
                            putExtra("ownerId", v.getTag().toString()));
        }
    };

}
