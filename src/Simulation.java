import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.IntStream;
import org.eclipse.collections.api.iterator.MutableIntIterator;
import org.eclipse.collections.impl.list.mutable.primitive.BooleanArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

public class Simulation implements Iterable<DiscBody> {
	
	private ArrayList<DiscBody> bodies;
	private HashSet<Object> overlapsX, overlapsY; //TODO finalize pair representation
	private DoubleArrayList boundsX, boundsY;
	private IntArrayList boundBodiesX, boundBodiesY;
	private BooleanArrayList boundTypesX, boundTypesY; // false is min, true is max
	
	public Simulation(DiscBody... bodies) {
		
		this.bodies = new ArrayList<DiscBody>(Arrays.asList(bodies));
		
		boundsX = new DoubleArrayList();
		boundsY = new DoubleArrayList();
		boundBodiesX = new IntArrayList();
		boundBodiesY = new IntArrayList();
		boundTypesX = new BooleanArrayList();
		boundTypesY = new BooleanArrayList();
				
		double[] bounds = new double[2*bodies.length];
		
		int[] boundBodies = new int[bounds.length];
		boolean[] boundTypes = new boolean[bounds.length];
		for(int i=0; i<bodies.length; i++) {
			boundBodies[2*i] = i;
			boundBodies[2*i+1] = i;
			boundTypes[2*i+1] = true;
		}
		
		boundsX.addAll(bounds);
		boundsY.addAll(bounds);
		boundBodiesX.addAll(boundBodies);
		boundBodiesY.addAll(boundBodies);
		boundTypesX.addAll(boundTypes);
		boundTypesY.addAll(boundTypes);
		
		overlapsX = new HashSet<Object>();
		overlapsY = new HashSet<Object>();
				
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
	
	private void initializeOverlaps(double timestep) {
		
	}
	
	public void advance(double timestep) {
		
		bodies.parallelStream().forEach(b->b.updateBounds(timestep));
		
		IntStream.range(0, 2*bodies.size()).parallel().forEach(i -> {
			
			if (boundTypesX.get(i))
				boundsX.set(i, bodies.get(boundBodiesX.get(i)).getMaxX());
			else
				boundsX.set(i, bodies.get(boundBodiesX.get(i)).getMinX());
			
			if (boundTypesY.get(i))
				boundsY.set(i, bodies.get(boundBodiesY.get(i)).getMaxY());
			else
				boundsY.set(i, bodies.get(boundBodiesY.get(i)).getMinY());
			
		});
		
		IntStream.range(0, 2).parallel().forEach(axis -> {
			
			HashSet<Object> overlaps;
			DoubleArrayList bounds;
			IntArrayList boundBodies;
			BooleanArrayList boundTypes;
			
			if (axis == 0) {
				overlaps = overlapsX;
				bounds = boundsX;
				boundBodies = boundBodiesX;
				boundTypes = boundTypesX;
			}
			else { // axis == 1
				overlaps = overlapsY;
				bounds = boundsY;
				boundBodies = boundBodiesY;
				boundTypes = boundTypesY;
			}
			
			int iterCount = 0;
			int rightI, leftI;
			double right;
			for (int i=1; i<bounds.size(); i++) {
				rightI = i;
				right = bounds.get(rightI);
				for (leftI = rightI-1; leftI >= 0; leftI--) {
					iterCount++;
					if (bounds.get(leftI) <= right)
						break;
					if (boundTypes.get(leftI) ^ boundTypes.get(rightI)) {
						//TODO finalize pair representation
						Integer pair = boundBodies.get(leftI)*boundBodies.get(rightI) + boundBodies.get(leftI) + boundBodies.get(rightI);
						if (!overlaps.add(pair))
							overlaps.remove(pair);
					}
					doubleSwap(bounds, leftI, rightI);
					intSwap(boundBodies, leftI, rightI);
					booleanSwap(boundTypes, leftI, rightI);
					rightI--;
				}
			}
			Test.println(axis, iterCount);
		});
		
		/*
		 * TODO only do one sweep on a customizable axis
		 */
		Test.println();
		Test.println(overlapsX.size(), overlapsY.size());
		Test.println();

		bodies.parallelStream().forEach(b->b.advance(timestep));
			
	}
	
	private static void doubleSwap(DoubleArrayList a, int i, int j) {
		final double tmp = a.get(i);
		a.set(i, a.get(j));
		a.set(j, tmp);
	}
	
	private static void intSwap(IntArrayList a, int i, int j) {
		final int tmp = a.get(i);
		a.set(i, a.get(j));
		a.set(j, tmp);
	}
	
	private static void booleanSwap(BooleanArrayList a, int i, int j) {
		final boolean tmp = a.get(i);
		a.set(i, a.get(j));
		a.set(j, tmp);
	}
	
}
