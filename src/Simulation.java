import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;

public class Simulation implements Iterable<DiscBody> {
	
	private ArrayList<DiscBody> bodies;
	private double sapAxisX, sapAxisY;
	private SAP sapPara, sapPerp;
	private IntIntHashMap aabbOverlapMinBodies;
	private IntIntHashMap aabbOverlapMaxBodies;
	private IntArrayList subseqIndices;
	
	public Simulation(DiscBody... bodies) {
		
		this.bodies = new ArrayList<DiscBody>(Arrays.asList(bodies));
				
		sapAxisX = 1280;
		sapAxisY = 720;
		
		subseqIndices = new IntArrayList();
		for (int i=0; i<bodies.length; i++)
			subseqIndices.add(getPairSubseqIndex(i, bodies.length));
		
		double axisSize = Math.sqrt(sapAxisX*sapAxisX + sapAxisY*sapAxisY);
		sapAxisX /= axisSize;
		sapAxisY /= axisSize;
		
		sapPara = new SAP(sapAxisX, sapAxisY, bodies);
		sapPerp = new SAP(sapAxisY, -1*sapAxisX, bodies);
		
		aabbOverlapMinBodies = new IntIntHashMap();
		aabbOverlapMaxBodies = new IntIntHashMap();
		
		sapPara.getOverlaps().forEach(pair -> {
			if (sapPerp.getOverlaps().contains(pair))
				addAabbOverlap(pair);
		});
		
	}
	
	private void addAabbOverlap(int pairIndex) {
		int bs = subseqIndices.binarySearch(pairIndex);
		int minBody = bs<0? -1*bs-2 : bs;
		int maxBody = pairIndex+minBody-subseqIndices.get(minBody)+1;
		aabbOverlapMinBodies.put(pairIndex, minBody);
		aabbOverlapMinBodies.put(pairIndex, maxBody);	
	}
	
	public static int getPairSubseqIndex(int minBody, int bodyCount) {
		int s = triangularNumber(minBody+1)-1;
		int i = 2+minBody;
		return s + (bodyCount-i)*minBody;
	}
	
	private static int triangularNumber(int n) {
		return n*(n+1)/2;
	}
	
	//TODO bodies mutators
	//TODO sapAxis mutator

	@Override
	public Iterator<DiscBody> iterator() {
		return bodies.iterator();
	}
	
	public void advance(double timestep) {
		
		sapPara.updateBounds(timestep);
		sapPerp.updateBounds(timestep);

		sapPara.sweep();
		sapPerp.sweep();
		
		sapPara.getRemovedOverlaps().forEach(pair -> {
			aabbOverlapMinBodies.remove(pair);
			aabbOverlapMaxBodies.remove(pair);
		});
		sapPerp.getRemovedOverlaps().forEach(pair -> {
			aabbOverlapMinBodies.remove(pair);
			aabbOverlapMaxBodies.remove(pair);
		});
		
		sapPara.getAddedOverlaps().forEach(pair -> {
			if (sapPerp.getOverlaps().contains(pair))
				addAabbOverlap(pair);
		});
		sapPerp.getAddedOverlaps().forEach(pair -> {
			if (sapPara.getOverlaps().contains(pair))
				addAabbOverlap(pair);
		});
		
//		Test.println(sapPara.getOverlaps().size(), sapPerp.getOverlaps().size(), aabbOverlapMinBodies.size());
//		Test.println(sapPara.getRemovedOverlaps().size(), sapPerp.getRemovedOverlaps().size());
//		Test.println(sapPara.getAddedOverlaps().size(), sapPerp.getAddedOverlaps().size());
//		Test.println();

		//TODO design constraint solver
		
		for (DiscBody b : bodies)
			b.advance(timestep);

	}

}