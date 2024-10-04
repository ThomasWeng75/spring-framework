/*
 * Copyright 2012-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.servlet;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class RecursivePrinter {
	public void printCurrentObject() {
		printObject(this, 0, new HashSet<>()); // "this" refers to the current instance
	}

	public static void printObject(Object obj)
	{
		printObject(obj,1,new HashSet<>());
	}
	public static void printObject(Object obj, int indentLevel, Set<Object> visitedObjects) {
		if (obj == null) {
			System.out.println("null");
			return;
		}

		// Directly handle primitives, wrappers, and strings
		if (isPrimitiveOrWrapper(obj.getClass())) {
			System.out.println(obj);
			return;
		}

		// If the object has already been visited, avoid cyclic recursion
		if (visitedObjects.contains(obj)) {
			System.out.println("(Cyclic reference detected)");
			return;
		}

		// Mark the object as visited
		visitedObjects.add(obj);

		Class<?> objClass = obj.getClass();
		Field[] fields = objClass.getDeclaredFields();

		// Handle indentation for nested objects
		String indent = "  ".repeat(indentLevel);

		System.out.println(indent + objClass.getSimpleName() + " {");

		for (Field field : fields) {
			// Ignore static fields
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}

			// Apply setAccessible(true) for non-String fields
			if (!String.class.equals(field.getType())) {
				field.setAccessible(true); // Make private fields accessible
			}

			try {
				Object value = field.get(obj);

				System.out.print(indent + "  " + field.getName() + ": ");

				if (value == null) {
					System.out.println("null");
				} else if (value.getClass().isArray()) {
					// Handle arrays
					int length = Array.getLength(value);
					System.out.println(indent + "  Array[" + length + "]:");
					for (int i = 0; i < length; i++) {
						printObject(Array.get(value, i), indentLevel + 1, visitedObjects);
					}
				} else if (value instanceof Collection) {
					// Handle collections
					Collection<?> collection = (Collection<?>) value;
					System.out.println(indent + "  Collection[" + collection.size() + "]:");
					for (Object element : collection) {
						System.out.print(indent + "  ");
						printObject(element, indentLevel + 1, visitedObjects);
					}
				} else if (value instanceof Map) {
					// Handle maps
					Map<?, ?> map = (Map<?, ?>) value;
					System.out.println(indent + "  Map[" + map.size() + "]:");
					for (Map.Entry<?, ?> entry : map.entrySet()) {
						System.out.print(indent + "  Key: ");
						printObject(entry.getKey(), indentLevel + 1, visitedObjects);
						System.out.print(indent + "  Value: ");
						printObject(entry.getValue(), indentLevel + 1, visitedObjects);
					}
				} else {
					// Recursively print fields of complex objects
					printObject(value, indentLevel + 1, visitedObjects);
				}

			} catch (IllegalAccessException e) {
				System.out.println(indent + "  Error: Unable to access field");
			}
		}

		System.out.println(indent + "}");
	}

	// Helper method to check if a class is primitive or a wrapper (e.g. Integer, Boolean, etc.)
	private static boolean isPrimitiveOrWrapper(Class<?> clazz) {
		return clazz.isPrimitive() ||
				clazz == Boolean.class ||
				clazz == Byte.class ||
				clazz == Character.class ||
				clazz == Short.class ||
				clazz == Integer.class ||
				clazz == Long.class ||
				clazz == Float.class ||
				clazz == Double.class ||
				clazz == String.class;
	}

	public static void main(String[] args) {
		// Example classes to test
		Example example = new Example();
		example.nestedList = Arrays.asList("one", "two", "three");
		example.nestedMap = Map.of("key1", "value1", "key2", "value2");

		// Print object recursively
		printObject(example, 0, new HashSet<>());
		//RecursivePrinter rp = new RecursivePrinter();
		//rp.printCurrentObject();
	}
}

// Example class to test
class Example {
	int number = 42;
	String text = "Hello";
	NestedExample nested = new NestedExample();
	//@Nullable
	@SuppressWarnings("NullAway")
	List<String> nestedList =null;
	//@Nullable
	@SuppressWarnings("NullAway")
	Map<String, String> nestedMap =null;
}

// Nested class
class NestedExample {
	double value = 3.14;
	String name = "Nested";
}
