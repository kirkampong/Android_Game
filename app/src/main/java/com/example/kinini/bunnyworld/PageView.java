package com.example.kinini.bunnyworld;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.*;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PageView extends View {
    private int pageWidth = 0;
    private int pageHeight = 0;
    protected Canvas canvas;
    private ArrayList<Shape> shapesInPage = new ArrayList<Shape>();
    private Paint linePaint;
    private float yOfLine = 500; //500
    private float startXPos = 0;
    private float startYPos = 0;
    private Shape currentShape = null;
    private Singleton st = Singleton.getInstance();
    private String TAG = "PageView";
    boolean onSizeChangedCalled = false;

    //every time a new shape is added, add it here.
    //every time the current page is changed, change the shapes here
    public PageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        st.setPageView(this);
        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        invalidate();
        //animation
        animatePageView();
    }

    public ArrayList<Shape> getShapesinPage() {
        return shapesInPage;
    }

    /*Animation set up help from : https://stackoverflow.com/questions/6796139/fade-in-fade-out-android-animation-in-java*/
    protected void animatePageView() {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(2000);
        AnimationSet animate = new AnimationSet(false);
        animate.addAnimation(fadeIn);
        this.setAnimation(animate);
        this.startAnimation(animate);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!onSizeChangedCalled) {
            pageWidth = w;
            pageHeight = h;
            onSizeChangedCalled = true;
        }
        yOfLine = 0.85f * pageHeight;

        Singleton st = Singleton.getInstance();
        //adds listeners for the editor mode
        if (st.getState() == Game.EDITOR_MODE) {
            EditText editText = ((Activity) getContext()).findViewById(R.id.editPage);
            editText.setEnabled(false);

            EditText xPos = ((Activity) getContext()).findViewById(R.id.xPos);
            EditText yPos = ((Activity) getContext()).findViewById(R.id.yPos);
            EditText width = ((Activity) getContext()).findViewById(R.id.editWidth);
            EditText height = ((Activity) getContext()).findViewById(R.id.editHeight);
            shapePropertiesListener(xPos);
            shapePropertiesListener(yPos);
            shapePropertiesListener(width);
            shapePropertiesListener(height);

            Button updateBtn = ((Activity) getContext()).findViewById(R.id.update_shape);
            if (updateBtn != null) {
                updateBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        updateImage();
                    }
                });
            }

        }
    }

    //this method checks to see if the shape properties are valid
    private void shapePropertiesListener(final EditText elem) {
        if (elem == null) return;
        elem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (currentShape != null) {
                    // TODO(oluwasanya): Also make sure this works when you drag around.
                    elem.setError(getValidationMsg(elem, currentShape));
                    Button updateShapeButton = ((Activity) getContext()).findViewById(R.id.update_shape);
                    updateShapeButton.setEnabled(elem.getError() == null);
                }
            }

            //checks if width and height are valid
            private String getValidationMsg(final EditText elem, Shape currShape) {
                String message = null;
                String text = elem.getText().toString();
                if (elem.getId() == R.id.yPos) {
                    float yPos = text.isEmpty() ? 0 : Float.parseFloat(text);

                    if (yPos < 0 || yPos > pageHeight) {
                        message = "Y position of shape must be between 0 and " + pageHeight;
                    }
                } else if (elem.getId() == R.id.xPos) {
                    float xPos = text.isEmpty() ? 0 : Float.parseFloat(text);
                    EditText widthEditTxt = ((Activity) getContext()).findViewById(R.id.editWidth);
                    String widthTxt = widthEditTxt.getText().toString();

                    float width = widthTxt.isEmpty() ? 0 : Float.parseFloat(widthTxt);

                    if (xPos < 0 || xPos + width >= pageWidth) {
                        message = "X position must be between 0 and " + (pageWidth - width);
                    }
                } else if (elem.getId() == R.id.editWidth) {
                    float shapeWidth = text.isEmpty() ? 0 : Float.parseFloat(text);
                    EditText xPosTxt = ((Activity) getContext()).findViewById(R.id.xPos);
                    String xTxt = xPosTxt.getText().toString();

                    float xPos = xTxt.isEmpty() ? 0 : Float.parseFloat(xTxt);
                    if (shapeWidth <= 5 || xPos + shapeWidth >= pageWidth) {
                        message = "Shape width must be between 5 and " + (pageWidth - xPos);
                    }

                } else if (elem.getId() == R.id.editHeight) {
                    float shapeHeight = text.isEmpty() ? 0 : Float.parseFloat(text);
                    EditText yPosTxt = ((Activity) getContext()).findViewById(R.id.yPos);
                    String yTxt = yPosTxt.getText().toString();

                    float yPos = yTxt.isEmpty() ? 0 : Float.parseFloat(yTxt);
                    if (yPos <= yOfLine - shapeHeight && (shapeHeight <= 5 || yPos + shapeHeight >= pageHeight)) {
                        message = "Shape height must be between 5 and " + (pageHeight - yPos);
                    }
                }
                return message;
            }
        });
    }

    //draws on the canvas
    @Override
    protected void onDraw(Canvas canvas) {
        this.canvas = canvas;
        super.onDraw(canvas);
        for (Shape shape : shapesInPage) {
            shape.onDraw(canvas);
        }
        //draw shapes in the possessions area
        ArrayList<Shape> possessions = GameActivity.getCurrentGame() == null ? PlayDefaultGameActivity.getCurrentGame().getPossessions() : GameActivity.getCurrentGame().getPossessions();
        for (Shape shape : possessions) {
            shape.onDraw(canvas);
        }

        //need to draw the possessions area line
        canvas.drawLine(0f, yOfLine, this.getWidth(), yOfLine, linePaint);
    }

    /*Updates the shapes in the pageView, such as due to a change in page*/
    public void updateShapes() {
        Game currentGame = GameActivity.getCurrentGame() == null ?
                PlayDefaultGameActivity.getCurrentGame() : GameActivity.getCurrentGame();
        if (currentGame != null) {
            shapesInPage = currentGame.getCurrentPage().getAllShapes();
            invalidate();
        }
    }

    //this method gets the current selected shape
    public Shape getCurrentShape() {
        return currentShape;
    }

    //this method is called to drag the shape around
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //need to check the mode
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                onMove(event);
                break;
            case MotionEvent.ACTION_UP:
                onUnPress(event);
                Singleton st = Singleton.getInstance();
                if (st.getState() == Game.GAME_MODE) executeScript(event);
                break;
        }

        invalidate();
        return true;
    }

    // executes the script on clicking the object.
    private void executeScript(MotionEvent event) {
        ArrayList<Shape> shapesAtPosition = getAllShapes(event);
        if (shapesAtPosition.size() != 0) {
            Shape sp = shapesAtPosition.get(0);
            sp.onActionPerformed(0);
            boolean foundOnDrop = false;

            for (Shape shape : shapesInPage) {
                if (shape != null && shapesAtPosition != null && currentShape != null) {
                    if (shapesAtPosition.contains(shape) && !shape.equals(currentShape) && shape.getOnDropShapeName().equals(currentShape.getName())) {//@temp fix
                        shape.onActionPerformed(2);
                        foundOnDrop = true;
                        updateShapes();
                    }
                }
            }
            //Snaps back to previous position
            if (!foundOnDrop) {
                if (currentShape != null && currentShape.isMovable() && shapesAtPosition.size() != 1)
                    currentShape.setPosition(currentShape.getPreviousPositionX(), currentShape.getPreviousPositionY());
                updateShapes();
            }

        }
    }

    //this method gets all the shapes in the page with respect to the touch event
    private ArrayList<Shape> getAllShapes(MotionEvent event) {
        ArrayList<Shape> sps = new ArrayList<>();
        for (int i = shapesInPage.size() - 1; i >= 0; i--) {
            Shape sp = shapesInPage.get(i);
            float shapeX = sp.getPosition().x;
            float shapeY = sp.getPosition().y;
            if (event.getX() >= shapeX && event.getX() <= shapeX + sp.getWidth() && event.getY() >= shapeY && event.getY() <= shapeY + sp.getHeight()) {
                sps.add(sp);
            }
        }
        return sps;

    }

    /*Responds to when the user first places a finger on the screen*/
    public void onTouchDown(MotionEvent event) {
        PlayDefaultGameActivity.stackGame();

        startXPos = event.getX();
        startYPos = event.getY();

        currentShape = shapeIsAtPosition(event);

        setCurrentShapeInPage();

        if (st.getState() == Game.EDITOR_MODE) updateViews();

        if (currentShape != null) currentShape.setPreviousPosition();
        if (st.getState() == Game.GAME_MODE) {
            if (currentShape != null && !currentShape.isVisible()) {
                currentShape = null;
            }
        }
    }

    /*returns the shape at the position where the event occurred*/
    public Shape shapeIsAtPosition(MotionEvent event) {
        //check in shapesInPage
        Shape sp = getShape(shapesInPage, event);
        if (sp != null) return sp;
        //check in shapes in possessions area
        Game currentGame = GameActivity.getCurrentGame() == null ? PlayDefaultGameActivity.getCurrentGame() : GameActivity.getCurrentGame();
        ArrayList<Shape> possessions = currentGame.getPossessions();
        sp = getShape(possessions, event);
        return sp;
    }

    //this method selects the current shape in the page
    public void setCurrentShapeInPage() {
        Game currentGame = GameActivity.getCurrentGame() == null ? PlayDefaultGameActivity.getCurrentGame() : GameActivity.getCurrentGame();
        currentGame.getCurrentPage().setSelected(currentShape);
    }

    //Gets the particular visible shape and adds it to the top of the array so that it's topmost when
    // selected.
    private Shape getShape(ArrayList<Shape> spList, MotionEvent event) {
        Shape shape = null;
        Singleton st = Singleton.getInstance();
        for (int i = spList.size() - 1; i >= 0; i--) {
            Shape sp = spList.get(i);
            float shapeX = sp.getPosition().x;
            float shapeY = sp.getPosition().y;

            // Allow for getting `invisible` images only in Editor mode.
            if ((st.getState() == Game.EDITOR_MODE || sp.isVisible()) && event.getX() >= shapeX &&
                    event.getX() <= shapeX + sp.getWidth() && event.getY() >= shapeY &&
                    event.getY() <= shapeY + sp.getHeight()) {
                shape = sp;
                break;
            }
        }

        // remove shape and place at end of array so that it's the first to be picked.
        if (shape != null) {
            spList.remove(shape);
            spList.add(shape);
        }
        return shape;
    }

    //this method updates the shape in the game
    private void updateShapeGame(MotionEvent event) {
        if (currentShape != null && currentShape.isMovable()) {
            updateShapePosition(event);
            ArrayList<Shape> shapesAtPosition = getAllShapes(event);
            for (Shape existing : shapesInPage) {
                if (existing != null && shapesAtPosition != null) {
                    if (shapesAtPosition.contains(existing) && existing.getName() !=
                            currentShape.getName()) {
                        if (existing.getOnDropShapeName().equals(currentShape.getName()))
                            existing.setHighlight(true);
                        else existing.setHighlight(false);
                    }
                }
            }
            updateShapes();
        }
    }

    //this method updates the shape in the editor
    private void updateShapeEditor(MotionEvent event) {
        if (currentShape != null) {
            updateShapePosition(event);
            ArrayList<Shape> shapesAtPosition = getAllShapes(event);
            for (Shape existing : shapesInPage) {
                if (existing != null && shapesAtPosition != null) {
                    if (shapesAtPosition.contains(existing) && existing.getName() !=
                            currentShape.getName()) {
                        if (existing.getOnDropShapeName().equals(currentShape.getName()))
                            existing.setHighlight(true);
                        else existing.setHighlight(false);
                    }
                }
            }
            updateShapes();
            updateViews();
        }
    }

    //this method is called when the user drags the finger
    public void onMove(MotionEvent event) {
        //check if there is a shape and if that shape is movable
        currentShape = shapeIsAtPosition(event);
        setCurrentShapeInPage();
        if (currentShape == null || !currentShape.isMovable()) {
            return;
        }
        if (st.getState() == Game.GAME_MODE) {
            updateShapeGame(event);
        } else { //editor mode
           updateShapeEditor(event);
        }
        startXPos = event.getX();
        startYPos = event.getY();
    }


    /*Updates the shape's position*/
    private void updateShapePosition(MotionEvent event) {
        //move by the displacement
        float changeInX = event.getX() - startXPos;
        float changeInY = event.getY() - startYPos;
        PointF pt = currentShape.getPosition();
        pt.x += changeInX;
        pt.y += changeInY;
        if (!(pt.x < 0 || pt.x + currentShape.getWidth() > pageWidth || pt.y < 0)) {
            currentShape.setPosition(pt.x, pt.y);
            if (st.getState() == Game.EDITOR_MODE) updatePositionFields();
        }
    }

    /*resets the start x and y and sets the shape into its final position*/
    public void onUnPress(MotionEvent event) {
        placeShape();
        startYPos = -1;
        startXPos = -1;
    }

    /*places the shape in position whenever the user brings the finger up*/
    public void placeShape() {
        if (currentShape == null) return;
        boolean isOnLine = checkIfOnLine(currentShape);
        if (isOnLine) {
            //check whether it should o above or below the possessions area.
            PointF pf = currentShape.getPosition();
            Float refPoint = pf.y + (currentShape.getHeight() / 2.0f);
            if (refPoint < yOfLine) {
                //place on top
                PointF newPf = new PointF();
                newPf.x = pf.x;
                newPf.y = yOfLine - currentShape.getHeight();
                newPf.y -= 2.0f;// to keep it slightly above the line
                currentShape.setPosition(newPf.x, newPf.y);
            } else {
                //place in possessions area
                PointF newPf = new PointF();
                newPf.x = pf.x;
                newPf.y = yOfLine + 2.0f;//to keep it slightly below the line
                currentShape.setPosition(newPf.x, newPf.y);
            }

        }
        PointF pt = currentShape.getPosition();
        float scaleF;

        Game currentGame = GameActivity.getCurrentGame() == null ? PlayDefaultGameActivity.defaultGame : GameActivity.getCurrentGame();

        //resizes the image to fit inside the possessions area and adds shape to page array or possessions
        if (pt.y < yOfLine) {
            //inside upper area
            scaleF = 1.0f;
            //transfer from possessions area to the game area
            currentGame.removeFromPossessions(currentShape);
            currentGame.getCurrentPage().addShape(currentShape);
        } else {
            //inside lower area
            scaleF = calculateScalingFactor(this.getHeight() - yOfLine);
            //transfer from the game area to the posssessions area
            currentGame.addToPossessions(currentShape);
            currentGame.getCurrentPage().removeShape(currentShape);
        }
        currentShape.setDrawFactor(scaleF);
    }

    /*calculates the scaling factor so that the image fits in the posssessions area.*/
    private float calculateScalingFactor(float heightOfLocation) {
        float scale = currentShape.getHeight() / heightOfLocation;
        if (scale < 1.0f) {
            scale = 1.0f;
        }
        scale = 1.0f / scale;
        return scale;
    }

    /*Checks if the shape is on the line*/
    private boolean checkIfOnLine(Shape sp) {
        PointF pf = sp.getPosition();
        return (pf.y <= yOfLine && (pf.y + sp.getHeight()) >= yOfLine);
    }

    //this method updates the image from the values from menu
    public void updateImage() {
        EditText shapeName = ((Activity) getContext()).findViewById(R.id.shapeId);
        EditText xPos = ((Activity) getContext()).findViewById(R.id.xPos);
        EditText yPos = ((Activity) getContext()).findViewById(R.id.yPos);
        EditText width = ((Activity) getContext()).findViewById(R.id.editWidth);
        EditText height = ((Activity) getContext()).findViewById(R.id.editHeight);
        Switch movable = ((Activity) getContext()).findViewById(R.id.movable);
        Switch visible = ((Activity) getContext()).findViewById(R.id.visible);
        TextView imageName = ((Activity) getContext()).findViewById(R.id.imageName);
        EditText shapeText = ((Activity) getContext()).findViewById(R.id.shapeText);
        EditText fontSize = ((Activity) getContext()).findViewById(R.id.font_size);
        CheckBox isBold = ((Activity) getContext()).findViewById(R.id.checkBold);
        CheckBox isItalic = ((Activity) getContext()).findViewById(R.id.checkItalic);

        if (shapeName == null) return;//game mode
        if (currentShape != null) {
            if (isFilled(xPos) && isFilled(yPos)) {
                currentShape.setPosition(Float.valueOf(xPos.getText().toString()), Float.valueOf(yPos.getText().toString()));
                placeShape();
            } else {
                Toast.makeText(getContext(), "Please input both x and y position",
                        Toast.LENGTH_SHORT).show();
            }

            if (isFilled(width)) {
                currentShape.setWidth(Float.valueOf(width.getText().toString()));
            }

            if (isFilled(height)) {
                currentShape.setHeight(Float.valueOf(height.getText().toString()));
            }

            if (isFilled(fontSize)) {
                currentShape.setFontSize(Float.valueOf(fontSize.getText().toString()));
            }
            //removed the check here to enable the user to be able to change back to an image
            currentShape.setShapeText(shapeText.getText().toString());
            currentShape.setMovable(movable.isChecked());
            currentShape.setVisibility(visible.isChecked());
            currentShape.setImageName(imageName.getText().toString());


            currentShape.setBold(isBold.isChecked());
            currentShape.setItalic(isItalic.isChecked());
            String oldName = currentShape.getName();
            String newName = shapeName.getText().toString().trim();
            if (!oldName.equals(newName) && !newName.trim().isEmpty()) {
                currentShape.setName(newName);
                updateAllScripts(oldName, newName);
            }

            updateShapes();
        }
    }

    //this method updates all the shape scripts
    public void updateAllScripts(String oldName, String newName) {
        Game currentGame = GameActivity.getCurrentGame() == null ?
                PlayDefaultGameActivity.getCurrentGame() : GameActivity.getCurrentGame();
        if (currentGame == null) return;
        ;
        ArrayList<Page> allPages = currentGame.getAllPages();
        if (allPages == null) return;
        for (Page pg : allPages) {
            for (Shape sp : pg.getAllShapes()) {
                sp.updateShapeScript(oldName, newName);
            }
        }
        for (Shape sp : shapesInPage) {
            sp.updateShapeScript(oldName, newName);
        }
    }


    //This method only updates the position fields in the menu
    private void updatePositionFields() {
        EditText xPos = ((Activity) getContext()).findViewById(R.id.xPos);
        EditText yPos = ((Activity) getContext()).findViewById(R.id.yPos);
        if (xPos == null || yPos == null) return;
        if (currentShape != null) {
            xPos.setText(Integer.toString((int) currentShape.getPosition().x));
            yPos.setText(Integer.toString((int) currentShape.getPosition().y));
        } else {
            xPos.setText("");
            yPos.setText("");
        }
    }

    //This method updates the menu view
    private void updateViews() {
        Button updateShapeButton = ((Activity) getContext()).findViewById(R.id.update_shape);
        EditText shapeName = ((Activity) getContext()).findViewById(R.id.shapeId);
        EditText width = ((Activity) getContext()).findViewById(R.id.editWidth);
        EditText height = ((Activity) getContext()).findViewById(R.id.editHeight);
        Switch movable = ((Activity) getContext()).findViewById(R.id.movable);
        Switch visible = ((Activity) getContext()).findViewById(R.id.visible);
        TextView imageName = ((Activity) getContext()).findViewById(R.id.imageName);
        EditText shapeText = ((Activity) getContext()).findViewById(R.id.shapeText);
        CheckBox isBold = ((Activity) getContext()).findViewById(R.id.checkBold);
        CheckBox isItalic = ((Activity) getContext()).findViewById(R.id.checkItalic);
        EditText fontSize = ((Activity) getContext()).findViewById(R.id.font_size);

        if (shapeName == null) return;//game mode
        updatePositionFields();

        if (currentShape != null) {
            width.setText(Float.toString((int) currentShape.getWidth()));
            height.setText(Float.toString((int) currentShape.getHeight()));
            movable.setChecked(currentShape.isMovable());
            visible.setChecked(currentShape.isVisible());
            imageName.setText(currentShape.getImageName());
            shapeText.setText(currentShape.getText());
            shapeName.setText(currentShape.getName());
            updateShapeButton.setEnabled(true);
            isBold.setChecked(currentShape.isBold());
            isItalic.setChecked(currentShape.isItalic());
            fontSize.setText("" + currentShape.getFontSize());
        } else {
            //xPos.setText("");
            //yPos.setText("");
            width.setText("");
            height.setText("");
            movable.setChecked(false);
            visible.setChecked(false);
            imageName.setText(Shape.DEFAULT_IMAGE_NAME);
            shapeText.setText("");
            shapeName.setText("");
            isBold.setChecked(false);
            isItalic.setChecked(false);
            fontSize.setText("");
        }
    }

    //checks if the editText is filled (prevents against invalid input)
    private boolean isFilled(EditText editText) {
        return !TextUtils.isEmpty(editText.getText().toString());
    }
}
