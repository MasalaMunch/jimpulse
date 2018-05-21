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
		
		DiscBody d1 = new DiscBody(0, 0);
		d1.setVelX(100); d1.setVelY(100);
		DiscBody d2 = new DiscBody(RES_X, 0);
		d2.setVelX(-100); d2.setVelY(100);
		DiscBodySet simulation = new DiscBodySet(d1, d2);
		
		new AnimationTimer() {
			@Override
			public void handle(long currentNanoTime) {
				
				gc.setFill(BG_COLOR);
				gc.fillRect(0, 0, RES_X, RES_Y);
				
				simulation.advance(TIMESTEP);
				
				gc.setFill(DISC_COLOR);
				for (DiscBody db : simulation)				
					gc.fillOval(db.getPosX(), db.getPosY(), 2*db.getRadius(), 2*db.getRadius());
				
			}
		}.start();
		
		stage.show();

	}
		
}
