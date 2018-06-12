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
	private boolean newBodiesExist, oldBodiesExist;
	private Set<DiscBody> newBodies, oldBodies;
	
	public SAP(double axisX, double axisY) {
		
		this.axisX = axisX;
		this.axisY = axisY;
		
		overlaps = new UnifiedSet<BodyPair>();
		addedOverlaps = new UnifiedSet<BodyPair>();
		removedOverlaps = new UnifiedSet<BodyPair>();
		
		bounds = new DoubleArrayList();
		boundTypes = new BooleanArrayList();
		boundBodies = new ArrayList<DiscBody>();
		
		newBodiesExist = false;
		oldBodiesExist = false;
		
		newBodies = new UnifiedSet<DiscBody>();
		oldBodies = new UnifiedSet<DiscBody>();

	}
	
	public void add(DiscBody body) {
		if (!oldBodies.remove(body)) {
			newBodies.add(body);
			newBodiesExist = true;
		}
	}
	
	public void remove(DiscBody body) {
		if (!newBodies.remove(body)) {
			oldBodies.add(body);
			oldBodiesExist = true;
		}
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
		
		if (oldBodiesExist)
			removeOldBodies(timestep);
				
		if (newBodiesExist)
			addNewBodies(timestep);
		
	}
	
	private void removeOldBodies(double timestep) {
		
		for (DiscBody body : oldBodies) {
			
			double min = body.getBound(timestep, axisX, axisY, false);
			int minIndex = getIndex(body, min);
			
			double max = body.getBound(timestep, axisX, axisY, true);
			int maxIndex = minIndex+1;
			while (bounds.get(maxIndex) == min)
				maxIndex++;
			while (bounds.get(maxIndex) != max) {
				BodyPair overlap = new BodyPair(
						body, boundBodies.get(maxIndex)
						);
				overlaps.remove(overlap);
				removedOverlaps.add(overlap);
				maxIndex++;
			}
			while (boundBodies.get(maxIndex) != body)
				maxIndex++;
			
			bounds.removeAtIndex(minIndex);
			boundTypes.removeAtIndex(minIndex);
			boundBodies.remove(minIndex);
			maxIndex--;
			bounds.removeAtIndex(maxIndex);
			boundTypes.removeAtIndex(maxIndex);
			boundBodies.remove(maxIndex);
			
		}
		
		oldBodies.clear();
		oldBodiesExist = false;
		
	}
	
	private void addNewBodies(double timestep) {
		
		for (DiscBody body : newBodies) {
			
			double min = body.getBound(timestep, axisX, axisY, false);
			int minIndex = bounds.binarySearch(min);
			boolean uniqueMin = (minIndex < 0);
			if (uniqueMin)
				minIndex = -1*(minIndex+1);
			bounds.addAtIndex(minIndex, min);
			boundTypes.addAtIndex(minIndex, false);
			boundBodies.add(minIndex, body);
			
			int maxIndex = minIndex+1;
			if (!uniqueMin) {
				maxIndex++;
				while (maxIndex < bounds.size() && bounds.get(maxIndex) == min)
					maxIndex++;
			}
			double max = body.getBound(timestep, axisX, axisY, true);
			while (maxIndex < bounds.size() && bounds.get(maxIndex) < max) {
				BodyPair overlap = new BodyPair(
						body, boundBodies.get(maxIndex)
						);
				overlaps.add(overlap);
				addedOverlaps.add(overlap);
				maxIndex++;
			}
			bounds.addAtIndex(maxIndex, max);
			boundTypes.addAtIndex(maxIndex, true);
			boundBodies.add(maxIndex, body);
			
		}
		
		newBodies.clear();
		newBodiesExist = false;	

	}
	
	private int getIndex(DiscBody body, double bound) {
		
		final int bs = bounds.binarySearch(bound);
		if (boundBodies.get(bs) == body)
			return bs;
		
		int upper = bs + 1;
		while (bounds.get(upper) == bound) {
			if (boundBodies.get(upper) == body)
				return upper;
			upper++;
		}
		int lower = bs - 1;
		while (bounds.get(lower) == bound) {
			if (boundBodies.get(lower) == body)
				return lower;
			lower--;
		}
		
		return -1;
		
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