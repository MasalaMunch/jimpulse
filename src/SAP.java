import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;
import org.eclipse.collections.api.iterator.MutableIntIterator;
import org.eclipse.collections.impl.list.mutable.primitive.BooleanArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

public class SAP {
	
	private double axisX;
	private double axisY;
	private ArrayList<DiscBody> bodies;
	private DoubleArrayList bounds;
	private BooleanArrayList boundTypes; // false is min, true is max
	private IntArrayList boundBodies; // index of each bound's body in bodies
	private IntHashSet overlaps;
	private IntHashSet addedOverlaps;
	private IntHashSet removedOverlaps;
	
	public SAP(double axisX, double axisY, DiscBody... bodies) {
		
		this.axisX = axisX;
		this.axisY = axisY;
		this.bodies = new ArrayList<DiscBody>(Arrays.asList(bodies));
		
		bounds = new DoubleArrayList();
		boundTypes = new BooleanArrayList();
		boundBodies = new IntArrayList();

		bounds.addAll(new double[2*bodies.length]);
		boundTypes.addAll(new boolean[2*bodies.length]);
		boundBodies.addAll(new int[2*bodies.length]);
		
		for (int i=0; i<bodies.length; i++) {
			boundTypes.set(2*i+1, true);
			boundBodies.set(2*i, i);
			boundBodies.set(2*i+1, i);
		}
		
		updateBounds(0);
		
		Integer[] indices = IntStream.range(0, bounds.size()).boxed().toArray(Integer[]::new);
		Arrays.sort(indices, (i,j) -> new Double(bounds.get(i)).compareTo(bounds.get(j)));
		
		DoubleArrayList oldBounds = DoubleArrayList.newList(bounds);
		BooleanArrayList oldBoundTypes = BooleanArrayList.newList(boundTypes);
		IntArrayList oldBoundBodies = IntArrayList.newList(boundBodies);
		
		for (int i=0; i<indices.length; i++) {
			int j = indices[i];
			bounds.set(i, oldBounds.get(j));
			boundTypes.set(i, oldBoundTypes.get(j));
			boundBodies.set(i, oldBoundBodies.get(j));
		}

		overlaps = new IntHashSet();
		
		IntHashSet activeBodies = new IntHashSet();
		for (int i=0; i<bounds.size(); i++) {
			if (boundTypes.get(i))
				activeBodies.remove(boundBodies.get(i));
			else {
				int body1 = boundBodies.get(i);
				MutableIntIterator iter = activeBodies.intIterator();
				while(iter.hasNext()) {
					int body2 = iter.next();
					int pair = body1*body2 + body1 + body2;
					overlaps.add(pair);
				}
				activeBodies.add(body1);
			}
		}

		addedOverlaps = new IntHashSet();
		removedOverlaps = new IntHashSet();

	}
	
	public IntHashSet getOverlaps() {
		return overlaps;
	}

	public IntHashSet getAddedOverlaps() {
		return addedOverlaps;
	}

	public IntHashSet getRemovedOverlaps() {
		return removedOverlaps;
	}

	public void updateBounds(double timestep) {
		
		for (int i=0; i<bounds.size(); i++) {
			DiscBody b = bodies.get(boundBodies.get(i));
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
		
		for (int i=1; i<bounds.size(); i++) {
			int rightI = i;
			int rightBody = boundBodies.get(rightI);
			double right = bounds.get(rightI);
			for (int leftI = rightI-1; leftI >= 0; leftI--) {
				if (bounds.get(leftI) <= right)
					break;
				if (boundTypes.get(leftI) ^ boundTypes.get(rightI)) {
					int leftBody = boundBodies.get(leftI);
					int pair = leftBody*rightBody + leftBody + rightBody;
					if (overlaps.add(pair))
						addedOverlaps.add(pair);
					else {
						overlaps.remove(pair);
						removedOverlaps.add(pair);
					}
				}
				doubleSwap(bounds, leftI, rightI);
				booleanSwap(boundTypes, leftI, rightI);
				intSwap(boundBodies, leftI, rightI);
				rightI--;
			}
		}
		
	}
	
	private static void doubleSwap(DoubleArrayList a, int i, int j) {
		double tmp = a.get(i);
		a.set(i, a.get(j));
		a.set(j, tmp);
	}
	
	private static void intSwap(IntArrayList a, int i, int j) {
		int tmp = a.get(i);
		a.set(i, a.get(j));
		a.set(j, tmp);
	}
	
	private static void booleanSwap(BooleanArrayList a, int i, int j) {
		boolean tmp = a.get(i);
		a.set(i, a.get(j));
		a.set(j, tmp);
	}
	
}