import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.eclipse.collections.impl.list.mutable.primitive.BooleanArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

public class SAP {
	
	private double axisX, axisY;
	private List<DiscBody> bodies, addedBodies;
	private boolean bodiesWereAdded;
	private Set<BodyIndexPair> overlaps, addedOverlaps, removedOverlaps;
	private DoubleArrayList bounds;
	private BooleanArrayList boundTypes; // false is min, true is max
	private IntArrayList boundBodyIndices;
	
	public SAP(double axisX, double axisY) {
		
		this.axisX = axisX;
		this.axisY = axisY;
		
		bodies = new ArrayList<DiscBody>();
		addedBodies = new ArrayList<DiscBody>();
		
		bodiesWereAdded = false;
		
		overlaps = new UnifiedSet<BodyIndexPair>();
		addedOverlaps = new UnifiedSet<BodyIndexPair>();
		removedOverlaps = new UnifiedSet<BodyIndexPair>();
		
		bounds = new DoubleArrayList();
		boundTypes = new BooleanArrayList();
		boundBodyIndices = new IntArrayList();

	}
	
	public void addAll(List<DiscBody> bodies) {
		addedBodies.addAll(bodies);
		bodiesWereAdded = true;
	}
	
	public void add(DiscBody body) {
		addedBodies.add(body);
		bodiesWereAdded = true;
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
	
	public void updateOverlaps(double timestep) {
		
		addedOverlaps.clear();
		removedOverlaps.clear();
		
		final int boundCount = bounds.size();
		
		for (int i=0; i<boundCount; i++)
			bounds.set(i, bodies.get(boundBodyIndices.get(i)).getBound(
					timestep, axisX, axisY, boundTypes.get(i))
					);
		
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
				
		if (bodiesWereAdded) {
			
			int newBodyIndex = bodies.size();
			for (DiscBody b : addedBodies) {
				
				double min = b.getBound(timestep, axisX, axisY, false);
				int minIndex = bounds.binarySearch(min);
				boolean uniqueMin = minIndex < 0;
				if (uniqueMin)
					minIndex = -1*(minIndex+1);
				bounds.addAtIndex(minIndex, min);
				boundTypes.addAtIndex(minIndex, false);
				boundBodyIndices.addAtIndex(minIndex, newBodyIndex);
				
				int maxIndex = minIndex+1;
				if (!uniqueMin) {
					while (maxIndex < bounds.size() && bounds.get(maxIndex) == min)
						maxIndex++;
				}
				double max = b.getBound(timestep, axisX, axisY, true);
				while (maxIndex < bounds.size() && bounds.get(maxIndex) < max) {
					BodyIndexPair overlap = new BodyIndexPair(
							newBodyIndex, boundBodyIndices.get(maxIndex)
							);
					overlaps.add(overlap);
					addedOverlaps.add(overlap);
					maxIndex++;
				}
				bounds.addAtIndex(maxIndex, max);
				boundTypes.addAtIndex(maxIndex, true);
				boundBodyIndices.addAtIndex(maxIndex, newBodyIndex);
				
				newBodyIndex++;
			}
			bodies.addAll(addedBodies);
			addedBodies = new ArrayList<DiscBody>();
			bodiesWereAdded = false;
			
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