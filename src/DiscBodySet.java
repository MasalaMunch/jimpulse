import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DiscBodySet implements Iterable<DiscBody> {
	
	private ArrayList<DiscBody> bodies;
	private double[] bounds;
	private int[] boundBodies;
	private boolean[] boundTypes; // false is min, true is max
	private HashMap<PointPair, Boolean> pointPairs;
	
	public DiscBodySet(DiscBody... bodies) {
		
		this.bodies = new ArrayList<DiscBody>(Arrays.asList(bodies));
		
		bounds = new double[2*bodies.length];
		boundBodies = new int[2*bodies.length];
		boundTypes = new boolean[2*bodies.length];
		
		IntStream.range(0, bodies.length).parallel().forEach(i -> {
			boundBodies[2*i] = i;
			boundBodies[2*i+1] = i;
			boundTypes[2*i] = false;
			boundTypes[2*i+1] = true;
			});
				
	}

	@Override
	public Iterator<DiscBody> iterator() {
		return bodies.iterator();
	}
	
//	public Stream<DiscBody> stream() {
//		return bodies.stream();
//	}
//	
//	public Stream<DiscBody> parallelStream() {
//		return bodies.parallelStream();
//	}
	
	public void advance(double timestep) {
		
		bodies.parallelStream().forEach(b->b.updateBounds(timestep));
		
		// solve constraints
		
		IntStream.range(0, bounds.length).parallel().forEach(i -> {
			if (boundTypes[i])
				bounds[i] = bodies.get(boundBodies[i]).getMaxY();
			else
				bounds[i] = bodies.get(boundBodies[i]).getMinY();
			});
		
		//TODO finish
				
		bodies.parallelStream().forEach(b->b.advance(timestep));
		
	}
	
}
