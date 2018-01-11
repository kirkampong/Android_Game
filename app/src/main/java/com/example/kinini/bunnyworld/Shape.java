package com.example.kinini.bunnyworld;

import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Created by kenneth on 11/19/17.
 * THIS IS JUST A STUB
 */

public class Shape implements Serializable {
    // have a flag of edit mode
    //if edit mode true, then, on click shows properties
    public String imageName = DEFAULT_IMAGE_NAME; // default imageName
    private String shapeText = "";
    private static int shapeCounter = 0;//help in shape naming
    private String text;
    private String name;
    private Typeface typeface;
    private boolean isBold;
    private boolean isItalic;

    //private Drawable associatedImage; //always set this by R.drawable..../ set to null by default
    private String shapeScript;
    private boolean isMovable;
    private boolean isHighlighted;
    private boolean isVisible;
    private float x, y, width, height, drawFactor;//int [] location;
    private int paintNum;
    private boolean clickable;
    private String actualName;
    private String onDropShapeName = "";
    private HashMap<Integer, String> triggerToAction = new HashMap<>();
    private static int onClick = 0;
    private static int onEnter = 1;
    private static int onDrop = 2;

    public static final String DEFAULT_IMAGE_NAME = "No Image";
    private float prevX = 0;
    private float prevY = 0;

    private int textSize = 60;

    //constructor
    public Shape() {
        shapeCounter++;
        initShapeProperties("shape" + shapeCounter);
    }

    public Shape(String nameOfGame) {
        initShapeProperties(Singleton.getInstance().nextShapeId(nameOfGame));
    }

    private void initShapeProperties(String name) {
        Singleton st = Singleton.getInstance();
        setName(name);
        setScript("");
        setText("");
        setMovable(st.getState() == Game.EDITOR_MODE); // Set true if in editor mode.
        setVisible(true);
        setHighlight(false);
        setX(10);
        setY(10);
        setActualName("no name");
        setWidth(200);
        setHeight(200);
        setDrawFactor(1.0f);
        setPaintNum(Color.LTGRAY);
        setClickable(true);
    }

    /*
    Util functions
     */
    public HashMap<String, Float> getProperties() {
        HashMap<String, Float> mapOfProperties = new HashMap<>();
        mapOfProperties.put("startX", x);
        mapOfProperties.put("startY", y);
        mapOfProperties.put("width", width);
        mapOfProperties.put("height", height);
        return mapOfProperties;
    }

    public void setBold(boolean newBold) {
        isBold = newBold;
    }

    public void setItalic(boolean newItalic) {
        isItalic = newItalic;
    }

    public boolean isBold() {
        return isBold;
    }

    public boolean isItalic() {
        return isItalic;
    }

    private void setTypefaceText(Paint paint) {
        int type;
        if (isBold && isItalic) {
            type = Typeface.BOLD_ITALIC;
        } else if (isBold) {
            type = Typeface.BOLD;
        } else if (isItalic) {
            type = Typeface.ITALIC;
        } else {
            type = Typeface.NORMAL;
        }
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, type));
    }

    //draws the shape
    public void onDraw(Canvas canvas) {
        int gameState = Singleton.getInstance().getState();

        // Don't bother rendering if in game mode
        if (!this.isVisible && gameState == Game.GAME_MODE) return;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        //Provide support for rich text (i.e., bold, italic, colors, and changing font and font size) within a text
        setTypefaceText(paint);

        paint.setColor(paintNum);
        if (isHighlighted) paint.setColor(Color.GREEN);

        // Make it fairly transparent if inVisible and in editor mode
        // otherwise it is visible
        int alpha = (!this.isVisible() && gameState == Game.EDITOR_MODE) ? 100 : 255;
        paint.setAlpha(alpha);

        float drawWidth = width * drawFactor;
        float drawHeight = height * drawFactor;
        float right = x + drawWidth;
        float bottom = y + drawHeight;

        if (!this.text.isEmpty()) {
            paint.setTextSize(textSize);
//            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); // Bold text
            float textWidth = paint.measureText(this.text);
            this.width = textWidth;
            this.height = textSize;
            canvas.drawText(text, x, y + textSize, paint);
        } else if (!this.imageName.equals(DEFAULT_IMAGE_NAME)) {
            Log.i("Shape", "Rendering on canvas");
            Game currentGame = GameActivity.getCurrentGame() == null ? PlayDefaultGameActivity.defaultGame : GameActivity.getCurrentGame();
            Drawable drawable = currentGame.getAssociatedImage(imageName);
            if (isHighlighted) drawable.setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);

            drawable.setAlpha(alpha);
            drawable.setBounds((int) x, (int) y, (int) right, (int) bottom);
            drawable.draw(canvas);
        } else {
            canvas.drawRect(x, y, right, bottom, paint);
        }
    }

    public boolean setShapeScript(String script) {
        boolean isAdded = true;
        String[] arr = script.split(";");
        for (String s : arr) {
            s = s.trim();
            s = s.toLowerCase();
            String actions = "";
            if (s.contains("on click")) {
                if (triggerToAction.containsKey(onClick)) {
                    isAdded = false;
                    continue;
                }
                actions = s.replace("on click", "");
                triggerToAction.put(onClick, actions);
            } else if (s.contains("on enter")) {
                if (triggerToAction.containsKey(onEnter)) {
                    isAdded = false;
                    continue;
                }
                actions = s.replace("on enter", "");
                triggerToAction.put(onEnter, actions);
            } else if (s.contains("on drop")) {
                if (triggerToAction.containsKey(onDrop)) {
                    isAdded = false;
                    continue;
                }
                actions = s.replace("on drop", "");
                actions = actions.trim();
                int index = actions.indexOf(" ");
                onDropShapeName = actions.substring(0, index).trim();//change to shape object, but that shape will have to be created first
                actions = actions.substring(index + 1);
                //split the actions in to a from a string to object
                triggerToAction.put(onDrop, actions);//play sound, show shape, hide shape, goto page
            }
        }
        return isAdded;
    }

    public void setActualName(String name) {
        this.actualName = name;
    }

    public String getActualName() {
        return this.actualName;
    }

    public void updateShapeScript(String oldName, String newName) {
        if (onDropShapeName.equals((oldName.trim()))) {
            onDropShapeName = newName.trim();
        }
        for (HashMap.Entry<Integer, String> entry : triggerToAction.entrySet()) {
            if (entry.getValue().contains(oldName)) {
                String newString = entry.getValue().replace(oldName, newName);
                triggerToAction.put(entry.getKey(), newString);
            }
        }
    }

    public void onActionPerformed(int action_code) {
        // return immediately if in editor mode
        Singleton st = Singleton.getInstance();
        if (st.getState() == Game.EDITOR_MODE) return;

        String[] actionWords = "goto,play,hide,show".split(",");
        Set<String> actionWordsSet = new HashSet<String>(Arrays.asList(actionWords));
        String nextActions = triggerToAction.get(action_code);
        if (nextActions == null) return;
        nextActions.trim();
        String[] actionsArr = nextActions.split(" ");
        for (int i = 0; i < actionsArr.length; i++) {

            if (actionWordsSet.contains(actionsArr[i])) {//if an action

                if (i < actionsArr.length - 1) {//avoid case of no target at the end
                    executeAction(actionsArr[i], actionsArr[i + 1]);
                }
            }
        }
    }

    private void executeAction(String actionName, String target) {
        Game currentGame = GameActivity.getCurrentGame() == null ?
                PlayDefaultGameActivity.getCurrentGame() : GameActivity.getCurrentGame();
        if (actionName.equals("play")) {
            //play sound with the name target
            currentGame.onPlaySound(target);
        } else if (actionName.equals("goto")) {
            //go to the page with the name target
            currentGame.changePage(target);
        } else if (actionName.equals("hide")) {
            //hide the shape with the name target
            currentGame.onChangeShapeVisibility(target, false);
        } else if (actionName.equals("show")) {
            //show the shape with the name target
            currentGame.onChangeShapeVisibility(target, true);
        }
    }

    public float getDrawFactor() {
        return drawFactor;
    }

    public void setWidth(float newWidth) {
        width = newWidth;
    }

    public void setHeight(float newHeight) {
        height = newHeight;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public boolean isMovable() {
        return isMovable;
    }

    // Setters.
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    public void setHighlight(boolean highlight) {
        this.isHighlighted = highlight;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setVisible(boolean bool) {
        this.isVisible = bool;
    }

//    public void setAssociatedImage(Drawable associatedImage) {
//        if (associatedImage == null) System.out.println("AssociatedImage is null at initializer");
//        this.associatedImage = associatedImage;
//    }

    public void setMovable(boolean isMovable) {
        this.isMovable = isMovable;
    }

    public void setScript(String script) {
        this.shapeScript = script;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setText(String text) {

        this.text = text;
        //this is causing the shape width and height to not update properly
        //if (text.trim().isEmpty()) {
        //    setWidth(200f);
        //    setHeight(200f);
        //}
    }

    public void setDrawFactor(float drawFactor) {
        this.drawFactor = drawFactor;
    }

    public void setPaintNum(int paintNum) {
        this.paintNum = paintNum;
    }

    // Getters
    public PointF getPosition() {
        PointF pf = new PointF();
        pf.x = x;
        pf.y = y;
        return pf;
    }

    public void setPosition(float prevX, float prevY) {
        this.x = prevX;
        this.y = prevY;
    }

    public void setPreviousPosition() {
        prevX = x;
        prevY = y;
    }

    public float getPreviousPositionX() {
        return this.prevX;
    }

    public float getPreviousPositionY() {
        return this.prevY;
    }

    public boolean setHighlight() {
        return isHighlighted;
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }


    public List<String> getShapeScripts() {
        ArrayList<String> scripts = new ArrayList<String>();
        for (int key : triggerToAction.keySet()) {
            if (key == 0) {
                scripts.add("on click " + triggerToAction.get(key));
            } else if (key == 1) {
                scripts.add("on enter " + triggerToAction.get(key));
            } else if (key == 2) {
                scripts.add("on drop " + triggerToAction.get(key));
            }
        }
        return scripts;
    }

    public void removeScript(String script) {
        for (int key : triggerToAction.keySet()) {
            if (key == 0) {
                if (("on click " + triggerToAction.get(key)).equals(script)) {
                    triggerToAction.remove(key);
                    return;
                }
            } else if (key == 1) {
                if (("on enter " + triggerToAction.get(key)).equals(script)) {
                    triggerToAction.remove(key);
                    return;
                }
            } else if (key == 2) {
                if (("on drop " + triggerToAction.get(key)).equals(script)) {
                    triggerToAction.remove(key);
                    return;
                }
            }
        }
    }

    public String getImageName(String imageName) {
        return this.imageName;
    }

    public String getShapeText() {
        return shapeText;
    }

    public boolean getVisibility() {
        return isVisible;
    }

    public boolean getMovable() {
        return isMovable;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
        Game currentGame = GameActivity.getCurrentGame() == null ? PlayDefaultGameActivity.defaultGame : GameActivity.getCurrentGame();
        currentGame.onRefresh();
    }

    public void setVisibility(boolean bool) {
        isVisible = bool;
    }

    public void setShapeText(String text) {
        setText(text);
    }

    public String getOnDropShapeName() {
        return onDropShapeName;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean getClickable() {
        return clickable;
    }

    //public Drawable getAssociatedImsetage() {
//        return associatedImage;
//    }

    public String getShapeScript() {
        return shapeScript;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public boolean equals(Shape obj) {
        if (obj != null) return this.name.equals(obj.getName());
        return false;
    }

    @Override
    public String toString() {
        return ("name: " + name + '\n' +
                "text: " + text + '\n' +
                "image name: " + imageName + '\n' +
                "x: " + x + '\n' +
                "y: " + y + '\n' +
                "width: " + width + '\n' +
                "height: " + height + '\n' +
                "actual name: " + actualName + '\n' +
                "shapeScript: " + shapeScript + '\n' +
                "isMovable: " + isMovable + '\n' +
                "isVisible: " + isVisible + '\n');
    }

    public void setFontSize(float textSize) {
        this.textSize = (int) textSize;
    }

    public int getFontSize() {
        return textSize;
    }
}
