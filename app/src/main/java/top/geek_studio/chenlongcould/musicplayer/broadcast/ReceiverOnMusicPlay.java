package top.geek_studio.chenlongcould.musicplayer.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import top.geek_studio.chenlongcould.musicplayer.Data;
import top.geek_studio.chenlongcould.musicplayer.Values;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.fragment.MusicDetailFragment;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chenlongcould
 */
public final class ReceiverOnMusicPlay extends BroadcastReceiver {

	public static final String TAG = "ReceiverOnMusicPlay";

	/**
	 * action type
	 *
	 * @see #INTENT_PLAY_TYPE
	 */
	public static final int CASE_TYPE_SHUFFLE = 90;
	public static final int CASE_TYPE_ITEM_CLICK = 15;
	public static final int CASE_TYPE_NOTIFICATION_RESUME = 2;
	public static final int RECEIVE_TYPE_COMMON = 6;

	public static void setDataSource(String path) {
		Data.S_HISTORY_PLAY.add(Data.sCurrentMusicItem);
		try {
			Data.sMusicBinder.setDataSource(path);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static final String INTENT_PLAY_TYPE = "play_type";
	public static final String INTENT_ARGS = "args";

	public static final String TYPE_NEXT = "next";
	public static final String TYPE_PREVIOUS = "previous";
	public static final String TYPE_SLIDE = "slide";

	/**
	 * the mediaPlayer is Ready?
	 */
	public static AtomicBoolean READY = new AtomicBoolean(true);

	/**
	 * setSeekBar
	 */
	private static void reSetSeekBar(Context context) {
		setSeekBar(context, 0);
	}

	private static void setSeekBar(Context context, int val) {
		Intent intent = new Intent();
		intent.setAction(MusicDetailFragment.BroadCastAction.ACTION_SET_SEEK_BAR);
		intent.putExtra("value", val);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	/**
	 * setButtonPlay
	 * setSeekBarColor
	 * setSeekBarPosition
	 * setSlideBar
	 *
	 * @param context fragment
	 * @param targetIndex         index of list
	 */
	private static void uiSet(final Context context, final int targetIndex) {
		final String musicName = Data.sPlayOrderList.get(targetIndex).getMusicName();
		final String albumName = Data.sPlayOrderList.get(targetIndex).getMusicAlbum();
		Data.setCurrentCover(Utils.Audio.getCoverBitmap(context, Data.sPlayOrderList.get(targetIndex).getAlbumId()));

		Message message = Message.obtain();
		message.what = MusicDetailFragment.HandlerWhat.SET_MUSIC_DATA;
		Bundle bundle = new Bundle();
		bundle.putString("name", musicName);
		bundle.putString("albumName", albumName);
		message.setData(bundle);
		MusicDetailFragment.mHandler.sendMessage(message);

		//防止seekBar跳动到Max
		reSetSeekBar(context);
		MusicDetailFragment.mHandler.sendEmptyMessage(MusicDetailFragment.HandlerWhat.INIT_SEEK_BAR);
		MusicDetailFragment.mHandler.sendEmptyMessage(MusicDetailFragment.HandlerWhat.RECYCLER_SCROLL);

		// FIXME: 2019/5/22 static context
		((MainActivity) Data.sActivities.get(0)).getMainBinding().slidingLayout.setTouchEnabled(true);
	}

	/**
	 * setFlags
	 *
	 * @param targetIndex index
	 */
	private static void setFlags(int targetIndex) {
		Values.CurrentData.CURRENT_MUSIC_INDEX = targetIndex;
	}

	////////////////////////MEDIA CONTROL/////////////////////////////

	public static void playMusic() {

		try {
			Data.sMusicBinder.setCurrentMusicData(Data.sCurrentMusicItem);
			Data.sMusicBinder.playMusic();
			Data.HAS_PLAYED = true;
		} catch (RemoteException e) {
			e.printStackTrace();
			try {
				Data.sMusicBinder.resetMusic();
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		}
	}

	public static void pauseMusic() {
		try {
			if (Data.sMusicBinder != null) Data.sMusicBinder.pauseMusic();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static void resetMusic() {
		try {
			if (Data.sMusicBinder != null) Data.sMusicBinder.resetMusic();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static void prepare() {
		try {
			if (Data.sMusicBinder != null) Data.sMusicBinder.prepare();
		} catch (RemoteException e) {
			e.printStackTrace();
			try {
				Data.sMusicBinder.resetMusic();
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unused")
	public static void stopMusic() {
		try {
			Data.sMusicBinder.stopMusic();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static int getDuration() {
		try {
			return Data.sMusicBinder.getDuration();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static boolean isPlayingMusic() {
		try {
			return Data.sMusicBinder.isPlayingMusic();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static int getCurrentPosition() {
		try {
			return Data.sMusicBinder.getCurrentPosition();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static void seekTo(int nowPosition) {
		try {
			Data.sMusicBinder.seekTo(nowPosition);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public static void sureCar() {
		//set data (image and name)
		if (Values.CurrentData.CURRENT_UI_MODE.equals(Values.UIMODE.MODE_CAR)) {
			Data.sCarViewActivity.getFragmentLandSpace().setData();
		}
	}

	@Nullable
	public static MusicItem getCurrentItem() {
		if (Data.sMusicBinder != null) {
			try {
				return Data.sMusicBinder.getCurrentItem();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			return null;
		}
		return null;
	}

	////////////////////////MEDIA CONTROL/////////////////////////////

	@Override
	public void onReceive(Context context, Intent intent) {

		final int type = intent.getIntExtra(INTENT_PLAY_TYPE, 0);

		Intent playIntent = new Intent();
		playIntent.setAction(type == -1 ? MusicDetailFragment.BroadCastAction.ACTION_CHANGE_MUSIC_PAUSE
				: MusicDetailFragment.BroadCastAction.ACTION_CHANGE_MUSIC_PLAY);
		LocalBroadcastManager.getInstance(context).sendBroadcast(playIntent);

		///////////////////////////BEFORE PLAYER SET/////////////////////////////////////////

		switch (type) {
			//clicked by notification, just resume play
			case CASE_TYPE_NOTIFICATION_RESUME: {
				Utils.Ui.setPlayButtonNowPlaying();
				playMusic();
			}
			break;

			//pause music
			case -1: {
				pauseMusic();
			}
			break;

			//Type Random (play)
			case CASE_TYPE_SHUFFLE: {
				if (!READY.get()) {
					break;
				}
				new FastShufflePlayback().execute();
			}
			break;

			//by next button...(in detail or noti) (must ActivityList isn't empty)
			//by auto-next(mediaPlayer OnCompletionListener) of next-play by user, at this time MainActivity is present
			//by MusicDetailFragment preview imageButton (view history song list)
			case RECEIVE_TYPE_COMMON: {

				//检测是否指定下一首播放
				if (Data.sNextWillPlayItem != null) {
					new DoesHasNextPlay().execute();
					break;
				}

				//检测循环
				if (Values.CurrentData.CURRENT_PLAY_TYPE.equals(Values.TYPE_REPEAT_ONE)) {
					seekTo(0);
					playMusic();
					break;
				}

				//检测大小
				if (Data.sPlayOrderList.size() <= 0) {
					Toast.makeText(context, "Data.sPlayOrderList.size() <= 0", Toast.LENGTH_SHORT).show();
					break;
				}

				//检测前后播放
				int targetIndex = 0;
				if (intent.getStringExtra(INTENT_ARGS) != null) {
					if (intent.getStringExtra(INTENT_ARGS).contains(TYPE_NEXT)) {
						targetIndex = Values.CurrentData.CURRENT_MUSIC_INDEX + 1;
						//超出范围自动跳转0
						if (targetIndex > Data.sPlayOrderList.size() - 1) {
							targetIndex = 0;
						}
					} else if (intent.getStringExtra(INTENT_ARGS).contains(TYPE_PREVIOUS)) {
						targetIndex = Values.CurrentData.CURRENT_MUSIC_INDEX - 1;
						if (targetIndex < 0) {
							//超出范围超转最后
							targetIndex = Data.sPlayOrderList.size() - 1;
						}
					}
				}
				Values.CurrentData.CURRENT_MUSIC_INDEX = targetIndex;

				Data.sCurrentMusicItem = Data.sPlayOrderList.get(targetIndex);

				playMusicInOne(Data.sPlayOrderList.get(targetIndex).getMusicPath());

				setFlags(targetIndex);

				//load data
				if (!Data.sActivities.isEmpty()) {
					final MusicDetailFragment musicDetailFragment = ((MainActivity) Data.sActivities.get(0)).getMusicDetailFragment();

					//slide 滑动切歌无需再次加载albumCover
					if (intent.getStringExtra(INTENT_ARGS) != null && intent.getStringExtra(INTENT_ARGS).contains(TYPE_SLIDE)) {
						final String musicName = Data.sPlayOrderList.get(targetIndex).getMusicName();
						final String albumName = Data.sPlayOrderList.get(targetIndex).getMusicAlbum();
						final Bitmap cover = Utils.Audio.getCoverBitmap(context, Data.sPlayOrderList.get(targetIndex).getAlbumId());

						Utils.Ui.setPlayButtonNowPlaying();
						musicDetailFragment.setCurrentInfoWithoutMainImage(musicName, albumName, cover);
						reSetSeekBar(context);

						musicDetailFragment.getHandler().sendEmptyMessage(MusicDetailFragment.HandlerWhat.INIT_SEEK_BAR);
						musicDetailFragment.getHandler().sendEmptyMessage(MusicDetailFragment.HandlerWhat.RECYCLER_SCROLL);
					} else {
						uiSet(context, targetIndex);
					}
					sureCar();
				}
			}
			break;

			//by MusicListFragment item click
			case CASE_TYPE_ITEM_CLICK: {
				//set current data
				Data.sCurrentMusicItem = Data.sMusicItems.get(Integer.parseInt(intent.getStringExtra(INTENT_ARGS)));

				playMusicInOne(Data.sCurrentMusicItem.getMusicPath());

				sureCar();

				Utils.Ui.setPlayButtonNowPlaying();

				//update seek
				final Intent initSeekBarIntent = new Intent();
				initSeekBarIntent.setAction(MusicDetailFragment.BroadCastAction.ACTION_INIT_SEEK_BAR);

				final Intent updateInfo = new Intent();
				updateInfo.setAction(MusicDetailFragment.BroadCastAction.ACTION_UPDATE_CURRENT_INFO);

				LocalBroadcastManager.getInstance(context).sendBroadcast(updateInfo);
				LocalBroadcastManager.getInstance(context).sendBroadcast(initSeekBarIntent);

				MainActivity.mHandler.sendEmptyMessage(MainActivity.SET_SLIDE_TOUCH_ENABLE);

			}
			break;
			default:
		}

		///////////////////////////AFTER PLAYER SET/////////////////////////////////////////

		final Intent pauseIntent = new Intent();
		pauseIntent.setAction(MusicDetailFragment.BroadCastAction.ACTION_SCROLL);
		LocalBroadcastManager.getInstance(context).sendBroadcast(pauseIntent);

		if (!Data.sActivities.isEmpty()) {
			((MainActivity) Data.sActivities.get(0)).getMainBinding().slidingLayout.setTouchEnabled(true);
		}

	}

	/**
	 * Receive Types
	 */
	@SuppressWarnings("unused")
	public interface ReceiveType {
		int CASE_TYPE_SHUFFLE = 90;
		int CASE_TYPE_ITEM_CLICK = 15;
		int CASE_TYPE_NOTIFICATION_RESUME = 2;
		int RECEIVE_TYPE_COMMON = 6;
	}

	private void playMusicInOne(@NonNull String path) {
		resetMusic();
		setDataSource(path);
		prepare();
		playMusic();
	}

	/**
	 * 当下一首歌曲存在(被手动指定时), auto-next-play and next-play will call this method
	 */
	public static class DoesHasNextPlay extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... voids) {

			if (Data.sNextWillPlayItem != null) {
				resetMusic();
				ReceiverOnMusicPlay.setDataSource(Data.sNextWillPlayItem.getMusicPath());
				prepare();
				playMusic();
			} else {
				return -1;
			}
			return 0;
		}

		@Override
		protected void onPostExecute(Integer status) {

			//error
			if (status != 0) {
				return;
			}

			if (!Data.sActivities.isEmpty()) {

				final MusicDetailFragment musicDetailFragment = ((MainActivity) Data.sActivities.get(0)).getMusicDetailFragment();

				final Bitmap cover = Utils.Audio.getCoverBitmap(musicDetailFragment.getContext(), Data.sNextWillPlayItem.getAlbumId());
				sureCar();

				Utils.Ui.setPlayButtonNowPlaying();
				musicDetailFragment.setCurrentInfo(Data.sNextWillPlayItem.getMusicName(), Data.sNextWillPlayItem.getMusicAlbum(), cover);

				reSetSeekBar(musicDetailFragment.getContext());         //防止seekBar跳动到Max
				musicDetailFragment.getHandler().sendEmptyMessage(MusicDetailFragment.HandlerWhat.INIT_SEEK_BAR);
				musicDetailFragment.getHandler().sendEmptyMessage(MusicDetailFragment.HandlerWhat.RECYCLER_SCROLL);
			}

			Data.sNextWillPlayItem = null;
		}
	}

	/**
	 * play_type: random (by nextButton or auto-next)
	 * just make a random Index (data by {@link Data#sPlayOrderList})
	 */
	public static class FastShufflePlayback extends AsyncTask<String, Void, Integer> {

		String path;

		int index;

		@Override
		protected Integer doInBackground(String... strings) {
			if (Data.sPlayOrderList.isEmpty()) {
				return -1;
			}

			READY.set(false);
			resetMusic();

			//get data
			final Random random = new Random();
			final int index = random.nextInt(Data.sPlayOrderList.size());
			this.index = index;
			Values.CurrentData.CURRENT_MUSIC_INDEX = index;

			Data.sCurrentMusicItem = Data.sPlayOrderList.get(index);

			path = Data.sPlayOrderList.get(index).getMusicPath();

			setFlags(index);

			setDataSource(path);
			prepare();
			playMusic();

			return 0;
		}

		@Override
		protected void onPostExecute(Integer status) {
			if (status != 0) {
				return;
			}

			if (Data.sActivities.size() >= 1) {
				uiSet(Data.sActivities.get(0), index);
				sureCar();
			}

			READY.set(true);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			READY.set(true);
		}
	}

}
