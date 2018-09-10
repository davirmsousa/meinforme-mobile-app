package com.example.android.meinforme.views;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.meinforme.R;
import com.example.android.meinforme.internet_connection.Conexao;
import com.example.android.meinforme.notice_class.Notice;
import com.example.android.meinforme.popup;
import com.example.android.meinforme.useful_classes.preferences_manipulator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/*
 * Created by Iniciação Científica Júnior - Núcleo de Inovações Tecnológicas.
*/

@RequiresApi(api = Build.VERSION_CODES.O)
public class notices extends AppCompatActivity{
    // Declaração de objetos do xml
    @SuppressLint("SimpleDateFormat")String dateString = new SimpleDateFormat("yyyy-M-d").format(System.currentTimeMillis());
    String url = "http://seuinforme.com.br/meinforme-apk/notices/notices_model.php", parametros = "", schoolCode;
    CoordinatorLayout MainLayoutNotices;
    LinearLayout NoticeLayout, menuIcon;
    DatePickerDialog datePick;
    TextView dateNotice;
    ImageView loadGif;
    int attempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_notices);

        // Vinculação dos objetos xml
        MainLayoutNotices = findViewById(R.id.MainCoordinatorLayout);
        NoticeLayout = findViewById(R.id.LinearNotices);
        loadGif = findViewById(R.id.imageLoadNotice);
        menuIcon = findViewById(R.id.LinearMenuIcon);
        dateNotice = findViewById(R.id.noticeDate);
        Glide.with(this).load(R.drawable.load4).asGif().into(loadGif);

        if(preferences_manipulator.getInt(getApplicationContext(), "introducing_popup", 0) == 0){
            preferences_manipulator.putInt(getApplicationContext(),"introducing_popup", 1);
            startActivity(new Intent(this, popup.class).putExtra("messageNum", 1));
        }

        //url = "http://10.0.2.2:8080/my%20portable%20files/MEINFORME/meinforme-apk/notices/notices_model.php";

        // Inflar o menu de estudante ou de responsavel
        int menu = R.layout.menu_student;
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        if(preferences_manipulator.getString(getApplicationContext(), "type", "").equals("responsible"))
            menu = R.layout.menu_responsible;
        assert inflater != null;
            View childLayout = inflater.inflate(menu, (ViewGroup) findViewById(R.id.LinearListIcon));
        menuIcon.addView(childLayout);

        // Mudar a imagem da Activity atual
        ImageView currentIconActivity = findViewById(R.id.iconNews);currentIconActivity.setOnClickListener(null);
        currentIconActivity.setImageDrawable(getResources().getDrawable(R.drawable.newspaper_o));

        // Função para construir url e parâmetros para buscar os avisos no banco
        requestNotices(true);
    }

    // responsavel por tratar o retorno de requestData
    private void processData(JSONObject returned){
        try {
            String status = returned.getString("status");

            switch (status){
                case "notice_ok": // Se a requisição for bem sucedida e se houver algum informativo pra mostrar
                    // Crio meu objeto informativos
                    ArrayList<Notice> ArrayNotices = new ArrayList<>();

                    // Recupero o array de informativos
                    JSONArray jsonArray = returned.getJSONArray("notices");

                    // Adiciono os informativos recebidos na lista
                    for(int i = 0, tam = jsonArray.length(); i < tam; i++){
                        Notice nc = new Notice();

                        nc.setBody(jsonArray.getJSONObject(i).getString("body"));
                        nc.setTitle(jsonArray.getJSONObject(i).getString("title"));
                        try { // Se o indice data existir
                            if(jsonArray.getJSONObject(i).getString("date") != null){
                                nc.setDate(jsonArray.getJSONObject(i).getString("date"));
                            }
                        }catch (Exception ignored){}

                        ArrayNotices.add(nc);
                    }

                    // Funcao que mostra os avisos na tela
                    ShowNotices(ArrayNotices);
                    break;
                case "nothing": // Se não houver informativo pra mostrar
                    Snackbar.make(MainLayoutNotices, "Não há avisos desta data.", Snackbar.LENGTH_SHORT).show();
                    break;
                default: // Se algo deu arrado
                    Snackbar.make(MainLayoutNotices, "Não foi possível recuperar os avisos.", Snackbar.LENGTH_SHORT).show();
                    break;
            }
        }catch (JSONException e){
            Log.i("erro json", e + "");
            messageError();
        }
    }

    // Função para centralizar o display da mensagem de erro
    private void messageError(){
        Snackbar.make(MainLayoutNotices, getString (R.string.data_error), Snackbar.LENGTH_INDEFINITE).setAction("Repetir", new View.OnClickListener(){
            @Override
            public void onClick(View view){
                new requestData().execute(url);
            }}).show();
    }

    // Função que faz a requisição dos dados para a função Conexao (outra classe Java)
    @SuppressLint("StaticFieldLeak")
    private class requestData extends AsyncTask<String, Void, String>{

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
                    Snackbar.make(MainLayoutNotices, getString (R.string.attempts_limit), Snackbar.LENGTH_LONG).show();
                }
            }finally {
                // Torna o gif invisível
                loadGif.setVisibility(View.GONE);
            }
        }
    }

    // Função para alterar a data de dd-mm-aaaa para aaaa-mm-dd
    private String reverseDate(String dt, String toCut, String toPut){
        String[] s = dt.split(toCut);
        if(s[1].length() == 1) s[1] = "0" + s[1];
        if(s[2].length() == 1) s[2] = "0" + s[2];
        return s[2] + toPut + s[1] + toPut + s[0];
    }

    // Função para mostrar a data
    @SuppressLint("SetTextI18n")
    private void showTodaysDate(){
        dateString = reverseDate(dateString, "-", "/");
        dateNotice.setText(dateString + getString(R.string.reload));
    }

    // Função para construir url e parâmetros para buscar os avisos no banco
    private void requestNotices(boolean getMostRecent){
        // Verificação da conexão com a internet
        if(Conexao.isConnected(getApplicationContext())){
            // Apaga todas as views filhas do layout
            NoticeLayout.removeAllViews();
            // Deixa o gif visivel
            loadGif.setVisibility(View.VISIBLE);
            // Constroi a url e os parâmetros para pegar os informativos do dia atual
            schoolCode = preferences_manipulator.getString(getApplicationContext(), "schoolcode", "");
            String classTarget = preferences_manipulator.getString(getApplicationContext(), "class", "all");

            parametros = "schoolCode=" + schoolCode + "&class=" + classTarget;

            if(getMostRecent){// Se for pra pegar os mais recentes
                parametros+= "&action=2";
            }else{// Se for pra pegar de data especifica
                parametros+= "&action=1&noticeDate=" + dateString;
            }
            new requestData().execute(url);
        }else{ // Se não houver conexão com a internet
            // Deixa o gif invisivel
            loadGif.setVisibility(View.GONE);
            Snackbar.make(MainLayoutNotices, getString (R.string.internet_connection), Snackbar.LENGTH_SHORT).show();
        }
    }

    // Função que mostra os informativos na tela
    private void ShowNotices(ArrayList<Notice> AllNotices){
        // Converte o valor para pixels
        int marginTopDate = getPixelValue(this, (int) getResources().getDimension(R.dimen.date_margin));
        // Armazena a data do último aviso
        String date = "";

        // Insere novas views com base na quantidade de avisos
        for(int i = 0, tam = AllNotices.size(); i < tam; i++){
            // Criação e estilização de objetos XML (TextView para o título e para o corpo do aviso)
            if ((AllNotices.get(i).getDate() != null) && (!date.equals(AllNotices.get(i).getDate()))) {
                // Atualizar a ultima data
                date = AllNotices.get(i).getDate();
                // Criar a view
                TextView txtViewDate = new TextView(getBaseContext());
                // Adicionar margem
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, marginTopDate, 0, 0);
                txtViewDate.setLayoutParams(layoutParams);
                // Adicionar a dada na view e inserir na tela
                txtViewDate.setText(reverseDate(date, "-", "/"));
                NoticeLayout.addView(txtViewDate);
                // Setar cor do texto, plano de fundo, paddings, alinhamento do texto e largura
                txtViewDate.setTextColor(getResources().getColor(R.color.white));
                txtViewDate.setBackground(ContextCompat.getDrawable(this, R.drawable.shape_shownoticedate));
                txtViewDate.setPadding((int) getResources().getDimension(R.dimen.date_padding),
                        (int) getResources().getDimension(R.dimen.date_padding),
                        (int) getResources().getDimension(R.dimen.date_padding),
                        (int) getResources().getDimension(R.dimen.date_padding));
                txtViewDate.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                txtViewDate.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
            }

            // Inflar o layout modelo
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") View newNoticeLayout = inflater.inflate(R.layout.toinflate_noticemodel, null);
            NoticeLayout.addView(newNoticeLayout);
            // Mudar o texto das views filhas
            TextView viewNoticeTitle = newNoticeLayout.findViewById(R.id.noticeTitle); viewNoticeTitle.setText(AllNotices.get(i).getTitle());
            TextView viewNoticeBody = newNoticeLayout.findViewById(R.id.noticeBody); viewNoticeBody.setText(AllNotices.get(i).getBody());
        }
    }

    // Abre o calendário
    public void showCalendar(View view){
        Calendar calendar = Calendar.getInstance();
        int ano = calendar.get(Calendar.YEAR);
        int dia = calendar.get(Calendar.DAY_OF_MONTH);
        int mes = calendar.get(Calendar.MONTH);
        datePick = new DatePickerDialog(this, new selectDate(), ano, mes, dia);
        datePick.show();
    }

    // Pega a data selecionada pelo usuário
    private class selectDate implements DatePickerDialog.OnDateSetListener{
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            Date date = calendar.getTime();

            // Formata a data para o formato dd-mm-aaaa
            @SuppressLint("SimpleDateFormat") String dt = new SimpleDateFormat("d/M/yyyy").format(date);

            // Função que inverte a data
            dateString = reverseDate(dt, "/", "-");

            // Função para construir url e parâmetros para buscar os avisos no banco
            requestNotices(false);

            // Função para mostrar a data
            showTodaysDate();
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
        finish();
    }

    // Funcao para transfrmar um valor em medidas de pixel
    public static int getPixelValue(Context context, int dimenId) {
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dimenId,
                resources.getDisplayMetrics()
        );
    }

    // Quando o usuario clicar em algum botao fisico do celular
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Se o botao for de voltar
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }else {
            return super.onKeyDown(keyCode, event);
        }

    }
}
