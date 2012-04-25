package org.selu.nixvalis;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class Login extends Activity {
    public static final String PREFS_NAME = "NixvalisPrefs";

    public Login() {
	// TODO Auto-generated constructor stub
    }

    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.login);
	// Restaurar las preferencias
	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	boolean recordar = settings.getBoolean("recordar", false);

	if (recordar) {
		CheckBox salvar = (CheckBox) findViewById(R.id.CheckBox01);
	    EditText user = (EditText) findViewById(R.id.EditText01);
	    EditText pass = (EditText) findViewById(R.id.EditText02);
	    user.setText(settings.getString("usuario", ""));
	    pass.setText(settings.getString("password", ""));
	    salvar.setChecked(settings.getBoolean("recordar", false));
	}

	Button ok = (Button) findViewById(R.id.Button01);

	ok.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent nixvalis = new Intent(getApplicationContext(),
			RSSReader.class);

		EditText user = (EditText) findViewById(R.id.EditText01);
		EditText pass = (EditText) findViewById(R.id.EditText02);

		Bundle bundle = new Bundle();
		bundle.putString("user", user.getText().toString());
		bundle.putString("pass", pass.getText().toString());

		nixvalis.putExtras(bundle);

		startActivity(nixvalis);
	    }
	});

    }

    public void onStop() {
	super.onStop();

	CheckBox salvar = (CheckBox) findViewById(R.id.CheckBox01);

	boolean salva = salvar.isChecked();

	if (salva) {
	    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();

	    editor.putBoolean("recordar", salva);

	    EditText user = (EditText) findViewById(R.id.EditText01);
	    EditText pass = (EditText) findViewById(R.id.EditText02);
	    editor.putString("usuario", user.getText().toString());
	    editor.putString("password", pass.getText().toString());

	    editor.commit();
	}
    }
}
