package alex.domain.misc;


public class Wrapper<T> {
	public T data;

	public Wrapper(T data) {
		this.data = data;
	}

	public static <T> Wrapper<T> wrap(T data) {
		return new Wrapper<>(data);
	}
}
