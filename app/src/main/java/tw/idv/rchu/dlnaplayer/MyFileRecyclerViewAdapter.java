package tw.idv.rchu.dlnaplayer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link FileContent.FileItem} and makes a call to the
 * specified {@link FileFragment.OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyFileRecyclerViewAdapter extends RecyclerView.Adapter<MyFileRecyclerViewAdapter.ViewHolder> {

    private final List<FileContent.FileItem> mValues;
    private final FileFragment.OnListFragmentInteractionListener mListener;

    public MyFileRecyclerViewAdapter(List<FileContent.FileItem> items,
                                     FileFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_file_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        if (mValues.get(position).type.startsWith("video/")) {
            holder.mIconView.setImageResource(R.drawable.ic_movie_black_36dp);
        } else if (mValues.get(position).type.startsWith("image/")) {
            holder.mIconView.setImageResource(R.drawable.ic_photo_black_36dp);
        } else if (mValues.get(position).type.startsWith("audio/")) {
            holder.mIconView.setImageResource(R.drawable.ic_audiotrack_black_36dp);
        } else {
            holder.mIconView.setImageResource(R.drawable.ic_folder_black_36dp);
        }
        holder.mContentView.setText(mValues.get(position).title);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mIconView;
        public final TextView mContentView;
        public FileContent.FileItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIconView = (ImageView) view.findViewById(R.id.icon);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
