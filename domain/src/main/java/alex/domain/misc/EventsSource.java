package alex.domain.misc;


import java.util.LinkedList;
import java.util.List;

public class EventsSource<Listener> {
	private List<Listener> listeners = new LinkedList<>();

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	public void clearListeners() {
		listeners.clear();
	}

	public void forEachListener(Actor<Listener> actor) {
		final LinkedList<Listener> listenersClone = new LinkedList<>(listeners);
		for (Listener listener : listenersClone) {
			actor.actOn(listener);
		}
	}

	public interface Actor<Listener> {
		void actOn(Listener listener);
	}
}
