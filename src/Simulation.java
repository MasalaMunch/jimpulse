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
		
		sapParaBoundTypes = new BooleanArrayList();
		sapParaBoundBodies = new IntArrayList();
		sapPerpBoundTypes = new BooleanArrayList();
		sapPerpBoundBodies = new IntArrayList();
		
		boolean[] boundTypes = new boolean[2*bodies.length];
		int[] boundBodies = new int[2*bodies.length];		
		for(int i=0; i<bodies.length; i++) {
			boundTypes[2*i+1] = true;
			boundBodies[2*i] = i;
			boundBodies[2*i+1] = i;
		}
		
		sapParaBoundTypes.addAll(boundTypes);
		sapParaBoundBodies.addAll(boundBodies);
		sapPerpBoundTypes.addAll(boundTypes);
		sapPerpBoundBodies.addAll(boundBodies);
		
		sapAxisX = 128;
		sapAxisY = 72;
		
		double axisSize = Math.sqrt(sapAxisX*sapAxisX + sapAxisY*sapAxisY);
		sapAxisX /= axisSize;
		sapAxisY /= axisSize;
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
	
	public void initSapBounds(double timestep) {
		
		sapParaBounds = new DoubleArrayList();
		sapPerpBounds = new DoubleArrayList();
	
		double[] bounds = new double[2*bodies.size()];
		
		sapParaBounds.addAll(bounds);
		sapPerpBounds.addAll(bounds);
		
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

	}
	
	public void initSapOverlaps() {
		
		sapParaOverlaps = new HashSet<Object>();
		sapPerpOverlaps = new HashSet<Object>();
		
		helpInitSapOverlaps(sapParaOverlaps, sapParaBounds, sapParaBoundTypes, sapParaBoundBodies);
		helpInitSapOverlaps(sapPerpOverlaps, sapPerpBounds, sapPerpBoundTypes, sapPerpBoundBodies);
		
	}
	
	private static void helpInitSapOverlaps(HashSet<Object> overlaps, DoubleArrayList bounds, BooleanArrayList boundTypes, IntArrayList boundBodies) {
		
		Integer[] indices = IntStream.range(0, bounds.size()).boxed().toArray(Integer[]::new);
		Arrays.parallelSort(indices, (i,j) -> new Double(bounds.get(i)).compareTo(bounds.get(j)));
		
		DoubleArrayList oldBounds = DoubleArrayList.newList(bounds);
		BooleanArrayList oldBoundTypes = BooleanArrayList.newList(boundTypes);
		IntArrayList oldBoundBodies = IntArrayList.newList(boundBodies);
		
		int j;
		for (int i=0; i<indices.length; i++) {
			j = indices[i];
			bounds.set(i, oldBounds.get(j));
			boundTypes.set(i, oldBoundTypes.get(j));
			boundBodies.set(i, oldBoundBodies.get(j));
		}
		
		int body1, body2;
		MutableIntIterator iter;
		IntHashSet activeBodies = new IntHashSet();
		for (int i=0; i<bounds.size(); i++) {
			if (boundTypes.get(i))
				activeBodies.remove(boundBodies.get(i));
			else {
				body1 = boundBodies.get(i);
				iter = activeBodies.intIterator();
				while(iter.hasNext()) {
					body2 = iter.next();
					overlaps.add(body1*body2 + body1 + body2);
				}
				activeBodies.add(body1);
			}
		}
		
	}
	
	public void advance(double timestep) {
		
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
		Test.println(overlapCount);
		
		//TODO clean up code
		//TODO handle overlaps
		
		bodies.parallelStream().forEach(b->b.advance(timestep, sapAxisX, sapAxisY));
			
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
