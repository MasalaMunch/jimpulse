import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.eclipse.collections.impl.set.mutable.UnifiedSet;

public class Simulation implements Iterable<DiscBody> {
	
	private List<DiscBody> bodies;
	private double sapAxisX, sapAxisY;
	private SAP sapPara, sapPerp;
	private Set<BodyPair> aabbOverlaps;
	
	public Simulation(DiscBody... bodies) {
		
		this.bodies = new ArrayList<DiscBody>();
		
		//TODO automatically choose optimal axes
		sapAxisX = 1280-280;
		sapAxisY = 720+280;
		double axisSize = Math.sqrt(sapAxisX*sapAxisX + sapAxisY*sapAxisY);
		sapAxisX /= axisSize;
		sapAxisY /= axisSize;
		sapPara = new SAP(sapAxisX, sapAxisY, bodies);
		sapPerp = new SAP(sapAxisY, -1*sapAxisX, bodies);
		
		aabbOverlaps = new UnifiedSet<BodyPair>();

	}
	
	public int size() {
		return bodies.size();
	}
		
	public void advance(double timestep) {
		
		//TODO re-enable parallel() when you're done debugging
		IntStream.range(0, 2).forEach(axis -> {
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

		//TODO design constraint solver
		
		for (DiscBody b : bodies)
			b.advance(timestep);
		
	}

	@Override
	public Iterator<DiscBody> iterator() {
		return bodies.iterator();
	}

}