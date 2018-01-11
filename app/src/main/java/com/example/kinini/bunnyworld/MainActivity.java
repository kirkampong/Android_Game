package com.example.kinini.bunnyworld;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    GamesData gamesData;
    boolean debug = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (debug) debugGamesData(); // TODO(oluwasanya): Remove once sure everything works.
    }

    public void onClickGo(View view) {
        Intent intent = new Intent(this, EditorActivity.class);
        startActivity(intent);
    }

    public void onClickLoad(View view) {
        Intent intent = new Intent(this, LoadActivity.class);
        startActivity(intent);
    }

    public void onClickLoadSerialized(View view) {
        Intent intent = new Intent(this, LoadSerializedActivity.class);
        startActivity(intent);
    }

    public void playDefaultGame(View view) {
        Intent intent = new Intent(this, PlayDefaultGameActivity.class);
        startActivity(intent);
    }

    // Used for debugging game data. Also shows how to make the different calls.
    private void debugGamesData() {
        System.out.println("Running Tests...");
        // Creating Games DB.
        gamesData = GamesData.getInstance(getApplicationContext());

        // Set DB to clean state.
        gamesData.resetDB();

        // Saving to DB.
        for (int i = 0; i < 3; i++) {
            gamesData.saveGameToDB(new Game("test" + i));
        }

        // Check to see all games are there.
        List<Game> games = gamesData.fetchGamesFromDB();
        assert (games.size() == 3);

        // Updating to DB.
        Game updatedGame = new Game("test0"); // test0 already exists in the DB.
        Page page = updatedGame.getCurrentPage();
        String newName = page.getName() + "_new";
        updatedGame.renamePage(page.getName(), newName);
        gamesData.updateGameInDB(updatedGame);

        // Check to make sure game is updated.
        Game game = gamesData.fetchGameFromDB("test0");
        assert (game != null && game.getCurrentPage().getName() == newName);

        System.out.println("Done Running Tests...");
    }
}