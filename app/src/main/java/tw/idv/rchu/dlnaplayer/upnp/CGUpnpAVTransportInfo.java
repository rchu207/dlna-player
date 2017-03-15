package tw.idv.rchu.dlnaplayer.upnp;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.std.av.renderer.AVTransport;

public class CGUpnpAVTransportInfo {
    public String transportState;
    public String transportStatus;
    public String speed;

    public int currentState;
    public boolean isError;

    public CGUpnpAVTransportInfo(Action action) {
        this.transportState = action.getArgumentValue(AVTransport.CURRENTTRANSPORTSTATE);
        this.transportStatus = action.getArgumentValue(AVTransport.CURRENTTRANSPORTSTATUS);
        this.speed = action.getArgumentValue(AVTransport.CURRENTSPEED);

        if (transportState.equals(AVTransport.PLAYING)) {
            this.currentState = Upnp.PLAYING;
        } else if (transportState.equals(AVTransport.TRANSITIONING)) {
            this.currentState = Upnp.TRANSITIONING;
        } else if (transportState.equals(AVTransport.PAUSED_PLAYBACK)) {
            this.currentState = Upnp.PAUSED_PLAYBACK;
        } else if (transportState.equals(AVTransport.PAUSED_RECORDING)) {
            this.currentState = Upnp.PAUSED_RECORDING;
        } else if (transportState.equals(AVTransport.RECORDING)) {
            this.currentState = Upnp.RECORDING;
        } else if (transportState.equals(AVTransport.NO_MEDIA_PRESENT)) {
            this.currentState = Upnp.NO_MEDIA_PRESENT;
        } else {
            this.currentState = Upnp.STOPPED;
        }

        this.isError = transportStatus.equals(AVTransport.ERROR_OCCURRED);
    }

    @Override
    public String toString() {
        return "State:" + transportState + ",Status:" + transportStatus;
    }
}
