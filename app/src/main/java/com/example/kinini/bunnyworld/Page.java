package com.example.kinini.bunnyworld;

import java.io.Serializable;
import java.util.ArrayList;

public class Page implements Serializable {
    //have a flag that checks whether the page is page1
    //have a way of knowing which game this page is in
    private String name;
    private Shape selectedShape;
    private ArrayList<Shape> listOfShapes = new ArrayList<>();
    private int pageCounter = 0;

    // Constructor with Page name specified (unnecessary?)
    public Page(String str) {
        name = str;
    }

    // Constructor with no args assigns default sequential names Page1, Page2 ...
    public Page() {
        name = "Page" + pageCounter;
        pageCounter++;
    }

    public String getName() {
        return name;
    }

    public void setName(String str) {
        name = str;
    }

    public Shape getSelectedShape() {
        return selectedShape;
    }

    public void addShape(Shape newShape) {
        for (Shape shape : listOfShapes) {
            if (shape.equals(newShape)) {
                return;
            }
        }
        listOfShapes.add(newShape);
        onShapesChange();
    }

    public void removeShape(Shape sp) {
        for (int i = 0; i < listOfShapes.size(); i++) {
            Shape shape = listOfShapes.get(i);
            if (sp.equals(shape)) {
                listOfShapes.remove(i);
                onShapesChange();
                return;
            }
        }
    }

    private void onShapesChange() {
        Singleton st = Singleton.getInstance();
        PageView view = st.getCurrentPageView();
        if (view != null) view.updateShapes();
    }

    public void setSelected(Shape shape) {
        selectedShape = shape;
    }


    public ArrayList<Shape> getAllShapes() {
        return listOfShapes;
    }

    @Override
    public boolean equals(Object otherPage) {
        if (!(otherPage instanceof Page)) {
            return false;
        }
        if (this.equals(otherPage)) {
            return true;
        }
        if (this.name.equals(((Page) otherPage).name)) {
            return true;
        }
        return false;
    }
}