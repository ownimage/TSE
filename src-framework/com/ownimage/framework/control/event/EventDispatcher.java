/**
 * This code is part of the Perception programme.
 * All code copyright (c) 2012, 2013 ownimage.cm, Keith Hart
 */
package com.ownimage.framework.control.event;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.ownimage.framework.util.Framework;
import com.ownimage.framework.util.Version;

public class EventDispatcher<L> {

	public final static Version mVersion = new Version(4, 0, 0, "2014/05/06 20:48");
	@SuppressWarnings("unused")
    private final static Logger mLogger = Framework.getLogger();
	private final Vector<WeakReference<L>> mListeners = new Vector<WeakReference<L>>();
	private final Object mOwner; // this is for debugging purposes so you can tell who owns a dispatcher

	public EventDispatcher(final Object pOwner) {
		mOwner = pOwner;
	}

	public void addListener(final L pListener) {
		removeListener(pListener);
		mListeners.add(new WeakReference<L>(pListener));
	}

	/**
	 * Invokes the function on all of the valid Listeners in the collection.
	 *
	 * @param pFunction
	 *            the function
	 */
	public void invokeAll(final Consumer<L> pFunction) {
		invokeAllExcept(null, pFunction);
	}

	/**
	 * Invokes the function on all of the valid Listeners in the collection except the one specified. This is so that Listener can
	 * change the value without it boomeranging back on them.
	 *
	 * @param pFunction
	 *            the function
	 * @param pListener
	 *            the view that is excluded from having the function invoked on it.
	 */
	public void invokeAllExcept(final L pListener, final Consumer<L> pFunction) {
		if (pFunction == null) {
			throw new IllegalArgumentException("pFunction must not be null.");
		}

		final Iterator<WeakReference<L>> iterator = mListeners.iterator();
		while (iterator.hasNext()) {
			final WeakReference<L> listenerRef = iterator.next();
			final L listener = listenerRef.get();
			if (listener != null) {
				if (listener != pListener) {
					pFunction.accept(listener);
				}
			} else {
				iterator.remove();
			}
		}
	}

	public void removeListener(final L pListener) {
		// using the iterator model as it prevents concurrent modification exceptions
		Iterator<WeakReference<L>> listenerIterator = mListeners.iterator();
		while (listenerIterator.hasNext()) {
			WeakReference<L> listenerRef = listenerIterator.next();
			L listener = listenerRef.get();
			if (listener == null || listener == pListener) {
				listenerIterator.remove();
			}
		}
	}

}
