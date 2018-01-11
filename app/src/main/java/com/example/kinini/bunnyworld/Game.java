package com.example.kinini.bunnyworld;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.util.Log;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kenneth on 11/19/17.
 */

public class Game implements Serializable {
    /**
     * remember to update these when starting
     */
    public static final int GAME_MODE = 0;
    public static final int EDITOR_MODE = 1;

    private String nameOfGame;
    protected ArrayList<Page> listOfPages;
    private Page currentPage;
    private ArrayList<Shape> possessions;
    int mode = EDITOR_MODE;

    //returns the list of pages in this game
    public ArrayList<Page> getAllPages() {
        return listOfPages;
    }

    /*Constructor takes the name of the game*/
    public Game(String str) {
        nameOfGame = str;
        possessions = new ArrayList<>();
        listOfPages = new ArrayList<>();

        // automatically create page one
        Singleton st = Singleton.getInstance();
        String pageName = st.nextPageId(nameOfGame);
        Page pg = new Page(pageName);
        listOfPages.add(pg);
        currentPage = pg;
    }

    protected String getName() {
        return nameOfGame;
    }

    /*creates a new page*/
    public void createPage() {
        Singleton st = Singleton.getInstance();
        String pageName = st.nextPageId(nameOfGame);
        Page pg = new Page(pageName);
        listOfPages.add(pg);

        //show new Empty Page
        changePage(pageName);
    }

    /*Renames a page*/
    public void renamePage(String oldName, String newName) {
        for (Page pg : listOfPages) {
            if (pg.getName().equals(oldName)) {
                pg.setName(newName);
            }
        }
    }

    /*Deletes a page and changes the page to the previous page*/
    public void deletePage(String pageName) {
        //page1 cannot be deleted
        if (pageName.equals(listOfPages.get(0))) {
            return;
        }
        for (int i = 0; i < listOfPages.size(); i++) {
            if (listOfPages.get(i).getName().equals(pageName)) {
                listOfPages.remove(i);
                if (currentPage.getName().equals(pageName)) {
                    changePage(listOfPages.get((i - 1)).getName());
                }
                return;
            }
        }
    }

    /*returns the currentPage*/
    public Page getCurrentPage() {
        return currentPage;
    }

    public void changePage(String nextPage) {
        Singleton.getInstance().getCurrentPageView().animatePageView();
        for (Page pg : listOfPages) {
            if (pg.getName().equals(nextPage)) {
                currentPage = pg;
                onRefresh();
                for (Shape sp : currentPage.getAllShapes()) {
                    List<String> list = sp.getShapeScripts();
                    for (String script : list) {
                        if (script.contains("on enter")) {
                            sp.onActionPerformed(1);
                        }
                    }
                }
                break;
            }
        }

        //on enter
    }

    public void onRefresh() {
        //need to draw shapes on current page
        Singleton st = Singleton.getInstance();
        PageView pageView = st.getCurrentPageView();
        pageView.updateShapes();
        if (pageView.getCurrentShape() != null) {
            System.out.println("The current shape name: " + pageView.getCurrentShape().getName());
        }
        Log.i("Game", "Refreshing: ");
    }

    public void addToPossessions(Shape sp) {
        //adds the shape to the possessions area and add to shape list
        possessions.add(sp);
    }

    public void removeFromPossessions(Shape sp) {
        for (int i = 0; i < possessions.size(); i++) {
            Shape shape = possessions.get(i);
            if (shape.equals(sp)) {
                possessions.remove(i);
                return;
            }
        }
    }

    public ArrayList<Shape> getPossessions() {
        return possessions;
    }

    public boolean isValidPageName(String str) {
        for (Page pg : listOfPages) {
            if (pg.getName().equals(str)) {
                return false;
            }
        }
        return true;
    }

    public boolean isValidShapeName(String str) {
        //check in all pages
        for (Page pg : listOfPages) {
            for (Shape sp : pg.getAllShapes()) {
                if (sp.getName().equals(str)) {
                    return false;
                }
            }
        }
        //check in possessions area
        for (Shape sp : possessions) {
            if (sp.getName().equals(str)) {
                return false;
            }
        }
        return true;
    }

    public void onChangeShapeVisibility(String shapeName, boolean visibility) {
       // System.out.println("The current name of game is: "+this.nameOfGame);
        for (Page pg : listOfPages) {
            for (Shape sp : pg.getAllShapes()) {
                if (sp.getName().equals(shapeName)) {
                    sp.setVisible(visibility);
                    onRefresh();
                }
            }

            for (Shape sp : possessions) {
                if (sp.getName().equals(shapeName)) {
                    sp.setVisible(visibility);
                    onRefresh();
                }
            }

        }
    }

    public void setGameMode(int gameMode) {
        mode = gameMode;
    }

    public void onPlaySound(String str) {
        System.out.println("Gettin here");
        System.out.println("Audio resource file is " + str);
        int resourceId = getAudioResourceId(str);
        Context context = Singleton.getInstance().getContext();
        if (context == null) {
            System.out.println("Context is null");
        }

        MediaPlayer mPlayer = MediaPlayer.create(context, resourceId);
        mPlayer.start();
    }

    public Drawable getAssociatedImage(String str) {
        str = str.trim();
        Context context = Singleton.getInstance().getCurrentPageView().getContext();
        // Drawable dr = context.getResources().getDrawable(R.drawable.str);
        switch (str) {
            case "door":
                return context.getResources().getDrawable(R.drawable.door);
            case "carrot":
                return context.getResources().getDrawable(R.drawable.carrot);
            case "carrot2":
                return context.getResources().getDrawable(R.drawable.carrot2);
            case "death":
                return context.getResources().getDrawable(R.drawable.death);
            case "duck":
                return context.getResources().getDrawable(R.drawable.duck);
            case "fire":
                return context.getResources().getDrawable(R.drawable.fire);
            case "mystic":
                return context.getResources().getDrawable(R.drawable.mystic);
        }
        return null;
    }

    public int getAudioResourceId(String target) {
        switch (target) {
            case "carrot-eating":
                return R.raw.carrotcarrotcarrot;
            case "evil-laugh":
                return R.raw.evillaugh;
            case "fire-sound":
                return R.raw.fire;
            case "victory":
                return R.raw.hooray;
            case "munch":
                return R.raw.munch;
            case "munching":
                return R.raw.munching;
            case "woof":
                return R.raw.woof;
        }
        return -1;
    }

    public String toJson() {
        Gson gson = new Gson();
        String json = gson.toJson(this);
        Log.i("Json", "JSON: " + json);
        return json;
    }

    public Game fromJson(String json) {
        Gson gson = new Gson();
        Game game = gson.fromJson(json, Game.class);
        Log.i("Json", "Game name from Json: " + game.getName());
        return game;
    }
}

