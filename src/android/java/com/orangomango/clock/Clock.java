package com.orangomango.clock;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.layout.StackPane;
import javafx.animation.*;
import javafx.util.Duration;

import java.time.ZonedDateTime;
import java.lang.reflect.*;
import java.io.*;
import java.nio.file.*;

import javafxports.android.FXActivity;
import android.os.Build;
import android.view.View;
import android.os.Vibrator;
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.content.Context;

/**
 A simple clock made in JavaFX

 @author OrangoMango [https://orangomango.github.io]
 @version 1.0
*/
public class Clock extends Application{
	private static double WIDTH;
	private static double HEIGHT;
	private static Font FONT = new Font("sans-serif", 70);
	private static Font FONT_13 = new Font("sans-serif", 13);
	private static final Vibrator vibrator = (Vibrator)FXActivity.getInstance().getSystemService(Context.VIBRATOR_SERVICE);

	static {
		DisplayMetrics displayMetrics = new DisplayMetrics();
		FXActivity.getInstance().getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
		int height = displayMetrics.heightPixels;
		int width = displayMetrics.widthPixels;
		float density = displayMetrics.density;
		WIDTH = width/density;
		HEIGHT = height/density;
	}

	@Override
	public void start(Stage stage) throws Exception{
		if (Build.VERSION.SDK_INT >= 29){
			Method forName = Class.class.getDeclaredMethod("forName", String.class);
			Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
			Class vmRuntimeClass = (Class) forName.invoke(null, "dalvik.system.VMRuntime");
			Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
			Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[] { String[].class} );
			Object vmRuntime = getRuntime.invoke(null);
			setHiddenApiExemptions.invoke(vmRuntime, (Object[])new String[][]{new String[]{"L"}});
		}

		FXActivity.getInstance().runOnUiThread(() -> {
			// Setup fullscreen
			FXActivity.getInstance().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			FXActivity.getInstance().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			WindowManager.LayoutParams lp = FXActivity.getInstance().getWindow().getAttributes();
            		lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
			FXActivity.getInstance().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

			// Clear useless temp files in cache of previous sessions
			for (File f : FXActivity.getInstance().getCacheDir().listFiles()){
				f.delete();
			}
		});

		copyFile("clock-sound.wav");

		stage.setTitle("Clock");
		StackPane pane = new StackPane();
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		pane.getChildren().add(canvas);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		AnimationTimer loop = new AnimationTimer(){
			@Override
			public void handle(long time){
				update(gc);
			}
		};
		loop.start();

		Timeline clockSound = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
			try {
				MediaPlayer mp = new MediaPlayer();
				mp.setDataSource(FXActivity.getInstance().getFilesDir().getAbsolutePath()+"/clock-sound.wav");
                        	mp.prepare();
				mp.setOnCompletionListener(player -> {
                              		player.release();
                        	});
				mp.start();
				vibrator.vibrate(100);
			} catch (IOException ex){
				ex.printStackTrace();
			}
		}));
		clockSound.setCycleCount(Animation.INDEFINITE);
		clockSound.play();

		Scene scene = new Scene(pane, WIDTH, HEIGHT);
		scene.setOnKeyPressed(e -> {
			AudioManager manager = (AudioManager)FXActivity.getInstance().getSystemService(Context.AUDIO_SERVICE);
			switch (e.getCode()){
				case VOLUME_UP:
					manager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
					break;
				case VOLUME_DOWN:
					manager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
					break;
			}
		});
		stage.setScene(scene);
		stage.show();
	}

	public static void copyFile(String name){
 	       File file = new File(FXActivity.getInstance().getFilesDir().getAbsolutePath(), name);
        	if (!file.exists()){
                	try {
                        	Files.copy(Clock.class.getResourceAsStream("/"+name), file.toPath());
        	        } catch (IOException ioe){
                	        ioe.printStackTrace();
                	}
        	}
	}

	private void update(GraphicsContext gc){
		gc.clearRect(0, 0, WIDTH, HEIGHT);
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, WIDTH, HEIGHT);

		final double ws = WIDTH*0.8;
		final double hs = HEIGHT*0.8;
		final double size = ws < hs ? ws : hs;
		final int seconds = ZonedDateTime.now().getSecond();
		final int minutes = ZonedDateTime.now().getMinute();
		final int hours = ZonedDateTime.now().getHour();

		gc.save();
		gc.translate(WIDTH/2, HEIGHT/2);

		gc.setLineWidth(3);
		for (int i = 1; i <= 60; i++){
			double angle = i/60.0*360;
			gc.rotate(angle);
			if (i % 5 == 0){
				gc.setStroke(Color.RED);
				gc.strokeLine(0, -size/2, 0, -size/2*0.85);
			} else {
				gc.setStroke(Color.BLUE);
				gc.strokeLine(0, -size/2, 0, -size/2*0.95);
			}
			gc.rotate(-angle);
		}

		gc.setLineWidth(5);
		gc.setGlobalAlpha(0.65);

		// Draw the hours
		gc.setFill(Color.RED);
		double hoursAngle = hours/12.0*360+minutes/60.0*30+seconds/60.0*0.1;
		gc.rotate(hoursAngle);
		drawTriangle(gc, size/2*0.5);
		gc.rotate(-hoursAngle);

		// Draw the minutes
		gc.setFill(Color.BLUE);
		double minutesAngle = minutes/60.0*360+seconds/60.0*6;
		gc.rotate(minutesAngle);
		drawTriangle(gc, size/2*0.65);
		gc.rotate(-minutesAngle);

		// Draw the seconds
		gc.setFill(Color.WHITE);
		gc.rotate(seconds/60.0*360);
		drawTriangle(gc, size/2*0.8);
		gc.rotate(-seconds/60.0*360);

		gc.restore();

		gc.setLineWidth(3);
		gc.setStroke(Color.WHITE);
		gc.strokeOval(WIDTH/2-size/2, HEIGHT/2-size/2, size, size);
		
		gc.setFill(Color.WHITE);
		gc.setFont(FONT);
		gc.fillText(String.format("%02d:%02d:%02d", hours, minutes, seconds), 20, 100);
		gc.setFont(FONT_13);
		gc.fillText("OrangoMango - https://orangomango.github.io - JavaFX clock", 20, HEIGHT-50);
	}
	
	private static void drawTriangle(GraphicsContext gc, double height){
		gc.fillPolygon(new double[]{-height*0.03, 0, height*0.03}, new double[]{height*0.07, -height, height*0.07}, 3);
	}

	public static void main(String[] args){
		launch(args);
	}
}
