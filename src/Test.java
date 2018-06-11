import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.lang.ref.WeakReference;

public class Test extends Application {
	
	private static final boolean FIX_TIMESTEP = false;
	private static final double TIMESTEP_FIX = 1.0/60.0;
	private static final double RES_X = 1280;
	private static final double RES_Y = 720;
	private static final Color FPS_COLOR = Color.WHITE;
	private static final Color BG_COLOR = Color.BLACK;
	private static final Color DISC_COLOR = Color.PURPLE;
	private static final String WINDOW_TITLE = "jimpulse";
		
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
		
		int bodyCount = 0;
		double velRange = 10;
		double accelRange = 50;
		double radiusRange = 10;
		DiscBody[] bodies = new DiscBody[bodyCount];
		for (int i=0; i<bodies.length; i++) {
			bodies[i] = new DiscBody(Math.random()*RES_X, Math.random()*RES_Y);
			bodies[i].setVelX(Math.random()*velRange * (Math.random()>0.5? 1:-1));
			bodies[i].setVelY(Math.random()*velRange * (Math.random()>0.5? 1:-1));
			bodies[i].setRadius(Math.random()*radiusRange);
			bodies[i].setAccelY(Math.random()*accelRange * (Math.random()>0.5? 1:-1));
//			bodies[i].setAccelY(200); // gravity
			bodies[i].setAccelX(Math.random()*accelRange * (Math.random()>0.5? 1:-1));
		}
		Simulation sim = new Simulation(bodies);

		forceGarbageCollection();
		
		new AnimationTimer() {
			
			long[] frameTimes = new long[100];
			int frameTimeIndex = 0;
			boolean arrayFilled = false;
			long prevNanoTime = System.nanoTime();
			
			@Override
			public void handle(long currNanoTime) {
				
				gc.setFill(BG_COLOR);
				gc.fillRect(0, 0, RES_X, RES_Y);
				
				DiscBody newDb = new DiscBody(Math.random()*RES_X, Math.random()*RES_Y);
//				newDb.setAccelY(200); // gravity
//				newDb.setVelY(-500); // initial jump before gravity pulls it down
				newDb.setRadius(Math.random()*radiusRange);
				newDb.setVelX(Math.random()*velRange * (Math.random()>0.5? 1:-1));
				newDb.setVelY(Math.random()*velRange * (Math.random()>0.5? 1:-1));
				newDb.setAccelX(Math.random()*accelRange * (Math.random()>0.5? 1:-1));
				newDb.setAccelY(Math.random()*accelRange * (Math.random()>0.5? 1:-1));
				sim.add(newDb);
				
				if (FIX_TIMESTEP)
					sim.advance(TIMESTEP_FIX);
				else
					sim.advance((currNanoTime-prevNanoTime)/1_000_000_000.0);
								
				gc.setFill(DISC_COLOR);
				for (DiscBody db : sim)
					gc.fillOval(db.getPosX(), db.getPosY(), 2*db.getRadius(), 2*db.getRadius());
			
				long oldFrameTime = frameTimes[frameTimeIndex];
				frameTimes[frameTimeIndex] = currNanoTime;
				frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length;
				if (frameTimeIndex == 0)
					arrayFilled = true;
				if (arrayFilled) {
                    long elapsedNanos = currNanoTime - oldFrameTime ;
                    long elapsedNanosPerFrame = elapsedNanos / frameTimes.length;
                    Integer frameRate = (int) (1_000_000_000.0 / elapsedNanosPerFrame);
    				gc.setFill(FPS_COLOR);
    				gc.fillText(frameRate.toString(), 0, RES_Y);
				}
				
				prevNanoTime = currNanoTime;
				
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
	
	public static void forceGarbageCollection() {
		Object object = new Object();
		final WeakReference<Object> ref = new WeakReference<>(object);
		object = null;
		while (ref.get() != null) {
		  System.gc();
		}
	}
		
}
