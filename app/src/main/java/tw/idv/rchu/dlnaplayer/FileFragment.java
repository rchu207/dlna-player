package tw.idv.rchu.dlnaplayer;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class FileFragment extends Fragment {
    static final String TAG = "[DLNA]FileFrag";

    private static final String ARG_COLUMN_COUNT = "column-count";

    private static final ArrayList<String> mVideoExtensions = new ArrayList<>(16);
    private static final ArrayList<String> mImageExtensions = new ArrayList<>(16);

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private RecyclerView mRecyclerView;
    private TextView mFolderPathView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FileFragment() {
    }

    public static FileFragment newInstance(int columnCount) {
        FileFragment fragment = new FileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        // Create supported file extensions.
        mVideoExtensions.add("3gp");
        mVideoExtensions.add("mp4");
        mVideoExtensions.add("ts");
        mVideoExtensions.add("webm");

        mImageExtensions.add("bmp");
        mImageExtensions.add("gif");
        mImageExtensions.add("jpg");
        mImageExtensions.add("png");
        mImageExtensions.add("webp");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);

        // Set the adapter
        Context context = mRecyclerView.getContext();
        if (mColumnCount <= 1) {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        mFolderPathView = (TextView) view.findViewById(R.id.folder_path);

        // Show no files.
        setRoot(null);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private static final FileFilter fileFilter = new FileFilter() {
        @Override
        public boolean accept(File pathName) {
            if (pathName.isHidden()) {
                return false;
            } else if (pathName.isDirectory()) {
                return true;
            } else {
                String fileExtension = getFileExtension(pathName.getName());
                return mVideoExtensions.contains(fileExtension) ||
                        mImageExtensions.contains(fileExtension);
            }
        }
    };

    private static String getFileExtension(String name) {
        if (name == null || name.length() == 0) {
            return "";
        }
        name = name.toLowerCase(Locale.US);

        int index = name.lastIndexOf(".") + 1;
        if (index > 0 && index < name.length()) {
            return name.substring(index);
        } else {
            return "";
        }
    }

    private static final String[] PROJECTION_VIDEO_BUCKET = {
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
    };

    private static final String[] PROJECTION_VIDEO = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.BUCKET_ID,
    };

    private static final String[] PROJECTION_IMAGE_BUCKET = {
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
    };

    private static final String[] PROJECTION_IMAGE = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.BUCKET_ID,
    };

    void setRoot(FileContent.FileItem root) {
        if (mRecyclerView == null) {
            return;
        }
        ArrayList<FileContent.FileItem> items = new ArrayList<>();

        //noinspection StatementWithEmptyBody
        if (root == null) {
            // Add nothing.
        } else if (root.content.getAuthority().equals(FileContent.AUTHORITY_ROOT)) {
            mFolderPathView.setText("");
            items.addAll(FileContent.ITEMS);
        } else if (root.content.getAuthority().equals(FileContent.AUTHORITY_LOCAL_STORAGE)
                || root.content.getAuthority().equals(FileContent.AUTHORITY_FOLDER)) {
            int id = 0;
            File folder;
            if (root.content.getAuthority().equals(FileContent.AUTHORITY_LOCAL_STORAGE)) {
                folder = Environment.getExternalStorageDirectory();
            } else {
                folder = new File(root.content.getPath());
            }
            mFolderPathView.setText(folder.getPath());

            File[] files = folder.listFiles(fileFilter);
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        items.add(new FileContent.FileItem(String.valueOf(id++),
                                FileContent.SCHEME + "://" + FileContent.AUTHORITY_FOLDER + file.getAbsolutePath(),
                                file.getName()));
                    } else {
                        String extension = getFileExtension(file.getName());
                        if (mVideoExtensions.contains(extension)) {
                            items.add(new FileContent.FileItem(String.valueOf(id++),
                                    FileContent.SCHEME + "://" + FileContent.AUTHORITY_FILE + file.getAbsolutePath(),
                                    file.getName(), "video/*"));
                        } else if (mImageExtensions.contains(extension)) {
                            items.add(new FileContent.FileItem(String.valueOf(id++),
                                    FileContent.SCHEME + "://" + FileContent.AUTHORITY_FILE + file.getAbsolutePath(),
                                    file.getName(), "image/*"));
                        }
                    }
                }
            }
        } else {
            Cursor cursor = null;
            String authority = "";
            if (root.content.getAuthority().equals(FileContent.AUTHORITY_VIDEO)) {
                // Query all video buckets from MediaStore.
                cursor = MediaStore.Video.query(getActivity().getContentResolver(),
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, PROJECTION_VIDEO_BUCKET);
                authority = FileContent.AUTHORITY_VIDEO_BUCKET;
            } else if (root.content.getAuthority().equals(FileContent.AUTHORITY_VIDEO_BUCKET)) {
                // Query all videos in the bucket from MediaStore.
                cursor = MediaStore.Video.query(getActivity().getContentResolver(),
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, PROJECTION_VIDEO);
                authority = FileContent.AUTHORITY_FILE;
            } else if (root.content.getAuthority().equals(FileContent.AUTHORITY_IMAGE)) {
                // Query all image buckets from MediaStore.
                cursor = MediaStore.Images.Media.query(getActivity().getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, PROJECTION_IMAGE_BUCKET);
                authority = FileContent.AUTHORITY_IMAGE_BUCKET;
            } else if (root.content.getAuthority().equals(FileContent.AUTHORITY_IMAGE_BUCKET)) {
                // Query all images in he bucket from MediaStore.
                String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
                String[] selectionArgs = new String[] {
                        root.id
                };
                cursor = MediaStore.Images.Media.query(getActivity().getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, PROJECTION_IMAGE,
                        selection, selectionArgs,
                        MediaStore.Images.Media.DEFAULT_SORT_ORDER);
                authority = FileContent.AUTHORITY_FILE;
            }

            mFolderPathView.setText(root.title);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    FileContent.FileItem child = new FileContent.FileItem(String.valueOf(cursor.getString(0)),
                            String.format("%s://%s/%s", FileContent.SCHEME, authority, cursor.getString(1)),
                            cursor.getString(2),
                            (cursor.getColumnCount() > 3) ? cursor.getString(3) : "*/*");
                    if (authority.equals(FileContent.AUTHORITY_FILE)) {
                        if (cursor.getString(4).equals(root.id)) {
                            items.add(child);
                        }
                    } else if (!items.contains(child)) {
                        items.add(child);
                    }
                } while (cursor.moveToNext());
            }
        }

        mRecyclerView.setAdapter(new MyFileRecyclerViewAdapter(items, mListener));
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(FileContent.FileItem item);
    }
}
