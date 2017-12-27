package com.osevsay.voicecommandapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;


public class MainActivity extends Activity implements View.OnClickListener, RecognitionListener {

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    public static final String myLanguage = "tr-TR"; // Locale.forLanguageTag("tr-TR").toString();
    private static final int REQUEST_AUDIO = 862;

    public ListView mList;
    public Button speakButton;
    public Button googleSpeakButton;
    private TextView helloTV;
    private LinearLayout rootLayout;

    public boolean isSpeechRecognizerActive = false;
    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MultiDex.install(this);

        rootLayout = (LinearLayout) findViewById(R.id.rootLayout);
        helloTV = (TextView) findViewById(R.id.helloTV);
        mList = (ListView) findViewById(R.id.listview);
        googleSpeakButton = (Button) findViewById(R.id.butonGoogle);
        speakButton = (Button) findViewById(R.id.buton);
        googleSpeakButton.setOnClickListener(this);
        speakButton.setOnClickListener(this);



    }

    public void onClick(View v) {


        if (googleSpeakButton.getId() == v.getId() || speakButton.getId() == v.getId()) {
            checkSpeechRecognizerPermissionGranted(v.getId());
        }

    }

    public void startVoiceRecognitionActivity() {

        if (isSpeechRecognizerActive) {
            if (speechRecognizer != null) {
                speechRecognizer.stopListening();
            }
        }
        else {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, myLanguage);
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it
            // could have heard
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mList.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, matches));
            // matches is the result of voice input. It is a list of what the
            // user possibly said.
            // Using an if statement for the keyword you want to use allows the
            // use of any activity if keywords match
            // it is possible to set up multiple keywords to use the same
            // activity so more than one word will allow the user
            // to use the activity (makes it so the user doesn't have to
            // memorize words from a list)
            // to use an activity from the voice input information simply use
            // the following format;
            // if (matches.contains("keyword here") { startActivity(new
            // Intent("name.of.manifest.ACTIVITY")

            if (matches.contains("information")) {

            }
        }
    }


    public void checkSpeechRecognizerPermissionGranted(int id) {
        int permissionStatus = getPermissionStatus(this, Manifest.permission.RECORD_AUDIO);
        switch (permissionStatus) {
            case GRANTED:
                if (googleSpeakButton.getId() == id) {
                    startVoiceRecognitionActivity();
                }
                else if (speakButton.getId() == id) {
                    startSpeechRecognizer();
                }
                break;
            case DENIED:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO);
                }
                break;
            case BLOCKED_OR_NEVER_ASKED:

                Snackbar snackbar = Snackbar.make(rootLayout, getResources().getString(R.string.message_no_storage_permission_snackbar), Snackbar.LENGTH_LONG);
                snackbar.setAction(getResources().getString(R.string.settings), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        MainActivity.this.startActivity(intent);
                    }
                });
                snackbar.show();

                break;
        }
    }

    private void startSpeechRecognizer() {
        if (isSpeechRecognizerActive) {
            if (speechRecognizer != null) {
                speechRecognizer.stopListening();
            }
        } else {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(this);
            Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, myLanguage);
            speechRecognizer.startListening(speechIntent);
        }
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        isSpeechRecognizerActive = true;
        speakButton.setText("Aramayı Durdur");
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {

        isSpeechRecognizerActive = false;
        speakButton.setText("Bas Konuş");
    }

    @Override
    public void onResults(Bundle results) {

        ArrayList matches  = (ArrayList) results.get(SpeechRecognizer.RESULTS_RECOGNITION);
        mList.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, matches));

        Log.e("onResults", "onResults triggered");

        helloTV.setText(matches.get(0).toString());

        isSpeechRecognizerActive = false;
        speakButton.setText("Bas Konuş");
    }


    @Override
    public void onPartialResults(Bundle partialResults) {

        ArrayList<String> result = partialResults.getStringArrayList("results_recognition");

        ArrayList<String> unstableText = partialResults.getStringArrayList("android.speech.extra.UNSTABLE_TEXT");

        String lang = partialResults.getString("results_language");

        if (result != null && unstableText != null) {
//            Log.e("results", result.get(0));
//            Log.e("unstableText", unstableText.get(0));

            String allText = result.get(0) + unstableText.get(0);
            helloTV.setText(allText);
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({GRANTED, DENIED, BLOCKED_OR_NEVER_ASKED })
    public @interface PermissionStatus {}

    public static final int GRANTED = 0;
    public static final int DENIED = 1;
    public static final int BLOCKED_OR_NEVER_ASKED = 2;

    @PermissionStatus
    public static int getPermissionStatus(Activity activity, String androidPermissionName) {
        if(ContextCompat.checkSelfPermission(activity, androidPermissionName) != PackageManager.PERMISSION_GRANTED) {
            if(!ActivityCompat.shouldShowRequestPermissionRationale(activity, androidPermissionName)){
                return BLOCKED_OR_NEVER_ASKED;
            }
            return DENIED;
        }
        return GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_AUDIO:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startSpeechRecognizer();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

}
