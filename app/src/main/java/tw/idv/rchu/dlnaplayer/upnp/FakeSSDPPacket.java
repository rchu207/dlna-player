package tw.idv.rchu.dlnaplayer.upnp;

import org.cybergarage.upnp.ssdp.SSDPPacket;

public class FakeSSDPPacket extends SSDPPacket {
    public FakeSSDPPacket(byte[] buf) {
        packetBytes = buf;
    }
}
