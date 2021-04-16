package cn.wpj.tvlauncher;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cn.wpj.tvlauncher.entity.AppInfo;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<AppInfo> datas;
    private Context mContext;
    private LayoutInflater mLiLayoutInflater;
    private Listener listener;

    public MyAdapter(List<AppInfo> datas, Context context,Listener listener) {
        this.datas = datas;
        this.mContext = context;
        this.mLiLayoutInflater = LayoutInflater.from(mContext);
        this.listener = listener;
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mLiLayoutInflater.inflate(R.layout.item_app_list, parent, false));
    }

    @Override
    public void onBindViewHolder(MyAdapter.ViewHolder holder, final int position) {
        holder.tv_title.setText(datas.get(position).getName());
        holder.img.setImageDrawable(datas.get(position).getIco());
    }

    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        ViewCompat.animate(v)
                                .setDuration(200)
                                .scaleX(1.1f)
                                .scaleY(1.1f)
                                .start();
                    } else {
                        ViewCompat.animate(v)
                                .setDuration(200)
                                .scaleX(1f)
                                .scaleY(1f)
                                .start();
                    }
                }
            });

            tv_title = (TextView) itemView.findViewById(R.id.tv_title);
            img = (ImageView) itemView.findViewById(R.id.img);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onLongClick(getAdapterPosition());
                    return true;
                }
            });
        }
    }

    public interface Listener{
        void onClick(int position);
        void onLongClick(int position);
    }
}
