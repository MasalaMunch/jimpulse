import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.eclipse.collections.impl.list.mutable.primitive.BooleanArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

public class SAP {
	
	private double axisX, axisY;
	private Set<BodyPair> overlaps, addedOverlaps, removedOverlaps;
	private DoubleArrayList bounds;
	private BooleanArrayList boundTypes; // false is min, true is max
	private List<DiscBody> boundBodies;
	public SAP(double axisX, double axisY, DiscBody... bodies) {
		
		this.axisX = axisX;
		this.axisY = axisY;
		
		overlaps = new UnifiedSet<BodyPair>();
		addedOverlaps = new UnifiedSet<BodyPair>();
		removedOverlaps = new UnifiedSet<BodyPair>();
		
		bounds = new DoubleArrayList();
		boundTypes = new BooleanArrayList();
		boundBodies = new ArrayList<DiscBody>();
		
		//TODO add bodies
		
		
		
	}
	
	public Set<BodyPair> getOverlaps() {
		return overlaps;
	}

	public Set<BodyPair> getAddedOverlaps() {
		return addedOverlaps;
	}

	public Set<BodyPair> getRemovedOverlaps() {
		return removedOverlaps;
	}
	
	public void updateOverlaps(double timestep) {
		
		addedOverlaps.clear();
		removedOverlaps.clear();
		
		final int boundCount = bounds.size();
		
		for (int i=0; i<boundCount; i++)
			bounds.set(i, boundBodies.get(i).getBound(
					timestep, axisX, axisY, boundTypes.get(i))
					);
		
		for (int i=1; i<boundCount; i++) {
			int rightIndex = i;
			for (int leftIndex = i-1; leftIndex >= 0; leftIndex--) {
				if (bounds.get(leftIndex) <= bounds.get(rightIndex))
					break;
				if (boundTypes.get(leftIndex) ^ boundTypes.get(rightIndex)) {
					BodyPair overlap = new BodyPair(
							boundBodies.get(leftIndex),
							boundBodies.get(rightIndex)
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
				Collections.swap(boundBodies, leftIndex, rightIndex);
				rightIndex--;
			}
		}
		
	}
			
	private static void doubleSwap(DoubleArrayList a, int i, int j) {
		final double cache = a.get(i);
		a.set(i, a.get(j));
		a.set(j, cache);
	}
	
	private static void booleanSwap(BooleanArrayList a, int i, int j) {
		final boolean cache = a.get(i);
		a.set(i, a.get(j));
		a.set(j, cache);
	}
	
}