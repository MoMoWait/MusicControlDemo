package cn.edu.fjnu.musicdemo;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ControlAdapter extends RecyclerView.Adapter implements View.OnClickListener{

    private Context mContext;
    private List<MusicInfo> musicInfos;
    private OnControlClick mControlclick;

    public ControlAdapter(Context context, List<MusicInfo> musicInfos, OnControlClick controlClick){
        mContext = context;
        this.musicInfos = musicInfos;
        mControlclick = controlClick;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View itemView =  LayoutInflater.from(mContext).inflate(R.layout.adpater_control, viewGroup, false);
        return new ControlHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        MusicInfo itemMusicInfo = musicInfos.get(position);
        TextView textAppName = viewHolder.itemView.findViewById(R.id.tv_app_name);
        textAppName.setText(itemMusicInfo.getAppName());
        TextView textMusicTitle = viewHolder.itemView.findViewById(R.id.tv_music_title);
        textMusicTitle.setText(itemMusicInfo.getTitle());
        ImageView imgPlayPause = viewHolder.itemView.findViewById(R.id.iv_play_pause);
        imgPlayPause.setImageResource(itemMusicInfo.isMusicState() ? R.mipmap.pause : R.mipmap.play);
        imgPlayPause.setOnClickListener(this);
        imgPlayPause.setTag(itemMusicInfo);
        ImageView imgLastMusic =  viewHolder.itemView.findViewById(R.id.iv_last_music);
        imgLastMusic.setOnClickListener(this);
        imgLastMusic.setTag(itemMusicInfo);
        ImageView imgNextMusic = viewHolder.itemView.findViewById(R.id.iv_next_music);
        imgNextMusic.setOnClickListener(this);
        imgNextMusic.setTag(itemMusicInfo);

    }

    @Override
    public void onClick(View v) {
        mControlclick.onClick(v);
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
