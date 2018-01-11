package com.example.kinini.bunnyworld;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

public class PlayDefaultGameActivity extends AppCompatActivity {
    public static Game defaultGame;
    Singleton st;
//    protected ArrayList<Page> visitedPages=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_default_game);
        initGame();
    }

    private void initGame() {
        st = Singleton.getInstance();
        st.setState(Game.GAME_MODE);
        st.setContext(getApplicationContext());
        st.setPackageName(this.getClass().getPackage().getName());
        st.deleteGame("DefaultGame");
        defaultGame = new Game("DefaultGame");
        defaultGame.onRefresh();
        // Create landing Page for default game
        defaultGame.createPage();
        System.out.println("Executing the default game!!");
        //need to set the currentGame in the editor to null
        GameActivity.nullCurrentGame();
        // Create Shapes for page
        createPage1Shapes();
        createPage2Shapes();
        createPage3Shapes();
        createPage4Shapes();
        createPage5Shapes();
    }

    private void createPage1Shapes() {
        ArrayList<Shape> shapes = new ArrayList<>();

        Shape s1 = new Shape();
        s1.setText("You are in a maze of twisty little passages all alike");
        s1.setX(400);
        s1.setY(120);
        s1.setMovable(false);
        s1.setClickable(false);
        shapes.add(s1);

        Shape s2 = new Shape();
        s2.setX(380);
        s2.setActualName("door1");
        s2.setName("door1");
        s2.setImageName("door");
        s2.setY(300);
        s2.setWidth(300);
        s2.setHeight(400);
        s2.setMovable(false);
        s2.setShapeScript("on click goto page3");
        shapes.add(s2);

        Shape s3 = new Shape();
        //s3.setAssociatedImage(getResources().getDrawable(R.drawable.door));
        s3.setX(900);
        s3.setY(300);
        s3.setWidth(300);
        s3.setHeight(400);
        s3.setActualName("door2");
        s3.setName("door2");
        s3.setImageName("door");
        s3.setShapeScript("on click goto page4;");
        s3.setMovable(false);
        s3.setVisible(false);
        shapes.add(s3);

        Shape s4 = new Shape();
        //s4.setAssociatedImage(getResources().getDrawable(R.drawable.door));
        s4.setX(1500);
        s4.setY(300);
        s4.setWidth(300);
        s4.setHeight(400);
        s4.setShapeScript("on click goto page5;");
        s4.setActualName("door3");
        s4.setName("door3");
        s4.setImageName("door");
        s4.setMovable(false);
        shapes.add(s4);

        for (Shape s : shapes) {
            defaultGame.getCurrentPage().addShape(s);
        }

    }


    private void createPage2Shapes() {
        ArrayList<Shape> shapes = new ArrayList<>();


        Shape s1 = new Shape();
       // s1.setAssociatedImage(getResources().getDrawable(R.drawable.death));
        //s1.setAssociatedImage(getResources().getDrawable(R.drawable.mystic));
        s1.setX(900);
        s1.setY(70);
        s1.setWidth(300);
        s1.setHeight(300);
        s1.setImageName("mystic");
        s1.setMovable(false);
        s1.setShapeScript("on click hide carrot play carrot-eating; on enter show door2;");
        s1.setClickable(true);
        shapes.add(s1);


        Shape s2 = new Shape();
        //s2.setAssociatedImage(getResources().getDrawable(R.drawable.door));
        s2.setX(200);
        s2.setY(500);
        s2.setWidth(300);
        s2.setHeight(400);
        s2.setMovable(false);
        s2.setActualName("door4");
        s2.setName("door4");
        s2.setImageName("door");

        s2.setShapeScript("on click goto page2");
        // s2.setShapeScript("on click show shape4");
        s2.setClickable(true);
        shapes.add(s2);

        Shape s3 = new Shape();
        s3.setText("Mystic Bunny- Rub my tummy for a big surprise");
        s3.setX(550);
        s3.setY(600);
        s3.setMovable(false);
        s3.setClickable(false);
        shapes.add(s3);

        String pageName = st.nextPageId("DefaultGame");
        Page pg = new Page(pageName);
        for (Shape s : shapes) {
            pg.addShape(s);
        }
        defaultGame.listOfPages.add(pg);

    }


    private void createPage3Shapes() {
        ArrayList<Shape> shapes = new ArrayList<>();


        Shape s1 = new Shape();
       // s1.setAssociatedImage(getResources().getDrawable(R.drawable.fire));
        s1.setX(900);
        s1.setY(50);
        s1.setWidth(300);
        s1.setHeight(300);
        s1.setImageName("fire");
        s1.setMovable(false);
        s1.setClickable(false);
        s1.setShapeScript("on enter play fire-sound");
        shapes.add(s1);

        Shape s2 = new Shape();
        s2.setText("Eek! Fire room run away!");
        s2.setX(700);
        s2.setY(400);
        s2.setMovable(false);
        s2.setClickable(false);
        shapes.add(s2);

        Shape s3 = new Shape();
       // s3.setAssociatedImage(getResources().getDrawable(R.drawable.door));
        s3.setX(350);
        s3.setY(500);
        s3.setWidth(300);
        s3.setHeight(400);
        s3.setImageName("door");
        s3.setShapeScript("on click goto page3");
        s3.setMovable(false);
        s3.setClickable(true);
        shapes.add(s3);


        Shape s4 = new Shape();
        //s4.setAssociatedImage(getResources().getDrawable(R.drawable.carrot));
        s4.setX(1200);
        s4.setY(500);
        s4.setWidth(300);
        s4.setHeight(400);
        s4.setActualName("carrot");
        s4.setName("carrot");
        s4.setImageName("carrot");
        s4.setClickable(true);
        s4.setMovable(true);
        shapes.add(s4);

        String pageName = st.nextPageId("DefaultGame");
        Page pg = new Page(pageName);
        for (Shape s : shapes) {
            pg.addShape(s);
        }
        defaultGame.listOfPages.add(pg);

    }

    private void createPage4Shapes() {
        ArrayList<Shape> shapes = new ArrayList<>();

        Shape s1 = new Shape();
        //s1.setAssociatedImage(getResources().getDrawable(R.drawable.death));
        s1.setX(700);
        s1.setY(160);
        s1.setWidth(300);
        s1.setHeight(300);
        s1.setActualName("death-bunny");
        s1.setName("death-bunny");
        s1.setImageName("death");
        s1.setMovable(false);
        s1.setShapeScript("on enter play evil-laugh; on drop carrot hide carrot play carrot-eating hide death-bunny show exit; on click play evil-laugh;");
        s1.setClickable(false);
        shapes.add(s1);

        Shape s2 = new Shape();
        //s2.setAssociatedImage(getResources().getDrawable(R.drawable.door));
        s2.setX(1300);
        s2.setY(400);
        s2.setWidth(300);
        s2.setHeight(400);
        s2.setVisible(false);
        s2.setMovable(false);
        s2.setActualName("exit");
        s2.setName("exit");
        s2.setImageName("door");
        s2.setShapeScript("on click goto page6");
        s2.setClickable(true);
        shapes.add(s2);

        Shape s3 = new Shape();
        s3.setText("You must appease the bunny of death");
        s3.setX(300);
        s3.setY(600);
        s3.setMovable(false);
        s3.setClickable(false);
        shapes.add(s3);


        String pageName = st.nextPageId("DefaultGame");
        Page pg = new Page(pageName);
        for (Shape s : shapes) {
            pg.addShape(s);
        }
        defaultGame.listOfPages.add(pg);

    }

    private void createPage5Shapes() {
        ArrayList<Shape> shapes = new ArrayList<>();


        Shape s1 = new Shape();
        //s1.setAssociatedImage(getResources().getDrawable(R.drawable.carrot));
        s1.setX(400);
        s1.setY(150);
        s1.setWidth(300);
        s1.setHeight(400);
        s1.setActualName("carrot2");
        s1.setName("carrot2");
        s1.setImageName("carrot");
        s1.setVisible(true);
        s1.setClickable(false);
        s1.setMovable(false);
        shapes.add(s1);


        Shape s2 = new Shape();
        //s2.setAssociatedImage(getResources().getDrawable(R.drawable.carrot));
        s2.setX(900);
        s2.setY(250);
        s2.setWidth(300);
        s2.setHeight(400);
        s2.setActualName("carrot3");
        s2.setName("carrot3");
        s2.setImageName("carrot");
        s2.setVisible(true);
        s2.setClickable(false);
        s2.setMovable(false);
        shapes.add(s2);

        Shape s3 = new Shape();
        //s3.setAssociatedImage(getResources().getDrawable(R.drawable.carrot));
        s3.setX(1400);
        s3.setWidth(300);
        s3.setHeight(400);
        s3.setActualName("carrot4");
        s3.setName("carrot4");
        s3.setImageName("carrot");
        s3.setY(150);
        s3.setVisible(true);
        s3.setClickable(false);
        s3.setMovable(false);
        shapes.add(s3);

        Shape s4 = new Shape();
        s4.setText("You win!!! Yaay!");
        s4.setX(880);
        s4.setY(700);
        s4.setShapeScript("on enter play victory;");
        s4.setClickable(false);
        s4.setMovable(false);
        shapes.add(s4);



        String pageName = st.nextPageId("DefaultGame");
        Page pg = new Page(pageName);
        for (Shape s : shapes) {
            System.out.println("Shapes on 5:  is  " + s.getName());
            pg.addShape(s);
        }
        defaultGame.listOfPages.add(pg);
        //defaultGame.onRefresh();

    }


    public static Game getCurrentGame() {
        return defaultGame;
    }

    public static void stackGame() {
    }

    public static Object deepClone(Object object) {
        return null;
    }

    private static byte[] serializeGame(Game game) {
        return null;
    }
}
