import java.util.Arrays;
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
	
	public int size() {
		return bodies.size();
	}
		
	public boolean add(DiscBody body) {
		final boolean out = bodies.add(body);
		if (out) {
			sapPara.add(body);
			sapPerp.add(body);
		}
		return out;
	}
	
	public boolean remove(DiscBody body) {
		final boolean out = bodies.remove(body);
		if (out) {
			sapPara.remove(body);
			sapPerp.remove(body);
		}
		return out;
	}
	
	public boolean addAll(Iterable<DiscBody> bodies) {
		boolean out = false;
		for (DiscBody body : bodies) {
			if (add(body))
				out = true;
		}
		return out;
	}
	
	public boolean removeAll(Iterable<DiscBody> bodies) {
		boolean out = false;
		for (DiscBody body : bodies) {
			if (remove(body))
				out = true;
		}
		return out;
	}
	
	//TODO sapAxis mutator

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
				
//		Test.println(bodies.size(), "bodies", aabbOverlaps.size(), "overlaps");
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