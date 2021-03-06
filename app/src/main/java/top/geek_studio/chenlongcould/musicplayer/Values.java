package top.geek_studio.chenlongcould.musicplayer;

/**
 * @author chenlongcould
 */
public interface Values {

	int DEF_CROSS_FATE_TIME = 500;

	/**
	 * final string(s), TAGs
	 */
	String INDEX = "index";
	String TAG_UNIVERSAL_ONE = "TAG_UNIVERSAL_ONE";
	String TYPE_RANDOM = "RANDOM";
	String TYPE_COMMON = "COMMON";
	String TYPE_REPEAT = "REPEAT";
	String TYPE_REPEAT_ONE = "REPEAT_ONE";

	/**
	 * permission RequestCode
	 */
	int REQUEST_WRITE_EXTERNAL_STORAGE = 60;
	int MAX_HEIGHT_AND_WIDTH = 100;

	/**
	 * sharedPrefs tag
	 */
	interface SharedPrefsTag {

		/**
		 * color
		 */
		String PRIMARY_COLOR = "mPrimaryColor";
		String PRIMARY_DARK_COLOR = "mPrimaryDarkColor";
		String ACCENT_COLOR = "mAccentColor";
		String TITLE_COLOR = "mTitleColor";

		String DETAIL_BG_STYLE = "DETAIL_BG_STYLE";
		String ORDER_TYPE = "ORDER_TYPE";
		String PLAY_TYPE = Values.TYPE_COMMON;

		String ALBUM_LIST_DISPLAY_TYPE = "ALBUM_LIST_DISPLAY_TYPE";
		String ARTIST_LIST_DISPLAY_TYPE = "ARTIST_LIST_DISPLAY_TYPE";
		String ALBUM_LIST_GRID_TYPE_COUNT = "ALBUM_LIST_GRID_TYPE_COUNT";

		String SELECT_THEME = "SELECT_THEME";
		String THEME_USE_NOTE = "THEME_USE_NOTE";

		String NOTIFICATION_COLORIZED = "NOTIFICATION_COLORIZED";

		String TRANSPORT_STATUS = "TRANSPORT_STATUS";

		String HIDE_SHORT_SONG = "HIDE_SHORT_SONG";

		String USE_NET_WORK_ALBUM = "USE_NET_WORK_ALBUM";

		/**
		 * 1 is MUSIC TAB
		 * 2 is ALBUM TAB
		 * 3 is ARTIST TAB
		 * 4 is PLAYLIST TAB
		 * 5 is FILE MANAGER TAB
		 * <p>
		 * default tab order is: 12345
		 */
		String CUSTOM_TAB_LAYOUT = "CUSTOM_TAB_LAYOUT";

		/**
		 * 提示是否扔进垃圾桶的警告
		 */
		String TIP_NOTICE_DROP_TRASH = "TIP_NOTICE_DROP_TRASH";

		@Deprecated
		String RECYCLER_VIEW_ITEM_STYLE = "RECYCLER_VIEW_ITEM_STYLE";

		/**
		 * 存储最后播放的音乐的Id
		 */
		String LAST_PLAY_MUSIC_ID = "LAST_PLAY_MUSIC_ID";
	}

	interface IntentTAG {
		String SHORTCUT_TYPE = "SHORTCUT_TYPE";
	}

	interface BroadCast {
		String ReceiverOnMusicPlay = "top.geek_studio.chenlongcould.musicplayer.broadcast.ReceiverOnMusicPlay";
//		String ReceiverOnMusicStop = "top.geek_studio.chenlongcould.musicplayer.broadcast.ReceiverOnMusicStop";
		//		String ReceiverOnMusicPause = "top.geek_studio.chenlongcould.musicplayer.broadcast.ReceiverOnMusicPause";
	}

	interface DefaultValues {
		int ANIMATION_DURATION = 300;
	}

	interface Permission {
		String BROAD_CAST = "top.geek_studio.chenlongcould.musicplayer.broadcast";
	}

	final class CurrentData {

		/**
		 * same as
		 *
		 * @see UIMODE#MODE_CAR
		 * @see UIMODE#MODE_COMMON
		 * @deprecated use {@link UIMODE#MODE_COMMON}, {@link UIMODE#MODE_CAR}
		 */
		@Deprecated
		public static String MODE_CAR = UIMODE.MODE_CAR;

		public static String CURRENT_UI_MODE = UIMODE.MODE_COMMON;

		/**
		 * TEMP DATA
		 * default value -1 or null
		 */
		public static int CURRENT_PAGE_INDEX = 0;

		/**
		 * 存储使浮动菜单弹出的item,def = -1
		 */
		public static int CURRENT_SELECT_ITEM_INDEX_WITH_ITEM_MENU = -1;

		/**
		 * 当前序列指针, 指向 {@link Data#sPlayOrderList} 的位置
		 */
		public static volatile int CURRENT_MUSIC_INDEX = -1;

		/**
		 * 用户手动切歌的播放模式
		 * 1. random
		 * 2. common
		 * <p>
		 * auto switch
		 * 1. common
		 * 2. repeat one
		 * 3. repeat list
		 * 4. random
		 */
		public static String CURRENT_PLAY_TYPE = "COMMON";

	}

	final class UIMODE {
		public static final String MODE_COMMON = "common";
		public static final String MODE_CAR = "car";
	}

	final class Style {
		public static final String STYLE_BACKGROUND_BLUR = "BLUR";
		public static final String STYLE_BACKGROUND_AUTO_COLOR = "AUTO_COLOR";

		/**
		 * background style model
		 */
		public static String DETAIL_BACKGROUND = STYLE_BACKGROUND_BLUR;
	}

}
