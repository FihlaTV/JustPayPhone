package info.guardianproject.justpayphone.app.popups;

import info.guardianproject.justpayphone.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.witness.informacam.InformaCam;
import org.witness.informacam.models.media.ILog;
import org.witness.informacam.models.organizations.IOrganization;
import org.witness.informacam.utils.Constants.Codes;
import org.witness.informacam.utils.Constants.Logger;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;

public class ExportAllPopup extends Popup {
	ProgressBar inProgressBar;
	
	private boolean mIsBatchExport = false;
	
	Handler h = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle b = msg.getData();
			
			if(b.containsKey(Codes.Keys.UI.PROGRESS)) {
			//	Log.d(LOG, "HELLO PROGRES: " + b.getInt(Codes.Keys.UI.PROGRESS) + "/" + inProgressBar.getProgress());
				inProgressBar.setProgress(inProgressBar.getProgress() + b.getInt(Codes.Keys.UI.PROGRESS));
			} else if(b.containsKey(Codes.Keys.BATCH_EXPORT_FINISHED)) {
				ExportAllPopup.this.cancel();
			}
			
			if (!mIsBatchExport)
			{
				String fileExport = null;
				
				if ((fileExport = b.getString("file")) != null)
				{
					ExportAllPopup.this.cancel();
					//this is the callback from the send/share export command
					boolean localShare = b.getBoolean("localShare");
					
					if (localShare)
					{
						Intent intent = new Intent()
						.setAction(Intent.ACTION_SEND)
						.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(fileExport)))
						.setType("*/*");
					
						Intent intentShare = Intent.createChooser(intent, a.getString(R.string.send));
						a.startActivity(intentShare);
					}
				}
			}
		}
	};
	
	List<ILog> observations;
	
	public ExportAllPopup(Activity a, List<ILog> observations) {
		super(a, R.layout.extras_waiter);
		
		this.observations = observations;
		
		inProgressBar = (ProgressBar) layout.findViewById(R.id.share_in_progress_bar);
		inProgressBar.setMax(observations.size() * 100);
		
		Show();
	}
	
	public void init() {
		init(true);
	}
	
	public void init(boolean localShare) {
		int observationsExported = 0;
		mIsBatchExport = true;
		
		//TODO keep the org null for now, as we don't want encryption on this export
		IOrganization org = null;
	
		boolean includeSensorLogs = true;
		
		if (!localShare)
			org = InformaCam.getInstance().installedOrganizations.getByName("GLSP");
		
		for(ILog iLog : ExportAllPopup.this.observations) {
			
			try {
				iLog.export(a, h, org, includeSensorLogs, localShare,!localShare);
			} catch(FileNotFoundException e) {
				Logger.e(LOG, e);
			}
			
			observationsExported++;
			
			inProgressBar.setProgress(observationsExported * 100);
			
			if(observationsExported == ExportAllPopup.this.observations.size()) {
				Bundle b = new Bundle();
				b.putBoolean(Codes.Keys.BATCH_EXPORT_FINISHED, true);
				
				Message msg = new Message();
				msg.setData(b);
				
				h.sendMessage(msg);
			}
		}
	}
	
	public void init(boolean localShare, ILog iLog) {
		
		inProgressBar.setMax(100);
		
		mIsBatchExport = false;
		boolean includeSensorLogs = true;

		IOrganization org = null;
		
		if (!localShare)
			org = InformaCam.getInstance().installedOrganizations.getByName("GLSP");
		
		try {
			iLog.export(a, h, org, includeSensorLogs, localShare, !localShare);
		} catch(FileNotFoundException e) {
			Logger.e(LOG, e);
		}
		
		inProgressBar.setProgress(100);
		
		
	}
}
