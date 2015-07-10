package com.example.meganwang.translate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import com.memetix.mst.detect.Detect;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

public class MainActivity extends Activity implements OnClickListener{

    Language[] languages = Language.values();

    Spinner EnterLan, TransLan;
    ImageView send;
    TextView languageEntered, textEntered, languageTranslated, textTranslated;
    EditText userText;
    ProgressBar loading;
    ImageButton mic;
    String lan[];
    private static final int VR_REQUEST = 999;

    protected void onCreate(Bundle icicle){
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);
        initViews();
        Locale loc = new Locale("en");
        Log.i("-------------",Arrays.toString(loc.getAvailableLocales()));
        check();
    }

    public void initViews(){
        //work with spinners
        EnterLan = (Spinner) findViewById(R.id.sTobTranslated);
        TransLan = (Spinner) findViewById(R.id.sTranslateto);

        EnterLan.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, GetAllValues()));

        TransLan.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, GetAllValues()));
        TransLan.setSelection(13);

        send = (ImageView) findViewById(R.id.ivSend);
        languageEntered = (TextView) findViewById(R.id.tvLanguageEntered);
        textEntered = (TextView) findViewById(R.id.tvTextEntered);
        languageTranslated = (TextView) findViewById(R.id.tvLanguageTranslated);
        textTranslated = (TextView) findViewById(R.id.tvTextTranslated);

        userText = (EditText) findViewById(R.id.etEnteredText);
        languageEntered.setVisibility(TextView.INVISIBLE);
        languageTranslated.setVisibility(TextView.INVISIBLE);
        textEntered.setVisibility(TextView.INVISIBLE);
        textTranslated.setVisibility(TextView.INVISIBLE);

        loading = (ProgressBar) findViewById(R.id.pbLoading);
        loading.setVisibility(ProgressBar.INVISIBLE);
        ((View) findViewById(R.id.view1)).setVisibility(View.INVISIBLE);
        ((View) findViewById(R.id.view2)).setVisibility(View.INVISIBLE);

        send.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                class bgStuff extends AsyncTask<Void, Void, Void>{

                    String translatedText = "";

                    @Override
                    protected void onPreExecute() {
                        // TODO Auto-generated method stub
                        loading.setVisibility(ProgressBar.VISIBLE);
                        super.onPreExecute();
                    }

                    @Override
                    protected Void doInBackground(Void... params) {
                        // TODO Auto-generated method stub
                        try {
                            translatedText = translateText();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            translatedText = "Unable to translate";
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        // TODO Auto-generated method stub

                        languageEntered.setVisibility(TextView.VISIBLE);
                        languageTranslated.setVisibility(TextView.VISIBLE);
                        textEntered.setVisibility(TextView.VISIBLE);
                        textTranslated.setVisibility(TextView.VISIBLE);

                        ((View) findViewById(R.id.view1)).setVisibility(View.VISIBLE);
                        ((View) findViewById(R.id.view2)).setVisibility(View.VISIBLE);

                        textEntered.setText(userText.getText().toString());
                        textTranslated.setText(translatedText);

                        languageEntered.setText(detectedLanguage);
                        languageTranslated.setText(languages[TransLan.getSelectedItemPosition()].name());
                        loading.setVisibility(ProgressBar.INVISIBLE);
                        super.onPostExecute(result);
                    }

                }

                new bgStuff().execute();
            }
        });
    }

    String detectedLanguage = "";

    public String translateText() throws Exception{
        // Set the Client ID / Client Secret once per JVM. It is set statically and applies to all services
        Translate.setClientId("mgnTranslatr");
        Translate.setClientSecret("7gr8aXmc9hT7THYrszEeQK4qQ6pevQBhJBA/yzsS4Jw=");

        String translatedText = Translate.execute(userText.getText().toString(),languages[EnterLan.getSelectedItemPosition()], languages[TransLan.getSelectedItemPosition()]);

        Language detectedLanguage = Detect.execute(userText.getText().toString());
        this.detectedLanguage = detectedLanguage.getName(Language.ENGLISH);

        return translatedText;
    }
    public String[] GetAllValues() {
        lan = new String[languages.length];
        for (int i = 0; i < languages.length; i++) {
            lan[i] = languages[i].name();
        }
        return lan;
    }
    public void check(){
        mic = (ImageButton)findViewById(R.id.mic);
        //find out whether speech recognition is supported
        PackageManager packManager = getPackageManager();
        List<ResolveInfo> intActivities = packManager.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (intActivities.size() != 0){
            //speech recognition is supported - detect user button clicks
            mic.setOnClickListener(this);
            //prepare the TTS to repeat chosen words
        }
        else{
            //speech recognition not supported, disable button and output message
            mic.setEnabled(false);
            Toast.makeText(this, "Speech recognition not supported.", Toast.LENGTH_LONG).show();
        }
    }
    public void onClick(View v){
        if(v.getId()==R.id.mic){
            //listen for results
            listenToSpeech();
        }
    }
    private void listenToSpeech(){
        //start speech recognition intent passing required data
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //indicate package
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        //message to display while listening
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now");
        //set speech model
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //start listening
        startActivityForResult(i, VR_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //check speech recognition result
        if (requestCode == VR_REQUEST && resultCode == RESULT_OK) {
            //store the returned word list as an ArrayList
            ArrayList<String> suggestedWords = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            userText.setText(suggestedWords.get(0));
        }
        //call superclass method
        super.onActivityResult(requestCode, resultCode, data);
    }
}
