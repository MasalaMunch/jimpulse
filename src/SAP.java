import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.eclipse.collections.impl.list.mutable.primitive.BooleanArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

public class SAP {
	
	private double axisX, axisY;
	private Set<BodyPair> overlaps, addedOverlaps, removedOverlaps;
	private DoubleArrayList bounds;
	private BooleanArrayList boundTypes; // false is min, true is max
	private List<DiscBody> boundBodies;
	private int boundCount;
	public SAP(double axisX, double axisY, DiscBody... bodies) {
		
		this.axisX = axisX;
		this.axisY = axisY;
				
		bounds = new DoubleArrayList();
		boundTypes = new BooleanArrayList();
		boundBodies = new ArrayList<DiscBody>();	
		for (int i=0; i<bodies.length; i++) {
			bounds.addAll(0, 0);
			boundTypes.addAll(false, true);
			boundBodies.add(bodies[i]);
			boundBodies.add(bodies[i]);
		}
		boundCount = bounds.size();
		updateBounds(0);
		
		Integer[] indices = IntStream.range(0, boundCount).boxed().toArray(Integer[]::new);
		Arrays.sort(indices, (i,j) -> new Double(bounds.get(i)).compareTo(bounds.get(j)));
		DoubleArrayList oldBounds = DoubleArrayList.newList(bounds);
		BooleanArrayList oldBoundTypes = BooleanArrayList.newList(boundTypes);
		ArrayList<DiscBody> oldBoundBodies = new ArrayList<DiscBody>(boundBodies);
		for (int i=0; i<indices.length; i++) {
			int j = indices[i];
			bounds.set(i, oldBounds.get(j));
			boundTypes.set(i, oldBoundTypes.get(j));
			boundBodies.set(i, oldBoundBodies.get(j));
		}
		
		overlaps = new HashSet<BodyPair>();
		HashSet<DiscBody> activeBodies = new HashSet<DiscBody>();
		for (int i=0; i<boundCount; i++) {
			if (boundTypes.get(i))
				activeBodies.remove(boundBodies.get(i));
			else {
				DiscBody bodyA = boundBodies.get(i);
				for (DiscBody bodyB : activeBodies) {
					overlaps.add(new BodyPair(bodyA, bodyB));
				}
				activeBodies.add(bodyA);
			}
		}
		
		addedOverlaps = new HashSet<BodyPair>();
		removedOverlaps = new HashSet<BodyPair>();
				
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
		
		updateBounds(timestep);
				
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
	
	private void updateBounds(double timestep) {
		for (int i=0; i<boundCount; i++)
			bounds.set(i, boundBodies.get(i).getBound(
					timestep, axisX, axisY, boundTypes.get(i))
					);		
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