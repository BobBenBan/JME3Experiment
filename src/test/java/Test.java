import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Test implements Savable {
	List<Test> tests = new ArrayList<>();
	
	public Test() {
		int a = 4;
		Savable[] savables = this.tests.toArray(Savable[]::new);
		for (var en : AnEnum.values()) {
			System.out.println(en);
			if (en.aClass.isInstance(a)) {
				System.out.println("yay");
			}
		}
	}
	
	public static void main(String[] args) {
//		org.bensnonorg.musicmachine.musicmachine.Test.test();
	}
	
	@Override
	public void write(JmeExporter ex) throws IOException {
	}
	
	@Override
	public void read(JmeImporter im) throws IOException {
	
	}
	
	enum AnEnum {
		A(int.class), B(double.class), C(float.class), D(void.class);
		public final Class<?> aClass;
		
		AnEnum(Class<?> aClass) {
			this.aClass = aClass;
		}
	}
}
