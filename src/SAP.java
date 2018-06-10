import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.IntStream;
import org.eclipse.collections.api.iterator.MutableIntIterator;
import org.eclipse.collections.impl.list.mutable.primitive.BooleanArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

public class SAP {
	
	private double axisX, axisY;
	private ArrayList<DiscBody> bodies;
	private DoubleArrayList bounds;
	private BooleanArrayList boundTypes; // false is min, true is max
	private IntArrayList boundBodyIndices; // index of each bound's body in bodies
	private IntHashSet overlaps, addedOverlaps, removedOverlaps;
	private boolean storeOverlapBodyIndices;
	private HashMap<Integer,Integer> overlapBodyIndicesA, overlapBodyIndicesB;
	
	public SAP(double axisX, double axisY,
			   boolean storeOverlapBodyIndices, DiscBody[] bodies) {
		
		this.axisX = axisX;
		this.axisY = axisY;
		this.storeOverlapBodyIndices = storeOverlapBodyIndices;
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
		IntArrayList oldBoundBodies = IntArrayList.newList(boundBodyIndices);
				
		for (int i=0; i<indices.length; i++) {
			int j = indices[i];
			bounds.set(i, oldBounds.get(j));
			boundTypes.set(i, oldBoundTypes.get(j));
			boundBodyIndices.set(i, oldBoundBodies.get(j));
		}

		overlaps = new IntHashSet();
		if (storeOverlapBodyIndices) {
			overlapBodyIndicesA = new HashMap<Integer,Integer>();
			overlapBodyIndicesB = new HashMap<Integer,Integer>();
		}
		
		IntHashSet activeBodyIndices = new IntHashSet();
		for (int i=0; i<bounds.size(); i++) {
			if (boundTypes.get(i))
				activeBodyIndices.remove(boundBodyIndices.get(i));
			else {
				int bodyIndexA = boundBodyIndices.get(i);
				MutableIntIterator iter = activeBodyIndices.intIterator();
				while(iter.hasNext()) {
					int bodyIndexB = iter.next();
					int overlap = DiscBodyPair.hashCode(bodyIndexA, bodyIndexB);
					overlaps.add(overlap);
					if (storeOverlapBodyIndices) {
						overlapBodyIndicesA.put(overlap, bodyIndexA);
						overlapBodyIndicesB.put(overlap, bodyIndexB);
					}
				}
				activeBodyIndices.add(bodyIndexA);
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
	
	public HashMap<Integer,Integer> getOverlapBodyIndicesA() {
		return overlapBodyIndicesA;
	}
	
	public HashMap<Integer,Integer> getOverlapBodyIndicesB() {
		return overlapBodyIndicesB;
	}

	public void updateBounds(double timestep) {
		
		for (int i=0; i<bounds.size(); i++) {
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
		
		int iterCount = 0;
		for (int i=1; i<bounds.size(); i++) {
			int rightIndex = i;
			int rightBodyIndex = boundBodyIndices.get(rightIndex);
			double right = bounds.get(rightIndex);
			for (int leftIndex = rightIndex-1; leftIndex >= 0; leftIndex--) {
				if (bounds.get(leftIndex) <= right)
					break;
				if (boundTypes.get(leftIndex) ^ boundTypes.get(rightIndex)) {
					iterCount++;
					int leftBodyIndex = boundBodyIndices.get(leftIndex);
					int overlap = DiscBodyPair.hashCode(leftBodyIndex, rightBodyIndex);
					if (overlaps.add(overlap)) {
						addedOverlaps.add(overlap);
						if (storeOverlapBodyIndices) {
							overlapBodyIndicesA.put(overlap, leftBodyIndex);
							overlapBodyIndicesB.put(overlap, rightBodyIndex);
						}
					}
					else {
						overlaps.remove(overlap);
						removedOverlaps.add(overlap);
						if (storeOverlapBodyIndices) {
							overlapBodyIndicesA.remove(overlap);
							overlapBodyIndicesB.remove(overlap);
						}
					}
				}
				doubleSwap(bounds, leftIndex, rightIndex);
				booleanSwap(boundTypes, leftIndex, rightIndex);
				intSwap(boundBodyIndices, leftIndex, rightIndex);
				rightIndex--;
			}
		}
//		Test.println("iterCount", iterCount);
		
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