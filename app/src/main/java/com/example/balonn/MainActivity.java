package com.example.balonn;

import android.content.Intent;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private int tntLeft=0;
    private Scene scene;
    private Camera camera;
    private ModelRenderable bulletRenderable;
    private boolean shouldStartTimer = true;
    private int balloonsLeft = 20;
    boolean countdownBoolean=false;
    private Point point;
    private TextView balloonsLeftTxt;
    private TextView countdown;
    private SoundPool soundPool;
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

        new Thread(()->{
            for (int i = 0;i < 200;i++) {

                int finalI = i;
                runOnUiThread(() -> {

                    Vector3 vector3 = ray.getPoint(finalI * 0.1f);
                    node.setWorldPosition(vector3);

                    Node nodeInContact = scene.overlapTest(node);

                    if (nodeInContact != null) {

                        if(nodeInContact.getName().equals("TNT")){
                            tntLeft--;
                            tnts.remove(vector3);
                            
                        }else if(nodeInContact.getName().equals("Balloon")){
                            balloonsLeft--;
                            balloons.remove(vector3);

                        }



                        if (balloonsLeft == 0) {
                            countDownMode();
                            addBaloonsToScene();
                            startTimer();
                            if(tntLeft==0){
                                addTnt();
                                tntLeft=8;
                            }
                            balloonsLeft = 20;
                        }


                        balloonsLeftTxt.setText("Balloons Left: " + balloonsLeft);
                        scene.removeChild(nodeInContact);


                        soundPool.play(sound, 1f, 1f, 1, 0
                                , 1f);

                    }

                    });

                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                runOnUiThread(() -> scene.removeChild(node));

        }).start();

    }



    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();

        int keyCode = event.getKeyCode();

        switch (keyCode) {

            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_UP) {
                    //TODO

                        shoot();

                }
                return true;



            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_UP) {
                    //TODO

                    shoot();}
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
            } }).start();

    }




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
                        balloons.add(v1);

                        node.setWorldPosition(v1);
                    }
                });
    }
}
