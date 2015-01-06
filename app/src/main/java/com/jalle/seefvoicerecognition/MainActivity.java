package com.jalle.seefvoicerecognition;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;

	private EditText metTextHint;
	private ListView mlvTextMatches;
	private Spinner msTextMatches;
	private Button mbtSpeak;
    EditText txtFolder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(com.jalle.seefvoicerecognition.R.layout.activity_main);
		metTextHint = (EditText) findViewById(com.jalle.seefvoicerecognition.R.id.txtFolder);
		mlvTextMatches = (ListView) findViewById(com.jalle.seefvoicerecognition.R.id.lvTextMatches);
		msTextMatches = (Spinner) findViewById(com.jalle.seefvoicerecognition.R.id.sNoOfMatches);
		mbtSpeak = (Button) findViewById(com.jalle.seefvoicerecognition.R.id.btSpeak);

        txtFolder = (EditText) findViewById(com.jalle.seefvoicerecognition.R.id.txtFolder);
        txtFolder.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
   // This always works
                Intent i = new Intent(getApplicationContext(), FilePickerActivity.class);
// This works if you defined the intent filter
// Intent i = new Intent(Intent.ACTION_GET_CONTENT);
// Set these depending on your use case. These are the defaults.
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

                startActivityForResult(i,FilePickerActivity.MODE_DIR);

            }
        });
	}
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            if (requestCode == VOICE_RECOGNITION_REQUEST_CODE)
            {
                //If Voice recognition is successful then it returns RESULT_OK
                if(resultCode == RESULT_OK) {

                    ArrayList<String> textMatchList = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    if (!textMatchList.isEmpty()) {
                        // If first Match contains the 'search' word
                        // Then start web search.
                        if (textMatchList.get(0).contains("search")) {

                            String searchQuery = textMatchList.get(0).replace("search", 	" ");
                            Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
                            search.putExtra(SearchManager.QUERY, searchQuery);
                            startActivity(search);
                        } else {
                            // populate the Matches
                            mlvTextMatches
                                    .setAdapter(new ArrayAdapter<String>(this,
                                            android.R.layout.simple_list_item_1,
                                            textMatchList));
                        }

                    }
                    //Result code for various error.
                }else if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
                    showToastMessage("Audio Error");
                }else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR){
                    showToastMessage("Client Error");
                }else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
                    showToastMessage("Network Error");
                }else if(resultCode == RecognizerIntent.RESULT_NO_MATCH){
                    showToastMessage("No Match");
                }else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
                    showToastMessage("Server Error");
                }
            super.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode == FilePickerActivity.MODE_DIR && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // For JellyBean and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip = data.getClipData();

                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            Uri uri = clip.getItemAt(i).getUri();
                            // Do something with the URI
                        }
                    }
                    // For Ice Cream Sandwich
                } else {
                    ArrayList<String> paths = data.getStringArrayListExtra
                            (FilePickerActivity.EXTRA_PATHS);

                    if (paths != null) {
                        for (String path: paths) {
                            Uri uri = Uri.parse(path);

                            txtFolder.setText(path);

                            // Do something with the URI
                        }
                    }
                }

            } else {
                Uri uri = data.getData();
                // Do something with the URI
                txtFolder.setText(uri.toString());
            }
        }
    }
	public void checkVoiceRecognition() {
		// Check if voice recognition is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0) {
			mbtSpeak.setEnabled(false);
			Toast.makeText(this, "Voice recognizer not present",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void speak(View view) {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		// Specify the calling package to identify your application
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
				.getPackage().getName());

		// Display an hint to the user about what he should say.
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, metTextHint.getText()
				.toString());

		// Given an hint to the recognizer about what the user is going to say
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

		// If number of Matches is not selected then return show toast message
		if (msTextMatches.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
			Toast.makeText(this, "Please select No. of Matches from spinner",
					Toast.LENGTH_SHORT).show();
			return;
		}

		int noOfMatches = Integer.parseInt(msTextMatches.getSelectedItem()
				.toString());
		// Specify how many results you want to receive. The results will be
		// sorted where the first result is the one with higher confidence.

		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, noOfMatches);

		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	void showToastMessage(String message){
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}
}
