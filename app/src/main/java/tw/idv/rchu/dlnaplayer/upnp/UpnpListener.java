package tw.idv.rchu.dlnaplayer.upnp;

public interface UpnpListener {
    void onPlay(boolean result);

    void onPaused(boolean result);

    void onSeek(boolean result);

    void onPositionInfo(CGUpnpAVPositionInfo info);

    void onTransportInfo(CGUpnpAVTransportInfo info);

    void onError(int reason);
}
