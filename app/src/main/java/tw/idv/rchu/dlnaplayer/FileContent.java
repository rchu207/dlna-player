package tw.idv.rchu.dlnaplayer;

import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class FileContent {
    public static final String SCHEME = "dlnaplayer";

    public static final String AUTHORITY_ROOT = "root";
    public static final String AUTHORITY_VIDEO = "video";
    public static final String AUTHORITY_VIDEO_BUCKET = "video-bucket";
    public static final String AUTHORITY_IMAGE = "image";
    public static final String AUTHORITY_IMAGE_BUCKET = "image-bucket";
    public static final String AUTHORITY_AUDIO = "audio";
    public static final String AUTHORITY_AUDIO_ALBUM = "audio-album";
    public static final String AUTHORITY_LOCAL_STORAGE = "local-storage";
    public static final String AUTHORITY_FOLDER = "folder";
    public static final String AUTHORITY_FILE = "file";

    public static final String[] PROJECTION_AUDIO_ALBUM = {
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM,
    };

    public static final String[] PROJECTION_AUDIO = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.ALBUM_ID,
    };

    public static final FileItem ITEM = new FileItem("0",
            SCHEME + "://" + AUTHORITY_ROOT);

    /**
     * An array of sample (dummy) items.
     */
    public static final List<FileItem> ITEMS = new ArrayList<>();


    static {
        // TODO: add translations.
        int id = 0;
        addItem(new FileItem(String.valueOf(id++), SCHEME + "://" + AUTHORITY_VIDEO, "Video"));
        addItem(new FileItem(String.valueOf(id++), SCHEME + "://" + AUTHORITY_IMAGE, "Image"));
        addItem(new FileItem(String.valueOf(id++), SCHEME + "://" + AUTHORITY_AUDIO, "Audio"));
        addItem(new FileItem(String.valueOf(id++), SCHEME + "://" + AUTHORITY_LOCAL_STORAGE, "Local Storage"));
    }

    private static void addItem(FileItem item) {
        ITEMS.add(item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class FileItem {
        public final String id;
        public final Uri content;
        public final String title;
        public final String type;

        public FileItem(String id, String content) {
            this(id, content, content, "*/*");
        }

        public FileItem(String id, String content, String title) {
            this(id, content, title, "*/*");
        }

        public FileItem(String id, String content, String title, String type) {
            this.id = id;
            this.content = Uri.parse(content);
            this.title = title;
            this.type = type;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (getClass() != obj.getClass()) {
                return false;
            }
            return this.content.equals(((FileItem) obj).content);
        }

        @Override
        public String toString() {
            return content.getScheme() + "," + content.getAuthority() + "," + content.getPath();
        }
    }
}
