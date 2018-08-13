package com.example.android.meinforme.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.android.meinforme.R;
import com.example.android.meinforme.fragments.ride_responsible;
import com.example.android.meinforme.fragments.ride_student;
import com.example.android.meinforme.popup;
import com.example.android.meinforme.useful_classes.preferences_manipulator;

/*
 * Created by Iniciação Científica Júnior - Núcleo de Inovações Tecnológicas.
 */

public class ride extends AppCompatActivity {
    // Declaração de objetos do xml
    LinearLayout menuIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_ride);

        if(preferences_manipulator.getInt(getApplicationContext(), "ride_popup", 0) == 0){
            preferences_manipulator.putInt(getApplicationContext(),"ride_popup", 1);
            startActivity(new Intent(this, popup.class).putExtra("messageNum", 2));
        }

        if(savedInstanceState == null){
            // Adicionar o fragmento
            if(preferences_manipulator.getString(getApplicationContext(), "type", "").equals("responsible"))
                getSupportFragmentManager().beginTransaction().add(R.id.frameReceptor, new ride_responsible()).commit();
            else
                getSupportFragmentManager().beginTransaction().add(R.id.frameReceptor, new ride_student()).commit();
        }

        // Vinculação dos objetos xml
        menuIcon = findViewById(R.id.LinearMenuIcon);

        // Inflar o menu de estudante ou de responsavel
        int menu = R.layout.menu_student;
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        if(preferences_manipulator.getString(getApplicationContext(), "type", "").equals("responsible"))
            menu = R.layout.menu_responsible;
        assert inflater != null;
        View childLayout = inflater.inflate(menu, (ViewGroup) findViewById(R.id.LinearListIcon));
        menuIcon.addView(childLayout);

        // Mudar a imagem da Activity atual
        ImageView currentIconActivity = findViewById(R.id.iconRide);currentIconActivity.setOnClickListener(null);
        currentIconActivity.setImageDrawable(getResources().getDrawable(R.drawable.car_o));
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
