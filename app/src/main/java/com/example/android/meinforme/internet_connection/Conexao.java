package com.example.android.meinforme.internet_connection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Iniciação Científica Júnior - Núcleo de Inovações Tecnológicas.
 */

public class Conexao {

    public static String postDados(String urlUsuario, String parametrosUsuario) throws IOException{
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        builder.url(urlUsuario);

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

        RequestBody body = RequestBody.create(mediaType, parametrosUsuario);

        builder.post(body);

        Request request = builder.build();

        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e){
            return "";
        }
    }

    public static boolean isConnected(Context context){
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

}
