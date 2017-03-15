package tw.idv.rchu.dlnaplayer.upnp;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.device.DeviceChangeListener;
import org.cybergarage.upnp.ssdp.SSDPPacket;
import org.cybergarage.util.Debug;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;

import tw.idv.rchu.dlnaplayer.Utils;

public class Upnp {
    static final String TAG = "[UPNP]Upnp";

    public static final int STOPPED = 0;
    public static final int PLAYING = 1;
    public static final int TRANSITIONING = 2;
    public static final int PAUSED_PLAYBACK = 3;
    public static final int PAUSED_RECORDING = 4;
    public static final int RECORDING = 5;
    public static final int NO_MEDIA_PRESENT = 6;

    static final int UPNP_PLAY = 0;
    static final int UPNP_PAUSE = 1;
    static final int UPNP_STOP = 2;
    static final int UPNP_SEEK = 3;
    static final int UPNP_GET_POSITION_INFO = 4;
    static final int UPNP_GET_TRANSPORT_INFO = 5;

    static final int UPNP_SELECT_DMR = 100;

    private static Upnp sInstance;

    private ControlPoint mControlPoint;
    private DiscoveryThread mDiscovery;

    private ArrayList<Device> mDmrDevices;
    private Device mSelectedDevice;
    private CGUpnpAvRenderer mAvRenderer;
    private UpnpListener mListener;


    private HandlerThread mUpnpThread;
    private Handler mUpnpHandler;

    private class MyUpnpCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == UPNP_SELECT_DMR) {
                if (msg.obj != null) {
                    mControlPoint.searchResponseReceived((SSDPPacket) msg.obj);
                } else {
                    mSelectedDevice = null;
                }
                return false;
            }

            boolean ret;
            final Device device = mSelectedDevice;
            if (device == null) {
                // TODO: show upnp_e2 message
                return false;
            }

            switch (msg.what) {
                case UPNP_PLAY:
                    String[] params = (String[]) msg.obj;
                    ret = mAvRenderer.setAVTransportURI(device, params[0], params[1]);
                    if (ret) {
                        ret = mAvRenderer.play(device);
                        if (!ret) {
                            Log.e(TAG, "play() is failed!");
                        }
                    } else {
                        Log.e(TAG, "setAVTransportURI() is failed!");
                    }

                    if (mListener != null) {
                        mListener.onPlay(ret);
                    }
                    break;
                case UPNP_PAUSE:
                    ret = mAvRenderer.pause(device);
                    if (!ret) {
                        Log.e(TAG, "pause() is failed!");
                    }

                    if (mListener != null) {
                        mListener.onPaused(ret);
                    }
                    break;
                case UPNP_STOP:
                    if (!mAvRenderer.stop(device)) {
                        Log.e(TAG, "stop() is failed!");
                    }
                    break;
                case UPNP_SEEK:
                    ret = mAvRenderer.seek(device, (String) msg.obj);
                    if (!ret) {
                        Log.e(TAG, "seek() is failed!");
                    }

                    if (mListener != null) {
                        mListener.onSeek(ret);
                    }
                    break;
                case UPNP_GET_POSITION_INFO: {
                    CGUpnpAVPositionInfo info = mAvRenderer.getPositionInfo(device);

                    if (mListener != null) {
                        mListener.onPositionInfo(info);
                    }
                    break;
                }
                case UPNP_GET_TRANSPORT_INFO: {
                    CGUpnpAVTransportInfo info = mAvRenderer.getTransportInfo(device);

                    if (mListener != null) {
                        mListener.onTransportInfo(info);
                    }
                    break;
                }
                default:
                    break;
            }
            return false;
        }
    }

    private Upnp() {
        super();

        mControlPoint = null;
    }

    public static Upnp getInstance() {
        if (sInstance == null) {
            sInstance = new Upnp();
        }
        return sInstance;
    }

    public void init() {
        Debug.on();

        mControlPoint = new ControlPoint();
        mControlPoint.addDeviceChangeListener(mDeviceChangeListener);
        mDmrDevices = new ArrayList<>(16);
        mSelectedDevice = null;

        mAvRenderer = new CGUpnpAvRenderer();

        // Start Upnp thread and initialize Upnp.
        mUpnpThread = new HandlerThread("Upnp");
        mUpnpThread.start();
        mUpnpHandler = new Handler(mUpnpThread.getLooper(), new MyUpnpCallback());

        mDiscovery = new DiscoveryThread(mControlPoint);
        mDiscovery.start();
    }

    public void destroy() {
        if (mUpnpThread != null) {
            mUpnpHandler.removeCallbacksAndMessages(null);
            if (Utils.hasKitkat()) {
                mUpnpThread.quitSafely();
            } else {
                mUpnpThread.quit();
            }
            mUpnpThread = null;
            mUpnpHandler = null;
        }

        mDiscovery.stopThread();
        mControlPoint = null;
    }

    private DeviceChangeListener mDeviceChangeListener = new DeviceChangeListener() {

        @Override
        public void deviceAdded(Device device) {
            Log.d(TAG, "Add a device: [" + device.getDeviceType() + "]" + device.getFriendlyName());

            if (isMediaRenderDevice(device)) {
                addDevice(device);
            }
        }

        @Override
        public void deviceRemoved(Device device) {
            Log.d(TAG, "Remove a device: " + device.getFriendlyName());

            if (isMediaRenderDevice(device)) {
                removeDevice(device);
            }
        }
    };

    public void setListener(UpnpListener listener) {
        mListener = listener;
    }

    synchronized void addDevice(Device device) {
        for (Device dmr : mDmrDevices) {
            if (dmr.getUDN().equalsIgnoreCase(device.getUDN())) {
                mSelectedDevice = device;
                return;
            }
        }

        mDmrDevices.add(device);
        mSelectedDevice = device;
    }

    synchronized void removeDevice(Device device) {
        // Remove the device from DMR device list.
        for (Device dmr : mDmrDevices) {
            if (dmr.getUDN().equalsIgnoreCase(device.getUDN())) {
                mDmrDevices.remove(dmr);
                break;
            }
        }

        // Remove the selected DMR device.
        if (mSelectedDevice != null && mSelectedDevice.getUDN().equalsIgnoreCase(device.getUDN())) {
            mSelectedDevice = null;
        }
    }

    /**
     * Start to set the selected device's name and IP.
     */
    public void setSelectedDevice(String name, String ip) {
        if (ip.length() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1 200 OK\r\n");
            //sb.append("NTS: ssdp:alive\r\n");
            sb.append("ST: upnp:rootdevice\r\n");
            //sb.append("USN: uuid:ec2856dc832c251b02:00:00:00:00:00::upnp:rootdevice\r\n");
            sb.append("Location: http://").append(ip).append(":38520/description.xml\r\n");
            sb.append("\r\n");
            byte[] buffer = sb.toString().getBytes();

            Message msg = mUpnpHandler.obtainMessage(UPNP_SELECT_DMR);
            msg.obj = new FakeSSDPPacket(buffer);
            mUpnpHandler.sendMessage(msg);
        } else {
            mUpnpHandler.sendEmptyMessage(UPNP_SELECT_DMR);
        }
    }

    /**
     * Start to play the video.
     */
    public void play(String path, String name) {
        Log.i(TAG, "Play:" + path);

        // Play the video.
        Message msg = new Message();
        msg.what = UPNP_PLAY;
        String[] params = new String[2];
        params[0] = path;
        params[1] = name;
        msg.obj = params;
        mUpnpHandler.sendMessage(msg);
    }

    /**
     * Start to pause the video.
     */
    public void pause() {
        Log.i(TAG, "Pause");

        // Pause the video.
        mUpnpHandler.sendEmptyMessage(UPNP_PAUSE);
    }

    /**
     * Start to stop the video.
     */
    public void stop() {
        Log.i(TAG, "Stop");

        // Stop the video.
        mUpnpHandler.sendEmptyMessage(UPNP_STOP);
    }

    /**
     * Start to seek the video.
     */
    public void seek(int time) {
        final String timeString = timeStringForSecs(time);
        Log.i(TAG, "Seek:" + timeString);

        // Seek the video.
        Message msg = new Message();
        msg.what = UPNP_SEEK;
        msg.obj = timeString;
        mUpnpHandler.sendMessage(msg);
    }

    public void getPositionInfo() {
        // Get the position info of video.
        mUpnpHandler.sendEmptyMessage(UPNP_GET_POSITION_INFO);
    }

    public void getTransportInfo() {
        // Get the transport info of video.
        mUpnpHandler.sendEmptyMessage(UPNP_GET_TRANSPORT_INFO);
    }

    private static final String MEDIA_RENDER = "urn:schemas-upnp-org:device:MediaRenderer:1";

    /**
     * Check if the device is a media render device.
     */
    public static boolean isMediaRenderDevice(Device device) {
        return (device != null && MEDIA_RENDER.equalsIgnoreCase(device.getDeviceType()));
    }

    public static String timeStringForSecs(int time) {
        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
        formatBuilder.setLength(0);

        int seconds = time % 60;
        int minutes = (time / 60) % 60;
        int hours = time / 3600;
        if (hours > 99) {
            return "99:59:59";
        } else {
            return formatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
        }
    }

    public static int parseTimeStringToSecs(String time) {
        String[] str = time.split(":|\\.");
        if (str.length < 3)
            return 0;

        int hour = Integer.parseInt(str[0]);
        int min = Integer.parseInt(str[1]);
        int sec = Integer.parseInt(str[2]);
        return hour * 3600 + min * 60 + sec;
    }
}
