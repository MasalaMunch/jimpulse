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
	private double sapAxisX, sapAxisY;
	private HashSet<Object> sapParaOverlaps, sapPerpOverlaps; //TODO finalize implementation
	private DoubleArrayList sapParaBounds, sapPerpBounds;
	private BooleanArrayList sapParaBoundTypes, sapPerpBoundTypes; // false is min, true is max
	private IntArrayList sapParaBoundBodies, sapPerpBoundBodies; // index of each bound's body in this.bodies
	
	public Simulation(DiscBody... bodies) {
		
		this.bodies = new ArrayList<DiscBody>(Arrays.asList(bodies));
		
		sapParaBounds = new DoubleArrayList();
		sapParaBoundTypes = new BooleanArrayList();
		sapParaBoundBodies = new IntArrayList();
		sapPerpBounds = new DoubleArrayList();
		sapPerpBoundTypes = new BooleanArrayList();
		sapPerpBoundBodies = new IntArrayList();
		
		double[] bounds = new double[2*bodies.length];
		
		boolean[] boundTypes = new boolean[bounds.length];
		int[] boundBodies = new int[bounds.length];		
		for(int i=0; i<bodies.length; i++) {
			boundTypes[2*i+1] = true;
			boundBodies[2*i] = i;
			boundBodies[2*i+1] = i;
		}
		
		sapParaBounds.addAll(bounds);
		sapParaBoundTypes.addAll(boundTypes);
		sapParaBoundBodies.addAll(boundBodies);
		sapPerpBounds.addAll(bounds);
		sapPerpBoundTypes.addAll(boundTypes);
		sapPerpBoundBodies.addAll(boundBodies);
				
		sapParaOverlaps = new HashSet<Object>();
		sapPerpOverlaps = new HashSet<Object>();
		
		sapAxisX = 128;
		sapAxisY = 72;
		
	}
	
	//TODO bodies mutators
	//TODO sapAxis mutator

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
	
	//TODO
	private void initializeOverlaps(double timestep) {
		
	}
	
	public void advance(double timestep) {
		
		bodies.parallelStream().forEach(b->b.updateSapBounds(timestep, sapAxisX, sapAxisY));
		
		IntStream.range(0, 2*bodies.size()).parallel().forEach(i -> {

			if (sapParaBoundTypes.get(i))
				sapParaBounds.set(i, bodies.get(sapParaBoundBodies.get(i)).getSapParaMaxBound());
			else
				sapParaBounds.set(i, bodies.get(sapParaBoundBodies.get(i)).getSapParaMinBound());
			
			if (sapPerpBoundTypes.get(i))
				sapPerpBounds.set(i, bodies.get(sapPerpBoundBodies.get(i)).getSapPerpMaxBound());
			else
				sapPerpBounds.set(i, bodies.get(sapPerpBoundBodies.get(i)).getSapPerpMinBound());
		
		});
		
		IntStream.range(0, 2).parallel().forEach(axis -> {
			
			HashSet<Object> overlaps;
			DoubleArrayList bounds;
			BooleanArrayList boundTypes;
			IntArrayList boundBodies;
			
			if (axis == 0) {
				overlaps = sapParaOverlaps;
				bounds = sapParaBounds;
				boundTypes = sapParaBoundTypes;
				boundBodies = sapParaBoundBodies;
			}
			else { // axis == 1
				overlaps = sapPerpOverlaps;
				bounds = sapPerpBounds;
				boundTypes = sapPerpBoundTypes;
				boundBodies = sapPerpBoundBodies;
			}
			
			int rightI, leftI;
			double right;
			for (int i=1; i<bounds.size(); i++) {
				rightI = i;
				right = bounds.get(rightI);
				for (leftI = rightI-1; leftI >= 0; leftI--) {
					if (bounds.get(leftI) <= right)
						break;
					if (boundTypes.get(leftI) ^ boundTypes.get(rightI)) {
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
						
		});
		
		long overlapCount = sapParaOverlaps.parallelStream().filter(x->sapPerpOverlaps.contains(x)).count();
//		Test.println(overlapCount);
		
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
