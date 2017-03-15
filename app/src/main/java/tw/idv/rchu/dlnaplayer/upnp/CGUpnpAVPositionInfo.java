package tw.idv.rchu.dlnaplayer.upnp;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.std.av.renderer.AVTransport;

public class CGUpnpAVPositionInfo {
    public int duration;
    public int currentTime;

    public CGUpnpAVPositionInfo(Action action) {
        String trackDuration = action.getArgumentValue(AVTransport.TRACKDURATION);
        String absTime = action.getArgumentValue(AVTransport.ABSTIME);

        this.duration = Upnp.parseTimeStringToSecs(trackDuration);
        this.currentTime = Upnp.parseTimeStringToSecs(absTime);
    }

    @Override
    public String toString() {
        return Upnp.timeStringForSecs(currentTime) + "/" + Upnp.timeStringForSecs(duration);
    }
}
