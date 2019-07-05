package com.example.balonn;

import android.content.Intent;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.collision.Ray;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.Texture;

import java.net.URI;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private int tntLeft=0;
    private Scene scene;
    private Camera camera;
    private ModelRenderable bulletRenderable;
    private ModelRenderable tntRenderable;
    private boolean shouldStartTimer = true;
    private int balloonsLeft = 20;
    boolean countdownBoolean=false;
    private Point point;
    private TextView balloonsLeftTxt;
    private TextView countdown;
    private SoundPool soundPool;
    int timeLeft;
    private int sound;
    Button bt_restart;
    ArrayList<Vector3> balloons= new ArrayList<>();
    ArrayList<Vector3> tnts= new ArrayList<>();





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Display display=getWindowManager().getDefaultDisplay();
        point=new Point();
        display.getRealSize(point);
        setContentView(R.layout.activity_main);
        loadSoundPool();
        balloonsLeftTxt=findViewById(R.id.balloonsCntTxt);
        bt_restart=findViewById(R.id.restart);
        countdown=findViewById(R.id.countDown);
        countdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countdown.setVisibility(View.INVISIBLE);
                countdownBoolean=true;
            }
        });

        bt_restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent restartIntent = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(restartIntent);
            }
        });

        customArFragment arFragment = (customArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        scene = arFragment.getArSceneView().getScene();
        camera = scene.getCamera();

       addBaloonsToScene();
        countDownMode();
       buildBulletModel();
        Button shoot= findViewById(R.id.shootButton);
        shoot.setOnClickListener(v -> {
            if (shouldStartTimer) {
                startTimer();
                shouldStartTimer=false;
            }
            if(countdownBoolean==true) {
                shoot();
            }


        });







    }




    private void loadSoundPool(){
        AudioAttributes audioAttributes=new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        sound = soundPool.load(this, R.raw.blop_sound, 1);


    }
    private void countDownMode(){
       countdown.setVisibility(View.VISIBLE);
       countdownBoolean=false;
    }

    private void shoot() {
        Ray ray= camera.screenPointToRay(point.x  /2f,point.y /2f);
        Node node= new Node();
        node.setRenderable(bulletRenderable);
        scene.addChild(node);

        Vector3 vec=new Vector3(5,5,5);



        new Thread(()->{
            for (int i = 0;i < 200;i++) {

                int finalI = i;
                runOnUiThread(() -> {

                            Vector3 vector3 = ray.getPoint(finalI * 0.1f);
                            node.setWorldPosition(vector3);


                            Node nodeInContact = scene.overlapTest(node);

                            if (nodeInContact != null) {

                                if (nodeInContact.getName().equals("TNT")) {
                                    Log.d("D","Exploded");
                                    double min1=0;
                                    double min2=0;
                                    Vector3 v1=null;
                                    Vector3 v2=null;
                                    tntLeft--;
                                    tnts.remove(vector3);
                                    scene.removeChild(nodeInContact);
                                    for(int c=0; c<balloons.size();c++){

                                           double minumum=getDistance(balloons.get(c), vector3);
                                           if(checkMinumum(min1, minumum)) {
                                               min1=minumum;
                                              v1=balloons.get(c);
                                           }else if(checkMinumum(min2, minumum )){
                                               v2=balloons.get(c);
                                               min2=minumum;


                                        }}
                                    balloonsLeft--;
                                    balloons.remove(v1);
                                        nodeInContact.setWorldPosition(v1);
                                        scene.removeChild(nodeInContact);
                                        balloonsLeft--;
                                        balloons.remove(v2);
                                        nodeInContact.setWorldPosition(v2);
                                        scene.removeChild(nodeInContact);


                                    }



                              /*      Node tnt=new Node();

                                    tnt.setRenderable(tntRenderable);
                            for(int c=0; c<balloonsLeft; c++){
                                Vector3 new2=balloons.get(c);
                                min=Vector3.angleBetweenVectors(vector3,new2);
                             */




                           /* float x=vector3.x-2;
                            float y=vector3.y-2;
                                while(x<vector3.x+2){
                                    while(y<vector3.y+2){
                                        vector3.set(x,y,vector3.z);
                                        node.setWorldPosition(vector3);
                                        nodeInContact=scene.overlapTest(node);
                                        if(nodeInContact!=null){
                                            scene.removeChild(nodeInContact);
                                        }
                                        y+=1;
                                    }
                                    x+=1;
                                }*/


                             else if (nodeInContact.getName().equals("Balloon")) {
                                    balloonsLeft--;
                                    balloons.remove(vector3);
                                    Log.d("D","Ballooonnn");

                                    scene.removeChild(nodeInContact);
                                    balloonsLeftTxt.setText("Balloons Left: " + balloonsLeft);

                                }}


                            if (balloonsLeft == 0) {
                                startTimer();
                                countDownMode();
                                addBaloonsToScene();
                                startTimer();
                                if (tntLeft == 0) {
                                    addTnt();
                                    tntLeft = 8;
                                }
                                balloonsLeft = 20;
                            }






                        soundPool.play(sound, 1f, 1f, 1, 0
                                , 1f);

                    }

                    );

                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                runOnUiThread(() -> scene.removeChild(node));

        }).start();

    }
    public double getDistance(Vector3 v1, Vector3 v2){
        double i;
        i=Math.sqrt(((v1.x-v2.x)*(v1.x-v2.x)+((v1.y-v2.y)*(v1.y-v2.y))+((v1.z-v2.z)*(v1.z-v2.z))));
        return i;
    }
    public boolean checkMinumum(double min1, double minumum){
        if(min1==0){

            return true;
        }else if(min1>minumum){

            return true;
        }
        return  false;
    }





    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();

        int keyCode = event.getKeyCode();

        switch (keyCode) {

            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_UP) {
                    //TODO
                    if(countdownBoolean==true) {
                        shoot();
                    }
                }
                return true;



            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_UP) {
                    //TODO
                    if(countdownBoolean==true) {
                    shoot();}}
                return true;

            default:
                return super.dispatchKeyEvent(event);
        }}

    private void startTimer(){
        TextView timer=findViewById(R.id.timerText);
        new Thread(()->{
            int seconds = 0;
            while (balloonsLeft > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
                seconds++;

                int minutesPassed = seconds / 60;
                int secondsPassed = seconds % 60;

                runOnUiThread(() -> timer.setText(minutesPassed + ":" + secondsPassed));

            }

        }).start();


    }


    /*private void buildExplosion(){
        Texture
                .builder()
                .setSource(this,R.drawable.desktop)
                .build()
                .thenAccept(texture -> ) {

                    tntRenderable=ShapeFactory
                            .



        }
    }
*/
    private void buildBulletModel() {

        Texture
                .builder()
                .setSource(this, R.drawable.texture)
                .build()
                .thenAccept(texture -> {


                    MaterialFactory
                            .makeOpaqueWithTexture(this, texture)
                            .thenAccept(material -> {

                                bulletRenderable = ShapeFactory
                                        .makeSphere(0.01f,
                                                new Vector3(0f, 0f, 0f),
                                                material);

                            });


                });

    }

    private void addTnt(){
        ModelRenderable
                .builder()
                .setSource(this, Uri.parse("model.sfb"))
                .build()
                .thenAccept(renderable->{
                    for(int i=0; i<7;i++){
                        Node node=new Node();
                        node.setRenderable(renderable);
                        scene.addChild(node);
                        node.setName("TNT");

                        Random random= new Random();
                        int x=random.nextInt(10);
                        int y= random.nextInt(10);
                        int z=random.nextInt(15);

                        z=-z;
                        Vector3 v1  =new Vector3( (float) x,
                                y/10f,
                                (float) z);
                        tnts.add(v1);
                        node.setWorldPosition(v1
                        );
                    }
                });
    }




    private void addBaloonsToScene() {
        ModelRenderable
                .builder()
                .setSource(this, Uri.parse("balloon.sfb"))
                .build()
                .thenAccept(renderable->{
                    for(int i=0; i<20;i++){
                        Node node=new Node();
                        node.setRenderable(renderable);
                        scene.addChild(node);
                        node.setName("Balloon");

                        Random random= new Random();
                        int x=random.nextInt(10);
                        int y= random.nextInt(10);
                        int z=random.nextInt(15);


                        z=-z;
                        Vector3 v1  =new Vector3( (float) x,
                                y/10f,
                                (float) z);
                        balloons.add( v1);

                        node.setWorldPosition(v1);
                    }
                });
    }
}
