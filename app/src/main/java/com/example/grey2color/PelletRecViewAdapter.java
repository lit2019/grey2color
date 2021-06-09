package com.example.grey2color;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.InputStream;

import static com.example.grey2color.MainActivity.photoView;

public class PelletRecViewAdapter extends RecyclerView.Adapter<PelletRecViewAdapter.ViewHolder> {

    private String[] pellets ;
    private int isChecked;
    private static final String TAG = "PelletRecViewAdapter";
    private Context mContext;
    private AssetManager assetManager;
    private boolean isSampleImage = false;

    public PelletRecViewAdapter(Context mContext, boolean isSampleImage) {
        this.mContext = mContext;
        this.isSampleImage = isSampleImage;
    }

    public PelletRecViewAdapter(Context mContext) {
        this.mContext = mContext;
        this.isChecked = 0;
    }

    public PelletRecViewAdapter(Context mContext,int isChecked) {
        this.isChecked = isChecked;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_pellet,parent,false);
        assetManager = parent.getContext().getAssets();
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG,"onBindViewHolder:Called");
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(pellets[position]);
            Bitmap pelletBitmap = BitmapFactory.decodeStream(inputStream);
            holder.imgPellet.setImageBitmap(pelletBitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(isChecked!=position){
            holder.parent.setBackgroundResource(R.color.white);
        }else {
            holder.parent.setBackgroundResource(R.color.yellow_color);
        }
    }

    @Override
    public int getItemCount() {
        return pellets.length;
    }

    public int getCheckedItemPosition(){
        return this.isChecked;
    }


    public void setpellets(String[] pellets) {
        this.pellets = pellets;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private CardView parent;
        private ImageView imgPellet;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            parent=itemView.findViewById(R.id.parentPelletCard);
            imgPellet=itemView.findViewById(R.id.imgPellet);

            parent.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View v) {
                    if (isChecked!=getAdapterPosition()){
                        if(isChecked!=-1){
                            notifyItemChanged(isChecked);
                        }
                        isChecked=getAdapterPosition();
                        parent.setBackgroundResource(R.color.yellow_color);
                    }

                    if(isSampleImage){
                        InputStream inputStream = null;
                        try {
                            inputStream = assetManager.open(pellets[getAdapterPosition()]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Bitmap pelletBitmap = BitmapFactory.decodeStream(inputStream);
                        Glide.with(mContext).asBitmap().load(pelletBitmap).into(photoView);


//                        stopThread();
                    }

                }
            });
        }
    }
}
