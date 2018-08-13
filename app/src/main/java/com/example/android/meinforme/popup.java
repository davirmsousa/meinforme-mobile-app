package com.example.android.meinforme;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.meinforme.useful_classes.preferences_manipulator;

/**
 * Created by Home on 16/01/2018.
 */

public class popup extends Activity {

    int textNumber, repeated = 0;
    Button continueButton;
    TextView messageView;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup);

        /*
        * ta tendo problema de design no popup de carona do responsavel
        * veja se vai dividir em dois ou como vai ser
        */

        // Verifica se existe algum putExtra
        Bundle extras = getIntent().getExtras();
        if(extras != null && extras.getInt("messageNum", 0) != 0){

            continueButton = findViewById(R.id.buttonPopupContinue);
            messageView = findViewById(R.id.messageBody);
            textNumber = extras.getInt("messageNum");

            setTextMessage(getTextMessage(textNumber, repeated));
            if(textNumber == 1 || (textNumber == 2 && !preferences_manipulator.getString(getApplicationContext(), "type", "").equals("student")))
                continueButton.setOnClickListener(btnNextListener);
            else
                continueButton.setOnClickListener(btnFinishListener);

            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);

            int width = dm.widthPixels;

            getWindow().setLayout((int) (width * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);

        }else{
            finish();
        }

    }

    // Funcao para mudar o texto do popup
    private void setTextMessage(String text){
        messageView.setText(text);
    }

    // Funcao para retornar a string a ser mostrada com base no putextra
    private String getTextMessage(int id, int repeated){
        String toReturn = getResources().getString(R.string.somethigis_wrong_popup);
        switch (id){
            case 1:
                switch (repeated){
                    case 0: toReturn = getResources().getString(R.string.popup_text_1_1); break;
                    case 1: toReturn = getResources().getString(R.string.popup_text_1_2); break;
                    default: finish(); break;
                }
                break;
            case 2:
                if(preferences_manipulator.getString(getApplicationContext(), "type", "").equals("student"))
                    toReturn = getResources().getString(R.string.popup_text_2_s);
                else{
                    switch (repeated){
                        case 0: toReturn = getResources().getString(R.string.popup_text_2_r_1); break;
                        case 1: toReturn = getResources().getString(R.string.popup_text_2_r_2); break;
                        case 2: toReturn = getResources().getString(R.string.popup_text_2_r_3); break;
                        case 3: toReturn = getResources().getString(R.string.popup_text_2_r_4); break;
                        default: finish(); break;
                    }
                }
                break;
            case 3: toReturn = getResources().getString(R.string.popup_text_3); break;
            case 4: toReturn = getResources().getString(R.string.popup_text_4); break;
            default: finish(); break;
        }
        return toReturn;
    }

    // Funcao do botao para fechar
    private View.OnClickListener btnFinishListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    // Funcao do botao para fechar mudar o texto
    private View.OnClickListener btnNextListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if((textNumber == 1 && repeated == 0) || (textNumber == 2 && repeated == 2)){
                continueButton.setOnClickListener(btnFinishListener);
            }
            repeated++;
            setTextMessage(getTextMessage(textNumber, repeated));
        }
    };
}
