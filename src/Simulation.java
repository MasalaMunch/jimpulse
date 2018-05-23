import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Simulation implements Iterable<DiscBody> {
	
	private ArrayList<DiscBody> bodies;
	boolean[] pairFlags;
//	int[] pairSubseqIndices;
	private double[] bounds;
	private int[] boundBodies;
	private boolean[] boundTypes; // false is min, true is max
	private IntStream boundIndicesParallelStream;
	
	public Simulation(DiscBody... bodies) {
		
		this.bodies = new ArrayList<DiscBody>(Arrays.asList(bodies));
		
		pairFlags = new boolean[bodies.length*(bodies.length-1)/2];
		
		int length = 2*bodies.length;
		bounds = new double[length];
		boundBodies = new int[length];
		boundTypes = new boolean[length];
		IntStream.range(0, bodies.length).parallel().forEach(i -> {
			boundBodies[2*i] = i;
			boundBodies[2*i+1] = i;
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
		
		// TODO solve constraints
		
		bodies.parallelStream().forEach(b->b.updateBounds(timestep));
		
		IntStream.range(0, bounds.length).parallel().forEach(i -> {
			if (boundTypes[i])
				bounds[i] = bodies.get(boundBodies[i]).getMaxY();
			else
				bounds[i] = bodies.get(boundBodies[i]).getMinY();
			});
		
		int i, rightI, leftI, pairI;
		double right, left;
		boolean swapEqualBounds = false;
		//TODO remove swapEqualBounds if bit flipping works
		for (i=1; i<bounds.length; i++) {
			rightI = i;
			right = bounds[rightI];
			for (leftI = rightI-1; leftI >= 0; leftI--) {
				left = bounds[leftI];
				if (left < right)
					break;
				if (left > right || (swapEqualBounds = boundTypes[leftI] && !boundTypes[rightI])) {
					doubleSwap(bounds, leftI, rightI);
					intSwap(boundBodies, leftI, rightI);
					booleanSwap(boundTypes, leftI, rightI);
					if (swapEqualBounds) {
						swapEqualBounds = false;
						break;						
					}
					else {
						pairI = pairIndex(boundBodies[leftI],boundBodies[rightI],bodies.size());
						pairFlags[pairI] = !pairFlags[pairI];
						rightI--;
					}
				}
			}
		}

		//TODO speed up, do this while sorting?
		ArrayList<Integer[]> crossingPairs = new ArrayList<Integer[]>();
		HashSet<Integer> activeBodies = new HashSet<Integer>();
		int bodyIndex;
		for(i=0; i<bounds.length-1; i++) {
			if (boundTypes[i]) {
				activeBodies.remove(boundBodies[i]);
			}
			else {
				bodyIndex = boundBodies[i];
				for (Integer ab : activeBodies)
					crossingPairs.add(new Integer[] {ab, bodyIndex});
				activeBodies.add(bodyIndex);
			}
		}
		
		if (crossingPairs.size() > 0)
			for (Integer[] pair : crossingPairs)
				Test.println(Arrays.toString(pair));
		
		//TODO form islands
		
		//TODO solve islands
		
		bodies.parallelStream().forEach(b->b.advance(timestep));
		
	}

	private static int pairSubseqIndex(int minBody, int bodyCount) {
		int s = triangularNumber(minBody+1)-1;
		int i = 2+minBody;
		return s + (bodyCount-i)*minBody;
	}
	
	private static int pairIndex(int body1, int body2, int bodyCount) {	
		int minBody = Math.min(body1, body2);
		int subseqIndex = pairSubseqIndex(minBody, bodyCount);
		int maxBody = minBody==body1? body2 : body1;
		return subseqIndex + (maxBody-minBody-1);
	}
	
	private static int triangularNumber(int n) {
		return n*(n+1)/2;
	}

	private static void doubleSwap(double[] a, int i, int j) {
		double t = a[i];
		a[i] = a[j];
		a[j] = t;
	}
	
	private static void intSwap(int[] a, int i, int j) {
		int t = a[i];
		a[i] = a[j];
		a[j] = t;
	}
	
	private static void booleanSwap(boolean[] a, int i, int j) {
		boolean t = a[i];
		a[i] = a[j];
		a[j] = t;
	}
	
}
