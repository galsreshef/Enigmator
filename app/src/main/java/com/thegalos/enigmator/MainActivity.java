package com.thegalos.enigmator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private EditText etText;
    private TextView tvResult;
    private TextView tvConverted;
    private int toMorse = 0;
    private TextToSpeech tts;
    private MediaPlayer mediaPlayer;
    private ArrayList<Integer> playlist;
    private int fileCounter = 0;
    private Button btnCopy;
    private Button btnPlay;

    private InterstitialAd mInterstitialAd;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ad Mob
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6870681415333406/6197864268");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });


        // Ad Mob
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        etText = findViewById(R.id.txt);
        tvResult = findViewById(R.id.tvResult);
        btnCopy = findViewById(R.id.btnClipboard);
        btnPlay = findViewById(R.id.btnPlay);
        RadioGroup rGroup = findViewById(R.id.rGroup);
        tvConverted = findViewById(R.id.tvConverted);



        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbToMorse) {
                    toMorse = 1;
                    liveConvert();
                    etText.setEnabled(true);
                }else if (checkedId == R.id.rbToText) {
                    toMorse = 2;
                    liveConvert();
                    etText.setEnabled(true);
                }else
                    toMorse = 0;
            }
        });

        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInterstitialAd.show();

                String txt = tvResult.getText().toString();
                if (toMorse == 1)
                    txt = txt.substring(0, txt.length() - 3);
                else
                    txt = txt.substring(0, txt.length() - 1);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(txt, txt);
                Toast.makeText(MainActivity.this, "Result copied", Toast.LENGTH_SHORT).show();
                assert clipboard != null;
                clipboard.setPrimaryClip(clip);
            }
        });

        etText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                liveConvert();
            }
        });

        tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.ENGLISH);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(getApplicationContext(), "Language not supported", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Initialization failed", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mInterstitialAd.show();


                String toTTS = tvConverted.getText().toString();
                String result = tvResult.getText().toString();
                tts.setPitch(0.9f);
                tts.setSpeechRate(0.8F);
                if (toMorse == 2){
                    toTTS = toTTS+ " " + result;
                }else btnPlay.setEnabled(false);

                    tts.speak(toTTS , TextToSpeech.QUEUE_FLUSH, null, null);

                playlist = new ArrayList<>();
                if (toMorse == 1) {
                    for (char a : result.toCharArray()) {
                        if (a == '-') {
                            playlist.add(R.raw.long_bip);
                            playlist.add(R.raw.long_zero);
                        } else if (a == '.') {
                            playlist.add(R.raw.short_bip);
                            playlist.add(R.raw.short_zero);
                        } else {
                            playlist.add(R.raw.short_zero);
                        }
                    }
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer = MediaPlayer.create(MainActivity.this, playlist.get(0));
                            mediaPlayer.setOnCompletionListener(MainActivity.this);
                            mediaPlayer.start();
                        }
                    }, 1800);

                }
            }
        });
    }

    private void liveConvert() {
        if (toMorse != 0) {
            String str = "";
            if (toMorse == 1) {
                tvResult.setText(Morse.alphaToMorse(etText.getText().toString()));
                str = "Text converted to:";
            } else if (toMorse == 2) {
                tvResult.setText(Morse.morseToAlpha(etText.getText().toString()));
                str = "Morse converted to:";
            }
            if (etText.getText().toString().equals("")) {
                btnPlay.setEnabled(false);
                btnCopy.setEnabled(false);
                tvConverted.setText("");
                tvResult.setText("");
            }else {
                tvConverted.setText(str);
                btnPlay.setEnabled(true);
                btnCopy.setEnabled(true);
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (playlist.size()-1>fileCounter)
            play();
        else {
            mp.release();
            btnPlay.setEnabled(true);
            fileCounter = 0;
        }
    }

    private void play() {
        fileCounter += 1;
        AssetFileDescriptor afd = this.getResources().openRawResourceFd(playlist.get(fileCounter));
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
            afd.close();
        }
        catch (IllegalArgumentException | IOException | IllegalStateException ignored) { }
    }
}