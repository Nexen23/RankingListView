package alex.domain.util;


import junit.framework.Assert;

import java.util.LinkedList;
import java.util.ListIterator;

public class ArrayUtil {
	private static LinkedList objects = new LinkedList();

	/**
	 * Sorts in ascending order
	 */
	public static <E extends Comparable<E>> void SortPartiallySortedList(LinkedList<E> list) {
		if (list.isEmpty()) return;
		//noinspection unchecked
		LinkedList<E> unsortedElements = objects;

		ListIterator<E> iterator = list.listIterator();
		E previous = iterator.next(), current;

		// Remove all unsorted elements
		boolean wasElementRemoved;
		do {
			wasElementRemoved = false;
			while (iterator.hasNext()) {
				current = iterator.next();
				if (current.compareTo(previous) < 0) {
					unsortedElements.add(current);
					iterator.remove();
					wasElementRemoved = true;
				}
			}
		} while (wasElementRemoved);
		Assert.assertFalse(list.isEmpty());

		// Add unsorted elements in sorted order
		while (!unsortedElements.isEmpty()) {
			E elementToAdd = unsortedElements.pollFirst();

			// Add first
			if (elementToAdd != null && list.getFirst().compareTo(elementToAdd) > 0) {
				list.addFirst(elementToAdd);
				elementToAdd = null;
			}

			// Add last
			if (elementToAdd != null && list.getLast().compareTo(elementToAdd) < 0) {
				list.addLast(elementToAdd);
				elementToAdd = null;
			}

			// Add in the middle
			iterator = list.listIterator();
			iterator.next(); // skip first
			while (iterator.hasNext() && elementToAdd != null) {
				current = iterator.next();
				if (elementToAdd.compareTo(current) < 0) {
					iterator.previous();
					iterator.add(elementToAdd);
					elementToAdd = null;
				}
			}
		}
	}
}
