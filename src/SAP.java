import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.eclipse.collections.api.iterator.MutableIntIterator;
import org.eclipse.collections.impl.list.mutable.primitive.BooleanArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

public class SAP {
	
	private double axisX, axisY;
	private List<DiscBody> bodies;
	private DoubleArrayList bounds;
	private BooleanArrayList boundTypes; // false is min, true is max
	private IntArrayList boundBodyIndices; // index of each bound's body in bodies
	private Set<BodyIndexPair> overlaps, addedOverlaps, removedOverlaps;
	
	public SAP(double axisX, double axisY, DiscBody[] bodies) {
		
		this.axisX = axisX;
		this.axisY = axisY;
		this.bodies = new ArrayList<DiscBody>(Arrays.asList(bodies));
		
		bounds = new DoubleArrayList();
		boundTypes = new BooleanArrayList();
		boundBodyIndices = new IntArrayList();

		bounds.addAll(new double[2*bodies.length]);
		boundTypes.addAll(new boolean[2*bodies.length]);
		boundBodyIndices.addAll(new int[2*bodies.length]);
		
		for (int i=0; i<bodies.length; i++) {
			boundTypes.set(2*i+1, true);
			boundBodyIndices.set(2*i, i);
			boundBodyIndices.set(2*i+1, i);
		}
		
		updateBounds(0);
		
		Integer[] indices = IntStream.range(0, bounds.size()).boxed().toArray(Integer[]::new);
		Arrays.sort(indices, (i,j) -> new Double(bounds.get(i)).compareTo(bounds.get(j)));
		
		DoubleArrayList oldBounds = DoubleArrayList.newList(bounds);
		BooleanArrayList oldBoundTypes = BooleanArrayList.newList(boundTypes);
		IntArrayList oldBoundBodyIndices = IntArrayList.newList(boundBodyIndices);
				
		for (int i=0; i<indices.length; i++) {
			int j = indices[i];
			bounds.set(i, oldBounds.get(j));
			boundTypes.set(i, oldBoundTypes.get(j));
			boundBodyIndices.set(i, oldBoundBodyIndices.get(j));
		}

		overlaps = new UnifiedSet<BodyIndexPair>();
		
		IntHashSet activeBodyIndices = new IntHashSet();
		for (int i=0; i<bounds.size(); i++) {
			if (boundTypes.get(i))
				activeBodyIndices.remove(boundBodyIndices.get(i));
			else {
				int bodyIndexA = boundBodyIndices.get(i);
				MutableIntIterator iter = activeBodyIndices.intIterator();
				while (iter.hasNext())
					overlaps.add(new BodyIndexPair(bodyIndexA, iter.next()));
				activeBodyIndices.add(bodyIndexA);
			}
		}

		addedOverlaps = new UnifiedSet<BodyIndexPair>();
		removedOverlaps = new UnifiedSet<BodyIndexPair>();

	}
	
	public Set<BodyIndexPair> getOverlaps() {
		return overlaps;
	}

	public Set<BodyIndexPair> getAddedOverlaps() {
		return addedOverlaps;
	}

	public Set<BodyIndexPair> getRemovedOverlaps() {
		return removedOverlaps;
	}
	
	public void updateBounds(double timestep) {
		
		final int boundCount = bounds.size();
		for (int i=0; i<boundCount; i++) {
			DiscBody b = bodies.get(boundBodyIndices.get(i));
			double bPos = axisX*b.getPosX() + axisY*b.getPosY();
			double bPosChange = timestep*(axisX*b.getVelX() + axisY*b.getVelY());
			if (bPosChange > 0) {
				if (boundTypes.get(i))
					bounds.set(i, bPos + b.getRadius() + bPosChange);
				else
					bounds.set(i, bPos - b.getRadius());
			}
			else {
				if (boundTypes.get(i))
					bounds.set(i, bPos + b.getRadius());
				else
					bounds.set(i, bPos - b.getRadius() + bPosChange);
			}
		}
		
	}
	
	public void sweep() {
		
		addedOverlaps.clear();
		removedOverlaps.clear();
		final int boundCount = bounds.size();
		for (int i=1; i<boundCount; i++) {
			int rightIndex = i;
			for (int leftIndex = i-1; leftIndex >= 0; leftIndex--) {
				if (bounds.get(leftIndex) <= bounds.get(rightIndex))
					break;
				if (boundTypes.get(leftIndex) ^ boundTypes.get(rightIndex)) {
					BodyIndexPair overlap = new BodyIndexPair(
							boundBodyIndices.get(leftIndex),
							boundBodyIndices.get(rightIndex)
							);
					if (overlaps.add(overlap))
						addedOverlaps.add(overlap);
					else {
						overlaps.remove(overlap);
						removedOverlaps.add(overlap);
					}
				}
				doubleSwap(bounds, leftIndex, rightIndex);
				booleanSwap(boundTypes, leftIndex, rightIndex);
				intSwap(boundBodyIndices, leftIndex, rightIndex);
				rightIndex--;
			}
		}
		
	}
	
	private static void doubleSwap(DoubleArrayList a, int i, int j) {
		final double cache = a.get(i);
		a.set(i, a.get(j));
		a.set(j, cache);
	}
	
	private static void intSwap(IntArrayList a, int i, int j) {
		final int cache = a.get(i);
		a.set(i, a.get(j));
		a.set(j, cache);
	}
	
	private static void booleanSwap(BooleanArrayList a, int i, int j) {
		final boolean cache = a.get(i);
		a.set(i, a.get(j));
		a.set(j, cache);
	}
	
}