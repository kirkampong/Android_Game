package com.example.kinini.bunnyworld;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by kenneth on 11/19/17.
 */

public class EditorActivity extends AppCompatActivity {
    public static final String NAME_OF_GAME = "NAME_OF_GAME";

    public EditorActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        initEditor();
    }

    void initEditor() {
        gameNameListener((EditText) findViewById(R.id.nameOfGame));
    }

    public void onClickSubmit(View view) {
        EditText editText = findViewById(R.id.nameOfGame);
        String name = editText.getText().toString();
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(NAME_OF_GAME, name);
        startActivity(intent);
    }

    private void gameNameListener(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Button updateShapeButton = findViewById(R.id.submitGame);
                String errorMessage = charSequence.toString().isEmpty() ? "Please provide game name" : null;
                editText.setError(errorMessage);
                updateShapeButton.setEnabled(editText.getError() == null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
}
