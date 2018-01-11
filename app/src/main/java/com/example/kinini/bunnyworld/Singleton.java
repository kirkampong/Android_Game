package com.example.kinini.bunnyworld;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import java.util.HashMap;

/**
 * Created by kenneth on 11/19/17.
 */

class Singleton {
    private static final Singleton ourInstance = new Singleton();
    private int shapeNum = 1;   // Load this from the database
    private HashMap<String, Integer> pagesIds = new HashMap<>();
    private HashMap<String, Integer> shapeIds = new HashMap<>();
    private PageView currentPageView;
    private int currentState = Game.EDITOR_MODE;
    private Shape copiedShape = null;
    private Context context;
    private String packageName;
    private String gameJson;

    static Singleton getInstance() {
        return ourInstance;
    }

    private Singleton() {
    }

    public String nextShapeId(String nameOfGame){
        if (!shapeIds.containsKey(nameOfGame)){
            shapeIds.put(nameOfGame, 1);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("shape");
        int nextShapeNum = shapeIds.get(nameOfGame);
        sb.append(nextShapeNum);
        nextShapeNum++;
        shapeIds.put(nameOfGame, nextShapeNum);
        return  sb.toString();
    }

    /*generates the next page given the name of the game*/
    public String nextPageId(String gameName) {
        // System.out.println("Getting id for : "+gameName);
        if (!pagesIds.containsKey(gameName)) {
            //System.out.println("first time for :"+gameName);
            pagesIds.put(gameName, 1);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("page");
        int nextPageNum = pagesIds.get(gameName);
        sb.append(nextPageNum);
        nextPageNum++;
        pagesIds.put(gameName, nextPageNum);
        return sb.toString();
    }

    public void deleteGame(String gameName) {
        if (pagesIds.containsKey(gameName)) {
            pagesIds.remove(gameName);
        }
        if (shapeIds.containsKey(gameName)) {
            shapeIds.remove(gameName);
        }
    }

    public void setPageView(PageView pg) {
        currentPageView = pg;
    }

    public PageView getCurrentPageView() {
        return currentPageView;
    }

    public void setCopiedShape(Shape shape) {
        copiedShape = shape;
    }

    public Shape getCopiedShape() {
        return copiedShape;
    }

    public void setState(int state) {
        currentState = state;
    }

    public int getState() {
        return currentState;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void setPackageName(String name) {
        packageName = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setCopiedGame(String gameJson) {
        this.gameJson = gameJson;

        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Game as Json", gameJson);
        clipboard.setPrimaryClip(clip);
    }
}
