package alex.rankinglist.misc;


public class TwoWayList<T> {
	private int size = 0;
	private Node<T> root;

	public TwoWayList() {
	}

	public int getSize() {
		return size;
	}

	public void clear() {
		root = null;
		size = 0;
	}

	public void add(T item) {

	}

	public static class Node<T> {
		Node<T> prev, next;
		T data;

		public Node(T data) {
			this.data = data;
		}
	}
}
