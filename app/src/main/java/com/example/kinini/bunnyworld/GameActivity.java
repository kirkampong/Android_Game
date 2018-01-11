package com.example.kinini.bunnyworld;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import java.io.*;
import java.util.*;

/*@meeting. There will be another game activity for the actual game, in which the pageview actually occupies the whole page
* @meeting  add cheatcodes... where the player can udo their action
* */
public class GameActivity extends AppCompatActivity {
    private static Game currentGame;
    //NB.... Clear this when the user creates another game since it is static and you don't want the old game on the new one!!
    private static Stack<Game> prevGames = new Stack<>();
    private String TAG = "GameActivity";
    MediaPlayer mediaPlayer;
    int[] songIds = {R.raw.rejected, R.raw.according, R.raw.average, R.raw.background, R.raw.floodash};
    String[] songNames = {"Wintergatan", "Beautiful Eulogy", "DJ Average Joe", "Vanilla", "Alert312"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        initGame();
        playMusic();
    }

    public void onChangePageName(View view){
        String oldName = currentGame.getCurrentPage().getName();
        if (oldName.equals("page1")) {
            Toast.makeText(getApplicationContext(), "Cannot edit Page 1!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        EditText pageName = findViewById(R.id.newPageName);
        String newName = pageName.getText().toString();
        newName = newName.trim();
        System.out.println("the text is: "+newName);
        if (newName.isEmpty()){
            Toast.makeText(getApplicationContext(), "Emptiness ain't a Bunny trait!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
       // System.out.println("New name is")
        //need to update the  page and the scripts
        currentGame.getCurrentPage().setName(newName);
        if (currentGame == null) return;
        ArrayList<Page>allPages = currentGame.getAllPages();
        if (allPages == null) return;
        for (Page pg : allPages){
            for (Shape sp : pg.getAllShapes() ){
                sp.updateShapeScript(oldName, newName);
            }
        }
        ArrayList <Shape> shapeInPageView = Singleton.getInstance().getCurrentPageView().getShapesinPage();
        for  (Shape sp  : shapeInPageView){
            sp.updateShapeScript(oldName, newName);
        }

        displayPageName();
    }

    //we create a game. wait for user to create pages
    public void initGame() {
        Intent intent = getIntent();
        String nameOfGame = intent.getStringExtra(EditorActivity.NAME_OF_GAME);
        boolean isLoadedGame = intent.getBooleanExtra(LoadActivity.IS_LOADED_GAME, false);

        Singleton.getInstance().deleteGame(nameOfGame.trim());

        Singleton st = Singleton.getInstance();
        st.deleteGame(nameOfGame.trim());
        st.setContext(getApplicationContext());

        int gameMode = (isLoadedGame) ? Game.GAME_MODE : Game.EDITOR_MODE;
        st.setState(gameMode);


        if (isLoadedGame) {
            currentGame = (Game) intent.getSerializableExtra(LoadActivity.GAME_OBJECT);
            currentGame.changePage("page1");
            currentGame.setGameMode(Game.GAME_MODE);

            //always remember to call this after creating a game object so as to refresh the display
            currentGame.onRefresh();

            ScrollView view = findViewById(R.id.sideBar);
            view.setVisibility(View.GONE);
        } else {
            currentGame = new Game(nameOfGame);
            currentGame.setGameMode(Game.EDITOR_MODE);
            //always remember to call this after creating a game object so as to refresh the display
            currentGame.onRefresh();

            EditText pageName = findViewById(R.id.editPage);
            pageName.setText(currentGame.getCurrentPage().getName());
            System.out.println("Setting to " + currentGame.getCurrentPage().getName());

            Button editScript = findViewById(R.id.edit_script);
            registerForContextMenu(editScript);

            //set the place to enter the name of the game to the next page number
            EditText editText = findViewById(R.id.editPage);
            editText.setEnabled(false);
        }
    }

    // stops playing the music once you exit the editor activity.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    // begins to play music the moment you start a game (currently in either editor or game mode).
    // from: https://stackoverflow.com/questions/37244357/how-to-play-music-in-android-studio
    private void playMusic() {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.background);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    public static Game getCurrentGame() {
        return currentGame;
    }

    public void onClickUndo(View view) {
        System.out.println("Actually onclicked!");
        if (!prevGames.isEmpty()) {
            currentGame = prevGames.pop();
            currentGame.onRefresh();
            System.out.println("Undoing!!!");
            displayPageName();
            clearEditShapeFields();
            //also need to update the views...
            // maybe not be a must, we can choose to leave them blank
            // TODO(oluwasanya): Need to update the update texts as well.
        }
    }

    protected static void stackGame() {
        Game gm = (Game) deepClone(currentGame);
        if (gm != null) {
            prevGames.push(gm);
        } else {
            System.out.println("NULL object returned by deepCLone");
        }
    }

    //displays nothing since there is no page in the beginning
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Singleton st = Singleton.getInstance();
        if (st.getState() == Game.GAME_MODE) return true;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.open_page:
                return onOpenPage();
            case R.id.new_page:
                return onNewPage();
            case R.id.delete_page:
                return onDeletePage();
            case R.id.new_shape:
                return onNewShape();
            case R.id.view_shape:
                return onViewShape();
            case R.id.delete_shape:
                return onDeleteShape();
            case R.id.copy_shape:
                return onCopyShape();
            case R.id.paste_shape:
                return onPasteShape();
            case R.id.save_game:
                return onSaveGame();
            case R.id.copy_game:
                return onCopyGame();
            default:
                return super.onContextItemSelected(item);
        }
    }

    //views all the shapes that are available on current page
    private boolean onViewShape() {
        ArrayList<Shape> shapesList = new ArrayList<Shape>();
        ArrayList<Page> pageList = currentGame.getAllPages();

        for (Page page : pageList) {
            shapesList.addAll(page.getAllShapes());
        }

        final String[] shapes = new String[shapesList.size()];
        for (int i = 0; i < shapesList.size(); i++) {
            shapes[i] = shapesList.get(i).getName();
        }
        //Log.i(TAG, "shape:" + shapesList.size());

        AlertDialog.Builder builderScript = new AlertDialog.Builder(this);
        builderScript.setTitle("All Shapes");
        //selects the shape
        builderScript.setItems(shapes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // currentGame.getCurrentPage().setSelected(shapesList.get(item));
            }
        });
        builderScript.create().show();
        return true;
    }

    // opens up dialog for loading a page
    private boolean onOpenPage() {
        ArrayList<Page> listPgs = currentGame.getAllPages();
        ArrayList<String> strPgs = new ArrayList<String>();

        for (Page page : listPgs) {
            strPgs.add(page.getName());
        }

        final String[] pgsArr = strPgs.toArray(new String[strPgs.size()]);
        AlertDialog.Builder builderScript = new AlertDialog.Builder(this);
        builderScript.setTitle("All Pages");
        builderScript.setItems(pgsArr, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                currentGame.changePage(pgsArr[item]);
                displayPageName();
            }
        });
        builderScript.create().show();
        return true;
    }

    // creates a new page
    private boolean onNewPage() {
        GameActivity.stackGame();
        currentGame.createPage();
        // PageView pg = Singleton.getInstance().getCurrentPageView();
        //pg.nullCurrentShape();//when we change page, null the current shape and update views
        //pg.setCurrentShapeInPage();
        displayPageName();
        return true;
    }

    // displays the page name on the view
    private void displayPageName() {
        EditText pageNameField = findViewById(R.id.editPage);
        String pageName = currentGame.getCurrentPage().getName();
        pageNameField.setText(pageName);
        // Disable EditText if it is page1.
        //boolean isPage1 = pageName.equals("page1");
        pageNameField.setEnabled(false);
    }

    // deletes the current page except page1
    private boolean onDeletePage() {
        GameActivity.stackGame();
        String pageName = currentGame.getCurrentPage().getName();
        if (pageName.equals("page1")) {
            Toast.makeText(getApplicationContext(), "Cannot delete page1",
                    Toast.LENGTH_SHORT).show();
        } else {
            currentGame.deletePage(pageName);
            displayPageName();
            clearEditShapeFields();
            Toast.makeText(getApplicationContext(), "Deleted " + pageName,
                    Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private boolean onNewShape() {
        GameActivity.stackGame();
        Shape newShape = new Shape(currentGame.getName());
        newShape.setMovable(true);
        currentGame.getCurrentPage().addShape(newShape);
        currentGame.getCurrentPage().setSelected(newShape);
        clearEditShapeFields();
        Toast.makeText(getApplicationContext(), "Created new shape " + newShape.getName(), Toast.LENGTH_SHORT).show();
        return true;
    }

    void clearEditShapeFields() {
        Log.i(TAG, "Clearing Fields");
        ((EditText) findViewById(R.id.xPos)).setText(null);
        ((EditText) findViewById(R.id.yPos)).setText(null);
        ((Switch) findViewById(R.id.movable)).setChecked(false);
        ((Switch) findViewById(R.id.visible)).setChecked(false);
        ((TextView) findViewById(R.id.imageName)).setText("");
        ((EditText) findViewById(R.id.shapeText)).setText(null);
    }

    private boolean onCopyShape() {
        Shape selectedShape = currentGame.getCurrentPage().getSelectedShape();
        if (selectedShape != null) {
            Singleton st = Singleton.getInstance();
            st.setCopiedShape(selectedShape);
            Toast.makeText(getApplicationContext(), "Copied " + selectedShape.getName(),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Nothing to copy",
                    Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private boolean onPasteShape() {
        Singleton st = Singleton.getInstance();
        Shape copiedShape = st.getCopiedShape();
        if (copiedShape != null) {
            // Right now, we do not permit duplicate shapes. This should be fixed, I think.
            Shape newShape = (Shape) bytesToObject(objectToBytes(copiedShape));
            newShape.setName(st.nextShapeId(currentGame.getName()));
            newShape.setX(newShape.getX() + 50);
            newShape.setY(newShape.getY() + 50);
            currentGame.getCurrentPage().addShape(newShape);
            Toast.makeText(getApplicationContext(), "Pasted shape as " + newShape.getName(),
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Nothing to paste",
                    Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private boolean onSaveGame() {
        GamesData gamesData = GamesData.getInstance(getApplicationContext());
        gamesData.saveGameToDB(currentGame);
        Toast.makeText(getApplicationContext(), "Saved game " + currentGame.getName(),
                Toast.LENGTH_SHORT).show();
        return true;
    }

    //deletes the current selected shape
    public boolean onDeleteShape() {
        Shape currentShape = currentGame.getCurrentPage().getSelectedShape();
        if (currentShape != null) {
            currentGame.getCurrentPage().removeShape(currentShape);
            Toast.makeText(getApplicationContext(), "Deleted shape " + currentShape.getName(),
                    Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    //copies the game to the clipboard in Singleton.
    public boolean onCopyGame() {
        Singleton st = Singleton.getInstance();
        st.setCopiedGame(currentGame.toJson());
        Toast.makeText(getApplicationContext(), "Copied game to clipboard", Toast.LENGTH_SHORT).show();
        return true;
    }

    public void onSetImageName(View view) {
        if (currentGame.getCurrentPage().getSelectedShape() == null) {
            Toast.makeText(getApplicationContext(), "Please select a shape.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] triggers = {"carrot", "carrot2", "door", "death", "duck", "fire", "mystic"};
        AlertDialog.Builder triggerBuilder = new AlertDialog.Builder(this);
        triggerBuilder.setTitle("Image Names");
        triggerBuilder.setItems(triggers, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                TextView imageNameField = findViewById(R.id.imageName);
                imageNameField.setText(triggers[item]);
                Shape selectedShape = currentGame.getCurrentPage().getSelectedShape();
                selectedShape.setImageName(triggers[item]);
            }
        });
        AlertDialog alert = triggerBuilder.create();
        alert.show();
    }

    public void onClearImageName(View view) {
        TextView imageNameField = findViewById(R.id.imageName);
        imageNameField.setText("No Image");
    }

    public void onEditScript(View view) {
        openContextMenu(view);
    }

    //creates context menu for editing shapes
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (currentGame.getCurrentPage().getSelectedShape() == null) {
            Toast.makeText(getApplicationContext(), "Please select a shape", Toast.LENGTH_SHORT).show();
            return true;
        }

        switch (item.getItemId()) {
            case R.id.create_script:
                return onCreateScript();
            case R.id.delete_script:
                return onDeleteScript();
            case R.id.show_script:
                return onShowScript();
            default:
                return super.onContextItemSelected(item);
        }
    }

    private boolean onCreateScript() {
        final String[] triggers = {"on click", "on enter", "on drop"}; //method to get list of shapescripts

        AlertDialog.Builder triggerBuilder = new AlertDialog.Builder(this);
        triggerBuilder.setTitle("Add Scripts");
        triggerBuilder.setItems(triggers, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (triggers[item]) {
                    case "on click":
                        createActionScripts("on click");
                        break;
                    case "on enter":
                        createActionScripts("on enter");
                        break;
                    case "on drop":
                        createListShapes("on drop");
                        break;
                    default:
                        break;
                }
            }
        });
        AlertDialog alert = triggerBuilder.create();
        alert.show();
        return true;
    }

    //this method allows for creating multiple action scripts
    private void createActionScripts(final String msg) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.script_dialog);
        dialog.setTitle("Add Action Script");
        dialog.show();
        addActionsToSpinner(dialog);
        addListenerToSpinner(dialog);

        Button submitBtn = dialog.findViewById(R.id.submitScript);
        Button addScriptBtn = dialog.findViewById(R.id.addScript);
        final Spinner actionList = dialog.findViewById(R.id.actionSpinner);
        final Spinner elemList = dialog.findViewById(R.id.elementSpinner);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveScript(msg, dialog);
                Toast.makeText(getApplicationContext(), "Shape script added.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        addScriptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionList.getSelectedItem() != null && elemList.getSelectedItem() != null) {
                    String action = actionList.getSelectedItem().toString();
                    String elem = elemList.getSelectedItem().toString();
                    TextView actionScript = dialog.findViewById(R.id.listActions);
                    actionScript.setText(actionScript.getText() + "\n" + action + " " + elem);

                    // saveScript(msg + " " + action + " " + elem);
                    // Toast.makeText(getApplicationContext(), "Script: " + action + " " + elem
                    //         + " added", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //saves the message to the script of the shape
    private void saveScript(String msg, Dialog dialog) {
        Shape selectedShape = currentGame.getCurrentPage().getSelectedShape();
        TextView actionScript = dialog.findViewById(R.id.listActions);
        String actionElems = actionScript.getText().toString();
        String[] actionElemList = actionElems.split(System.getProperty("line.separator"));
        String script = msg;
        for (String actionElem : actionElemList) {
            script += " " + actionElem;
        }
        boolean isAdded = selectedShape.setShapeScript(script);
        if (!isAdded) {
            Toast.makeText(getApplicationContext(), "Shape script" + msg + "already " +
                    "exists. Delete to add shape script.", Toast.LENGTH_SHORT).show();
        }
    }

    //deletes the scripts
    private boolean onDeleteScript() {
        //shows script to be deleted
        final Shape selectedShape = currentGame.getCurrentPage().getSelectedShape();
        List<String> scriptsList = selectedShape.getShapeScripts();
        final String[] scripts = (scriptsList.size() == 0) ? new String[]{}
                : scriptsList.toArray(new String[scriptsList.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Scripts");
        builder.setItems(scripts, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                selectedShape.removeScript(scripts[item]);
            }
        });
        builder.create().show();
        return true;
    }

    //This method shows the shape scripts that are available
    private boolean onShowScript() {
        final Shape selectedShape = currentGame.getCurrentPage().getSelectedShape();
        List<String> scriptsList = selectedShape.getShapeScripts();
        final String[] scripts = (scriptsList.size() == 0) ? new String[]{}
                : scriptsList.toArray(new String[scriptsList.size()]);

        AlertDialog.Builder builderScript = new AlertDialog.Builder(this);
        builderScript.setTitle("All Scripts");
        builderScript.setItems(scripts, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                //do nothing
            }
        });
        builderScript.create().show();
        return true;
    }

    //This method creates a dialog showing a list of shapes that can be chosen
    private void createListShapes(final String msg) {
        ArrayList<Shape> shapesList = new ArrayList<Shape>();
        ArrayList<Page> pageList = currentGame.getAllPages();

        for (Page page : pageList) {
            shapesList.addAll(page.getAllShapes());
        }

        final String[] shapes = new String[shapesList.size()];
        for (int i = 0; i < shapesList.size(); i++) {
            shapes[i] = shapesList.get(i).getName();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Shape");
        builder.setItems(shapes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String selectedShape = shapes[item];
                //if the caller is on drop which is the sole caller right now
                createActionScripts(msg + " " + selectedShape);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    //adds actions to the spinner
    private void addActionsToSpinner(Dialog dialog) {
        Spinner actionList = dialog.findViewById(R.id.actionSpinner);
        //gets the list of actions
        String[] listActions = {"goto", "play", "hide", "show"};
        //mykong
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, listActions);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionList.setAdapter(dataAdapter);
    }

    //adds listeners to first spinner
    private void addListenerToSpinner(final Dialog dialog) {
        Spinner actionList = dialog.findViewById(R.id.actionSpinner);
        actionList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String actionName = adapterView.getItemAtPosition(i).toString();
                addElements(actionName, dialog);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    //adds elements to second spinner of dialog depending on the name of the action
    private void addElements(String actionName, Dialog dialog) {
        Log.i(TAG + "addElements", "Adding element: " + actionName);
        //if actionName is sth, adds shape, else, adds sounds into the spinner
        Spinner elemList = dialog.findViewById(R.id.elementSpinner);

        if (actionName.equals("goto")) {
            //shows pages
            ArrayList<Page> listPgs = currentGame.getAllPages();
            String[] strPgs = new String[listPgs.size()];

            for (int i = 0; i < listPgs.size(); i++) {
                strPgs[i] = listPgs.get(i).getName();
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, strPgs);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            elemList.setAdapter(dataAdapter);
            Log.i(TAG + "addElements", "Setting adapter in goto :" + Arrays.toString(strPgs));

        } else if (actionName.equals("play")) {
            //shows sound
            String[] songs = {"carrot-eating", "evil-laugh", "fire-sound", "victory", "munch", "munching", "woof"};

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, songs);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            elemList.setAdapter(dataAdapter);
            Log.i(TAG + "addElements", "Setting adapter in play :" + Arrays.toString(songs));

        } else if (actionName.equals("hide") || actionName.equals("show")) {
            //shows shape names
            ArrayList<Shape> shapesList = new ArrayList<Shape>();
            ArrayList<Page> pageList = currentGame.getAllPages();

            for (Page page : pageList) {
                shapesList.addAll(page.getAllShapes());
            }
            String[] shapeStr = new String[shapesList.size()];

            for (int i = 0; i < shapesList.size(); i++) {
                shapeStr[i] = shapesList.get(i).getName();
            }

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, shapeStr);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            elemList.setAdapter(dataAdapter);
            Log.i(TAG + "addElements", "Setting adapter in hide|show :" + Arrays.toString(shapeStr));
        }
    }

    ///// UTILITY METHODS //////
    public static Object deepClone(Object object) {
        Game game = null;
        try {
            byte[] bytes = serializeGame(currentGame);
            if (bytes == null) {
                System.out.println("BYTES IS NULL!!");
            }
            ObjectInputStream objectIn = new ObjectInputStream(new ByteArrayInputStream(bytes));
            game = (Game) objectIn.readObject();
            System.out.println("Deserialized Game: " + game.getName());
        } catch (Exception e) {
            System.out.println(" exeption!!");
            e.printStackTrace();
        }
        return game;
    }

    private static byte[] serializeGame(Game game) {
        //System.out.println("Serializing Game: " + game.getName());
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


    // Generic methods for any type
    private byte[] objectToBytes(Object shape) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out;
        byte[] bytes = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(shape);
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

    private Object bytesToObject(byte[] bytes) {
        Object object = null;
        try {
            ObjectInputStream objectIn = new ObjectInputStream(new ByteArrayInputStream(bytes));
            object = objectIn.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;
    }

    //useful so that the default game can play
    public static void nullCurrentGame() {
        currentGame = null;
    }

    public void onClickPlayPause(View view) {
        if (mediaPlayer == null) return;
        Button btn = findViewById(R.id.play_pause);
        if (mediaPlayer.isPlaying()) {
            btn.setText("play");
            mediaPlayer.pause();
        } else {
            btn.setText("pause");
            mediaPlayer.start();
        }
    }

    public void onClickNext(View view) {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
        mediaPlayer.release();
        int index = (new Random()).nextInt(songIds.length);
        int songId = songIds[index];

        Button btn = findViewById(R.id.play_pause);
        btn.setText("pause");
        mediaPlayer = MediaPlayer.create(getApplicationContext(), songId);
        mediaPlayer.start();
        Toast.makeText(getApplicationContext(), songNames[index], Toast.LENGTH_SHORT).show();
    }
}
