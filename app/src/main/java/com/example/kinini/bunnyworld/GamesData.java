package com.example.kinini.bunnyworld;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

// SQLiteOpenHelper notes from
// https://www.concretepage.com/android/android-sqlite-database-tutorial-with-oncreate-onupgrade-getwritabledatabase-of-sqliteopenhelper-execsql-compilestatement-rawquery-of-sqlitedatabase-example
public class GamesData extends SQLiteOpenHelper {
    private static GamesData gameDataInstance = null;
    private static final String DB_NAME = "BunnyWorldDB";
    private static final int DB_VERSION = 1;
    private static final String TAG = "GamesData";

    private GamesData(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static GamesData getInstance(Context context) {
        if (gameDataInstance == null) {
            gameDataInstance = new GamesData(context);
        }
        return gameDataInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "Creating DB ");
        createGamesTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    // Returns the list of all persisted games in the database.
    public List<Game> fetchGamesFromDB() {
        Log.i(TAG, "Getting list of games...");
        List<Game> games = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT serialized_game FROM games", null);
        while (cursor.moveToNext()) {
            Game game = deserializeGame(cursor.getBlob(0));
            Log.i(TAG, "" + game);
            if (game != null) {
                games.add(game);
            }
        }
        Log.i(TAG, "Returning the following games: ");
        for (Game g : games) {
            Log.i(TAG, "" + g);
            Log.i(TAG, g.getName());
            Log.i(TAG, "==============");
        }
        return games;
    }

    // Returns a game if found or `null` otherwise.
    public Game fetchGameFromDB(String name) {
        Log.i(TAG, "Getting Game Object from DB: " + name + "...");
        String sql = "SELECT serialized_game FROM games WHERE name = '" + name + "' LIMIT 1;";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) return deserializeGame(cursor.getBlob(0));
        return null;
    }

    // Adds a game to the DB.
    public void saveGameToDB(Game game) {
        byte[] bytes = serializeGame(game);
        Log.i(TAG, "Adding Game Object to DB: " + game.getName() + "...");
        String sql = "REPLACE INTO games VALUES (?, ?);";
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement statement = db.compileStatement(sql);
        statement.bindString(1, game.getName());
        statement.bindBlob(2, bytes);
        statement.executeInsert();
        Log.i(TAG, "Added Game to Database!");
    }

    // Updates a game sharing the same name as `updatedGame`.
    public void updateGameInDB(Game updatedGame) {
        Log.i(TAG, "Updating Game Object in DB: " + updatedGame.getName() + "...");
        String sql = "UPDATE games SET serialized_game = ? WHERE name = ?;";
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement statement = db.compileStatement(sql);
        statement.bindBlob(1, serializeGame(updatedGame));
        statement.bindString(2, updatedGame.getName());
    }

    // Resets the Games DB.
    public void resetDB() {
        Log.i(TAG, "Resetting DB");
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS games;");
        createGamesTable(db);
    }

    ///// Utility Methods /////
    private void createGamesTable(SQLiteDatabase db) {
        Log.i(TAG, "Creating Games Table");
        String setupStr = "CREATE TABLE games ("
                + "name TEXT PRIMARY KEY, serialized_game BLOB"
                + ");";
        db.execSQL(setupStr);
    }

    // Converts a serialized byte array to a Game object.
    private Game deserializeGame(byte[] bytes) {
        Log.i(TAG, "Deserializing Game...");
        Game game = null;
        try {
            ObjectInputStream objectIn = new ObjectInputStream(new ByteArrayInputStream(bytes));
            game = (Game) objectIn.readObject();
            Log.i(TAG, "Deserialized Game: " + game.getName());
        } catch (InvalidClassException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return game;
    }

    // Converts a game object to a serialized byte array.
    private byte[] serializeGame(Game game) {
        Log.i(TAG, "Serializing Game: " + game.getName());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out;
        byte[] bytes = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(game);
            out.close();
            bytes = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }
}