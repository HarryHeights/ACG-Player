package top.geek_studio.chenlongcould.musicplayer.adapter;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.view.animation.OvershootInterpolator;
import android.widget.*;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.parser.XmlTreeBuilder;
import org.jsoup.select.Elements;
import org.litepal.LitePal;
import top.geek_studio.chenlongcould.geeklibrary.DownloadUtil;
import top.geek_studio.chenlongcould.geeklibrary.HttpUtil;
import top.geek_studio.chenlongcould.musicplayer.*;
import top.geek_studio.chenlongcould.musicplayer.activity.AlbumDetailActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.BaseCompatActivity;
import top.geek_studio.chenlongcould.musicplayer.activity.MainActivity;
import top.geek_studio.chenlongcould.musicplayer.broadcast.ReceiverOnMusicPlay;
import top.geek_studio.chenlongcould.musicplayer.database.CustomAlbumPath;
import top.geek_studio.chenlongcould.musicplayer.model.MusicItem;
import top.geek_studio.chenlongcould.musicplayer.threadPool.ItemCoverThreadPool;
import top.geek_studio.chenlongcould.musicplayer.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author chenlongcould
 */
public final class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

	private static final String TAG = "MyRecyclerAdapter";

	/**
	 * @see top.geek_studio.chenlongcould.musicplayer.R.layout#recycler_music_list_item_mod
	 */
	private static final int MOD_TYPE = -1;

	/**
	 * the json data, from network
	 */
	private static final String RESULT_OK = "ok";

	/**
	 * MAIN
	 */
	private BaseCompatActivity mActivity;

	/**
	 * 媒体库，默认顺序排序
	 */
	private List<MusicItem> mMusicItems;

	private ArrayList<ItemHolder> mViewHolders = new ArrayList<>();

	private ArrayList<Integer> mSelected = new ArrayList<>();

	private boolean isChoose = false;

	private Config mConfig;

	public MyRecyclerAdapter(BaseCompatActivity activity, List<MusicItem> musicItems, @NonNull Config config) {
		mActivity = activity;
		mMusicItems = musicItems;
		mConfig = config;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int itemType) {

		View view;

		ItemHolder holder;

		/*
		 * ModHolder: baseHolder + ModHolder
		 * baseHolder: common item
		 * ModHolder: common item + fastPlay item
		 * */
		//在 MusicListFragment 的第一选项上面添加"快速随机播放项目"
		if (itemType == MOD_TYPE && mActivity.getActivityTAG().equals(MainActivity.TAG)) {

			//style switch
			switch (mConfig.styleId) {
				case 1: {
					view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_music_list_item_mod_style_1, viewGroup, false);
					holder = new ModHolderS1(view);
				}
				break;
				default: {
					view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_music_list_item_mod, viewGroup, false);
					holder = new ModHolder(view);
				}

			}

			//when clicked ModHolder(fastPlay item)
			holder.itemView.setOnClickListener(v -> Utils.SendSomeThing.sendPlay(mActivity, ReceiverOnMusicPlay.CASE_TYPE_SHUFFLE, "null"));

		} else {
			switch (mConfig.styleId) {
				case 1: {
					view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_music_list_item_style_1, viewGroup, false);
					holder = new ItemHolderS1(view);
				}
				break;
				default: {
					view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_music_list_item, viewGroup, false);
					holder = new ItemHolder(view);
				}
			}

		}

		mViewHolders.add(holder);

		//默认设置扩展button opacity 0, (default)
		holder.mButton1.setAlpha(0);
		holder.mButton2.setAlpha(0);
		holder.mButton3.setAlpha(0);
		holder.mButton4.setAlpha(0);
		holder.mButton1.setVisibility(View.INVISIBLE);
		holder.mButton2.setVisibility(View.INVISIBLE);
		holder.mButton3.setVisibility(View.INVISIBLE);
		holder.mButton4.setVisibility(View.INVISIBLE);
		holder.mExpandText.setVisibility(View.INVISIBLE);

		holder.mExpandView.setVisibility(View.VISIBLE);
		holder.mExpandView.setAlpha(1f);

		holder.mCoverReference.get().setOnClickListener(v -> {

			holder.mExpandView.clearAnimation();

			holder.mButton1.setVisibility(View.VISIBLE);
			holder.mButton2.setVisibility(View.VISIBLE);
			holder.mButton3.setVisibility(View.VISIBLE);
			holder.mButton4.setVisibility(View.VISIBLE);
			holder.mExpandText.setVisibility(View.VISIBLE);

			final ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) holder.mExpandView.getLayoutParams();

			final ValueAnimator animator = new ValueAnimator();
			animator.setDuration(300);

			if (((ConstraintLayout.LayoutParams) holder.mExpandView.getLayoutParams()).topMargin == 0) {
				animator.setIntValues(0, (int) mActivity.getResources().getDimension(R.dimen.recycler_expand_view));
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
				animator.setIntValues((int) mActivity.getResources().getDimension(R.dimen.recycler_expand_view), 0);
				animator.addListener(new Animator.AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animation) {

					}

					@Override
					public void onAnimationEnd(Animator animation) {
						//set default...
						holder.mButton1.setAlpha(0);
						holder.mButton2.setAlpha(0);
						holder.mButton3.setAlpha(0);
						holder.mButton4.setAlpha(0);

						holder.mButton1.setVisibility(View.INVISIBLE);
						holder.mButton2.setVisibility(View.INVISIBLE);
						holder.mButton3.setVisibility(View.INVISIBLE);
						holder.mButton4.setVisibility(View.INVISIBLE);
						holder.mExpandText.setVisibility(View.INVISIBLE);

					}

					@Override
					public void onAnimationCancel(Animator animation) {

					}

					@Override
					public void onAnimationRepeat(Animator animation) {

					}
				});
			}

			//cover rotation
			final ValueAnimator rotationAnima = new ValueAnimator();
			rotationAnima.setFloatValues(0f, 360f);
			rotationAnima.setDuration(300);
			rotationAnima.setInterpolator(new OvershootInterpolator());
			rotationAnima.addUpdateListener(animation -> holder.mCoverReference.get().setRotation((Float) animation.getAnimatedValue()));
			rotationAnima.start();

			animator.addUpdateListener(animation -> {
				layoutParams.setMargins(0, (int) animation.getAnimatedValue(), 0, 0);
				holder.mExpandView.setLayoutParams(layoutParams);
				holder.mExpandView.requestLayout();
			});

			animator.start();

		});

		// TODO: 2018/12/5 button
		holder.mButton1.setOnClickListener(v -> Toast.makeText(mActivity, "1", Toast.LENGTH_SHORT).show());
		holder.mButton2.setOnClickListener(v -> Toast.makeText(mActivity, "2", Toast.LENGTH_SHORT).show());
		holder.mButton3.setOnClickListener(v -> Utils.Audio.setRingtone(mActivity, mMusicItems.get(holder.getAdapterPosition()).getMusicID()));
		holder.mButton4.setOnClickListener(v -> mActivity.startActivity(Intent.createChooser(Utils.Audio.createShareSongFileIntent(mMusicItems.get(holder.getAdapterPosition()), mActivity), null)));

		holder.mItemMenuButton.setOnClickListener(v -> holder.mPopupMenu.show());

		holder.mPopupMenu.setOnMenuItemClickListener(item -> {

			@SuppressWarnings("UnnecessaryLocalVariable") int index = holder.getAdapterPosition();

			Values.CurrentData.CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = index;

			switch (item.getItemId()) {
				//noinspection PointlessArithmeticExpression
				case Menu.FIRST + 0: {
					final MusicItem target = mMusicItems.get(holder.getAdapterPosition());
					if (!target.equals(Data.sNextWillPlayItem)) {
						Data.sNextWillPlayItem = target;
					}
				}
				break;

				//add to list
				case Menu.FIRST + 2: {
					Utils.DataSet.addListDialog(mActivity, mMusicItems.get(holder.getAdapterPosition()));
				}
				break;

				/* in ListViewActivity */
				case Menu.FIRST + 3: {

				}
				break;

				// show album
				case Menu.FIRST + 4: {
					final String albumName = mMusicItems.get(holder.getAdapterPosition()).getMusicAlbum();
					final Cursor cursor = mActivity.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null,
							MediaStore.Audio.Albums.ALBUM + "= ?", new String[]{albumName}, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
					if (cursor != null && cursor.getCount() > 0) {
						cursor.moveToFirst();
						int id = Integer.parseInt(cursor.getString(0));
						cursor.close();

						final Intent intent = new Intent(mActivity, AlbumDetailActivity.class);
						intent.putExtra(AlbumDetailActivity.IntentKey.ALBUM_NAME, albumName);
						intent.putExtra(AlbumDetailActivity.IntentKey.ID, id);
						mActivity.startActivity(intent);
					} else {
						Toast.makeText(mActivity, "Cursor error, please check your MediaStore.", Toast.LENGTH_SHORT).show();
					}

				}
				break;

				// show song detail
				case Menu.FIRST + 5: {
					final List<String> data = Utils.Audio.extractMetadata(mMusicItems.get(holder.getAdapterPosition()));
					ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_list_item_1
							, data);
					AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
							.setTitle(mActivity.getString(R.string.detail))
							.setAdapter(arrayAdapter, (dialog, which) -> {
								final ClipboardManager clipboardManager = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
								final ClipData clipData = new ClipData("Copied by song detail", new String[]{"text"}, new ClipData.Item(data.get(which).split(":")[1]));
								clipboardManager.setPrimaryClip(clipData);
							})
							.setCancelable(false)
							.setNegativeButton(mActivity.getString(R.string.done), (dialog, which) -> dialog.dismiss());
					builder.show();
				}
				break;

				//share
				case Menu.FIRST + 6: {
					Intent intent = new Intent(Intent.ACTION_SEND);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
						intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(mActivity, mActivity.getApplicationContext().getPackageName(), new File(mMusicItems.get(holder.getAdapterPosition()).getMusicPath())));
						intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
						intent.setType("audio/*");
					} else {
						intent.setDataAndType(Uri.fromFile(new File(mMusicItems.get(holder.getAdapterPosition()).getMusicPath())), "audio/*");
					}

					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

					/*
					 * by Karim Abou Zeid (kabouzeid)
					 * */
					try {
						mActivity.startActivity(intent);
					} catch (IllegalArgumentException e) {
						// TODO the path is most likely not like /storage/emulated/0/... but something like /storage/28C7-75B0/...
						e.printStackTrace();
						Toast.makeText(mActivity, "Could not share this file, I'm aware of the issue.", Toast.LENGTH_SHORT).show();
					}
				}
				break;
				default:
			}

			Values.CurrentData.CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = -1;

			return true;
		});

		onMusicItemClick(holder.mUView, holder);

		holder.mUView.setOnLongClickListener(v -> {
			for (int id : mSelected) {
				if (id == mMusicItems.get(holder.getAdapterPosition()).getMusicID()) {
					holder.mBody.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.card_bg));
					mSelected.remove((Integer) mMusicItems.get(holder.getAdapterPosition()).getMusicID());
					if (mSelected.size() == 0) {
						isChoose = false;
						mActivity.inflateCommonMenu();
					}
					return true;
				}
			}

			mSelected.add(mMusicItems.get(holder.getAdapterPosition()).getMusicID());
			isChoose = true;

			holder.mBody.setBackgroundColor(Utils.Ui.getAccentColor(mActivity));

			mActivity.inflateChooseMenu();
			return true;
		});

		return holder;
	}

	@NonNull
	@Override
	public String getSectionName(int position) {
		return String.valueOf(mMusicItems.get(position).getMusicName().charAt(0));
	}

	@Override
	public void onViewRecycled(@NonNull ViewHolder holder) {
		super.onViewRecycled(holder);
		if (holder instanceof ItemHolder) {
			ItemHolder itemHolder = ((ItemHolder) holder);
			itemHolder.mCoverReference.get().setTag(R.string.key_id_1, null);
			GlideApp.with(mActivity).clear(itemHolder.mCoverReference.get());
			GlideApp.get(mActivity).clearMemory();
		}
	}

	private void onMusicItemClick(View view, ViewHolder holder) {
		view.setOnClickListener(v -> {
			//在多选模式下
			if (isChoose) {
				for (int id : mSelected) {
					if (id == mMusicItems.get(holder.getAdapterPosition()).getMusicID()) {
						((ItemHolder) holder).mBody.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.card_bg));
						mSelected.remove((Integer) mMusicItems.get(holder.getAdapterPosition()).getMusicID());
						if (mSelected.size() == 0) {
							isChoose = false;
							mActivity.inflateCommonMenu();
						}
						return;
					}
				}

				mSelected.add(mMusicItems.get(holder.getAdapterPosition()).getMusicID());
				((ItemHolder) holder).mBody.setBackgroundColor(Utils.Ui.getAccentColor(mActivity));
			} else {

				Data.sCurrentMusicItem = mMusicItems.get(holder.getAdapterPosition());
				if (mConfig.recordIndex) {
					Values.CurrentData.CURRENT_MUSIC_INDEX = Data.sPlayOrderList.indexOf(Data.sCurrentMusicItem);
				}
				Utils.SendSomeThing.sendPlay(mActivity, ReceiverOnMusicPlay.CASE_TYPE_ITEM_CLICK, "null");

			}
		});
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

		final ItemHolder holder = ((ItemHolder) viewHolder);

		//check selection
		if (mSelected.contains(mMusicItems.get(holder.getAdapterPosition()).getMusicID())) {
			holder.mBody.setBackgroundColor(Utils.Ui.getAccentColor(mActivity));
		} else {
			holder.mBody.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.card_bg));
		}

		holder.mMusicText.setText(mMusicItems.get(holder.getAdapterPosition()).getMusicName());
		holder.mMusicAlbumName.setText(mMusicItems.get(holder.getAdapterPosition()).getMusicAlbum());
		String prefix = mMusicItems.get(holder.getAdapterPosition()).getMusicPath().substring(mMusicItems.get(holder.getAdapterPosition()).getMusicPath().lastIndexOf(".") + 1);
		holder.mMusicExtName.setText(prefix);
		holder.mTime.setText(Data.S_SIMPLE_DATE_FORMAT.format(new Date(mMusicItems.get(holder.getAdapterPosition()).getDuration())));

		/*--- 添加标记以便避免ImageView因为ViewHolder的复用而出现混乱 ---*/
		holder.mCoverReference.get().setTag(R.string.key_id_1, holder.getAdapterPosition());

		ItemCoverThreadPool.post(() -> {

			albumLoader(mActivity, holder.mCoverReference.get(), mMusicItems.get(i).getAlbumId()
					, mMusicItems.get(i).getArtist(), mMusicItems.get(i).getMusicAlbum());

			switch (mConfig.styleId) {
				case 1: {
					ItemHolderS1 holderS1 = (ItemHolderS1) holder;
					final Bitmap bitmap = Utils.Ui.readBitmapFromFile(Utils.Audio.getCoverPath(mActivity, mMusicItems.get(i).getAlbumId()), 50, 50);
					if (bitmap != null) {
						//color set (album tag)
						Palette.from(bitmap).generate(p -> {
							if (p != null) {
								@ColorInt int color = p.getVibrantColor(ContextCompat.getColor(mActivity, R.color.notVeryBlack));
								GradientDrawable drawable = new GradientDrawable();
								drawable.setStroke(((int) mActivity.getResources().getDimension(R.dimen.frame_width) * 2), color);
								drawable.setCornerRadius(mActivity.getResources().getDimension(R.dimen.frame_corners));
								holderS1.mFrame.setBackground(drawable);
								bitmap.recycle();
							}
						});
					}
				}
				break;
				default:
			}

		});
	}

	/**
	 * loader
	 * load image to imageView (net, defDB, customDB, defAlbum)
	 * <p>
	 * 1. DEFAULT DB {@link MediaStore.Audio.Albums#EXTERNAL_CONTENT_URI}
	 * <p>
	 * 2. NET WORK <a href="http://ws.audioscrobbler.com"/>
	 */
	private void albumLoader(@NonNull final Context activity, @NonNull final ImageView imageView, final int albumId, @NonNull final String artist, @NonNull final String albumName) {
		final String[] albumPath = {null};

		final Cursor cursor = activity.getContentResolver().query(
				Uri.parse(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI + String.valueOf(File.separatorChar) + albumId)
				, new String[]{MediaStore.Audio.Albums.ALBUM_ART}, null, null, null);

		if (cursor != null && cursor.getCount() != 0) {
			cursor.moveToFirst();
			albumPath[0] = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
			cursor.close();
		} else {
			Log.d(TAG, "albumLoader: the DEFAULT_DB is null or empty!");
		}

		final String baseCoverPath = albumPath[0];

		if (baseCoverPath != null && !TextUtils.isEmpty(baseCoverPath)) {
			final File file = new File(baseCoverPath);
			if (file.exists()) {
				Log.d(TAG, "albumLoader: the album id DEFAULT_DB is ability, loading def");
				loadPath2ImageView(baseCoverPath, imageView);
			} else {
				//load default res
				imageView.post(() -> GlideApp.with(imageView)
						.load(R.drawable.default_album_art)
						.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
						.centerCrop()
						.override(100, 100)
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						.into(imageView));
			}
		} else {
			Log.d(TAG, "albumLoader: the album id DEFAULT_DB is NOT ability, loading from {network or diskCache}");
			//检查是否勾选了网络Album
			if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(Values.SharedPrefsTag.USE_NET_WORK_ALBUM, false)) {
				final List<CustomAlbumPath> customs = LitePal.where("mAlbumId = ?", String.valueOf(albumId)).find(CustomAlbumPath.class);

				//检测DB是否准备完成(IntentService 是否完成)
				if (customs.size() != 0) {
					final CustomAlbumPath custom = customs.get(0);

					try {
						final File file = new File(custom.getAlbumArt());

						//判断CUSTOM_DB下albumArt是否存在
						if ("null".equals(custom.getAlbumArt()) && !file.exists()) {

							final String mayPath = ifExists(albumId);

							if (mayPath != null) {
								Log.d(TAG, "onBindViewHolder: (in CUSTOM_DB) DB not ability, path is ability, save in db and loading...");

								custom.setAlbumArt(mayPath);
								custom.save();
								loadPath2ImageView(mayPath, imageView);
							} else {
								//DB内不存在Cover, 且缓存也不存在, 进行下载
								final String request = "http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=" +
										App.LAST_FM_KEY +
										"&artist=" +
										artist +
										"&album=" +
										albumName;
								HttpUtil.sedOkHttpRequest(request, new Callback() {
									@Override
									public void onFailure(@NotNull Call call, @NotNull IOException e) {
										imageView.post(() -> Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show());
									}

									@Override
									public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
										if (response.body() != null) {

											final Document document = Jsoup.parse(response.body().string(), "UTF-8", new Parser(new XmlTreeBuilder()));
											final Elements content = document.getElementsByAttribute("status");
											final String status = content.select("lfm[status]").attr("status");

											if (RESULT_OK.equals(status)) {
												StringBuilder img = new StringBuilder(content.select("image[size=extralarge]").text());

												if (img.toString().contains("http") && img.toString().contains("https")) {
													Log.d(TAG, "onResponse: ok, now downloading...");

													DownloadUtil.get().download(img.toString(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separatorChar + "AlbumCovers"
															, albumId + "." + img.substring(img.lastIndexOf(".") + 1), new DownloadUtil.OnDownloadListener() {
																@Override
																public void onDownloadSuccess(File file) {
																	Log.d(TAG, "onDownloadSuccess: " + file.getAbsolutePath());
																	content.clear();

																	final String newPath = file.getAbsolutePath();
																	final CustomAlbumPath c = customs.get(0);
																	c.setAlbumArt(newPath);
																	c.save();

																	try {
																		if (Data.sMusicBinder.getCurrentItem().getAlbumId() == albumId && Data.getCurrentCover() == null) {
																			Data.setCurrentCover(BitmapFactory.decodeFile(newPath));
																		}
																	} catch (RemoteException e) {
																		e.printStackTrace();
																	}

																	loadFile2ImageView(file, imageView);
																}

																@Override
																public void onDownloading(int progress) {

																}

																@Override
																public void onDownloadFailed(Exception e) {
																	Log.d(TAG, "onDownloadFailed: " + img.toString() + " " + e.getMessage());
																}
															});
												} else {
													Log.d(TAG, "onResponse: img url error" + img);
													loadDefaultArt(imageView);
												}
											} else {
												Log.d(TAG, "onResponse: result not ok");
												loadDefaultArt(imageView);
											}
										} else {
											Log.d(TAG, "onResponse: response is NUll!");
											imageView.post(() -> Toast.makeText(activity, "response is NUll!", Toast.LENGTH_SHORT).show());
											loadDefaultArt(imageView);
										}
									}
								});
							}

						} else {
							Log.d(TAG, "albumLoader: has data in DB, loading...");
							loadFile2ImageView(file, imageView);
						}
					} catch (Exception e) {
						Log.d(TAG, "albumLoader: load customAlbum Error, loading default..., msg: " + e.getMessage());
						loadDefaultArt(imageView);
					}
				} else {
					Log.d(TAG, "customDB size is 0");
					loadDefaultArt(imageView);
				}
			} else {
				Log.d(TAG, "albumLoader: load from Cache..., msg: FROM NET switch not checked");
				if (!TextUtils.isEmpty(baseCoverPath) && !"null".equals(baseCoverPath)) {
					assert baseCoverPath != null;
					File file = new File(baseCoverPath);
					if (file.exists()) {
						Log.d(TAG, "albumLoader: exists...");
						loadPath2ImageView(baseCoverPath, imageView);
					} else {
						loadDefaultArt(imageView);
					}
				} else {
					Log.d(TAG, "albumLoader: not exists");
					loadDefaultArt(imageView);
				}
			}
		}

	}

	/**
	 * file type for AlbumCover or ArtistCover
	 */
	private interface FileType {
		String PNG = "png";
		String JPG = "jpg";
		String GIF = "gif";
	}

	/**
	 * check the cacheImage exists
	 *
	 * @param id the AlbumId
	 * @return if exists return the path else return null;
	 */
	private String ifExists(int id) {
		String mayPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
				+ File.separatorChar + "ArtistCovers"
				+ File.separatorChar + id + ".";

		if (new File(mayPath + FileType.PNG).exists()) {
			return mayPath + FileType.PNG;
		}

		if (new File(mayPath + FileType.JPG).exists()) {
			return mayPath + FileType.JPG;
		}

		if (new File(mayPath + FileType.GIF).exists()) {
			return mayPath + FileType.GIF;
		}

		return null;
	}

	/**
	 * config for {@link MyRecyclerAdapter}
	 * */
	public static class Config {
		/**
		 * 样式id
		 * <p>
		 * 1: 带随机播放item
		 * 0：default
		 */
		int styleId = 0;
		boolean recordIndex = true;

		public Config() {
		}

		public Config(int styleId) {
			this.styleId = styleId;
		}

		public Config(boolean recordIndex) {
			this.recordIndex = recordIndex;
		}

		public Config(int styleId, boolean recordIndex) {
			this.styleId = styleId;
			this.recordIndex = recordIndex;
		}
	}

	/**
	 * load from defaultDB {@link MediaStore.Audio.Albums}
	 */
	private void loadPath2ImageView(@NonNull final String path, @NonNull final ImageView imageView) {
		if (verify(imageView)) {
			imageView.post(() -> GlideApp.with(imageView)
					.load(path)
					.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
					.centerCrop()
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.into(imageView));
		}
	}

	/**
	 * load from defaultDB {@link MediaStore.Audio.Albums}
	 */
	private void loadFile2ImageView(@NonNull final File path, @NonNull final ImageView imageView) {
		if (verify(imageView)) {
			imageView.post(() -> imageView.post(() -> GlideApp.with(imageView)
					.load(path)
					.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
					.centerCrop()
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.into(imageView)));
		}
	}

	private void loadDefaultArt(@Nullable final ImageView imageView) {
		if (imageView != null) {
			imageView.post(() -> GlideApp.with(imageView)
					.load(R.drawable.default_album_art)
					.transition(DrawableTransitionOptions.withCrossFade(Values.DEF_CROSS_FATE_TIME))
					.centerCrop()
					.override(100, 100)
					.diskCacheStrategy(DiskCacheStrategy.NONE)
					.into(imageView));
		}
	}

	/**
	 * verify if key, null
	 *
	 * @param imageView the imageView to verify
	 */
	private boolean verify(@NonNull final ImageView imageView) {
		boolean flag = false;
		if (imageView.getTag(R.string.key_id_1) == null) {
			Log.e(TAG, "key null clear_image");
			GlideApp.with(imageView).clear(imageView);
		} else {
			flag = true;
		}
		return flag;
	}

	@Override
	public int getItemCount() {
		return mMusicItems.size();
	}

	public ArrayList<Integer> getSelected() {
		return mSelected;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return MOD_TYPE;
		}
		return 0;
	}

	public void clearSelection() {
		mSelected.clear();
		mActivity.inflateCommonMenu();
		for (ItemHolder holder : mViewHolders) {
			holder.mBody.setBackgroundColor(ContextCompat.getColor(mActivity, R.color.card_bg));
		}
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		ViewHolder(@NonNull View itemView) {
			super(itemView);
		}
	}

	class ItemHolder extends ViewHolder {

		View mBody;

		View mUView;

		TextView mExpandText;

		WeakReference<ImageView> mCoverReference;

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
			mCoverReference = new WeakReference<>(itemView.findViewById(R.id.recycler_item_album_image));

			mMusicAlbumName = itemView.findViewById(R.id.recycler_item_music_album_name);
			mMusicText = itemView.findViewById(R.id.recycler_item_music_name);
			mItemMenuButton = itemView.findViewById(R.id.recycler_item_menu);
			mMusicExtName = itemView.findViewById(R.id.recycler_item_music_type_name);
			mTime = itemView.findViewById(R.id.recycler_item_music_duration);
			mExpandView = itemView.findViewById(R.id.music_item_expand_view);
			mBody = itemView.findViewById(R.id.recycler_music_item_group);
			mExpandText = itemView.findViewById(R.id.expand_text);
			mUView = itemView.findViewById(R.id.u_view);

			mButton1 = itemView.findViewById(R.id.expand_button_1);
			mButton2 = itemView.findViewById(R.id.expand_button_2);
			mButton3 = itemView.findViewById(R.id.expand_button_3);
			mButton4 = itemView.findViewById(R.id.expand_button_share);

			mPopupMenu = new PopupMenu(mActivity, mItemMenuButton);
			mMenu = mPopupMenu.getMenu();

			final Resources resources = mActivity.getResources();

			//Menu Load
			//noinspection PointlessArithmeticExpression
			mMenu.add(Menu.NONE, Menu.FIRST + 0, 0, resources.getString(R.string.next_play));
			mMenu.add(Menu.NONE, Menu.FIRST + 2, 0, resources.getString(R.string.add_to_playlist));
			if (!mActivity.getActivityTAG().equals(AlbumDetailActivity.TAG)) {
				mMenu.add(Menu.NONE, Menu.FIRST + 4, 0, resources.getString(R.string.show_album));
			}
			mMenu.add(Menu.NONE, Menu.FIRST + 5, 0, resources.getString(R.string.more_info));
			mMenu.add(Menu.NONE, Menu.FIRST + 6, 0, resources.getString(R.string.share));

			MenuInflater menuInflater = mActivity.getMenuInflater();
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

	class ItemHolderS1 extends ItemHolder {

		ConstraintLayout mFrame;

		ItemHolderS1(@NonNull View itemView) {
			super(itemView);
			mFrame = itemView.findViewById(R.id.line_set);
		}
	}

	class ModHolderS1 extends ItemHolderS1 {

		ConstraintLayout mRandomItem;

		ModHolderS1(@NonNull View itemView) {
			super(itemView);
			mRandomItem = itemView.findViewById(R.id.random_play_item);
		}
	}
}
