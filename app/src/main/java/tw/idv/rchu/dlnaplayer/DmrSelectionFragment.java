
package tw.idv.rchu.dlnaplayer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DmrSelectionFragment extends DialogFragment {
    static final String TAG = "[DLNA]DmrSelect";

    static final String ASSET_FILE_NAME = "VueMagicAndroid.ppt";
    static final String PREFERENCE_NAME = "vuemagic_office";
    static final String PREFERENCE_KEY_ACTIVATED = "pref_ACTIVATED";
    static final String EXTRA_GL_MAX_TEXTURE_SIZE = "gl_max_texture_size";

    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Not show the title of dialog.
//        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the XML layout.
        View view = inflater.inflate(R.layout.dialog_select_dmr, container, false);

        // Get UI widgets.
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);

        // Set the adapter
        Context context = mRecyclerView.getContext();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setAdapter(new MyFileRecyclerViewAdapter(FileContent.ITEMS, null));

        return view;
    }

}
