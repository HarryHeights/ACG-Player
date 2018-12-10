/*
 * ************************************************************
 * 文件：MyRecyclerAdapter.java  模块：app  项目：MusicPlayer
 * 当前修改时间：2018年12月10日 14:49:08
 * 上次修改时间：2018年12月10日 14:47:45
 * 作者：chenlongcould
 * Geek Studio
 * Copyright (c) 2018
 * ************************************************************
 */

package top.geek_studio.chenlongcould.musicplayer.Adapters;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import top.geek_studio.chenlongcould.musicplayer.Activities.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.Activities.PublicActivity;
import top.geek_studio.chenlongcould.musicplayer.BroadCasts.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Fragments.MusicListFragment;
import top.geek_studio.chenlongcould.musicplayer.Fragments.PlayListFragment;
import top.geek_studio.chenlongcould.musicplayer.GlideApp;
import top.geek_studio.chenlongcould.musicplayer.IStyle;
import top.geek_studio.chenlongcould.musicplayer.Models.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.Models.PlayListItem;
import top.geek_studio.chenlongcould.musicplayer.R;
import top.geek_studio.chenlongcould.musicplayer.Utils.PlayListsUtil;
import top.geek_studio.chenlongcould.musicplayer.Utils.Utils;
import top.geek_studio.chenlongcould.musicplayer.Values;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter, IStyle {

    private static final String TAG = "MyRecyclerAdapter";

    /**
     * @see R.layout#recycler_music_list_item_mod
     */
    private static final int MOD_TYPE = -1;

    private volatile boolean READY = true;

    private List<MusicItem> mMusicItems;

    private MainActivity mMainActivity;

    private Context mContext;

    private ItemHolder currentBind;

    private String mCurrentUiPosition;

    public MyRecyclerAdapter(List<MusicItem> musicItems, Context context, String calledFrag) {
        mMusicItems = musicItems;
        mMainActivity = (MainActivity) Data.sActivities.get(0);
        mContext = context;
        mCurrentUiPosition = calledFrag;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return String.valueOf(mMusicItems.get(position).getMusicName().charAt(0));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int itemType) {

        View view;

        ItemHolder holder;

        /*
         * show ExpandView (more opt)...
         * */
        // TODO: 2018/12/4 do
        if (itemType == MOD_TYPE && mCurrentUiPosition.equals(MusicListFragment.TAG)) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_music_list_item_mod, viewGroup, false);
            holder = new ModHolder(view);

            onMusicItemClick(view, holder, mCurrentUiPosition);

            ((ModHolder) holder).mRandomItem.setOnClickListener(v -> Utils.SendSomeThing.sendPlay(mMainActivity, ReceiverOnMusicPlay.TYPE_SHUFFLE, null));

        } else {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_music_list_item, viewGroup, false);
            holder = new ItemHolder(view);
            onMusicItemClick(view, holder, mCurrentUiPosition);
        }

        //默认设置扩展button opacity 0, (default)
        holder.mButton1.setAlpha(0);
        holder.mButton2.setAlpha(0);
        holder.mButton3.setAlpha(0);
        holder.mButton4.setAlpha(0);

        //默认设置扩展布局GONE 以便优化
        holder.mExpandView.setVisibility(View.GONE);

        holder.mMusicCoverImage.setOnClickListener(v -> {

            holder.mExpandView.clearAnimation();

            final ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) holder.mExpandView.getLayoutParams();

            final ValueAnimator animator = new ValueAnimator();
            animator.setDuration(300);

            if (((ConstraintLayout.LayoutParams) holder.mExpandView.getLayoutParams()).topMargin == 0) {
                animator.setIntValues(0, (int) mMainActivity.getResources().getDimension(R.dimen.recycler_expand_view));
                holder.setIsRecyclable(false);
                holder.mExpandView.setVisibility(View.VISIBLE);
                animator.setInterpolator(new OvershootInterpolator());

                /*--- button alpha animation ---*/
                ValueAnimator alphaAnim = new ValueAnimator();
                alphaAnim.setFloatValues(0f, 1f);
                alphaAnim.setDuration(500);
                alphaAnim.addUpdateListener(animation -> {
                    holder.mButton1.setAlpha((Float) animation.getAnimatedValue());
                    holder.mButton2.postDelayed(() -> holder.mButton2.setAlpha((Float) animation.getAnimatedValue()), 100);
                    holder.mButton3.postDelayed(() -> holder.mButton3.setAlpha((Float) animation.getAnimatedValue()), 200);
                    holder.mButton4.postDelayed(() -> holder.mButton4.setAlpha((Float) animation.getAnimatedValue()), 300);
                });
                alphaAnim.start();
                /*--- button alpha animation ---*/

            } else {

                animator.setIntValues((int) mMainActivity.getResources().getDimension(R.dimen.recycler_expand_view), 0);
                holder.setIsRecyclable(true);
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        //set default...
                        holder.mExpandView.setVisibility(View.GONE);
                        holder.mButton1.setAlpha(0);
                        holder.mButton2.setAlpha(0);
                        holder.mButton3.setAlpha(0);
                        holder.mButton4.setAlpha(0);

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
            }

            ValueAnimator rotationAnima = new ValueAnimator();
            rotationAnima.setFloatValues(0f, 360f);
            rotationAnima.setDuration(300);
            rotationAnima.setInterpolator(new OvershootInterpolator());
            rotationAnima.addUpdateListener(animation -> holder.mMusicCoverImage.setRotation((Float) animation.getAnimatedValue()));
            rotationAnima.start();

            animator.addUpdateListener(animation -> {
                layoutParams.setMargins(0, (int) animation.getAnimatedValue(), 0, 0);
                holder.mExpandView.setLayoutParams(layoutParams);
                holder.mExpandView.requestLayout();
            });

            animator.start();

        });

        // TODO: 2018/12/5 button
        holder.mButton1.setOnClickListener(v -> Toast.makeText(mMainActivity, holder.mButton1.getText(), Toast.LENGTH_SHORT).show());
        holder.mButton2.setOnClickListener(v -> Toast.makeText(mMainActivity, holder.mButton2.getText(), Toast.LENGTH_SHORT).show());
        holder.mButton3.setOnClickListener(v -> Toast.makeText(mMainActivity, holder.mButton3.getText(), Toast.LENGTH_SHORT).show());
        holder.mButton4.setOnClickListener(v -> Toast.makeText(mMainActivity, holder.mButton4.getText(), Toast.LENGTH_SHORT).show());

        holder.mItemMenuButton.setOnClickListener(v -> holder.mPopupMenu.show());

        holder.itemView.setOnLongClickListener(v -> {
            holder.mPopupMenu.show();
            return true;
        });

        holder.mPopupMenu.setOnMenuItemClickListener(item -> {

            @SuppressWarnings("UnnecessaryLocalVariable") int index = holder.getAdapterPosition();

            Values.CurrentData.CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = index;

            switch (item.getItemId()) {
                //noinspection PointlessArithmeticExpression
                case Menu.FIRST + 0: {
                    Data.sNextWillPlayItem = Data.sMusicItems.get(holder.getAdapterPosition());
                }
                break;

                case Menu.FIRST + 1: {
                    // TODO: 2018/11/8 待完善(最喜爱歌曲列表)
                    Toast.makeText(mContext, "Building...", Toast.LENGTH_SHORT).show();
//                    SharedPreferences mPlayListSpf = mMainActivity.getSharedPreferences(Values.SharedPrefsTag.PLAY_LIST_SPF_NAME_MY_FAVOURITE, 0);
//                    SharedPreferences.Editor editor = mPlayListSpf.edit();
//                    editor.putString(Values.PLAY_LIST_SPF_KEY, mMusicItems.get(index).getMusicPath());
//                    editor.apply();
//
//                    Utils.Ui.fastToast(Data.sActivities.get(0).getApplicationContext(), "Done!");
                }
                break;

                //add to list
                case Menu.FIRST + 2: {
                    if (Data.sPlayListItems.isEmpty()) {
                        new PlayListFragment.MyLoadListTask(mMainActivity).execute();
                    }

                    final Resources resources = mMainActivity.getResources();

                    final AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity);
                    builder.setTitle(resources.getString(R.string.add_to_playlist));

                    builder.setNegativeButton(resources.getString(R.string.new_list), (dialog, which) -> {
                        final AlertDialog.Builder b2 = new AlertDialog.Builder(mMainActivity);
                        b2.setTitle(resources.getString(R.string.enter_name));

                        final EditText et = new EditText(mMainActivity);
                        b2.setView(et);

                        et.setHint(resources.getString(R.string.enter_name));
                        et.setSingleLine(true);
                        b2.setNegativeButton(resources.getString(R.string.cancel), null);
                        b2.setPositiveButton(resources.getString(R.string.sure), (dialog1, which1) -> {
                            if (TextUtils.isEmpty(et.getText())) {
                                Toast.makeText(mMainActivity, "name can not empty!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            final int result = PlayListsUtil.createPlaylist(mMainActivity, et.getText().toString());
                            if (result != -1)
                                PlayListsUtil.addToPlaylist(mMainActivity, mMusicItems.get(holder.getAdapterPosition()), result, false);
                            dialog.dismiss();
                            Data.sPlayListItems.add(0, new PlayListItem(result, et.getText().toString()));
                            mMainActivity.getPlayListFragment().getPlayListAdapter().notifyItemInserted(0);
                        });
                        b2.show();
                    });
                    builder.setSingleChoiceItems(mMainActivity.getContentResolver()
                                    .query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null, null, null, null),
                            -1, MediaStore.Audio.Playlists.NAME, (dialog, which) -> {
                                PlayListsUtil.addToPlaylist(mMainActivity, mMusicItems.get(holder.getAdapterPosition()), Data.sPlayListItems.get(which).getId(), false);
                                dialog.dismiss();
                            });
                    builder.setCancelable(true);
                    builder.show();
                }
                break;

                case Menu.FIRST + 4: {
                    String albumName = mMusicItems.get(holder.getAdapterPosition()).getMusicAlbum();
                    Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null,
                            MediaStore.Audio.Albums.ALBUM + "= ?", new String[]{mMusicItems.get(holder.getAdapterPosition()).getMusicAlbum()}, null);

                    //int MainActivity
                    MainActivity mainActivity = (MainActivity) Data.sActivities.get(0);
                    Intent intent = new Intent(mainActivity, AlbumDetailActivity.class);
                    intent.putExtra("key", albumName);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int id = Integer.parseInt(cursor.getString(0));
                        intent.putExtra("_id", id);
                        cursor.close();
                    }
                    mContext.startActivity(intent);

                }
                break;

                case Menu.FIRST + 5: {
                    Intent intent = new Intent(mContext, PublicActivity.class);
                    intent.putExtra("start_by", "detail");
                    mContext.startActivity(intent);
                }
                break;
            }

            Values.CurrentData.CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = -1;

            return false;
        });

        return holder;

//            view = LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout.simple_list_item_1, viewGroup, false);
//            return new ViewHolder(view);
    }

    @SuppressLint("StaticFieldLeak")
    private void onMusicItemClick(View view, ViewHolder holder, String currentUiPosition) {
        view.setOnClickListener(v -> new AsyncTask<Void, Void, Integer>() {

            String clickedPath;
            String clickedSongName;
            String clickedSongAlbumName;
            Bitmap cover;

            @Override
            protected Integer doInBackground(Void... voids) {

                if (!READY) {
                    mMainActivity.runOnUiThread(() -> Toast.makeText(mMainActivity, "Wait...", Toast.LENGTH_SHORT).show());
                    return null;
                }
                READY = false;

                //使得当前播放列表成为"待播放列表"
                if (currentUiPosition.equals("PublicActivity") && Values.CurrentData.CURRENT_PLAY_LIST.equals("default")) {
                    PublicActivity activity = ((PublicActivity) mContext);
                    Values.CurrentData.CURRENT_PLAY_LIST = activity.getCurrentListName();
                    Data.sPlayOrderList.clear();
                    Data.sPlayOrderList.addAll(activity.getMusicItemList());        //更新数据
                    Values.CurrentData.CURRENT_MUSIC_INDEX = holder.getAdapterPosition();
                } else {
                    //恢复待播放列表为通常(ALL MUSIC)
                    if (!Values.CurrentData.CURRENT_PLAY_LIST.equals("default")) {
                        Data.sPlayOrderList.clear();
                        Data.sPlayOrderList.addAll(Data.sMusicItems);
                        if (Values.CurrentData.CURRENT_PLAY_TYPE.equals(Values.TYPE_RANDOM)) {
                            Collections.shuffle(Data.sPlayOrderList);
                        }
                        Values.CurrentData.CURRENT_PLAY_LIST = "default";
                        Values.CurrentData.CURRENT_MUSIC_INDEX = holder.getAdapterPosition();
                    }
                }

                Data.sMusicBinder.resetMusic();

                //get & save data
                clickedPath = mMusicItems.get(holder.getAdapterPosition()).getMusicPath();
                clickedSongName = mMusicItems.get(holder.getAdapterPosition()).getMusicName();
                clickedSongAlbumName = mMusicItems.get(holder.getAdapterPosition()).getMusicAlbum();
                cover = Utils.Audio.getMp3Cover(clickedPath);
                Data.sCurrentMusicAlbum = clickedSongAlbumName;
                Data.sCurrentMusicName = clickedSongName;
                Data.sCurrentMusicBitmap = cover;
                Values.MUSIC_PLAYING = true;
                Values.HAS_PLAYED = true;

                try {
                    Data.sMusicBinder.setDataSource(clickedPath);
                    Data.sMusicBinder.prepare();
                    Data.sMusicBinder.playMusic();
                } catch (IOException e) {
                    e.printStackTrace();
                    Data.sMusicBinder.resetMusic();
                    Toast.makeText(mMainActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                for (int i = 0; i < Data.sPlayOrderList.size(); i++) {
                    if (Data.sPlayOrderList.get(i).getMusicID() == mMusicItems.get(holder.getAdapterPosition()).getMusicID()) {
                        Values.CurrentData.CURRENT_MUSIC_INDEX = i;
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Integer result) {
                READY = true;

                mMainActivity.getMusicDetailFragment().getMyWaitListAdapter().notifyDataSetChanged();       //更新UI

                //set InfoBar
                mMainActivity.getMusicDetailFragment().setSlideInfo(clickedSongName, clickedSongAlbumName, cover);
                mMainActivity.getMusicDetailFragment().setCurrentInfo(clickedSongName, clickedSongAlbumName, cover);

                Utils.Ui.setPlayButtonNowPlaying();
                mMainActivity.getMusicDetailFragment().getHandler().sendEmptyMessage(Values.HandlerWhat.INIT_SEEK_BAR);
            }
        }.execute());

        view.setOnLongClickListener(v -> {
            if (Data.sMusicBinder.isPlayingMusic()) {
                Utils.SendSomeThing.sendPause(mContext);
            }
            return true;
        });
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        if (viewHolder instanceof ItemHolder) {

            ItemHolder holder = ((ItemHolder) viewHolder);

            Object tag = holder.mMusicCoverImage.getTag(R.string.key_id_1);
            if (tag != null && (int) tag != i) {
                GlideApp.with(mMainActivity).clear(holder.mMusicCoverImage);
            }

            currentBind = holder;

            /* show song name, use songNameList */
            Values.CurrentData.CURRENT_BIND_INDEX_MUSIC_LIST = viewHolder.getAdapterPosition();

            holder.mMusicText.setText(mMusicItems.get(i).getMusicName());
            holder.mMusicAlbumName.setText(mMusicItems.get(i).getMusicAlbum());
            String prefix = mMusicItems.get(i).getMusicPath().substring(mMusicItems.get(i).getMusicPath().lastIndexOf(".") + 1);
            holder.mMusicExtName.setText(prefix);
            holder.mTime.setText(Data.sSimpleDateFormat.format(new Date(mMusicItems.get(i).getDuration())));

            initStyle();

            /*--- 添加标记以便避免ImageView因为ViewHolder的复用而出现混乱 ---*/
            holder.mMusicCoverImage.setTag(R.string.key_id_1, i);

            new MyTask(holder.mMusicCoverImage, mMusicItems, mContext, i).execute();

        }

    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        if (holder instanceof ItemHolder)
            ((ItemHolder) holder).mMusicCoverImage.setTag(R.string.key_id_1, null);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        if (holder instanceof ItemHolder) {
            GlideApp.with(mMainActivity).clear(((ItemHolder) holder).mMusicCoverImage);
        }

        super.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(@NonNull ViewHolder holder) {
        if (holder instanceof ItemHolder) {
            Log.d(TAG, "onFailedToRecycleView: " + ((ItemHolder) holder).mMusicText);
            GlideApp.with(mMainActivity).clear(((ItemHolder) holder).mMusicCoverImage);
            holder.itemView.setBackgroundColor(Color.RED);
            ((ItemHolder) holder).mMusicText.setText("This item recycler failed...");
        }
        return super.onFailedToRecycleView(holder);
    }

    @Override
    public int getItemCount() {
        return mMusicItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return MOD_TYPE;
        }
        return 0;
    }

    @Override
    public void initStyle() {

        //type_background
        if (Values.Style.NIGHT_MODE) currentBind.mMusicExtName.setBackgroundColor(Color.GRAY);
        else {
            if (currentBind.mMusicExtName.getText().equals("mp3"))
                currentBind.mMusicExtName.setBackgroundResource(R.color.mp3TypeColor);
            else
                currentBind.mMusicExtName.setBackgroundColor(Color.CYAN);
        }

        //style
        currentBind.mMusicText.setTextColor(Color.parseColor(Values.Color.TEXT_COLOR));
    }

    static class MyTask extends AsyncTask<Void, Void, String> {

        private WeakReference<ImageView> mImageViewWeakReference;

        private WeakReference<Context> mContextWeakReference;

        private WeakReference<List<MusicItem>> mListWeakReference;

        private int mPosition;

        MyTask(ImageView imageView, List<MusicItem> musicItems, Context context, int position) {
            mImageViewWeakReference = new WeakReference<>(imageView);
            mContextWeakReference = new WeakReference<>(context);
            mPosition = position;
            mListWeakReference = new WeakReference<>(musicItems);
        }

        @Override
        protected void onPostExecute(String result) {

            if (mImageViewWeakReference.get() == null || result == null) return;

            if (result.equals("null")) {
                Log.d(TAG, "onPostExecute: image is null");
                GlideApp.with(mContextWeakReference.get()).load(R.drawable.ic_audiotrack_24px).into(mImageViewWeakReference.get());
                return;
            }

            GlideApp.with(mContextWeakReference.get()).load(result)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .override(Values.MAX_HEIGHT_AND_WIDTH, Values.MAX_HEIGHT_AND_WIDTH)
                    .skipMemoryCache(true)
                    .into(mImageViewWeakReference.get());
        }

        @Override
        protected String doInBackground(Void... voids) {

            if (mImageViewWeakReference.get() == null || mImageViewWeakReference.get().getTag(R.string.key_id_1) == null) {
                Log.e(TAG, "doInBackground: key null------------------skip");
                return null;
            }

            //根据position判断是否为复用ViewHolder
            if (((int) mImageViewWeakReference.get().getTag(R.string.key_id_1)) != mPosition) {
                Log.e(TAG, "doInBackground: key error------------------skip");
                GlideApp.with(mContextWeakReference.get()).clear(mImageViewWeakReference.get());
                return null;
            }

            String img;
            Cursor cursor = mContextWeakReference.get().getContentResolver().query(
                    Uri.parse(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI + String.valueOf(File.separatorChar) + mListWeakReference.get().get(mPosition).getAlbumId())
                    , new String[]{MediaStore.Audio.Albums.ALBUM_ART}, null, null, null);

            if (cursor != null && cursor.getCount() != 0) {
                cursor.moveToFirst();
                img = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
                cursor.close();
                return img;
            } else {
                return "null";
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class ItemHolder extends ViewHolder {

        ImageView mMusicCoverImage;

        ImageView mItemMenuButton;

        TextView mMusicText;

        TextView mMusicAlbumName;

        TextView mMusicExtName;

        TextView mTime;

        PopupMenu mPopupMenu;

        Menu mMenu;

        ConstraintLayout mExpandView;

        Button mButton1;

        Button mButton2;

        Button mButton3;

        Button mButton4;

        ItemHolder(@NonNull View itemView) {
            super(itemView);
            mMusicAlbumName = itemView.findViewById(R.id.recycler_item_music_album_name);
            mMusicCoverImage = itemView.findViewById(R.id.recycler_item_album_image);
            mMusicText = itemView.findViewById(R.id.recycler_item_music_name);
            mItemMenuButton = itemView.findViewById(R.id.recycler_item_menu);
            mMusicExtName = itemView.findViewById(R.id.recycler_item_music_type_name);
            mTime = itemView.findViewById(R.id.recycler_item_time);
            mExpandView = itemView.findViewById(R.id.music_item_expand_view);

            mButton1 = itemView.findViewById(R.id.expand_button_1);
            mButton2 = itemView.findViewById(R.id.expand_button_2);
            mButton3 = itemView.findViewById(R.id.expand_button_3);
            mButton4 = itemView.findViewById(R.id.expand_button_4);

            mPopupMenu = new PopupMenu(mMainActivity, mItemMenuButton);
            mMenu = mPopupMenu.getMenu();

            Resources resources = mMainActivity.getResources();

            //Menu Load
            //noinspection PointlessArithmeticExpression
            mMenu.add(Menu.NONE, Menu.FIRST + 0, 0, resources.getString(R.string.next_play));
            mMenu.add(Menu.NONE, Menu.FIRST + 2, 0, resources.getString(R.string.add_to_playlist));
            mMenu.add(Menu.NONE, Menu.FIRST + 5, 0, resources.getString(R.string.more_info));
            mMenu.add(Menu.NONE, Menu.FIRST + 1, 0, resources.getString(R.string.love_music));
            if (!mCurrentUiPosition.equals(AlbumDetailActivity.TAG)) {
                mMenu.add(Menu.NONE, Menu.FIRST + 4, 0, resources.getString(R.string.show_album));
            }

            MenuInflater menuInflater = mMainActivity.getMenuInflater();
            menuInflater.inflate(R.menu.recycler_song_item_menu, mMenu);
        }
    }

    class ModHolder extends ItemHolder {

        ConstraintLayout mRandomItem;

        ModHolder(@NonNull View itemView) {
            super(itemView);
            mRandomItem = itemView.findViewById(R.id.random_play_item);
        }
    }

}
