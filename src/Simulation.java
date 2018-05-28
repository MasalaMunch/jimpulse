import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.IntStream;
import org.eclipse.collections.api.iterator.MutableIntIterator;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

public class Simulation implements Iterable<DiscBody> {
	
	private ArrayList<DiscBody> bodies;
	private IntHashSet overlapsX, overlapsY;
	//TODO use primitive ArrayLists from eclipse.collections instead of Arrays?
	private double[] boundsX;
	private int[] boundBodiesX;
	private boolean[] boundTypesX; // false is min, true is max
	
	public Simulation(DiscBody... bodies) {
		
		this.bodies = new ArrayList<DiscBody>(Arrays.asList(bodies));
		
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
	
	private void initializeOverlaps(double timestep) {
		
		Integer[] boundIndices = IntStream.range(0, bounds.length).parallel().boxed().toArray(Integer[]::new);
		Arrays.parallelSort(boundIndices, (i,j) -> new Double(bounds[i]).compareTo(bounds[j]));
		
		double[] newBounds = new double[bounds.length];
		int[] newBoundBodies = new int[boundBodies.length];
		boolean[] newBoundTypes = new boolean[boundTypes.length];
		
		IntStream.range(0, boundIndices.length).parallel().forEach(i -> {
			final int j = boundIndices[i];
			newBounds[i] = bounds[j];
			newBoundBodies[i] = boundBodies[j];
			newBoundTypes[i] = boundTypes[j];
		});
		
		bounds = newBounds;
		boundBodies = newBoundBodies;
		boundTypes = newBoundTypes;
		
		activePairs = new IntHashSet();
		final int bodyCount = bounds.length/2;
		IntHashSet activeBodies = new IntHashSet();
		int body1;
		MutableIntIterator iter;
		for (int i=0; i<bounds.length-1; i++) {
			if (boundTypes[i])
				activeBodies.remove(boundBodies[i]);
			else {
				body1 = boundBodies[i];
				iter = activeBodies.intIterator();
				while (iter.hasNext())
					activePairs.add(pairIndex(body1, iter.next(), bodyCount));
				activeBodies.add(body1);
			}
		}
	}
	
	public void advance(double timestep) {
		
		if (overlapsX == null)
			bodies.parallelStream().forEach(b->b.updateBounds(timestep));
		
		IntStream.range(0, boundsX.length).parallel().forEach(i -> {
			if (boundTypesX[i])
				boundsX[i] = bodies.get(boundBodiesX[i]).getMaxX();
			else
				boundsX[i] = bodies.get(boundBodiesX[i]).getMinX();
			});
		 
		//TODO filter X in parallel with Y
		
		if (overlapsX == null)
			initializeOverlaps(timestep);
		else {
			
			final int bodyCount = bounds.length/2;
			int rightI, leftI, pairIndex;
			double right;
			for (int i=1; i<bounds.length; i++) {
				rightI = i;
				right = bounds[rightI];
				for (leftI = rightI-1; leftI >= 0; leftI--) {
					if (bounds[leftI] <= right)
						break;
					if (boundTypes[leftI] ^ boundTypes[rightI]) {
						pairIndex = pairIndex(boundBodies[leftI], boundBodies[rightI], bodyCount);
						if (!activePairs.add(pairIndex))
							activePairs.remove(pairIndex);
					}
					doubleSwap(bounds, leftI, rightI);
					intSwap(boundBodies, leftI, rightI);
					booleanSwap(boundTypes, leftI, rightI);
					rightI--;
				}
			}
		
		}
	
		
//		Test.println(activePairs.size());
						
		//TODO form islands
		
		//TODO solve islands
		
		bodies.parallelStream().forEach(b->b.advance(timestep));
		
	}
	
	//TODO write reverse function for gong from pairIndex to bodyIndices
	
	//TODO figure out when this produces negative values as a result of int overflow
	private static int pairIndex(int body1, int body2, int bodyCount) {	
		final int minBody = Math.min(body1, body2);
		return triangularNumber(minBody+1)
			   + (bodyCount-2-minBody)*minBody
			   + (minBody==body1? body2 : body1)
			   - minBody - 2;
	}
	
	private static int triangularNumber(int n) {
		return n*(n+1)/2;
	}

	private static void doubleSwap(double[] a, int i, int j) {
		final double tmp = a[i];
		a[i] = a[j];
		a[j] = tmp;
	}
	
	private static void intSwap(int[] a, int i, int j) {
		final int tmp = a[i];
		a[i] = a[j];
		a[j] = tmp;
	}
	
	private static void booleanSwap(boolean[] a, int i, int j) {
		final boolean tmp = a[i];
		a[i] = a[j];
		a[j] = tmp;
	}
	
}
