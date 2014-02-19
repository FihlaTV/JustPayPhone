package info.guardianproject.justpayphone.app;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import info.guardianproject.justpayphone.R;
import info.guardianproject.justpayphone.utils.Constants.Codes;
import info.guardianproject.justpayphone.utils.Constants.Codes.Extras;

import org.witness.informacam.ui.SurfaceGrabberActivity;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CameraActivity extends SurfaceGrabberActivity {

	private View mBtnSave;
	private View mBtnRedo;
	private byte[] mData;
	private View mBtnTake;

	public CameraActivity() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBtnTake = findViewById(R.id.surface_grabber_button);
		mBtnSave = this.findViewById(R.id.btnSave);
		mBtnSave.setOnClickListener(this);
		mBtnRedo = this.findViewById(R.id.btnRedo);
		mBtnRedo.setOnClickListener(this);
		
		mBtnRedo.setEnabled(false);
		mBtnSave.setEnabled(false);
	}

	@Override
	protected int getLayout()
	{
		return R.layout.activity_camera;
	}
	
	@Override
	protected int getCameraDirection() {
		return CameraInfo.CAMERA_FACING_BACK;
	}

	@Override
	protected Size choosePictureSize(List<Size> localSizes) {
		return super.choosePictureSize(localSizes);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		mData = data;
		mBtnTake.setEnabled(false);
		mBtnSave.setEnabled(true);
		mBtnRedo.setEnabled(true);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if(view == mBtnSave)
		{
			if (mData != null)
			{
				File outputDir = getCacheDir(); // context being the Activity pointer
				File tempFile;
				try {
					tempFile = File.createTempFile("image", ".jpg", outputDir);
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile));
					bos.write(mData);
					bos.flush();
					bos.close();
					setResult(Activity.RESULT_OK, new Intent().putExtra(Extras.PATH_TO_FILE, tempFile.getAbsolutePath()));
				} catch (IOException e) {
					e.printStackTrace();
					setResult(Activity.RESULT_CANCELED);
				}
			}
			finish();
		}
		else if (view == mBtnRedo)
		{
			mData = null;
			mBtnSave.setEnabled(false);
			mBtnRedo.setEnabled(false);
			mBtnTake.setEnabled(true);
			resumePreview();
		}
	}
}