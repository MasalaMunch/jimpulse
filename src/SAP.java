import java.util.ArrayList;
import java.util.Arrays;
import org.eclipse.collections.impl.list.mutable.primitive.BooleanArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.DoubleArrayList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

public class SAP {
	
	private double axisX, axisY;
	private ArrayList<DiscBody> bodies;
	private DoubleArrayList bounds;
	private BooleanArrayList boundTypes; // false is min, true is max
	private IntArrayList boundBodyIndices;
	
	public SAP(double axisX, double axisY, DiscBody[] bodies) {
		
		this.axisX = axisX;
		this.axisY = axisY;
		this.bodies = new ArrayList<DiscBody>(Arrays.asList(bodies));
		
		bounds = new DoubleArrayList();
		boundTypes = new BooleanArrayList();
		boundBodyIndices = new IntArrayList();
		
		bounds.addAll(new double[2*bodies.length]);
		boundTypes.addAll(new boolean[2*bodies.length]);
		boundBodyIndices.addAll(new int[2*bodies.length]);
		
		for (int i=0; i<2*bodies.length; i++) {
			boundBodyIndices.set(2*i, i);
			boundBodyIndices.set(2*i+1, i);
			boundTypes.set(2*i+1, true);
		}

	}
	
	public void updateBound(int index, double timestep) {
		
//		DiscBody body = bodies.get(boundBodyIndices.get(index));
//		double pos = axisX*body.getPosX() + axisY*body.getPosY();
//		double posChange = timestep * (axisX*body.getVelX() + axisY*body.getVelY());
//		
//		if (boundTypes.get(index)) {
//			
//			if boundBody.
//			
//		}
//		else {
//			
//		}
//		
		
	}
		
	//TODO mutators for adding/removing bodies and changing axis
	
	

}
