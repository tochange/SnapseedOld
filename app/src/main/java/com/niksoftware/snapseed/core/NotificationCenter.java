package com.niksoftware.snapseed.core;

import android.util.Log;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class NotificationCenter {
    private static final String LOG_TAG = "NotificationCenter";
    private static NotificationCenter _instance;
    private HashMap<ListenerType, ArrayList<NotificationCenterListener>> _listeners = new HashMap();

    private NotificationCenter() {
    }

    public static NotificationCenter getInstance() {
        if (_instance == null) {
            _instance = new NotificationCenter();
        }
        return _instance;
    }

    private void dump() {
        int cnt = 0;
        Log.e(LOG_TAG, "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
        for (ListenerType type : this._listeners.keySet()) {
            ArrayList<NotificationCenterListener> listeners = (ArrayList) this._listeners.get(type);
            Log.e(LOG_TAG, "Type: " + type.toString() + " Listeners: " + listeners.size());
            Iterator i$ = listeners.iterator();
            while (i$.hasNext()) {
                Log.e(LOG_TAG, ((NotificationCenterListener) i$.next()).toString());
                cnt++;
            }
        }
        Log.e(LOG_TAG, String.format("COUNT %d", new Object[]{Integer.valueOf(cnt)}));
    }

    public void addListener(NotificationCenterListener listener, ListenerType type) {
        ArrayList<NotificationCenterListener> list = this._listeners.containsKey(type) ? (ArrayList) this._listeners.get(type) : new ArrayList();
        list.add(listener);
        this._listeners.put(type, list);
    }

    public void removeListener(NotificationCenterListener listener, ListenerType type) {
        if (this._listeners.containsKey(type)) {
            ArrayList<NotificationCenterListener> list = (ArrayList) this._listeners.get(type);
            for (int i = 0; i < list.size(); i++) {
                if (((NotificationCenterListener) list.get(i)) == listener) {
                    list.remove(i);
                    break;
                }
            }
            if (list.size() == 0) {
                this._listeners.remove(type);
            } else {
                this._listeners.put(type, list);
            }
        }
    }

    public void performAction(ListenerType type, Object arg) {
        if (this._listeners.containsKey(type)) {
            Iterator i$ = ((ArrayList) ((ArrayList) this._listeners.get(type)).clone()).iterator();
            while (i$.hasNext()) {
                ((NotificationCenterListener) i$.next()).performAction(arg);
            }
        }
    }

    public static void destoryInstance() {
        for (ListenerType key : _instance._listeners.keySet()) {
            ((ArrayList) _instance._listeners.get(key)).clear();
        }
        _instance._listeners.clear();
        _instance = null;
    }
}
