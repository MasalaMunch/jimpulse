import java.util.stream.Stream;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import static java.util.concurrent.TimeUnit.NANOSECONDS;


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
		
		int howManyBodies = 500;
		double velRange = 50;
		double radius = 20;
		DiscBody[] bodies = new DiscBody[howManyBodies];
		for (int i=0; i<bodies.length; i++) {
			bodies[i] = new DiscBody(Math.random()*RES_X, Math.random()*RES_Y);
			bodies[i].setVelX(Math.random()*velRange * (Math.random()>0.5? 1:-1));
			bodies[i].setVelY(Math.random()*velRange * (Math.random()>0.5? 1:-1));
			bodies[i].setRadius(radius);
		}
		Simulation sim = new Simulation(bodies);
		
//		DiscBody b0 = new DiscBody(0, 0);
//		b0.setVelX(100); b0.setVelX(100);
//		DiscBody b1 = new DiscBody(0, RES_Y);
//		b1.setVelX(100); b1.setVelY(-100);
//		DiscBody b2 = new DiscBody(RES_X, RES_Y);
//		b2.setVelX(-100); b2.setVelY(-100);
//		Simulation sim = new Simulation(b0, b1, b2);
		
		new AnimationTimer() {
			public double frames = 0;
			public long startTime = System.nanoTime();
			@Override
			public void handle(long currentNanoTime) {
				gc.setFill(BG_COLOR);
				gc.fillRect(0, 0, RES_X, RES_Y);
				sim.advance(TIMESTEP);
				gc.setFill(DISC_COLOR);
				for (DiscBody db : sim)				
					gc.fillOval(db.getPosX(), db.getPosY(), 2*db.getRadius(), 2*db.getRadius());
//				frames++;
//				double elapsedTime = NANOSECONDS.toSeconds(currentNanoTime-startTime);
//				println(frames/elapsedTime); //TODO render onto the canvas instead of printing to console
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
