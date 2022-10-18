package com.game.neetease;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class convertThread {
	public static final String UI_MESSAGE = "u.i.m.s.g";
	Context m_context;
	Messenger m_messenger
	;
	private static final int BUFFER_LENGTH = 500;

	public convertThread(Messenger messenger) {
		m_messenger=messenger;
	}

	void SendMessage(String strMsg) {
		if(null!=m_messenger) {
			try {
				Bundle myBundle = new Bundle();
				myBundle.putSerializable(UI_MESSAGE, strMsg);
				Message msg = new Message();
				msg.setData(myBundle);
				m_messenger.send(msg); // update GUI
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void startServer (Context appContext) {
		m_context=appContext;
		Runnable serverTask = new Runnable() {
			@Override
			public void run () {
				{
					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(m_context);
					String source_path = settings.getString("source_path", "/storage/emulated/0/netease/cloudmusic/Cache/Music1")
							,   dest_path = settings.getString("dest_path", "/storage/emulated/0/download/neetease")
							;
					byte[] array_byte_buffer
							;
					int n_bytes_read
							;
					try {
						File dir = new File(source_path);
						File[] list_of_files = dir.listFiles();
						array_byte_buffer = new byte[BUFFER_LENGTH];
						DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
						Date date = new Date();
						String input_file_name
								,   base_file_name=dateFormat.format(date)
								, output_file_name
								,   file_ext
								;
						if (null == list_of_files)
							SendMessage("No files to work\n");
						else {
							int pep = list_of_files.length;
							SendMessage("Setting to work on " + String.valueOf(pep) + " files.\n");
							int n_done = 0;
							for (File aFile : list_of_files) {
								input_file_name=aFile.getName();
								if( ! input_file_name.endsWith(".uc!"))
									continue;
								file_ext=input_file_name.replace(".uc!","");
								file_ext=file_ext.substring(file_ext.indexOf('.'));
								output_file_name = dest_path+"/"+base_file_name+"."+n_done+file_ext;
								SendMessage("👍");
								FileInputStream fis = null;
								FileOutputStream fos = null;
								fis = new FileInputStream(aFile);
								fos = new FileOutputStream(output_file_name);
								while (fis.available() > 0) {
									n_bytes_read = fis.read(array_byte_buffer, 0, BUFFER_LENGTH);
									for (int idx = 0; idx < BUFFER_LENGTH; idx++)
										array_byte_buffer[idx] = (byte) (array_byte_buffer[idx] ^ 0xa3);
									fos.write(array_byte_buffer, 0, n_bytes_read);
								}
								fis.close();
								fos.close();
								n_done++;
							}
							SendMessage("\nDone "+n_done+" files.");
						}
					} catch (Exception e) {
						SendMessage(e.getMessage());
					}
				}
			}
		};
		Thread serverThread = new Thread(serverTask);
		serverThread.start();
	}
}
