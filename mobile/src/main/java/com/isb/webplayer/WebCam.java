package com.isb.webplayer;

/*
 * Copyright (C) 2013 Surviving with Android (http://www.survivingwithandroid.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */




import android.content.Context;
import android.content.pm.PackageManager;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;



public class WebCam {



	private String url;
	private int interval=1000;
	private int num=-1;

	int camera_index=0;
	SurfaceTexture surfaceTexture;

	Identifiers ids;



	public WebCam(Context context, AtomicInteger cameras)
	{

		surfaceTexture = new SurfaceTexture(10);

		if(checkCameraHardware(context))
		{

			cameras.set( Camera.getNumberOfCameras());
			num=cameras.get();
			return;
		}
		else
		{
			cameras.set(-1);
		}
		camera_index=0;

	}



	void Initialize(String url, String email, String android_id,String playlistName)
	{

		this.url=url;
		this.ids.email=email;
		this.ids.android_id=android_id;
		this.ids.playlist=playlistName;
	}

	void Initialize(String url, Identifiers ids)
	{
		this.url=url;
		this.ids=ids;
	}





	void TakePictures(int firstCamera)
	{
		if (num<1) return;
		camera_index=firstCamera;
		TakePictures();


	}


	void TakePictures()
	{
		if (num<1) return;

		if(camera_index > num-1){

			camera_index=0;


		}

		StringBuilder err= new StringBuilder();
		Camera mCamera;
		try
		{
				Calendar rightNow=Calendar.getInstance();


				mCamera = getCameraInstance(camera_index, err);
				Camera.Parameters p= mCamera.getParameters();

				p.set("playlist",ids.playlist);

				if(ids.location!=null && ids.location.getLatitude()!=0) {
					p.removeGpsData();
					p.setGpsLatitude(ids.location.getLatitude());
					p.setGpsLongitude(ids.location.getLongitude());
					p.setGpsAltitude(ids.location.getAltitude());
					p.setGpsProcessingMethod(ids.location.getProvider().toUpperCase());
					p.setGpsTimestamp(rightNow.getTimeInMillis() / 1000);
				}


				mCamera.setPreviewTexture(surfaceTexture);
				mCamera.startPreview();
				mCamera.setParameters(p);
				mCamera.takePicture(null, null, pictureCallback);


		}
		catch(IOException e)
		{

		}
	}


	void Upload(byte[] data,String filename)
	{

	//	InputStream targetStream = new ByteArrayInputStream(data);
		if(url==null)return;
		SendHttpRequestTask t = new SendHttpRequestTask(data);
		Log.d("camera", "uploading "+filename);
		String[] params;

		if(ids.location!=null) {
			params = new String[]{url, ids.email, ids.android_id, filename, Integer.toString(camera_index), ids.playlist, Boolean.toString(ids.bHdmiSwitchSet), ids.mac_address,Double.toString(ids.location.getLatitude()), Double.toString(ids.location.getLongitude())};
		}
		else
		{
			params = new String[]{url, ids.email, ids.android_id, filename, Integer.toString(camera_index), ids.playlist, Boolean.toString(ids.bHdmiSwitchSet), ids.mac_address};
		}


		t.execute(params);

	}

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "ChameleonTV");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("camera", "failed to create directory");
				return null;
			}
		}
		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		String filename=mediaStorageDir.getPath() + File.separator + timeStamp + ".jpg";
		if (type == MEDIA_TYPE_IMAGE){
			Log.d("Camera", filename);
			mediaFile = new File(filename);


		} else if(type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"VID_"+ timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;

	}

	Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {


		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			Log.d("Camera", "PictureCallback creating file");

			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null) {
				Log.d("Camera", "Error creating media file, check storage permissions");
				return;
			}



			try {

				if(ids.SaveLocal) {
					FileOutputStream fos = new FileOutputStream(pictureFile);
					fos.write(data);
					fos.close();
				}
				Upload(data,pictureFile.getName());
				camera.stopPreview();
				camera.release();


			} catch (FileNotFoundException e) {
				Log.d("Camera", "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d("Camera", "Error accessing file: " + e.getMessage());
			}

			//  capturedImageHolder.setImageBitmap(scaleDownBitmapImage(bitmap, 300, 200 ));
		}
	};

	// ** A safe way to get an instance of the Camera object. */
	public  Camera getCameraInstance(int index,StringBuilder err){
		Camera c = null;
		try {
			c = Camera.open(index); // attempt to get a Camera instance
		}
		catch (Exception e){
			// Camera is not available (in use or does not exist)'
			err.append(e.getMessage());
		}
		return c; // returns null if camera is unavailable
	}

	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			return false;
		}
	}

	private class SendHttpRequestTask extends AsyncTask<String, Void, String> {

		byte[] data;



		public  SendHttpRequestTask(byte[] data)
		{
			this.data=data.clone();
		}

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			String email = params[1];
			String android_id = params[2];
			String filename=params[3];
			String cameraId=params[4];
			String playlistName=params[5];
			String hdmi=params[6];
			String mac_address=params[7];

			String response;

			try {

				HttpClient client = new HttpClient(url);
				client.connectForMultipart();
				int index = email.indexOf('@');
				email = email.substring(0,index);

				if(params.length > 8) {
					String latitude = params[8];
					String longitude = params[9];
					client.addFormPart("latitude", latitude);
					client.addFormPart("longitude", longitude);
				}

				client.addFormPart("email", email);
				client.addFormPart("android_id", android_id);
				client.addFormPart("cameraId", cameraId);
				client.addFormPart("playlist",playlistName);
				client.addFormPart("hdmi",hdmi);
				client.addFormPart("mac_address",mac_address);
				client.addFilePart("filename", filename,data);
				client.finishMultipart();
				response = client.getResponse();
				Thread.sleep( interval );
				return  response;
			}
			catch(Throwable t) {
				Log.d("camera","HttpClient error "+ t.getMessage());
			}

			return "response failed";
		}

		@Override
		protected void onPostExecute(String data) {

			Log.d("camera", "uploaded to "+url);
			Log.d("camera", data);


		//	if(camera_index++ < num-1)TakePictures();

		}
		
		
		
	}


}
