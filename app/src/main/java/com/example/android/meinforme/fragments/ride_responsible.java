package com.example.android.meinforme.fragments;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.example.android.meinforme.R;
import com.example.android.meinforme.internet_connection.Conexao;
import com.example.android.meinforme.useful_classes.preferences_manipulator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

/*
 * Created by Iniciação Científica Júnior - Núcleo de Inovações Tecnológicas.
 */

public class ride_responsible extends Fragment {
    // Declaração de objetos do xml
    String url = "http://meinforme.pe.hu/apk/ride/ride_model.php", parametros = "", routeParameters = "";
    TextInputLayout widgetMark, widgetModel, widgetColor, widgetPlate, widgetVacancies;
    LinearLayout linearLRoute, mainLayout, linearContent, linearDeleteCar;
    EditText editMark, editModel, editColor, editPlate, editVacancies;
    LayoutInflater systemServiceInflater;
    int countField = 0, attempts = 0;
    ImageView addRoute, loadGif;
    Button btnSaveRoute;
    View fragment;

    public ride_responsible() {}

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar o Layout
        fragment = inflater.inflate(R.layout.fragment_ride_responsible, container, false);

        url = "http://10.0.2.2:8080/my%20portable%20files/MEINFORME/Meinforme-Apk/ride/ride_model.php";

        // Vinculação dos objetos xml
        // Layouts
        mainLayout = fragment.findViewById(R.id.mainRideResponsibleLayout);
        linearDeleteCar = fragment.findViewById(R.id.layoutDeleteCar);
        linearContent = fragment.findViewById(R.id.LayoutContent);
        linearLRoute = fragment.findViewById(R.id.LayoutRoute);
        // Botoes
        addRoute = fragment.findViewById(R.id.imgButtonAddRoute);
        btnSaveRoute = fragment.findViewById(R.id.btnSaveRoute);
        // Inflater
        systemServiceInflater = inflater;
        // Elementos do form Detalhes do carro
        widgetMark = fragment.findViewById(R.id.widgetCarMark);
        editMark = fragment.findViewById(R.id.EditCarMark);
        widgetModel = fragment.findViewById(R.id.widgetCarModel);
        editModel = fragment.findViewById(R.id.EditCarModel);
        widgetColor = fragment.findViewById(R.id.widgetCarColor);
        editColor = fragment.findViewById(R.id.EditCarColor);
        widgetPlate = fragment.findViewById(R.id.widgetCarPlate);
        editPlate = fragment.findViewById(R.id.EditCarPlate);
        widgetVacancies = fragment.findViewById(R.id.widgetCarVacancies);
        editVacancies = fragment.findViewById(R.id.EditCarVacancies);
        // gif loader
        loadGif = fragment.findViewById(R.id.imageLoadRide);
        Glide.with(getActivity()).load(R.drawable.load4).asGif().into(loadGif);

        // Adicionar um Listener aos botoes
        addRoute.setOnClickListener(btnAddRouteListener);
        btnSaveRoute.setOnClickListener(btnSaveRouteListener);
        linearDeleteCar.setOnClickListener(deleteCarListener);

        //Adicionar Listeners aos EditTexts
        addChangeListener(editMark, widgetMark);
        addChangeListener(editModel, widgetModel);
        addChangeListener(editColor, widgetColor);
        addChangeListener(editPlate, widgetPlate);
        addChangeListener(editVacancies, widgetVacancies);

        if(Conexao.isConnected(Objects.requireNonNull(getActivity()).getApplicationContext())){
            parametros = "action=2&carOwnerId="+ preferences_manipulator.getString(getActivity().getApplicationContext(), "identifier", "");
            new requestData().execute(url);
        }else{
            // Adicionar 5 EditText para as rotas
            for(int o = 0; o < 5; o++){addFieldRoute(null);}
            loadGif.setVisibility(View.GONE);
            linearContent.setVisibility(View.VISIBLE);
            Snackbar.make(mainLayout, getString (R.string.internet_connection), Snackbar.LENGTH_SHORT).show();
        }

        // Retornar o layout inflado para a view 'ride'
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
                case "success_submitting": // Cadastro / Update
                    Snackbar.make(mainLayout, getString (R.string.success_data_insert), Snackbar.LENGTH_SHORT).show();
                    break;
                case "irreal_vacancies": // Cadastro / Update
                    Snackbar.make(mainLayout, getString (R.string.irreal_vacancies), Snackbar.LENGTH_SHORT).show();
                    break;
                case "data_route_error": // Cadastro / Update
                    dialogNeutralButton(getString(R.string.somethigis_wrong), "As rotas"+returned.getString("routesErrorList")+" não foram inseridas com sucesso"+"\nPor favor, tente novamente.");
                    break;
                case "car_found": // Pegar o carro
                    insertCarDetails(returned);
                    break;
                case "car_not_found": // Pegar o carro
                    for(int o = 0; o < 5; o++){addFieldRoute(null);}
                    break;
                case "car_not_exists": // Deletar o carro
                    Snackbar.make(mainLayout, getString (R.string.car_not_exists), Snackbar.LENGTH_SHORT).show();
                    break;
                case "car_exists": // Deletar o carro
                    afterDeleteCar();
                    Snackbar.make(mainLayout, getString (R.string.success_data_delete), Snackbar.LENGTH_SHORT).show();
                    break;
                default: // Usuario nao encontrado
                    Snackbar.make(mainLayout, getString (R.string.somethigis_wrong), Snackbar.LENGTH_SHORT).show();
                    break;
            }
        }catch (JSONException e){
            messageError();
        }
    }

    // Inserir os dados do carro nos EditText
    private void insertCarDetails(JSONObject data){
        try {
            JSONObject details = data.getJSONObject("carDetails");
            editMark.setText(details.getString("mark"));
            editModel.setText(details.getString("model"));
            editColor.setText(details.getString("color"));
            editPlate.setText(details.getString("plate"));
            editVacancies.setText(details.getString("vacancies"));

            try{
                JSONArray routes = data.getJSONArray("route");
                for(int i = 0; i < routes.length(); i++){
                    addFieldRoute(routes.get(i).toString());
                }
            }catch (Throwable ignore){
                Snackbar.make(mainLayout, getString (R.string.can_not_get_the_routes), Snackbar.LENGTH_SHORT).show();
                for(int o = 0; o < 5; o++){addFieldRoute(null);}
            }
        } catch (JSONException e) {
            e.printStackTrace();
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

    // Funcao para adicionar mais um campo para digitar a rota
    @SuppressLint("ResourceType")
    private void addFieldRoute(String text){
        if(countField < 10){
            // incrementar a contagem de EditText de rotas
            countField++;

            // Inflar o layout EditText
            assert systemServiceInflater != null;
            @SuppressLint("InflateParams") View newLayout = systemServiceInflater.inflate(R.layout.toinflate_edittext, null);
            linearLRoute.addView(newLayout);
            // Mudar o id do Layout
            newLayout.setId(((+(linearLRoute.getId())) + countField));
            // Mudar o EditText
            EditText et = newLayout.findViewById(R.id.inflatedEditText);
            et.setId(((+(linearLRoute.getId() * 2)) + countField));
            et.setText(text);
            // Mudar a dica flutuante
            TextInputLayout til = newLayout.findViewById(R.id.widgetInflatedEditText);
            til.setId((+(linearLRoute.getId() * 3) + (countField)));
            til.setHint(getResources().getString(R.string.route_hint) + " " + countField);
        }else{
            Snackbar.make(mainLayout, R.string.route_limit, Snackbar.LENGTH_SHORT).show();
        }
    }

    // Funcao para ver se os detalhes do carro foram preenchidos
    public boolean carDatailsIsFilled(){
        boolean toReturn = true;
        if(editMark.getText().toString().isEmpty()){
            toReturn = false;
            widgetMark.setError(getString(R.string.required_input));
        }
        if(editModel.getText().toString().isEmpty()){
            toReturn = false;
            widgetModel.setError(getString(R.string.required_input));
        }
        if(editColor.getText().toString().isEmpty()){
            toReturn = false;
            widgetColor.setError(getString(R.string.required_input));
        }
        if(editPlate.getText().toString().isEmpty()){
            toReturn = false;
            widgetPlate.setError(getString(R.string.required_input));
        }
        if(editVacancies.getText().toString().isEmpty()){
            toReturn = false;
            widgetVacancies.setError(getString(R.string.required_input));
        }
        /* Se ao final da funcao toReturn for true significa que
        *  todos os campos foram preenchidos*/
        return toReturn;
    }

    // Funcao para contar quantos EditTexts de rotas estao preenchidos
    @SuppressLint("ResourceType")
    public int routeIsFilled(){
        int countFilled = 0;
        routeParameters = "";
        try{
            StringBuilder routeParametersBuilder = new StringBuilder();
            for(int c = 0; c < countField; c++){
                // Pego o id e crio um objeto RelativeLayout (Layout que foi inflado)
                RelativeLayout relativeLayout = (RelativeLayout) linearLRoute.getChildAt(c);
                // Pego o id e crio um objeto TextInputLayout (elemento dentro do relativeLayoutId)
                TextInputLayout textInputLayout = (TextInputLayout) relativeLayout.getChildAt(0);
                // Pego o id e crio um objeto EditText (elemento dentro do textInputLayout)
                EditText editText = textInputLayout.getEditText();
                // Se o campo nao estiver vazio incremento countFilled
                assert editText != null;
                if(!editText.getText().toString().isEmpty()){
                    routeParametersBuilder.append("&route").append(countFilled).append("=").append(editText.getText().toString());
                    countFilled++;
                }
            }
            routeParameters = routeParametersBuilder.toString();
            /* Se o loop conseguir recuperar com sucesso os EditTexts ele vai retornar
            * o numero de rotas preenchidas*/
            return countFilled;
        }catch (Exception e){
            // Se o loop falhar ele retorna -1
            return -1;
        }
    }

    // Funcao para anular os EditText depois de excluir o carro
    private void afterDeleteCar(){
        editMark.setText(null);
        editModel.setText(null);
        editColor.setText(null);
        editPlate.setText(null);
        editVacancies.setText(null);

        linearLRoute.removeAllViews();
        for(int o = 0; o < 5; o++){addFieldRoute(null);}
    }

    private void addChangeListener(EditText view, final TextInputLayout widget){
        view.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s){widget.setError(null);}
            public void beforeTextChanged(CharSequence s, int start, int count, int after){widget.setError(null);}
            public void onTextChanged(CharSequence s, int start, int before, int count){widget.setError(null);}
        });
    }

    @SuppressLint("NewApi")
    private void dialogNeutralButton(String title, String body){
        new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setTitle(title)
                .setMessage(body)
                .setNeutralButton("Ok", null)
                .show();
    }

    // Funcao do addRoute para adicionar mais um item de rota
    private View.OnClickListener btnAddRouteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addFieldRoute(null);
        }
    };

    // Funcao para enviar os dados pro banco
    private View.OnClickListener btnSaveRouteListener = new View.OnClickListener() {
        @SuppressLint("NewApi")
        @Override
        public void onClick(View v) {
            if(Conexao.isConnected(Objects.requireNonNull(getActivity()).getApplicationContext())){
                if(carDatailsIsFilled()){
                    int countRoutesFilled = routeIsFilled();
                    if(countRoutesFilled > 4){
                        parametros = "action=1&carOwnerId="+ preferences_manipulator.getString(getActivity().getApplicationContext(), "identifier", "")+
                                "&carMark="+editMark.getText().toString()+"&carModel="+editModel.getText().toString()+"&carColor="+editColor.getText().toString()+
                                "&carPlate="+editPlate.getText().toString()+"&carVacancies="+editVacancies.getText().toString()+"&routesAmount="+countRoutesFilled;
                        parametros += routeParameters;
                        new requestData().execute(url);
                    }else if(countRoutesFilled < 0){
                        Snackbar.make(mainLayout, getString (R.string.can_not_get_the_routes), Snackbar.LENGTH_SHORT).show();
                    }else{
                        Snackbar.make(mainLayout, getString (R.string.route_required), Snackbar.LENGTH_LONG).show();
                    }
                }else{
                    Snackbar.make(mainLayout, getString (R.string.car_details_required), Snackbar.LENGTH_LONG).show();
                }
            }else{
                Snackbar.make(mainLayout, getString (R.string.internet_connection), Snackbar.LENGTH_SHORT).show();
            }
        }
    };

    // Funcao para apagar o carro
    private View.OnClickListener deleteCarListener = new View.OnClickListener() {
        @SuppressLint("NewApi")
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                    .setTitle(R.string.confirm_the_action)
                    .setMessage(R.string.dialog_delete_car_body)
                    .setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which){
                            parametros = "action=3&carOwnerId="+ preferences_manipulator.getString(getActivity().getApplicationContext(), "identifier", "");
                            new requestData().execute(url);
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        }
    };

}