import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

public class Simulation implements Iterable<DiscBody> {
	
	private ArrayList<DiscBody> bodies;
	private double sapAxisX, sapAxisY;
	private SAP sapPara, sapPerp;
	private IntHashSet aabbOverlaps;
	
	public Simulation(DiscBody... bodies) {
		
		this.bodies = new ArrayList<DiscBody>(Arrays.asList(bodies));
				
		sapAxisX = 1280;
		sapAxisY = 720;
		
		double axisSize = Math.sqrt(sapAxisX*sapAxisX + sapAxisY*sapAxisY);
		sapAxisX /= axisSize;
		sapAxisY /= axisSize;
		
		sapPara = new SAP(sapAxisX, sapAxisY, bodies);
		sapPerp = new SAP(sapAxisY, -1*sapAxisX, bodies);
		
		aabbOverlaps = new IntHashSet();
		
		sapPara.getOverlaps().forEach(pair -> {
			if (sapPerp.getOverlaps().contains(pair))
				aabbOverlaps.add(pair);
		});
		
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
			aabbOverlaps.remove(pair);
		});
		sapPerp.getRemovedOverlaps().forEach(pair -> {
			aabbOverlaps.remove(pair);
		});
		
		sapPara.getAddedOverlaps().forEach(pair -> {
			if (sapPerp.getOverlaps().contains(pair))
				aabbOverlaps.add(pair);
		});
		sapPerp.getAddedOverlaps().forEach(pair -> {
			if (sapPara.getOverlaps().contains(pair))
				aabbOverlaps.add(pair);
		});
		
		Test.println(aabbOverlaps.size());

		//TODO how can the single-threaded bottleneck that is sweep() be divided into independent subtasks (spatial partitioning, multi-SAP, shellsort, etc)
		//TODO design constraint solver
		
		for (DiscBody b : bodies)
			b.advance(timestep);

	}

}