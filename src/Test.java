import java.util.stream.Stream;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


public class Test extends Application {
	
	private static final double TIMESTEP = 1.0/60.0;
	private static final double RES_X = 1280;
	private static final double RES_Y = 720;
	private static final Color BG_COLOR = Color.BLACK;
	private static final Color DISC_COLOR = Color.WHITE;
	private static final String WINDOW_TITLE = "Jimpulse";
		
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		
		stage.setTitle(WINDOW_TITLE);
		Group root = new Group();
		Scene scene = new Scene(root);
		stage.setScene(scene);
		Canvas canvas = new Canvas(RES_X, RES_Y);	
		root.getChildren().add(canvas);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		
		int howManyDiscs = 500;
		double velRange = 10;
		DiscBody[] aLot = new DiscBody[howManyDiscs];
		for (int i=0; i<aLot.length; i++) {
			aLot[i] = new DiscBody(Math.random()*RES_X, Math.random()*RES_Y);
			aLot[i].setVelX(Math.random()*velRange * (Math.random()>0.5? 1:-1));
			aLot[i].setVelY(Math.random()*velRange * (Math.random()>0.5? 1:-1));
		}
		
		Simulation sim = new Simulation(aLot);
		
		new AnimationTimer() {
			@Override
			public void handle(long currentNanoTime) {
				gc.setFill(BG_COLOR);
				gc.fillRect(0, 0, RES_X, RES_Y);
				sim.advance(TIMESTEP);
				gc.setFill(DISC_COLOR);
				for (DiscBody db : sim)				
					gc.fillOval(db.getPosX(), db.getPosY(), 2*db.getRadius(), 2*db.getRadius());	
			}
		}.start();
		
		stage.show();

	}
	
	public static void println(Object... args) {
		print(args);
		System.out.println();
	}
	
	public static void print(Object... args) {
		int i = 0;
		for (Object o : args) {
			System.out.print(o);
			if (++i < args.length)
				System.out.print(" ");
		}
	}
		
}
