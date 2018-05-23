import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.IntStream;

public class Simulation implements Iterable<DiscBody> {
	
	private ArrayList<DiscBody> bodies;
	boolean[] pairFlags;
	private double[] bounds;
	private int[] boundBodies;
	private boolean[] boundTypes; // false is min, true is max
	
	public Simulation(DiscBody... bodies) {
		
		this.bodies = new ArrayList<DiscBody>(Arrays.asList(bodies));
		
		pairFlags = new boolean[triangularNumber(bodies.length)];
		
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
		
		int rightI, leftI, pairI;
		double right;
		for (int i=1; i<bounds.length; i++) {
			rightI = i;
			right = bounds[rightI];
			for (leftI = rightI-1; leftI >= 0; leftI--) {
				if (bounds[leftI] <= right)
					break;
				doubleSwap(bounds, leftI, rightI);
				intSwap(boundBodies, leftI, rightI);
				booleanSwap(boundTypes, leftI, rightI);
				if (boundTypes[leftI] ^ boundTypes[rightI]) {
					pairI = pairIndex(boundBodies[leftI], boundBodies[rightI], bodies.size());
					pairFlags[pairI] ^= true;
				}
				rightI--;
			}
		}
		
		/*TODO decide:
		 * filter the other axis in parallel to this one?
		 * filter the output of this axis check normally?
		 * filter the output of this axis check using a smaller-sweep-and-prune algorithm?
		 */
		
		long howManyOverlaps = IntStream.range(0, pairFlags.length).parallel().filter(x->pairFlags[x]).count();
		Test.println(howManyOverlaps);
		
		//TODO form islands
		
		//TODO solve islands
		
		bodies.parallelStream().forEach(b->b.advance(timestep));
		
	}
	
	//TODO write reverse function for gong from pairIndex to bodyIndices

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
