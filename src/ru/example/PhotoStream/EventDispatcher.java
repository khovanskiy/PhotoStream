package ru.example.PhotoStream;

import java.util.Stack;
import java.util.Vector;

public class EventDispatcher implements IEventDispatcher {
    private int recuirsion_count;
    private Vector<Pair<IEventHandler, Boolean>> listeners;
    private Stack<Pair<IEventHandler, Boolean>> nn;

    public EventDispatcher() {
        recuirsion_count = 0;
        listeners = new Vector<Pair<IEventHandler, Boolean>>();
        nn = new Stack<Pair<IEventHandler, Boolean>>();
    }

    @Override
    public void addEventListener(IEventHandler listener) {
        if (listener == null) {
            return;
        }
        if (recuirsion_count == 0) {
            listeners.add(new Pair<>(listener, true));
        } else {
            nn.push(new Pair<>(listener, true));
        }
    }

    @Override
    public void removeEventListener(IEventHandler listener) {
        if (listener == null) {
            return;
        }
        if (recuirsion_count == 0) {
            listeners.remove(listener);
        } else {
            for (int i = 0; i < listeners.size(); ++i) {
                if (listeners.get(i).first == listener) {
                    listeners.get(i).second = false;
                    break;
                }
            }
        }
    }

    @Override
    public void dispatchEvent(Event event) {
        if (recuirsion_count == 0) {
            while (!nn.empty()) {
                listeners.add(nn.peek());
                nn.pop();
            }
        }

        ++recuirsion_count;
        for (Pair<IEventHandler, Boolean> i : listeners) {
            if (i.second) {
                i.first.handleEvent(event);
            }
        }
        --recuirsion_count;

        if (recuirsion_count == 0) {
            for (Pair<IEventHandler, Boolean> i : listeners) {
                if (!i.second) {
                    listeners.remove(i.first);
                }
            }
        }
    }
}
