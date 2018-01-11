package com.example.kinini.bunnyworld;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class LoadActivity extends AppCompatActivity {
    ListView listView;
    public static final String IS_LOADED_GAME = "IS_LOADED_GAME";
    public static final String GAME_OBJECT = "GAME_OBJECT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        List<Game> games = GamesData.getInstance(getApplicationContext()).fetchGamesFromDB();
        final String[] gameNames = new String[games.size()];
        final Game[] gamesArray = new Game[games.size()];
        for (int i = 0; i < games.size(); i++) {
            Game game = games.get(i);
            gameNames[i] = game.getName();
            gamesArray[i] = game;
        }

        listView = findViewById(R.id.gamesList);
        listView.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, gameNames));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String gameName = (String) listView.getItemAtPosition(position);
                System.out.println("Picked Game Name: " + gameName);

                // Go to the activity to play the game. Refactor to make it GameActivity.
                Intent intent = new Intent(getBaseContext(), GameActivity.class);

                intent.putExtra(IS_LOADED_GAME, true);
                intent.putExtra(EditorActivity.NAME_OF_GAME, gameNames[position]);
                intent.putExtra(GAME_OBJECT, gamesArray[position]);

                System.out.println("Passing on Game");
                System.out.println("Num Shapes: " + gamesArray[position].getCurrentPage().getAllShapes().size());
                startActivity(intent);
            }
        });
    }
}
