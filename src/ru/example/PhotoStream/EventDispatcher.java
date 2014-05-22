package ru.example.PhotoStream;

import java.util.Stack;
import java.util.Vector;

public class EventDispatcher implements IEventDispatcher {
    private int recuirsion_count;
    private Vector<Pair<IEventHadler, Boolean>> listeners;
    private Stack<Pair<IEventHadler, Boolean>> nn;

    public EventDispatcher() {
        recuirsion_count = 0;
        listeners = new Vector<Pair<IEventHadler, Boolean>>();
        nn = new Stack<Pair<IEventHadler, Boolean>>();
    }

    @Override
    public void addEventListener(IEventHadler listener) {
        if (listener == null) {
            return;
        }
        if (recuirsion_count == 0) {
            listeners.add(new Pair<IEventHadler, Boolean>(listener, true));
        } else {
            nn.push(new Pair<IEventHadler, Boolean>(listener, true));
        }
    }

    @Override
    public void removeEventListener(IEventHadler listener) {
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
        for (Pair<IEventHadler, Boolean> i : listeners) {
            if (i.second) {
                i.first.handleEvent(event);
            }
        }
        --recuirsion_count;

        if (recuirsion_count == 0) {
            for (Pair<IEventHadler, Boolean> i : listeners) {
                if (!i.second) {
                    listeners.remove(i.first);
                }
            }
        }
    }
}
