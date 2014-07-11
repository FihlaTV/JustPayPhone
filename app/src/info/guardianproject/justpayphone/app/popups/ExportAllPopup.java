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
			
			String fileExport = null;
			
			if ((fileExport = b.getString("file")) != null)
			{
				
				//this is the callback from the send/share export command
				
				Intent intent = new Intent()
				.setAction(Intent.ACTION_SEND)
				.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(fileExport)))
				.setType("*/*");
			
				Intent intentShare = Intent.createChooser(intent, a.getString(R.string.send));
				a.startActivity(intentShare);
				
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
		
		//TODO keep the org null for now, as we don't want encryption on this export
		IOrganization org = null;//InformaCam.getInstance().installedOrganizations.getByName("GLSP");
		
		for(ILog iLog : ExportAllPopup.this.observations) {
			
			try {
				iLog.export(a, h, org, localShare);
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
		int observationsExported = 0;
		
		//TODO keep the org null for now, as we don't want encryption on this export
		IOrganization org = null;//InformaCam.getInstance().installedOrganizations.getByName("GLSP");
		
		try {
			iLog.export(a, h, org, localShare);
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
