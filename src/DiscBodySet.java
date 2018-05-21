import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

public class DiscBodySet implements Iterable<DiscBody> {
	
	private ArrayList<DiscBody> bodies;
	
	public DiscBodySet(DiscBody... bodies) {
		this.bodies = new ArrayList<DiscBody>(Arrays.asList(bodies));
	}

	@Override
	public Iterator<DiscBody> iterator() {
		return bodies.iterator();
	}
	
	public Stream<DiscBody> stream() {
		return bodies.stream();
	}
	
	public void advance(double timestep) {
		
		//TODO use a stream
		for (DiscBody db : bodies)
			db.advanceVel(timestep);
		
		//TODO solve constraints
		
		//TODO use a stream
		for (DiscBody db : bodies)
			db.advancePos(timestep);
		
	}
	
}
