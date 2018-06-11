import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.eclipse.collections.impl.set.mutable.UnifiedSet;

public class Simulation implements Iterable<DiscBody> {
	
	private List<DiscBody> bodies;
	private double sapAxisX, sapAxisY;
	private SAP sapPara, sapPerp;
	private Set<BodyIndexPair> aabbOverlaps;
	
	public Simulation(DiscBody... bodies) {
		
		this.bodies = new ArrayList<DiscBody>(Arrays.asList(bodies));
		
		//TODO automatically choose optimal axis
		sapAxisX = 1280-280;
		sapAxisY = 720+280;
		double axisSize = Math.sqrt(sapAxisX*sapAxisX + sapAxisY*sapAxisY);
		sapAxisX /= axisSize;
		sapAxisY /= axisSize;
		
		sapPara = new SAP(sapAxisX, sapAxisY, bodies);
		sapPerp = new SAP(sapAxisY, -1*sapAxisX, bodies);
		
		aabbOverlaps = new UnifiedSet<BodyIndexPair>();
		
		for (BodyIndexPair overlap : sapPara.getOverlaps()) {
			if (sapPerp.getOverlaps().contains(overlap))
				aabbOverlaps.add(overlap);
		}
		
	}
	
	//TODO bodies mutators
	//TODO sapAxis mutator

	public void advance(double timestep) {
		
		sapPara.updateBounds(timestep);
		sapPerp.updateBounds(timestep);

		IntStream.range(0, 2).parallel().forEach(axis -> {
			if (axis == 0)
				sapPara.updateOverlaps();
			else
				sapPerp.updateOverlaps();
		});
		
		for (BodyIndexPair overlap : sapPara.getRemovedOverlaps())
			aabbOverlaps.remove(overlap);
		for (BodyIndexPair overlap : sapPerp.getRemovedOverlaps())
			aabbOverlaps.remove(overlap);
		
		for (BodyIndexPair overlap : sapPara.getAddedOverlaps()) {
			if (sapPerp.getOverlaps().contains(overlap))
				aabbOverlaps.add(overlap);
		}
		for (BodyIndexPair overlap : sapPerp.getAddedOverlaps()) {
			if (sapPara.getOverlaps().contains(overlap))
				aabbOverlaps.add(overlap);
		}
				
//		Test.println(aabbOverlaps.size());
//		Test.println(sapPara.getAddedOverlaps().size(), sapPerp.getAddedOverlaps().size());
//		Test.println(sapPara.getRemovedOverlaps().size(), sapPerp.getRemovedOverlaps().size());
//		Test.println();

		//TODO design constraint solver
		
		for (DiscBody b : bodies)
			b.advance(timestep);
		
	}

	@Override
	public Iterator<DiscBody> iterator() {
		return bodies.iterator();
	}

}