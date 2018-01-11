package com.example.kinini.bunnyworld;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

public class LoadSerializedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_serialized);
        EditText editText = findViewById(R.id.serialized_game);
        validationListener(editText);
    }

    public void onCreateSerializedGame(View view) {
        EditText editText = findViewById(R.id.serialized_game);
        String text = editText.getText().toString();
        Gson gson = new Gson();
        Game game = gson.fromJson(text, Game.class);

        Intent intent = new Intent(getBaseContext(), GameActivity.class);
        intent.putExtra(LoadActivity.IS_LOADED_GAME, true);
        intent.putExtra(EditorActivity.NAME_OF_GAME, game.getName());
        intent.putExtra(LoadActivity.GAME_OBJECT, game);

        Log.i("SerializedGame", "Passing on Game");
        Log.i("SerializedGame", "Num Shapes: " + game.getCurrentPage().getAllShapes().size());
        startActivity(intent);
    }

    public void validationListener(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Button btn = findViewById(R.id.create_serialized_game);
                String text = editText.getText().toString();
                btn.setEnabled(!TextUtils.isEmpty(text));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Button btn = findViewById(R.id.create_serialized_game);
                try {
                    String text = editText.getText().toString();
                    (new Gson()).fromJson(text, Game.class);
                    editText.setError(null);
                    btn.setEnabled(true);
                } catch (Exception e) {
                    editText.setError("Cannot parse game");
                    btn.setEnabled(false);
                }
            }
        });
    }
}
