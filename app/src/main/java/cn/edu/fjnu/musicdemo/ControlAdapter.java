package cn.edu.fjnu.musicdemo;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class ControlAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<MusicInfo> musicInfos;
    public ControlAdapter(Context context, List<MusicInfo> musicInfos){
        mContext = context;
        this.musicInfos = musicInfos;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View itemView = View.inflate(mContext, R.layout.adpater_control, viewGroup);
        return new ControlHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

    }

    @Override
    public int getItemCount() {
        return musicInfos.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }


    static class ControlHolder extends RecyclerView.ViewHolder{

        public ControlHolder(@NonNull View itemView) {
            super(itemView);
        }

    }
}
