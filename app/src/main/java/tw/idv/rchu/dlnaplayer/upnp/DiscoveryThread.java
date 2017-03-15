package tw.idv.rchu.dlnaplayer.upnp;

import android.util.Log;

import org.cybergarage.upnp.ControlPoint;

public class DiscoveryThread extends Thread {
    static final String TAG = "[UPNP]Discovery";

    static final int INTERVAL_SHORT = 10 * 1000;        // 10 seconds.
    static final int INTERVAL_LONG = 2 * 60 * 1000;     // 2 minutes.

    private ControlPoint mControlPoint;

    private boolean mIsRunning;
    private int mSearchTimes;

    public DiscoveryThread(ControlPoint controlPoint) {
        super(DiscoveryThread.TAG);
        mControlPoint = controlPoint;
    }

    @Override
    public void run() {
        // Start DMC control point.
        if (mControlPoint == null) {
            Log.e(TAG, "DMC control point is not created!");
            return;
        }
        if (!mControlPoint.start()) {
            Log.e(TAG, "Can not start DMC control point!");
            return;
        }

        // Search control points in the network.
        mIsRunning = true;
        mSearchTimes = 0;
        while (mIsRunning) {
            searchDevices();
        }

        // Stop DMC control point.
        mControlPoint.stop();
    }

    /**
     * Search control points in the network.
     */
    private void searchDevices() {
        Log.d(TAG, "Search control points");
        try {
            mControlPoint.search();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        synchronized (this) {
            try {
                mSearchTimes++;
                if (mSearchTimes >= 6) {
                    wait(INTERVAL_LONG);
                } else {
                    wait(INTERVAL_SHORT);
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    /**
     * Stop the thread.
     */
    public void stopThread() {
        mIsRunning = false;
        synchronized (this) {
            notifyAll();
        }
    }

    public void resetSearch() {
        mSearchTimes = 0;
        synchronized (this) {
            notifyAll();
        }
    }
}
