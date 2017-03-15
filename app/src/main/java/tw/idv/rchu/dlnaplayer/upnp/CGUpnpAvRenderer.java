package tw.idv.rchu.dlnaplayer.upnp;

import android.text.TextUtils;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.Service;
import org.cybergarage.upnp.std.av.renderer.AVTransport;
import org.cybergarage.upnp.std.av.renderer.RenderingControl;

public class CGUpnpAvRenderer {
    @SuppressWarnings("unused")
    static final String TAG = "[UPNP]AvRenderer";

    static final String INSTANCE0 = "0";

    public boolean setAVTransportURI(Device device, String path, String name) {
        Service service = device.getService(AVTransport.SERVICE_TYPE);
        if (service == null) {
            return false;
        }

        final Action action = service.getAction(AVTransport.SETAVTRANSPORTURI);
        if (action == null) {
            return false;
        }

        if (TextUtils.isEmpty(path)) {
            return false;
        }

        action.setArgumentValue(AVTransport.INSTANCEID, INSTANCE0);
        action.setArgumentValue(AVTransport.CURRENTURI, path);

        String xml = "<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite\"" +
                " xmlns:dc=\"http://purl.org/dc/elements/1.1/\"" +
                " xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\">";
        xml += "<item><dc:title>" + TextUtils.htmlEncode(name) + "</dc:title></item>";
        xml += "</DIDL-Lite>";
        action.setArgumentValue(AVTransport.CURRENTURIMETADATA, xml);
        return action.postControlAction();
    }

    public boolean play(Device device) {
        Service service = device.getService(AVTransport.SERVICE_TYPE);
        if (service == null) {
            return false;
        }

        final Action action = service.getAction(AVTransport.PLAY);
        if (action == null) {
            return false;
        }

        action.setArgumentValue(AVTransport.INSTANCEID, INSTANCE0);
        action.setArgumentValue(AVTransport.SPEED, "1");
        return action.postControlAction();
    }

    public boolean pause(Device device) {
        Service service = device.getService(AVTransport.SERVICE_TYPE);
        if (service == null) {
            return false;
        }

        final Action action = service.getAction(AVTransport.PAUSE);
        if (action == null) {
            return false;
        }

        action.setArgumentValue(AVTransport.INSTANCEID, INSTANCE0);
        return action.postControlAction();
    }

    public boolean stop(Device device) {
        Service service = device.getService(AVTransport.SERVICE_TYPE);
        if (service == null) {
            return false;
        }

        final Action action = service.getAction(AVTransport.STOP);
        if (action == null) {
            return false;
        }

        action.setArgumentValue(AVTransport.INSTANCEID, INSTANCE0);
        return action.postControlAction();
    }

    public boolean seek(Device device, String time) {
        Service service = device.getService(AVTransport.SERVICE_TYPE);
        if (service == null) {
            return false;
        }

        final Action action = service.getAction(AVTransport.SEEK);
        if (action == null) {
            return false;
        }

        action.setArgumentValue(AVTransport.INSTANCEID, INSTANCE0);
        action.setArgumentValue(AVTransport.UNIT, "ABS_TIME");
        action.setArgumentValue(AVTransport.TARGET, time);
        return action.postControlAction();
    }

    public CGUpnpAVPositionInfo getPositionInfo(Device device) {
        Service service = device.getService(AVTransport.SERVICE_TYPE);
        if (service == null) {
            return null;
        }

        final Action action = service.getAction(AVTransport.GETPOSITIONINFO);
        if (action == null) {
            return null;
        }

        action.setArgumentValue(AVTransport.INSTANCEID, INSTANCE0);
        if (action.postControlAction()) {
            return new CGUpnpAVPositionInfo(action);
        }

        return null;
    }

    @SuppressWarnings("unused")
    public String getMediaDuration(Device device) {
        Service service = device.getService(AVTransport.SERVICE_TYPE);
        if (service == null) {
            return null;
        }

        final Action action = service.getAction(AVTransport.GETMEDIAINFO);
        if (action == null) {
            return null;
        }

        action.setArgumentValue(AVTransport.INSTANCEID, INSTANCE0);
        if (action.postControlAction()) {
            return action.getArgumentValue(AVTransport.MEDIADURATION);
        } else {
            return null;
        }
    }

    public CGUpnpAVTransportInfo getTransportInfo(Device device) {
        Service service = device.getService(AVTransport.SERVICE_TYPE);
        if (service == null) {
            return null;
        }

        final Action action = service.getAction(AVTransport.GETTRANSPORTINFO);
        if (action == null) {
            return null;
        }

        action.setArgumentValue(AVTransport.INSTANCEID, INSTANCE0);
        if (action.postControlAction()) {
            return new CGUpnpAVTransportInfo(action);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unused")
    public boolean setMute(Device device, boolean isMute) {
        Service service = device.getService(RenderingControl.SERVICE_TYPE);
        if (service == null) {
            return false;
        }
        final Action action = service.getAction(RenderingControl.SETMUTE);
        if (action == null) {
            return false;
        }

        action.setArgumentValue(RenderingControl.INSTANCEID, INSTANCE0);
        action.setArgumentValue(RenderingControl.CHANNEL, RenderingControl.MASTER);
        if (isMute) {
            action.setArgumentValue(RenderingControl.DESIREDMUTE, "1");
        } else{
            action.setArgumentValue(RenderingControl.DESIREDMUTE, "0");
        }
        return action.postControlAction();
    }

    @SuppressWarnings("unused")
    public boolean getMute(Device device) {
        Service service = device.getService(RenderingControl.SERVICE_TYPE);
        if (service == null) {
            return false;
        }

        final Action action = service.getAction(RenderingControl.GETMUTE);
        if (action == null) {
            return false;
        }
        action.setArgumentValue(RenderingControl.INSTANCEID, INSTANCE0);
        action.setArgumentValue(RenderingControl.CHANNEL, RenderingControl.MASTER);
        if (action.postControlAction()) {
            String isMute = action.getArgumentValue(RenderingControl.CURRENTMUTE);
            if (isMute != null && isMute.equals("1")) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    public boolean setVolume(Device device, int value) {
        Service service = device.getService(RenderingControl.SERVICE_TYPE);
        if (service == null) {
            return false;
        }

        final Action action = service.getAction(RenderingControl.SETVOLUME);
        if (action == null) {
            return false;
        }

        action.setArgumentValue(RenderingControl.INSTANCEID, INSTANCE0);
        action.setArgumentValue(RenderingControl.CHANNEL, RenderingControl.MASTER);
        action.setArgumentValue(RenderingControl.DESIREDVOLUME, value);
        return action.postControlAction();
    }

    @SuppressWarnings("unused")
    public int getVolume(Device device) {
        Service service = device.getService(RenderingControl.SERVICE_TYPE);
        if (service == null) {
            return -1;
        }

        final Action action = service.getAction(RenderingControl.GETVOLUME);
        if (action == null) {
            return -1;
        }

        action.setArgumentValue(RenderingControl.INSTANCEID, INSTANCE0);
        action.setArgumentValue(RenderingControl.CHANNEL, RenderingControl.MASTER);
        if (action.postControlAction()) {
            return action.getArgumentIntegerValue(RenderingControl.CURRENTVOLUME);
        } else {
            return -1;
        }
    }
}
