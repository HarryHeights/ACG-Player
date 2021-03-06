package top.geek_studio.chenlongcould.musicplayer.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.appcompat.app.AlertDialog;

import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.geek_studio.chenlongcould.geeklibrary.DialogUtil;
import top.geek_studio.chenlongcould.musicplayer.R;

/**
 * @author chenlongcould
 */
public final class AboutLicActivity extends BaseCompatActivity {
	
	private static final String TAG = "AboutLicActivity";
	
	private Disposable mDisposable;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_lic);
		
		((Toolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> onBackPressed());
		
		final Button close = findViewById(R.id.close_button_activity_lic);
		final TextView textView = findViewById(R.id.show_lic_activity_lic);
		final AlertDialog load = DialogUtil.getLoadingDialog(this, "Loading...");
		
		close.setEnabled(true);
		close.setClickable(true);
		close.setOnClickListener(v -> finish());
		
		load.show();
		
		Observable.create((ObservableOnSubscribe<String>) observableEmitter -> {
			try {
				final InputStream inputStream = getAssets().open("Licenses");
				byte[] b = new byte[inputStream.available()];
				if (inputStream.read(b) != -1) {
					observableEmitter.onNext(new String(b));
					observableEmitter.onComplete();
				}
			} catch (IOException e) {
				e.printStackTrace();
				observableEmitter.onError(new Throwable("Load Licence Error"));
			}
		}).subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
			@Override
			public void onSubscribe(Disposable disposable) {
				mDisposable = disposable;
			}
			
			@Override
			public void onNext(String data) {
				textView.setText(data);
				load.dismiss();
			}
			
			@Override
			public void onError(Throwable throwable) {
				load.dismiss();
				throwable.printStackTrace();
				Toast.makeText(AboutLicActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void onComplete() {
			
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		mDisposable.dispose();
		super.onDestroy();
	}
	
	@Override
	public String getActivityTAG() {
		return TAG;
	}
	
	@Override
	public void inflateCommonMenu() {
		//not need
	}
	
	@Override
	public void inflateChooseMenu() {
		//not need
	}
	
}
