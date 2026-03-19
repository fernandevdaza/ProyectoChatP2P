package com.fernandev.chatp2p.view.state;

import java.util.ArrayList;
import java.util.List;

public class StateManager {
    private List<StateListener> listeners = new ArrayList<>();
    private State state = new State();
    private final Object stateLock = new Object();
    private static final StateManager stateManager = new StateManager();

    private StateManager() {

    }

    public static StateManager getInstance() {
        return stateManager;
    }

    public void subscribeToState(StateListener listener) {
        this.listeners.add(listener);
    }

    public void unsubscribeToState(StateListener listener) {
        this.listeners = this.listeners.stream().filter(subscriber -> subscriber != listener)
                .collect(java.util.stream.Collectors.toList());
    }

    public void setNewState(State newState, List<Class<? extends StateListener>> stateListeners) {
        synchronized (stateLock){
            this.state = newState;
        }

        for (StateListener listener : listeners) {
            if (isInstanceOf(listener, stateListeners)) {
                listener.onChange(newState);
                if (stateListeners.size() == 1)
                    return;
            }
        }

    }

    private boolean isInstanceOf(StateListener stateListener, List<Class<? extends StateListener>> stateListeners) {
        for (Class<? extends StateListener> classType : stateListeners) {
            if (classType.isInstance(stateListener)) {
                return true;
            }
        }
        return false;
    }

    public State getCurrentState() {
        synchronized (stateLock){
            return state;
        }
    }
}
