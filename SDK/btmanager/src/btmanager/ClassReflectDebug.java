package btmanager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ClassReflectDebug {

	public static String printClassMethod(Class<?> clsShow) {
		StringBuilder tmp = new StringBuilder();
		try {
			Method[] hideMethod = clsShow.getMethods();
			int i = 0;
			for (; i < hideMethod.length; i++) {
				tmp.append("method name: ");
				tmp.append(hideMethod[i].getName());
				tmp.append("\n");
			}
			Field[] allFields = clsShow.getFields();
			for (i = 0; i < allFields.length; i++) {
				tmp.append("Field name: ");
				tmp.append(allFields[i].getName());
				tmp.append("\n");
			}
		} catch (SecurityException e) {
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tmp.toString();
	}
}
