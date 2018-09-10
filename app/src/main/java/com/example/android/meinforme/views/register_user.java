package com.example.android.meinforme.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.android.meinforme.R;
import com.example.android.meinforme.internet_connection.Conexao;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/*
 * Created by Iniciação Científica Júnior - Núcleo de Inovações Tecnológicas.
 */

public class register_user extends AppCompatActivity {

    // Declaração de objetos do xml
    Spinner spinSerie, spinTurma ;
    Button btnCadastrarAluno;
    TextView viewCodEscolaAluno, viewcadNomeAluno, viewCadEmailAluno, viewCadSenhaAluno, viewCadMatriAln;
    String url = "http://seuinforme.com.br/meinforme-apk/cadastraaluno/registrarAluno.php", parametros = "", serieEscolhida = "", turmaEscolhida = "", codigo;
    LinearLayout linerarGif;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_register_user);

        // Vinculação dos objetos xml ao Java por ID
        btnCadastrarAluno = findViewById(R.id.cadAcessarAluno);
        viewCadMatriAln = findViewById(R.id.matriculaAluno);
        viewCodEscolaAluno = findViewById(R.id.codigoEscolaAluno);
        viewcadNomeAluno = findViewById(R.id.cadNomeAluno);
        viewCadEmailAluno = findViewById(R.id.cadEmailAluno);
        viewCadSenhaAluno = findViewById(R.id.cadSenhaAluno);

        // Gif de carregamento
        Glide.with(this)
                .load(R.drawable.load3) // aqui é teu gif
                .asGif()
                .into((ImageView) findViewById(R.id.imggif));
        linerarGif = findViewById(R.id.LinearGif);

        spinSerie = findViewById(R.id.serie);// Associação da referencia do spinner no XML
        ArrayAdapter Serie = ArrayAdapter.createFromResource(this,R.array.serie_aluno,android.R.layout.simple_spinner_item);// definição da ligação dos itens do stringArray para serem exibidos
        spinSerie.setAdapter(Serie);

        spinTurma = findViewById(R.id.turma);// Associação da referencia do spinner no XML
        ArrayAdapter Turma = ArrayAdapter.createFromResource(this, R.array.turma_aluno,android.R.layout.simple_spinner_item);// definição da ligação dos itens do stringArray para serem exibidos
        spinTurma.setAdapter(Turma);

        // Guarda a série e a turma escolhidas pelo usuário logo após a seleção dos valores no spinner
        AdapterView.OnItemSelectedListener selecaoDeSerieTurma = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                serieEscolhida = spinSerie.getSelectedItem().toString();
                turmaEscolhida = spinTurma.getSelectedItem().toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        };

        spinSerie.setOnItemSelectedListener(selecaoDeSerieTurma);
        spinTurma.setOnItemSelectedListener(selecaoDeSerieTurma);

        // Quando clicar no botão para finalizar o cadastro
        btnCadastrarAluno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Verificação da conexão com a internet
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if(networkInfo != null && networkInfo.isConnected()){
                    //Pega o que o usuário escreveu para comparar com os dados do banco
                    String senha = viewCadSenhaAluno.getText().toString();
                    String nome = viewcadNomeAluno.getText().toString();
                    String email = viewCadEmailAluno.getText().toString();
                    String matricula = viewCadMatriAln.getText().toString();
                    codigo = viewCodEscolaAluno.getText().toString();

                    //Verifica se o usuário esqueceu de digitar algum dos campos
                    if(matricula.isEmpty() && senha.isEmpty() && email.isEmpty() && nome.isEmpty() && codigo.isEmpty()){
                        Toast.makeText(getApplicationContext(), "Preencha os campos.", Toast.LENGTH_LONG).show();
                    } else if(senha.isEmpty()){
                        Toast.makeText(getApplicationContext(), "Digite sua senha.", Toast.LENGTH_LONG).show();
                    } else if(email.isEmpty()){
                        Toast.makeText(getApplicationContext(), "Digite seu e-mail.", Toast.LENGTH_LONG).show();
                    } else if(nome.isEmpty()){
                        Toast.makeText(getApplicationContext(), "Digite seu nome.", Toast.LENGTH_LONG).show();
                    } else if(codigo.isEmpty()){
                        Toast.makeText(getApplicationContext(), "Digite o código fornecido pela instituição.", Toast.LENGTH_LONG).show();
                    } else if(matricula.isEmpty()){
                        Toast.makeText(getApplicationContext(), "Digite sua matrícula da instituição.", Toast.LENGTH_LONG).show();
                    }else{
                        // Mostrar o gif de carregamento
                        linerarGif.setVisibility(View.VISIBLE);

                        //Se ele completou os campos vamos fazer a verificação dos dados digitados
                        parametros = "studentRegistry=" + matricula+ "&senha=" + senha + "&email=" + email + "&nome=" + nome + "&codigo=" + codigo + "&turma=" + turmaEscolhida + "&serie=" + serieEscolhida;
                        new SolicitaDados().execute(url);
                    }
                } else{// Se não houver conexão com a internet
                    Toast.makeText(getApplicationContext(), "Não há conexão com a Internet", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Função que faz a requisição dos dados cpara a função Conexao (outra classe Java)
    @SuppressLint("StaticFieldLeak")
    private class SolicitaDados extends AsyncTask<String, Void, String> {

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
            // Esconder o gif de carregamento
            linerarGif.setVisibility(View.INVISIBLE);

            try{
                // Crio um objeto JSON para recuperar o status do view_login
                JSONObject respLogin = new JSONObject(resultado);
                String status = respLogin.getString("retorno");

                switch(status){
                    case "email_error"://Se o e-mail digitado for inválido
                        Toast.makeText(getApplicationContext(), "E-mail inválido.", Toast.LENGTH_LONG).show();
                        break;
                    case "email_exists"://Se o e-mail digitado já estiver cadastrado
                        Toast.makeText(getApplicationContext(), "E-mail já cadastrado.", Toast.LENGTH_LONG).show();
                        break;
                    case "turma_error"://Se a turma não existe
                        Toast.makeText(getApplicationContext(), "A turma e/ou série escolhida não existe.", Toast.LENGTH_LONG).show();
                        break;
                    case "codigo_error"://Se o código da instituição não for encontrado
                        Toast.makeText(getApplicationContext(), "O código da instituição não existe.", Toast.LENGTH_LONG).show();
                        break;
                    case "cadastro_error"://Se o cadastro falhar
                        Toast.makeText(getApplicationContext(), "Ocorreu um erro no cadastro. Tente novamente.", Toast.LENGTH_LONG).show();
                        break;
                    case "cadastro_ok":// Se o cadastro for aprovado;
                        //Cria o objeto SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("user_logado", 0);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("codEscAlun", codigo);
                        editor.putString("matriAln", viewCadMatriAln.getText().toString());
                        editor.putString("turmAlun", serieEscolhida + "" + turmaEscolhida);
                        editor.putBoolean("estaLogado", true);
                        editor.putBoolean("-", true);
                        editor.apply();

                        // Vai para a tela de avisos
                        Intent abreInicio = new Intent(register_user.this, notices.class);
                        startActivity(abreInicio);
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "Algo deu errado. Tente novamente.", Toast.LENGTH_LONG).show();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Ocorreu um erro no cadastro. Repita-o novamente.", Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    protected void onPause(){
        super.onPause();
        finish();
    }
}
