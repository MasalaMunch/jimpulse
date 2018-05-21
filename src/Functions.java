
public class Functions {
	
	public static void println(Object... args) {
		print(args);
		System.out.println();
	}
	
	public static void print(Object... args) {
		int i = 0;
		for (Object o : args) {
			System.out.print(o);
			if (++i < args.length)
				System.out.print(" ");
		}
	}

}
