import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.eclipse.collections.api.iterator.MutableIntIterator;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.list.mutable.primitive.BooleanArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

public class Simulation implements Iterable<DiscBody> {
	
	private ArrayList<DiscBody> bodies;
	private MutableIntSet heaven;
	private MutableIntIntMap purgatory;
	private MutableIntIntMap candidates;
	private double sapAxisX, sapAxisY;
	private SAP sapPara, sapPerp;
	
	private class SAP {
		
		private IntHashSet overlaps;
		private DoubleArrayList bounds;
		private BooleanArrayList boundTypes; // false is min, true is max
		private IntArrayList boundBodies; // index of each bound's body in bodies
		private boolean para;
		
		private SAP(boolean para) {
			
			this.para = para;
			
			bounds = new DoubleArrayList();
			boundTypes = new BooleanArrayList();
			boundBodies = new IntArrayList();
			
			bounds.addAll(new double[2*bodies.size()]);
			boundTypes.addAll(new boolean[bounds.size()]);
			boundBodies.addAll(new int[bounds.size()]);
			
			for (int i=0; i<bodies.size(); i++) {
				boundBodies.set(2*i, i);
				boundBodies.set(2*i+1, i);
				boundTypes.set(2*i+1, true);
			}
			
			for (int i=0; i<bounds.size(); i++)
				updateBound(i);
			
			Integer[] indices = IntStream.range(0, bounds.size()).boxed().toArray(Integer[]::new);
			Arrays.sort(indices, (i,j) -> new Double(bounds.get(i)).compareTo(bounds.get(j)));
			
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
			
			overlaps = new IntHashSet();
			int body1, body2, pair;
			IntHashSet activeBodies = new IntHashSet();
			MutableIntIterator iter;
			for (int i=0; i<bounds.size(); i++) {
				if (boundTypes.get(i))
					activeBodies.remove(boundBodies.get(i));
				else {
					body1 = boundBodies.get(i);
					iter = activeBodies.intIterator();
					while(iter.hasNext()) {
						body2 = iter.next();
						pair = body1*body2 + body1 + body2;
						overlaps.add(pair);
						purgatory.addToValue(pair, 1);
					}
					activeBodies.add(body1);
				}
			}

		}
				
		private void updateBound(int i) {
			
			if (para) {
				if (boundTypes.get(i))
					bounds.set(i, bodies.get(boundBodies.get(i)).getSapParaMaxBound());
				else
					bounds.set(i, bodies.get(boundBodies.get(i)).getSapParaMinBound());
			}
			else {
				if (boundTypes.get(i))
					bounds.set(i, bodies.get(boundBodies.get(i)).getSapPerpMaxBound());
				else
					bounds.set(i, bodies.get(boundBodies.get(i)).getSapPerpMinBound());
			}
			
		}
		
		private void sweep() {
			
			int iterCount = 0;
			
			int rightI, leftI, rightBody, leftBody, pair;
			double right;
			for (int i=1; i<bounds.size(); i++) {
				rightI = i;
				rightBody = boundBodies.get(rightI);
				right = bounds.get(rightI);
				for (leftI = rightI-1; leftI >= 0; leftI--) {
					iterCount++;
					if (bounds.get(leftI) <= right)
						break;
					if (boundTypes.get(leftI) ^ boundTypes.get(rightI)) {
						leftBody = boundBodies.get(leftI);
						pair = leftBody*rightBody + leftBody + rightBody;
						if (overlaps.add(pair))
							candidates.addToValue(pair, 1);
						else {
							overlaps.remove(pair);
							purgatory.addToValue(pair, -1);
							heaven.remove(pair);
						}
					}
					doubleSwap(bounds, leftI, rightI);
					intSwap(boundBodies, leftI, rightI);
					booleanSwap(boundTypes, leftI, rightI);
					rightI--;
				}
			}
			
//			Test.println(para, iterCount);
			
		}

	}
	
	public Simulation(DiscBody... bodies) {
		
		this.bodies = new ArrayList<DiscBody>(Arrays.asList(bodies));
				
		sapAxisX = 1280;
		sapAxisY = 720;
		
		double axisSize = Math.sqrt(sapAxisX*sapAxisX + sapAxisY*sapAxisY);
		sapAxisX /= axisSize;
		sapAxisY /= axisSize;
		
		for (DiscBody b : bodies)
			b.updateSapBounds(0.0, sapAxisX, sapAxisY);
		
		purgatory = new IntIntHashMap().asSynchronized();
		candidates = new IntIntHashMap().asSynchronized();
		
		sapPara = new SAP(true);
		sapPerp = new SAP(false);
		
		heaven = new IntHashSet().asSynchronized();
		purgatory.forEachKeyValue((k,v) -> {
			if (v == 2)
				heaven.add(k);
		});
		
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
	
	public void advance(double timestep) {
		
		bodies.parallelStream().forEach(b->b.updateSapBounds(timestep, sapAxisX, sapAxisY));
		
		IntStream.range(0, 2*bodies.size()).parallel().forEach(i -> {
			sapPara.updateBound(i);
			sapPerp.updateBound(i);
		});
				
		IntStream.range(0, 2).parallel().forEach(axis -> {
			if (axis == 0)
				sapPara.sweep();
			else
				sapPerp.sweep();
		});
		
		candidates.forEachKeyValue((k, v) -> {
			if (purgatory.addToValue(k, v) == 2)
				heaven.add(k);
		});
		candidates.clear();
		
//		Test.println(heaven.size());

		//TODO clean up and modularize code
		//TODO design constraint solver
		
		bodies.parallelStream().forEach(b -> b.advance(timestep));

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