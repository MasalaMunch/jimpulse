import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.eclipse.collections.impl.set.mutable.UnifiedSet;

public class Simulation {
	
	private ArrayList<DiscBody> bodies;
	private double sapAxisX, sapAxisY;
	private SAP sapPara, sapPerp;
	private UnifiedSet<BodyIndexPair> aabbOverlaps;
	
	public Simulation(DiscBody... bodies) {
		
		this.bodies = new ArrayList<DiscBody>(Arrays.asList(bodies));
				
		sapAxisX = 1280-280;
		sapAxisY = 720+280;
		
		double axisSize = Math.sqrt(sapAxisX*sapAxisX + sapAxisY*sapAxisY);
		sapAxisX /= axisSize;
		sapAxisY /= axisSize;
		
		sapPara = new SAP(sapAxisX, sapAxisY, bodies);
		sapPerp = new SAP(sapAxisY, -1*sapAxisX, bodies);
		
		aabbOverlaps = new UnifiedSet<BodyIndexPair>();
		
		sapPara.getOverlaps().forEach(overlap -> {
			if (sapPerp.getOverlaps().contains(overlap))
				aabbOverlaps.add(overlap);
		});
		
	}
	
	//TODO bodies mutators
	//TODO sapAxis mutator

	public int size() {
		return bodies.size();
	}
	
	public DiscBody get(int i) {
		return bodies.get(i);
	}
	
	public void advance(double timestep) {
		
		sapPara.updateBounds(timestep);
		sapPerp.updateBounds(timestep);

		IntStream.range(0, 2).parallel().forEach(axis -> {
			if (axis == 0)
				sapPara.sweep();
			else
				sapPerp.sweep();
		});
		
		sapPara.getRemovedOverlaps().each(overlap -> {
			aabbOverlaps.remove(overlap);
		});
		sapPerp.getRemovedOverlaps().each(overlap -> {
			aabbOverlaps.remove(overlap);
		});

		sapPara.getAddedOverlaps().each(overlap -> {
			if (sapPerp.getOverlaps().contains(overlap))
				aabbOverlaps.add(overlap);
		});
		sapPerp.getAddedOverlaps().each(overlap -> {
			if (sapPara.getOverlaps().contains(overlap))
				aabbOverlaps.add(overlap);
		});
		
//		Test.println(aabbOverlaps.size());
//		Test.println(sapPara.getAddedOverlaps().size(), sapPerp.getAddedOverlaps().size());
//		Test.println(sapPara.getRemovedOverlaps().size(), sapPerp.getRemovedOverlaps().size());
//		Test.println();

		//TODO design constraint solver
		
		final int bodyCount = bodies.size();
		for (int i=0; i<bodyCount; i++)
			bodies.get(i).advance(timestep);
		
	}

}