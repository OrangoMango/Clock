package com.orangomango.clock;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.layout.StackPane;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.media.AudioClip;

import java.time.ZonedDateTime;

/**
	A simple clock made in JavaFX

	@author OrangoMango [https://orangomango.github.io]
	@version 1.0
*/
public class Clock extends Application{
	private static double WIDTH = 800;
	private static double HEIGHT = 800;
	private static Font FONT = new Font("sans-serif", 70);
	private static Font FONT_13 = new Font("sans-serif", 13);
	private static AudioClip AUDIO = new AudioClip(Clock.class.getResource("/clock-sound.wav").toString());

	@Override
	public void start(Stage stage){
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
		
		Timeline clockSound = new Timeline(new KeyFrame(Duration.seconds(1), e -> AUDIO.play()));
		clockSound.setCycleCount(Animation.INDEFINITE);
		clockSound.play();
		
		stage.widthProperty().addListener((ob, oldV, newV) -> {
			WIDTH = (double)newV;
			canvas.setWidth(WIDTH);
		});
		stage.heightProperty().addListener((ob, oldV, newV) -> {
			HEIGHT = (double)newV;
			canvas.setHeight(HEIGHT);
		});

		stage.setMaximized(true);
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
		stage.setScene(new Scene(pane, WIDTH, HEIGHT));
		stage.show();
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
		gc.fillText(String.format("%02d:%02d:%02d", hours, minutes, seconds), 50, 100);
		gc.setFont(FONT_13);
		gc.fillText("OrangoMango - https://orangomango.github.io - JavaFX clock", 50, HEIGHT-50);
	}
	
	private static void drawTriangle(GraphicsContext gc, double height){
		gc.fillPolygon(new double[]{-height*0.03, 0, height*0.03}, new double[]{height*0.07, -height, height*0.07}, 3);
	}

	public static void main(String[] args){
		launch(args);
	}
}
