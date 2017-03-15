package tw.idv.rchu.dlnaplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;

import tw.idv.rchu.dlnaplayer.upnp.CGUpnpAVPositionInfo;
import tw.idv.rchu.dlnaplayer.upnp.CGUpnpAVTransportInfo;
import tw.idv.rchu.dlnaplayer.upnp.Upnp;
import tw.idv.rchu.dlnaplayer.upnp.UpnpListener;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MediaControllerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MediaControllerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MediaControllerFragment extends Fragment implements
        View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    static final String TAG = "[DLNA]Controller";

    static final String ARG_HOST = "arg_host";
    static final String ARG_PORT = "arg_port";
    static final String ARG_URI = "arg_uri";
    static final String ARG_MIME_TYPE = "arg_mime_type";
    static final String ARG_FILE_NAME = "arg_file_name";

    // TODO: Rename and change types of parameters
//    private OnFragmentInteractionListener mListener;

    private Handler mUiHandler = new Handler();

    private Upnp mUpnp;
    private boolean mIsPlaying;
    private Uri mFileUri;
    private String mFileName;
    private int mVolume;
    private int mMute;
    private boolean mIsLoaded;
    private boolean mIsRepeatOne = false;

    private TextView mTextCurrentTime;
    private SeekBar mSeekBar;
    private TextView mTextDuration;
    private ImageButton mButtonPlay;
    private ImageButton mButtonVolumeDown;
    private ImageButton mButtonVolumeUp;
    private ImageButton mButtonVolumeOff;
    private boolean mIsStartTracking;
    private boolean mIsPositionInfoGot;
    private ProgressBar mProgressBar;

    private UpnpListener mListener = new UpnpListener() {
        @Override
        public void onPlay(final boolean result) {
            if (result) {
                mIsPositionInfoGot = false;
                mUiHandler.removeCallbacks(mVideoTimeRefresher);
                mUiHandler.post(mVideoTimeRefresher);

                mUiHandler.removeCallbacks(mVideoStateRefresher);
                mUiHandler.postDelayed(mVideoStateRefresher, 500);
            }

            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!result) {
                        enableControlButtons(true);
                        mProgressBar.setVisibility(View.GONE);
                        mIsPlaying = false;
                        mButtonPlay.setImageResource(R.drawable.ic_play_arrow);

                        // TODO: show upnp_e1 message.
                    } else if (mIsLoaded) {
                        enableControlButtons(true);
                    }
                }
            });
        }

        @Override
        public void onPaused(final boolean result) {
            if (result) {
                mIsPositionInfoGot = false;
                mUiHandler.removeCallbacks(mVideoTimeRefresher);
            }

            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    enableControlButtons(true);
                    if (!result) {
                        mIsPlaying = false;
                        mButtonPlay.setImageResource(R.drawable.ic_play_arrow);

                        // TODO: show upnp_e1 message.
                    }
                }
            });
        }

        @Override
        public void onSeek(final boolean result) {
            if (result) {
                mIsPositionInfoGot = false;
                mUiHandler.removeCallbacks(mVideoTimeRefresher);
                mUiHandler.postDelayed(mVideoTimeRefresher, 1000);
            }

            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    enableControlButtons(true);
                }
            });
        }

        @Override
        public void onPositionInfo(final CGUpnpAVPositionInfo info) {
            if (info == null || info.duration <= 0) {
                return;
            }

            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (!mIsStartTracking && mIsPositionInfoGot) {
                        mTextCurrentTime.setText(Utils.stringForTime(info.currentTime * 1000));
                        mSeekBar.setProgress(info.currentTime);
                    }

                    mTextDuration.setText(Utils.stringForTime(info.duration * 1000));
                    mSeekBar.setMax(info.duration);
                }
            });
        }

        @Override
        public void onTransportInfo(final CGUpnpAVTransportInfo info) {
            if (info == null) {
                return;
            }

            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (info.isError) {
                        Log.e(TAG, "onTransportInfo:" + info);
                        enableControlButtons(true);
                        mProgressBar.setVisibility(View.GONE);
                        mIsPlaying = false;
                        mButtonPlay.setImageResource(R.drawable.ic_play_arrow);

                        // TODO: show upnp_e1 message.
                        return;
                    }

                    if (info.currentState == Upnp.PLAYING) {
                        if (!mIsLoaded) {
                            enableControlButtons(true);
                            mProgressBar.setVisibility(View.GONE);
                        }
                        mIsLoaded = true;
                    } else if (info.currentState == Upnp.STOPPED
                            || info.currentState == Upnp.NO_MEDIA_PRESENT) {
                        if (!mIsLoaded) {
                            return;
                        }

                        mIsPositionInfoGot = false;
                        mUiHandler.removeCallbacks(mVideoTimeRefresher);

                        mSeekBar.setEnabled(false);
                        mSeekBar.setProgress(0);
                        mSeekBar.setMax(0);
                        mTextCurrentTime.setText(R.string.empty_time);
                        mTextDuration.setText(R.string.empty_time);
                        mIsLoaded = false;
                        mIsPlaying = false;
                        mButtonPlay.setImageResource(R.drawable.ic_play_arrow);

                        if (mIsRepeatOne) {
                            mUiHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startCapture();
                                }
                            }, 1000);
                        }
                    }
                }
            });
        }

        @Override
        public void onError(final int reason) {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    enableControlButtons(false);
                    mProgressBar.setVisibility(View.GONE);
                    mIsPlaying = false;
                    mButtonPlay.setImageResource(R.drawable.ic_play_arrow);

                    // TODO: show errors.
                }
            });
        }
    };

    public MediaControllerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MediaControllerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MediaControllerFragment newInstance(String host, int port, File file) {
        MediaControllerFragment fragment = new MediaControllerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HOST, host);
        args.putInt(ARG_PORT, port);
        args.putParcelable(ARG_URI, Uri.fromFile(file));
        args.putString(ARG_FILE_NAME, file.getName());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUpnp = Upnp.getInstance();

        if (getArguments() != null) {
            Uri uri = getArguments().getParcelable(ARG_URI);
            if (uri != null) {
                String uriStr = "http://" + getArguments().getString(ARG_HOST) + ":"
                        + getArguments().getInt(ARG_PORT) + Uri.encode(uri.getPath(), "/");
                mFileUri = Uri.parse(uriStr);
                mFileName = getArguments().getString(ARG_FILE_NAME);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO: Setup action bar.
//        updateActionBar();
//        setHasOptionsMenu(true);

        // Inflate the XML layout.
        View view = inflater.inflate(R.layout.fragment_media_controller, container, false);

        // Get UI widgets.
        ImageView imageView = (ImageView) view.findViewById(R.id.fileDetailView);

        mTextCurrentTime = (TextView) view.findViewById(R.id.textViewCurrentTime);
        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mTextDuration = (TextView) view.findViewById(R.id.textViewDuration);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        ImageButton mButtonPrevious = (ImageButton) view.findViewById(R.id.imageButtonPrevious);
        mButtonPrevious.setOnClickListener(this);
        mButtonPlay = (ImageButton) view.findViewById(R.id.imageButtonPlay);
        mButtonPlay.setOnClickListener(this);
        ImageButton mButtonNext = (ImageButton) view.findViewById(R.id.imageButtonNext);
        mButtonNext.setOnClickListener(this);

        mButtonVolumeDown = (ImageButton) view.findViewById(R.id.imageButtonVolumeDown);
        mButtonVolumeDown.setOnClickListener(this);
        mButtonVolumeUp = (ImageButton) view.findViewById(R.id.imageButtonVolumeUp);
        mButtonVolumeUp.setOnClickListener(this);
        mButtonVolumeOff = (ImageButton) view.findViewById(R.id.imageButtonVolumeOff);
        mButtonVolumeOff.setOnClickListener(this);

        // Setup file detail view.
        mIsStartTracking = false;
        mSeekBar.setEnabled(false);
        mSeekBar.setProgress(0);
        mSeekBar.setMax(0);
        mProgressBar.setVisibility(View.GONE);

        mIsPlaying = false;
        mButtonPlay.setImageResource(R.drawable.ic_play_arrow);
        Utils.updateButtonStatus(mButtonPlay, false);
        Utils.updateButtonStatus(mButtonPrevious, false);
        Utils.updateButtonStatus(mButtonNext, false);

        // Setup image.
        if (imageView != null) {
            imageView.setKeepScreenOn(true);
            String mimeType = getArguments().getString(ARG_MIME_TYPE);
            if (mimeType != null) {
                if (mimeType.startsWith("video/")) {
                    imageView.setImageResource(R.drawable.google_movie);
                } else if (mimeType.startsWith("audio/")) {
                    imageView.setImageResource(R.drawable.google_music);
                }
            }
        }

        mIsPositionInfoGot = false;

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mUpnp.setListener(mListener);

        startCapture();
    }

    @Override
    public void onStop() {
        mUpnp.setListener(null);

        stopCapture();

        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void enableControlButtons(boolean enable) {
        Utils.updateButtonStatus(mButtonPlay, enable);
        mSeekBar.setEnabled(enable);
    }

    private void updateVolumeButtons() {
        Utils.updateButtonStatus(mButtonVolumeDown, mVolume >= 0);
        Utils.updateButtonStatus(mButtonVolumeUp, mVolume >= 0);

        if (mMute >= 0) {
            if (mMute == 1) {
                Utils.updateButtonStatus(mButtonVolumeDown, false);
                Utils.updateButtonStatus(mButtonVolumeUp, false);
            }
            Utils.updateButtonStatus(mButtonVolumeOff, true);
        } else {
            Utils.updateButtonStatus(mButtonVolumeOff, false);
        }
    }

    public void startCapture() {
        Log.i(TAG, "startCapture");

        // TODO: Setup volume settings.
        mVolume = -1;
        mMute = -1;
        updateVolumeButtons();

        // Setup DLNA.
        enableControlButtons(false);
        if (mFileUri != null) {
            // Load video and play it.
            mIsLoaded = false;
            mIsPlaying = true;
            mProgressBar.setVisibility(View.VISIBLE);
            mButtonPlay.setImageResource(R.drawable.ic_pause);
            // TODO: select device.
//            mUpnp.setSelectedDevice(projector.getName(), projector.getIp());
            mUpnp.play(mFileUri.toString(), mFileName);
        }
    }

    public void stopCapture() {
        Log.i(TAG, "stopCapture");
        mUiHandler.removeCallbacks(mVideoTimeRefresher);
        mUiHandler.removeCallbacks(mVideoStateRefresher);

        // Setup volume settings.
        mVolume = -1;
        mMute = -1;
        updateVolumeButtons();

        // Setup DLNA.
        enableControlButtons(false);
        mIsLoaded = false;
        mIsPlaying = false;
        mButtonPlay.setImageResource(R.drawable.ic_play_arrow);

        // Stop video playback.
        mUpnp.stop();
        mUpnp.setSelectedDevice("", "");
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.imageButtonPlay) {
            enableControlButtons(false);

            if (mIsPlaying) {
                mIsPlaying = false;
                mButtonPlay.setImageResource(R.drawable.ic_play_arrow);
                mUpnp.pause();
            } else if (mFileUri != null) {
                if (!mIsLoaded) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                mIsPlaying = true;
                mButtonPlay.setImageResource(R.drawable.ic_pause);
                mUpnp.play(mFileUri.toString(), mFileName);
            }
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mTextCurrentTime.setText(Utils.stringForTime(progress * 1000));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mIsStartTracking = true;
        mIsPositionInfoGot = false;
        mUiHandler.removeCallbacks(mVideoTimeRefresher);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mIsStartTracking = false;
        enableControlButtons(false);
        mUpnp.seek(seekBar.getProgress());
    }

    class ProgressRefresher implements Runnable {
        public void run() {
            mIsPositionInfoGot = true;
            mUpnp.getPositionInfo();

            mUiHandler.postDelayed(mVideoTimeRefresher, 1000);
        }
    }

    ProgressRefresher mVideoTimeRefresher = new ProgressRefresher();

    class UpnpTransportRefresher implements Runnable {
        public void run() {
            mUpnp.getTransportInfo();

            mUiHandler.postDelayed(mVideoStateRefresher, 1000);
        }
    }

    UpnpTransportRefresher mVideoStateRefresher = new UpnpTransportRefresher();

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
