package ru.example.PhotoStream;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

public class EventDispatcher implements IEventDispatcher {
    private class HandlerReference extends WeakReference<IEventHandler> {
        private boolean valid = true;

        public HandlerReference(IEventHandler handler) {
            super(handler);
        }

        public boolean isValid() {
            return this.valid;
        }

        public void invalidate() {
            this.valid = false;
        }
    }

    private int recuirsionCount;
    private LinkedList<HandlerReference> listeners;
    private Stack<HandlerReference> toAdd;

    public EventDispatcher() {
        recuirsionCount = 0;
        listeners = new LinkedList<>();
        toAdd = new Stack<>();
    }

    @Override
    public void addEventListener(IEventHandler listener) {
        if (listener == null) {
            return;
        }
        if (recuirsionCount == 0) {
            listeners.add(new HandlerReference(listener));
        } else {
            toAdd.push(new HandlerReference(listener));
        }
    }

    @Override
    public void removeEventListener(IEventHandler listener) {
        if (listener == null) {
            return;
        }
        Iterator<HandlerReference> iterator = listeners.iterator();
        while (iterator.hasNext()) {
            HandlerReference reference = iterator.next();
            if (reference.get() == listener) {
                if (recuirsionCount == 0) {
                    iterator.remove();
                } else {
                    reference.invalidate();
                }
                break;
            }
        }
    }

    @Override
    public void dispatchEvent(String type, Map<String, Object> data) {
        if (recuirsionCount == 0) {
            while (!toAdd.empty()) {
                listeners.add(toAdd.peek());
                toAdd.pop();
            }
        }

        ++recuirsionCount;
        for (HandlerReference reference : listeners) {
            if (reference.isValid()) {
                IEventHandler handler = reference.get();
                if (handler != null) {
                    handler.handleEvent(this, type, data);
                } else {
                    reference.invalidate();
                }
            }
        }
        --recuirsionCount;

        if (recuirsionCount == 0) {
            Iterator<HandlerReference> iterator = listeners.iterator();
            while (iterator.hasNext()) {
                HandlerReference reference = iterator.next();
                if (!reference.isValid()) {
                    iterator.remove();
                }
            }
        }
    }
}
