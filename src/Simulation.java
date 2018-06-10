import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.collections.api.iterator.MutableIntIterator;
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.set.mutable.UnifiedSet;

public class Simulation implements Iterable<DiscBody> {
	
	private ArrayList<DiscBody> bodies;
	private double sapAxisX, sapAxisY;
	private SAP sapPara, sapPerp;
	private IntObjectHashMap<DiscBodyPair> bodyPairObjects;
	private HashMap<Integer,Integer> bodyPairIndicesA, bodyPairIndicesB;
	private UnifiedSet<DiscBodyPair> aabbOverlaps;
	
	public Simulation(DiscBody... bodies) {
		
		this.bodies = new ArrayList<DiscBody>(Arrays.asList(bodies));
				
		sapAxisX = 1280;
		sapAxisY = 720;
		
		double axisSize = Math.sqrt(sapAxisX*sapAxisX + sapAxisY*sapAxisY);
		sapAxisX /= axisSize;
		sapAxisY /= axisSize;
		
		sapPara = new SAP(sapAxisX, sapAxisY, true, bodies);
		bodyPairIndicesA = sapPara.getOverlapBodyIndicesA();
		bodyPairIndicesB = sapPara.getOverlapBodyIndicesB();
		sapPerp = new SAP(sapAxisY, -1*sapAxisX, false, bodies);
		
		bodyPairObjects = new IntObjectHashMap<DiscBodyPair>();
		aabbOverlaps = new UnifiedSet<DiscBodyPair>();
		
		sapPara.getOverlaps().forEach(overlap -> {
			if (sapPerp.getOverlaps().contains(overlap))
				addAabbOverlap(overlap);
		});
		
	}
	
	private void addAabbOverlap(int bodyPairHash) {
		if (!bodyPairObjects.contains(bodyPairHash)) {
			if (!bodyPairIndicesA.containsKey(bodyPairHash)) {
				Test.println("fuck", bodyPairHash);
			}
			bodyPairObjects.put(
					bodyPairHash,
					new DiscBodyPair(
							bodyPairIndicesA.get(bodyPairHash),
							bodyPairIndicesB.get(bodyPairHash)
							)
					);
		}
		aabbOverlaps.add(bodyPairObjects.get(bodyPairHash));
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
		
		Test.println(bodyPairIndicesA.size(), bodyPairIndicesB.size());
		
		sapPara.getRemovedOverlaps().each(overlap -> {
			aabbOverlaps.remove(bodyPairObjects.get(overlap));
		});
		sapPerp.getRemovedOverlaps().each(overlap -> {
			aabbOverlaps.remove(bodyPairObjects.get(overlap));
		});
//		Test.println("addedOverlaps", sapPara.getAddedOverlaps().size()+sapPerp.getAddedOverlaps().size());
//		sapPara.getAddedOverlaps().each(overlap -> {
//			if (sapPerp.getOverlaps().contains(overlap))
//				addAabbOverlap(overlap);
//		});
//		sapPerp.getAddedOverlaps().each(overlap -> {
//			if (sapPara.getOverlaps().contains(overlap))
//				addAabbOverlap(overlap);
//		});
		MutableIntIterator iter = sapPara.getAddedOverlaps().intIterator();
		while (iter.hasNext()) {
			int overlap = iter.next();
			if (sapPerp.getOverlaps().contains(overlap))
				addAabbOverlap(overlap);
		}
		iter = sapPerp.getAddedOverlaps().intIterator();
		while (iter.hasNext()) {
			int overlap = iter.next();
			if (sapPara.getOverlaps().contains(overlap))
				addAabbOverlap(overlap);
		}
		
//		Test.println(aabbOverlaps.size());
//		Test.println(sapPara.getAddedOverlaps().size(), sapPerp.getAddedOverlaps().size());
//		Test.println(sapPara.getRemovedOverlaps().size(), sapPerp.getRemovedOverlaps().size());
//		Test.println();

		//TODO design constraint solver
		
		for (DiscBody b : bodies)
			b.advance(timestep);

	}

}