import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.IntStream;

import org.eclipse.collections.impl.set.mutable.UnifiedSet;

public class Simulation implements Iterable<DiscBody> {
	
	private Set<DiscBody> bodies;
	private double sapAxisX, sapAxisY;
	private SAP sapPara, sapPerp;
	private Set<BodyPair> aabbOverlaps;
	
	public Simulation(DiscBody... bodies) {
		
		this.bodies = new UnifiedSet<DiscBody>();
		
		//TODO automatically choose optimal axis
		sapAxisX = 1280-280;
		sapAxisY = 720+280;
		double axisSize = Math.sqrt(sapAxisX*sapAxisX + sapAxisY*sapAxisY);
		sapAxisX /= axisSize;
		sapAxisY /= axisSize;
		sapPara = new SAP(sapAxisX, sapAxisY);
		sapPerp = new SAP(sapAxisY, -1*sapAxisX);
		
		aabbOverlaps = new UnifiedSet<BodyPair>();
		
		addAll(Arrays.asList(bodies));
				
	}
		
	public void add(DiscBody body) {
		bodies.add(body);
		sapPara.add(body);
		sapPerp.add(body);
	}
	
	public void addAll(Collection<DiscBody> bodies) {
		this.bodies.addAll(bodies);
		sapPara.addAll(bodies);
		sapPerp.addAll(bodies);
	}
	
	//TODO removal mutators
	//TODO sapAxis mutator

	public void advance(double timestep) {
		
		IntStream.range(0, 2).parallel().forEach(axis -> {
			if (axis == 0)
				sapPara.updateOverlaps(timestep);
			else
				sapPerp.updateOverlaps(timestep);
		});
		
		for (BodyPair overlap : sapPara.getRemovedOverlaps())
			aabbOverlaps.remove(overlap);
		for (BodyPair overlap : sapPerp.getRemovedOverlaps())
			aabbOverlaps.remove(overlap);
		
		for (BodyPair overlap : sapPara.getAddedOverlaps()) {
			if (sapPerp.getOverlaps().contains(overlap))
				aabbOverlaps.add(overlap);
		}
		for (BodyPair overlap : sapPerp.getAddedOverlaps()) {
			if (sapPara.getOverlaps().contains(overlap))
				aabbOverlaps.add(overlap);
		}
				
		Test.println(bodies.size(), "bodies", aabbOverlaps.size(), "overlaps");
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