package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.Thread.sleep;

public class SimpleDynamoProvider extends ContentProvider {
	static final String TAG = SimpleDynamoProvider.class.getSimpleName();
	static final String REMOTE_PORT0 = "11108";
	static final String REMOTE_PORT1 = "11112";
	static final String REMOTE_PORT2 = "11116";
	static final String REMOTE_PORT3 = "11120";
	static final String REMOTE_PORT4 = "11124";
	//public static int noofneeded ;
	public static ArrayList<String> order = new ArrayList<String>();
	public static ArrayList<String> hashkeys = new ArrayList<String>();
	final int SERVER_PORT = 10000;
	String portStr;  //5554
	String myPort;  //11108
	String pre;
	String suc;   //same as suc1
	String suc1;
	String suc2;
	int count = 0;
	int myplace;
	//https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=1&cad=rja&uact=8&ved=2ahUKEwi2qNW3msrhAhVCmVkKHYWgDqIQFjAAegQIARAB&url=http%3A%2F%2Ftutorials.jenkov.com%2Fjava-util-concurrent%2Fblockingqueue.html&usg=AOvVaw3DnsnEmUB4_XHVX1ZEapUM
	BlockingQueue<String> b = new ArrayBlockingQueue<String>(1);
	BlockingQueue<String> w = new ArrayBlockingQueue<String>(1);
	public static Object obj = new Object();

	volatile boolean recoverystatus;
	ArrayList<Node> nodes = new ArrayList();
	//static ArrayList<> rows=
	ArrayList<String> Keystoshow = new ArrayList<String>();
	int what = 0;
	Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledynamo.provider");

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		File dir = getContext().getFilesDir();
		//  https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=11&cad=rja&uact=8&ved=2ahUKEwiwk9Cg88nhAhUwn-AKHU4HDyYQFjAKegQIAhAB&url=https%3A%2F%2Fstackoverflow.com%2Fquestions%2F3554722%2Fhow-to-delete-internal-storage-file-in-android&usg=AOvVaw3MALgBj65Q14XwruSdlQqJ
		if (Keystoshow.contains(selection)) {
			File file = new File(dir, selection);
			if(file.exists()){
				file.delete();
			}

				new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "delete:"+selection, myPort);

		} else if (selection.contains("@")) {
			int i = 0;
			File dir1[] = getContext().getFilesDir().listFiles();
			for (File file : dir1) {
				file.delete();
			}
			/*
			while (Keystoshow.size() > 0) {
				File file = new File(dir, Keystoshow.get(i));
				boolean deleted = file.delete();
				//Keystoshow.remove(i);
				i++;
			}*/

		}


		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}



	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
		Log.e(TAG, "" + myPort);


		ContentValues newvalue = new ContentValues();
		String keyValue = values.getAsString("key");
		String msg = values.getAsString("value");
		Log.e("INSERT", keyValue);
		if (keyValue.contains("#")) {


			File dir = getContext().getFilesDir();
			File file = new File(dir, keyValue.split("#")[1]);


			if (file.exists()) {

				FileInputStream fis = null;
				try {
					fis = getContext().openFileInput(keyValue.split("#")[1]);
					BufferedReader br = new BufferedReader(new InputStreamReader(fis));
					String r = br.readLine();
					Long exist = Long.parseLong(r.split(" ")[1]);
					Long newval = Long.parseLong(msg.split(" ")[1]);
					if (newval > exist) {
						OutputStreamWriter osw = new OutputStreamWriter(getContext().openFileOutput(keyValue.split("#")[1], Context.MODE_PRIVATE));
						osw.write(msg);
						osw.close();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				String FileName = keyValue.split("#")[1];
				Log.e(FileName, "trying replication again");
				FileOutputStream outputStream;
				outputStream = null;
				try {
					Keystoshow.add(FileName);
					Log.e("keys", FileName);
					outputStream = getContext().openFileOutput(FileName, MODE_PRIVATE);
					outputStream.write(msg.getBytes());
					outputStream.close();
					//what =1000;
					//new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, total, myPort);

				} catch (FileNotFoundException e) {
					Log.d("TAG", "FIle not found");
				} catch (IOException e) {
					Log.d("Tag", "IOexception");
				}
			}
		} else if (keyValue.contains(":")) {

			File dir = getContext().getFilesDir();
			File file = new File(dir, keyValue.split(":")[1]);
			if (file.exists()) {

				FileInputStream fis = null;
				try {
					fis = getContext().openFileInput(keyValue.split(":")[1]);
					BufferedReader br = new BufferedReader(new InputStreamReader(fis));
					String r = br.readLine();
					Long exist = Long.parseLong(r.split(" ")[1]);
					Long newval = Long.parseLong(msg.split(" ")[1]);
					if (newval > exist) {
						OutputStreamWriter osw = new OutputStreamWriter(getContext().openFileOutput(keyValue.split(":")[1], Context.MODE_PRIVATE));
						osw.write(msg);
						osw.close();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				String FileName = keyValue.split(":")[1];
				Log.e(FileName, "trying replication again");
				FileOutputStream outputStream;
				outputStream = null;
				try {
					Keystoshow.add(FileName);
					Log.e("keys", FileName);
					outputStream = getContext().openFileOutput(FileName, MODE_PRIVATE);
					outputStream.write(msg.getBytes());
					outputStream.close();
					//what =1000;
					//new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, total, myPort);

				} catch (FileNotFoundException e) {
					Log.d("TAG", "FIle not found");
				} catch (IOException e) {
					Log.d("Tag", "IOexception");
				}
			}

		} else {
			String FileName = keyValue;
			//  Log.e("key",keyValue);

			Log.e(TAG, "HERE2");


			//Log.e("val",msg);
			Long time = System.currentTimeMillis();
			String total = keyValue + " " + msg + " " + time;


			try {
				String hash = genHash(keyValue);


				if ((genHash(keyValue).compareTo(genHash(portStr)) <= 0) && (genHash(keyValue).compareTo(genHash(pre)) <= 0) && (genHash(pre).compareTo(genHash(portStr)) > 0)) {
					FileOutputStream outputStream;
					outputStream = null;
					try {
						Keystoshow.add(FileName);
						outputStream = getContext().openFileOutput(FileName, MODE_PRIVATE);
						outputStream.write((msg + " " + time).getBytes());
						outputStream.close();
						//what = 1000;
						String n = new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "mine:" + total, myPort).get();

					} catch (FileNotFoundException e) {
						Log.d("TAG", "FIle not found");
					} catch (IOException e) {
						Log.d("Tag", "IOexception");
					} catch (InterruptedException e) {
						Log.e("tag", e.getMessage());
					} catch (ExecutionException e) {
						e.printStackTrace();
					}

				} else if ((genHash(keyValue).compareTo(genHash(portStr)) > 0) && (genHash(keyValue).compareTo(genHash(pre)) > 0) && (genHash(pre).compareTo(genHash(portStr)) > 0)) {
					FileOutputStream outputStream;
					Log.e("tag", "I dont know");
					outputStream = null;
					try {
						// what = 1000;
						Keystoshow.add(FileName);
						outputStream = getContext().openFileOutput(FileName, MODE_PRIVATE);
						outputStream.write((msg + " " + time).getBytes());
						outputStream.close();

						new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "mine:" + total, myPort);

					} catch (FileNotFoundException e) {
						Log.d("TAG", "FIle not found");
					} catch (IOException e) {
						Log.d("Tag", "IOexception");
					}//catch(InterruptedException e){
					//Log.e("tag",e.getMessage());
					//}catch (ExecutionException e) {
					//e.printStackTrace();
					//}


				} else if ((genHash(keyValue).compareTo(genHash(pre)) > 0) && (genHash(keyValue).compareTo(genHash(portStr)) <= 0)) {
					FileOutputStream outputStream;
					Log.e("tag", "wrong2");
					outputStream = null;
					try {
						Keystoshow.add(FileName);
						outputStream = getContext().openFileOutput(FileName, MODE_PRIVATE);
						outputStream.write((msg + " " + time).getBytes());
						outputStream.close();
						//  what = 1000;
						new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "mine:" + total, myPort);

					} catch (FileNotFoundException e) {
						Log.d("TAG", "FIle not found");
					} catch (IOException e) {
						Log.d("Tag", "IOexception");
					}//catch(InterruptedException e){
					//Log.e("tag",e.getMessage());
					//}catch (ExecutionException e) {
					//e.printStackTrace();
					// }

				} else {
					for (int i = 0; i < 5; i++) {
						if (!order.get(i).equals(portStr)) {
							if (i == 0) {
								if ((genHash(keyValue).compareTo(genHash(order.get(i))) <= 0) && (genHash(keyValue).compareTo(genHash(order.get(4))) <= 0) && (genHash(order.get(4)).compareTo(genHash(order.get(i))) > 0)) {
									Log.e("here it is", order.get(i));
									// what = 20;
									new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "you:" + total + "-" + order.get(i) + "-" + i, myPort);
									break;

								} else if ((genHash(keyValue).compareTo(genHash(order.get(i))) > 0) && (genHash(keyValue).compareTo(genHash(order.get(4))) > 0) && (genHash(order.get(4)).compareTo(genHash(order.get(i))) > 0)) {
									Log.e("here it is2", order.get(i));
									//  what = 20;
									new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "you:" + total + "-" + order.get(i) + "-" + i, myPort);
									break;

								} else if ((genHash(keyValue).compareTo(genHash(order.get(4))) > 0) && (genHash(keyValue).compareTo(genHash(order.get(i))) <= 0)) {
									Log.e("here it is33", order.get(i));
									// what = 20;
									new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "you:" + total + "-" + order.get(i) + "-" + i, myPort);
									break;
								}
							} else {
								if ((genHash(keyValue).compareTo(genHash(order.get(i))) <= 0) && (genHash(keyValue).compareTo(genHash(order.get(i - 1))) <= 0) && (genHash(order.get(i - 1)).compareTo(genHash(order.get(i))) > 0)) {
									Log.e("here it is", order.get(i));
									//  what = 20;
									new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "you:" + total + "-" + order.get(i) + "-" + i, myPort);
									break;

								} else if ((genHash(keyValue).compareTo(genHash(order.get(i))) > 0) && (genHash(keyValue).compareTo(genHash(order.get(i - 1))) > 0) && (genHash(order.get(i - 1)).compareTo(genHash(order.get(i))) > 0)) {
									Log.e("here it is2", order.get(i));
									// what = 20;
									new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "you:" + total + "-" + order.get(i) + "-" + i, myPort);
									break;

								} else if ((genHash(keyValue).compareTo(genHash(order.get(i - 1))) > 0) && (genHash(keyValue).compareTo(genHash(order.get(i))) <= 0)) {
									Log.e("here it is33", order.get(i));
									// what = 20;
									new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "you:" + total + "-" + order.get(i) + "-" + i, myPort);
									break;
								}

							}


						}
					}

				}


				Log.e(TAG, "here5");
			} catch (NoSuchAlgorithmException e) {
				Log.e(TAG, e.getMessage());
			}

		}
		return null;
	}


	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		// super.onCreate(savedInstanceState);
		//delete(mUri,"@",null);
		TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
		System.out.println("PORT!!!!!!::" + portStr);

     	recoverystatus = false;
		try {

			ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
			new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
		} catch (IOException e) {

			Log.e(TAG, "Can't create a ServerSocket");
		}
		Log.e(TAG, portStr);
		String[] remotePort = new String[5];
		String[] rm = new String[5];

		remotePort[0] = REMOTE_PORT0;
		rm[0] = "5554";
		remotePort[1] = REMOTE_PORT1;
		rm[1] = "5556";
		remotePort[2] = REMOTE_PORT2;
		rm[2] = "5558";

		remotePort[3] = REMOTE_PORT3;
		rm[3] = "5560";
		remotePort[4] = REMOTE_PORT4;
		rm[4] = "5562";
		for (int i = 0; i < 5; i++) {
			//Node node = null;
			try {
				Node node = new Node(rm[i], genHash(rm[i]));
				Log.e("tag1", rm[i]);
				nodes.add(node);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}


		}
		Log.e("tag2", "here");
		int i;
		Collections.sort(nodes);
		for (i = 0; i < 5; i++) {
			if (portStr.equals(nodes.get(i).portNo)) {
				break;
			}
		}
		myplace = i;
		Log.e(TAG, myplace + "");

		for (int j = 0; j < 5; j++) {

			order.add(nodes.get(j).portNo);
		}
		Log.e("TAG:ORDER", order.get(0));
		Log.e("TAG:ORDER", order.get(1));
		Log.e("TAG:ORDER", order.get(2));
		Log.e("TAG:ORDER", order.get(3));
		Log.e("TAG:ORDER", order.get(4));


		for (int x = 0; x < 5; x++) {
			try {
				hashkeys.add(genHash(order.get(x)));
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}

		if (myplace == 0) {
			pre = order.get(4);
			suc1 = order.get(myplace + 1);
			suc = suc1;
			suc2 = order.get(myplace + 2);
		} else if (myplace == 4) {
			pre = order.get(myplace - 1);
			suc1 = order.get(0);
			suc = suc1;
			suc2 = order.get(1);
		} else if (myplace == 3) {
			pre = order.get(myplace - 1);
			suc1 = order.get(myplace + 1);
			suc = suc1;
			suc2 = order.get(0);
		} else {
			pre = order.get(myplace - 1);
			suc1 = order.get(myplace + 1);
			suc = suc1;
			suc2 = order.get(myplace + 2);
		}

		Log.e("HASHKEY BELOW", "");
		System.out.println(hashkeys);
		//what = 27;
		delete(mUri, "@", null);
		// new Recoverme().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "RECOVER ME", myPort);
		new Recovernow().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "RECOVERMENOW", myPort);

		


		return false;
	}



	public synchronized Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
						String sortOrder) {


		TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
		FileInputStream newFile = null;
		MatrixCursor matrixCursor = new MatrixCursor(new String[]{"key", "value"});


		// TODO Auto-generated method stub
		if (!(selection.contains("*") || selection.contains("@"))) {
			MatrixCursor matrixCursor1 = null;
			try {    //noofneeded = noofneeded +1;
				String hash = genHash(selection);
				Log.e("here", "indide selection");
				//FileInputStream newFile = null;

                
				for (int i = 0; i < 5; i++) {

					if (i == 0) {
						if ((genHash(selection).compareTo(genHash(order.get(i))) <= 0) && (genHash(selection).compareTo(genHash(order.get(4))) <= 0) && (genHash(order.get(4)).compareTo(genHash(order.get(i))) > 0)) {
							Log.e("here it is ", "proud of what i did");

							what = 25;
							String ans = new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "query=" + selection + " " + order.get(i) + " " + i, myPort).get();
							matrixCursor1 = new MatrixCursor(new String[]{"key", "value"});
							String s = ans;

							String[] k = s.split(" ");
							matrixCursor1.addRow(new String[]{k[0], k[1]});
							// b.clear();
							Log.e("correct", "value");
							matrixCursor = matrixCursor1;
							return matrixCursor;
							//break;

						} else if ((genHash(selection).compareTo(genHash(order.get(i))) > 0) && (genHash(selection).compareTo(genHash(order.get(4))) > 0) && (genHash(order.get(4)).compareTo(genHash(order.get(i))) > 0)) {
							Log.e("here it is2", order.get(i));
							what = 25;
							String ans = new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "query=" + selection + " " + order.get(i) + " " + i, myPort).get();
							matrixCursor1 = new MatrixCursor(new String[]{"key", "value"});
							String s = ans;

							String[] k = s.split(" ");
							matrixCursor1.addRow(new String[]{k[0], k[1]});
							//  b.clear();
							Log.e("correct", "value");
							matrixCursor = matrixCursor1;
							return matrixCursor;
							//break;

						} else if ((genHash(selection).compareTo(genHash(order.get(4))) > 0) && (genHash(selection).compareTo(genHash(order.get(i))) <= 0)) {
							Log.e("here it is33", order.get(i));
							what = 25;
							String ans = new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "query=" + selection + " " + order.get(i) + " " + i, myPort).get();
							matrixCursor1 = new MatrixCursor(new String[]{"key", "value"});
							String s = ans;

							String[] k = s.split(" ");
							matrixCursor1.addRow(new String[]{k[0], k[1]});
							// b.clear();
							Log.e("correct", "value");
							matrixCursor = matrixCursor1;
							return matrixCursor;
							// break;
						}
					} else {
						if ((genHash(selection).compareTo(genHash(order.get(i))) <= 0) && (genHash(selection).compareTo(genHash(order.get(i - 1))) <= 0) && (genHash(order.get(i - 1)).compareTo(genHash(order.get(i))) > 0)) {
							Log.e("here it is", order.get(i));
							what = 25;
							String ans = new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "query=" + selection + " " + order.get(i) + " " + i, myPort).get();
							matrixCursor1 = new MatrixCursor(new String[]{"key", "value"});
							String s = ans;

							String[] k = s.split(" ");
							matrixCursor1.addRow(new String[]{k[0], k[1]});
							//b.clear();
							Log.e("correct", "value");
							matrixCursor = matrixCursor1;
							return matrixCursor;
							//break;

						} else if ((genHash(selection).compareTo(genHash(order.get(i))) > 0) && (genHash(selection).compareTo(genHash(order.get(i - 1))) > 0) && (genHash(order.get(i - 1)).compareTo(genHash(order.get(i))) > 0)) {
							Log.e("here it is2", order.get(i));
							what = 25;
							String ans = new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "query=" + selection + " " + order.get(i) + " " + i, myPort).get();
							matrixCursor1 = new MatrixCursor(new String[]{"key", "value"});
							String s = ans;

							String[] k = s.split(" ");
							matrixCursor1.addRow(new String[]{k[0], k[1]});
							//b.clear();
							Log.e("correct", "value");
							matrixCursor = matrixCursor1;
							return matrixCursor;
							// break;

						} else if ((genHash(selection).compareTo(genHash(order.get(i - 1))) > 0) && (genHash(selection).compareTo(genHash(order.get(i))) <= 0)) {
							Log.e("here it is33", order.get(i));
							what = 25;
							String ans = new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "query=" + selection + " " + order.get(i) + " " + i, myPort).get();
							matrixCursor1 = new MatrixCursor(new String[]{"key", "value"});
							String s = ans;

							String[] k = s.split(" ");
							matrixCursor1.addRow(new String[]{k[0], k[1]});
							b.clear();
							Log.e("correct", "value");
							matrixCursor = matrixCursor1;
							return matrixCursor;
							//break;
						}

					}


				}


			} catch (NoSuchAlgorithmException e) {
				Log.e(TAG, e.getMessage());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}


		}
		if (selection.contains("*")) {
			// Log.e(TAG,"IN QUERY *");
			//FileInputStream newFile = null;
			// MatrixCursor matrixCursor = null;
			// matrixCursor = new MatrixCursor(new String[]{"key", "value"});
			Log.d(TAG, "AllKeyValue");
			String passquery = "";
			try {
				if (suc == null) {
					for (int i = 0; i < Keystoshow.size(); i++) {
						newFile = getContext().openFileInput(Keystoshow.get(i));
						InputStreamReader inputStreamReader = new InputStreamReader(newFile);
						BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

						String[] val = bufferedReader.readLine().split(" ");
						matrixCursor.addRow(new String[]{Keystoshow.get(i), val[0]});

						inputStreamReader.close();


					}
					return matrixCursor;
				} else if (sortOrder == null && suc != null) {
					// String s="";
					String msg1 = "";
					for (int i = 0; i < Keystoshow.size(); i++) {
						newFile = getContext().openFileInput(Keystoshow.get(i));
						InputStreamReader inputStreamReader = new InputStreamReader(newFile);
						BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
						String[] val = bufferedReader.readLine().split(" ");
						msg1 += Keystoshow.get(i) + " " + val[0] + "|";
						Log.d(TAG, "FileContent:" + msg1);
						//matrixCursor.addRow(new String[]{Keystoshow.get(i), bufferedReader.readLine()});


					}
					passquery = msg1;

					what = 12;//for query passing
					new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, passquery, myPort);
					Log.d(TAG, " Called for what=12");
					String s = "";
					try {
						s = w.take();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					String[] k = s.split("\\|");
					for (int i = 0; i < k.length; i++) {
						String g[] = k[i].split(" ");
						matrixCursor.addRow(new String[]{g[0], g[1]});
					}
					w.clear();
					return matrixCursor;


				}


			} catch (FileNotFoundException e) {
				Log.d("Tag", "File notfound");

			} catch (IOException e) {
				Log.d("Tag", "IO exception");
			}
			Log.v("query", selection);
			return matrixCursor;
		}
		if (selection.contains("@")) {
			
			while(!recoverystatus){
				try {
					sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}


			try {
				for (int i = 0; i < Keystoshow.size(); i++) {
					newFile = getContext().openFileInput(Keystoshow.get(i));
					// FileInputStream in = newFile;
					InputStreamReader inputStreamReader = new InputStreamReader(newFile);
					BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
					String[] val = bufferedReader.readLine().split(" ");
					Log.e("showing of 5554", "5554");
					matrixCursor.addRow(new String[]{Keystoshow.get(i), val[0]});

					inputStreamReader.close();
				}

			} catch (FileNotFoundException e) {
				Log.d("Tag", "File notfound");

			} catch (IOException e) {
				Log.d("Tag", "IO exception");
			}
			Log.v("query", selection);

			return matrixCursor;
		}
		Log.e("returning", "null");

		return matrixCursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	private String genHash(String input) throws NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte[] sha1Hash = sha1.digest(input.getBytes());
		Formatter formatter = new Formatter();
		for (byte b : sha1Hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

	private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

		@Override
		protected Void doInBackground(ServerSocket... sockets) {
			final ServerSocket serverSocket = sockets[0];
			TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
			String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
			final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

			try {
				while (true) {

					Socket sc = serverSocket.accept();

					InputStream inl = sc.getInputStream();
					InputStreamReader isr = new InputStreamReader(inl);
					BufferedReader br = new BufferedReader(isr);
					String msg = br.readLine();

					// Log.e("msg", ""+msg);
					if (msg.contains("insert")) {

						String[] k = msg.split("-");
						String[] t = k[1].split(" ");


						final ContentResolver mContentResolver = getContext().getContentResolver();
						final ContentValues mContentValues = new ContentValues();
						//Keystoshow.add(key);
						mContentValues.put("key", t[0]);
						mContentValues.put("value", t[1] + " " + t[2]);
						mContentResolver.insert(mUri, mContentValues);
					} else if (msg.contains("!")) {

						String[] k = msg.split("!");
						Log.e("value of k", k[1]);

						FileInputStream newFile = null;
						InputStreamReader inputStreamReader = null;
						String res = "";
						if (Keystoshow.size() != 0) {
							Log.e("inside", "" + Keystoshow.size());
							try {
								for (int i = 0; i < Keystoshow.size(); i++) {


									newFile = getContext().openFileInput(Keystoshow.get(i));

									inputStreamReader = new InputStreamReader(newFile);
									BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
									res += Keystoshow.get(i) + " " + bufferedReader.readLine() + "|";

									Log.e("resultzzzzzzz", res);
								}
								//matrixCursor.addRow(new String[]{Keystoshow.get(i), bufferedReader.readLine()});

								///inputStreamReader.close();

							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
							//byte[]  b;
							String b = res;
							Log.e(TAG, "RES!!!!!!!!!!!!" + res);
							//OutputStreamWriter outputStreamWriter = new OutputStreamWriter(sc.getOutputStream());
							//outputStreamWriter.write(b);
							//outputStreamWriter.flush();
							DataOutputStream d = new DataOutputStream(sc.getOutputStream());
							d.writeBytes(b + "\n");
							d.flush();

						} else {
							Log.e("TAGGGGGGGGGGG", "returning null");
							DataOutputStream d = new DataOutputStream(sc.getOutputStream());
							d.writeBytes("none!!" + "\n");
							d.flush();

						}


					} else if (msg.contains("my#")) {


						String[] msg2 = msg.split("#");
						String[] msg1 = msg2[1].split(" ");
						final ContentResolver mContentResolver = getContext().getContentResolver();
						final ContentValues mContentValues = new ContentValues();
						//Keystoshow.add(key);
						mContentValues.put("key", "my#" + msg1[0]);
						mContentValues.put("value", msg1[1] + " " + msg1[2]);
						mContentResolver.insert(mUri, mContentValues);

					} else if (msg.contains("replica")) {


						String[] msg2 = msg.split(":");
						String[] msg1 = msg2[1].split(" ");

						FileOutputStream outputStream;
						Log.e("tag", "I dont know");

						for (int y = 0; y < msg1.length; y++) {
							Log.e("REPLICAAAAAAA", msg1[y]);
						}
						Log.e(TAG, "GOT MSG Replica");
						Log.e(TAG, msg1[0]);
						Log.e(TAG, msg1[1]);
						final ContentResolver mContentResolver = getContext().getContentResolver();
						final ContentValues mContentValues = new ContentValues();
						//Keystoshow.add(key);
						mContentValues.put("key", "replica:" + msg1[0]);
						mContentValues.put("value", msg1[1] + " " + msg1[2]);
						mContentResolver.insert(mUri, mContentValues);

					} else if (msg.contains("finished")) {


						FileInputStream newFile = null;
						InputStreamReader inputStreamReader = null;
						String res = "";
						try {
							for (int i = 0; i < Keystoshow.size(); i++) {


								newFile = getContext().openFileInput(Keystoshow.get(i));

								inputStreamReader = new InputStreamReader(newFile);
								BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
								String val[] = bufferedReader.readLine().split(" ");
								res += Keystoshow.get(i) + " " + val[0] + "|";


							}
							//matrixCursor.addRow(new String[]{Keystoshow.get(i), bufferedReader.readLine()});

							///inputStreamReader.close();

						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
						//byte[]  b;
						String b = res;
						Log.e(TAG, "RES!!!!!!!!!!!!" + res);
						//OutputStreamWriter outputStreamWriter = new OutputStreamWriter(sc.getOutputStream());
						//outputStreamWriter.write(b);
						//outputStreamWriter.flush();
						DataOutputStream d = new DataOutputStream(sc.getOutputStream());
						d.writeBytes(b + "\n");
						d.flush();


					} else if (msg.contains("query=")) {
                                /*
                                while (recover) {
                                    sleep(0);
                                }*/
						//sleep(5);
						String[] q = msg.split("=");


						Log.e("here it is", "working2");
						FileInputStream newFile = null;
						InputStreamReader inputStreamReader = null;
						String res = "";

						try {
							for (int i = 0; i < Keystoshow.size(); i++) {
								if (q[1].equals(Keystoshow.get(i))) {
									newFile = getContext().openFileInput(Keystoshow.get(i));

									inputStreamReader = new InputStreamReader(newFile);
									BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
									res = Keystoshow.get(i) + " " + bufferedReader.readLine() + "|";
									break;

								}
							}
							//matrixCursor.addRow(new String[]{Keystoshow.get(i), bufferedReader.readLine()});

							///inputStreamReader.close();

						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}

						DataOutputStream d = new DataOutputStream(sc.getOutputStream());
						d.writeBytes(res + "\n");
						d.flush();


					} else if (msg.contains("-")) {

						String[] msg2 = msg.split("-");
						String[] msg1 = msg2[1].split(" ");
						Log.e(TAG, "GOT MSG");
						Log.e(TAG, msg1[0]);
						Log.e(TAG, msg1[1]);
						final ContentResolver mContentResolver = getContext().getContentResolver();
						final ContentValues mContentValues = new ContentValues();
						// Keystoshow.add(key);
						mContentValues.put("key", msg1[0]);
						mContentValues.put("value", msg1[1]);
						mContentResolver.insert(mUri, mContentValues);

					} else if (msg.contains("delete")) {
						String[] ms = msg.split(":");
						File dir = getContext().getFilesDir();
						File file = new File(dir, ms[1]);
						boolean deleted = file.delete();
						// final ContentResolver mContentResolver = getContext().getContentResolver();
						//final ContentValues mContentValues= new ContentValues();
						//mContentResolver.delete(mUri,ms[1],null);
					} else if (msg.contains("RECOVERMENOW")) {
						FileInputStream newFile = null;
						String myvalues = "";
						try {
							for (int i = 0; i < Keystoshow.size(); i++) {
								newFile = getContext().openFileInput(Keystoshow.get(i));
								// FileInputStream in = newFile;
								InputStreamReader inputStreamReader = new InputStreamReader(newFile);
								BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
								String val = bufferedReader.readLine();
								myvalues += Keystoshow.get(i) + " " + val + "|";
								inputStreamReader.close();
							}

						} catch (FileNotFoundException e) {
							Log.d("Tag", "File notfound");

						} catch (IOException e) {
							Log.d("Tag", "IO exception");
						}
						DataOutputStream d = new DataOutputStream(sc.getOutputStream());
						d.writeBytes(myvalues + "\n");
						d.flush();


					}


					//  publishProgress(br.readLine());
				}

			} catch (IOException e) {
				Log.e(TAG, "IDK");
			} /*catch (InterruptedException e) {
                e.printStackTrace();
            }*/



			/*
			 * TODO: Fill in your server code that receives messages and passes them
			 * to onProgressUpdate().
			 */


			return null;
		}

		protected void onProgressUpdate(String... strings) {
			/*
			 * The following code displays what is received in doInBackground().
			 */
			String strReceived = strings[0].trim();
			//TextView remoteTextView = (TextView) findViewById(R.id.remote_text_display);
			//remoteTextView.append(strReceived + "\t\n");
			//TextView localTextView = (TextView) findViewById(R.id.local_text_display);
			//localTextView.append("\n");

			/*
			 * The following code creates a file in the AVD's internal storage and stores a file.
			 *
			 * For more information on file I/O on Android, please take a look at
			 * http://developer.android.com/training/basics/data-storage/files.html
			 */

			/**
			 * buildUri() demonstrates how to build a URI for a ContentProvider.
			 *
			 * @param scheme
			 * @param authority
			 * @return the URI
			 */
			String key = strReceived.split(" ")[0];
			String value = strReceived.split(" ")[1];

			final ContentResolver mContentResolver = getContext().getContentResolver();

			final Uri mUri;
			final ContentValues mContentValues = new ContentValues();

			mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");

			//String filename = "SimpleMessengerOutput";
			Keystoshow.add(key);
			mContentValues.put("key", key);
			mContentValues.put("value", value);
			mContentResolver.insert(mUri, mContentValues);


		}
	}


	private class ClientTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... msgs) {
			try {

				TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
				String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
				final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
				String[] remotePort = new String[5];
				remotePort[0] = REMOTE_PORT0;
				remotePort[1] = REMOTE_PORT1;
				remotePort[2] = REMOTE_PORT2;
				remotePort[3] = REMOTE_PORT3;
				remotePort[4] = REMOTE_PORT4;
				String msgToSend = msgs[0];
				Log.e("MSG TO SEND AT CLI:", msgs[0]);
				//Log.e("MSGS[1]",msgs[1]);
				if (msgToSend.contains("insert")) {
					String[] s = msgToSend.split("-");
					String success1;
					String success2;
					if (s[2].equals("4")) {
						success1 = order.get(0);
						success2 = order.get(1);
					} else if (s[2].equals("3")) {
						success1 = order.get(4);
						success2 = order.get(0);
					} else {
						success1 = order.get(Integer.parseInt(s[2]) + 1);
						success2 = order.get(Integer.parseInt(s[2]) + 2);

					}
					String mine = order.get(Integer.parseInt(s[2]));
					String tosend = "insert-" + s[1];
					int i = 0;
					String Porttosend = "";
					while (i < 3) {
						try {
							if (i == 0) {
								Porttosend = String.valueOf((Integer.parseInt(mine) * 2));
							} else if (i == 1) {
								Porttosend = String.valueOf((Integer.parseInt(success1) * 2));
							} else if (i == 2) {
								Porttosend = String.valueOf((Integer.parseInt(success2) * 2));
							}
							Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
									Integer.parseInt(Porttosend));
							socket.setSoTimeout(1500);


							DataOutputStream d = new DataOutputStream(socket.getOutputStream());
							d.writeBytes(tosend + "\n");
							d.flush();


							socket.close();
						} catch (IOException e) {
							//Log.e("tag", e.getMessage());
						}


						//socket.close();
						i++;
					}


				} else if (msgToSend.contains("query2")) {
					String s[] = msgToSend.split("-");
					String success1;
					String success2;
					if (s[2].equals("4")) {
						success1 = order.get(0);
						success2 = order.get(1);
					} else if (s[2].equals("3")) {
						success1 = order.get(4);
						success2 = order.get(0);
					} else {
						success1 = order.get(Integer.parseInt(s[2]) + 1);
						success2 = order.get(Integer.parseInt(s[2]) + 2);

					}
					String mine = order.get(Integer.parseInt(s[2]));
					String tosend = "query-" + s[1];
					int i = 0;
					String msg1 = "";
					String m1 = "";
					String Porttosend = "";
					while (i < 3) {
						try {
							if (i == 0) {
								Porttosend = String.valueOf((Integer.parseInt(mine) * 2));
							} else if (i == 1) {
								Porttosend = String.valueOf((Integer.parseInt(success1) * 2));
							} else if (i == 2) {
								Porttosend = String.valueOf((Integer.parseInt(success2) * 2));
							}
							Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
									Integer.parseInt(Porttosend));
							socket.setSoTimeout(1500);


							DataOutputStream d = new DataOutputStream(socket.getOutputStream());
							d.writeBytes(tosend + "\n");
							d.flush();

							InputStreamReader isr = new InputStreamReader(socket.getInputStream());
							BufferedReader br = new BufferedReader(isr);
							if ((msg1 = br.readLine()) != null) {
								// msg1 = br.readLine();
								m1 += msg1;

							}


							socket.close();
						} catch (IOException e) {
							//Log.e("tag", e.getMessage());
						}


						//socket.close();
						i++;
					}
					String[] k = m1.split("\\|");
					for (int q = 0; q < k.length; q++) {
						Log.e("k inside:", "yeh:" + k[q]);
					}
					Log.e("k length:", "" + k.length);
					long temp = 0L;
					String key = null;
					String value = null;
					for (String t : k) {
						Log.e("S::::", t);
						String[] l = t.split(" ");
						if (l.length == 3) {
							System.out.println("Checking" + l.length);
							for (int r = 0; r < l.length; r++) {
								System.out.println(l[r]);
							}
							if (Long.parseLong(l[2]) > temp) {
								temp = Long.parseLong(l[2]);
								key = l[0];
								value = l[1];
							}
						}
					}
					String res = key + " " + value;
					//if(k[0].equals(""))
					//return k[1];
					// b.put(msg1);
					// else

                   /* for(String s:k){
                        if(!s.equals("")){
                            return s;
                        }
                    }*/
					return res;

				} else if (what == 27)//for recovering
				{

					String qport = portStr;
					Log.e(TAG, "qport:::::::" + qport);
					String result = "";
					String qwerty = pre;
					String msg = "";
					int i = 0;
					while (i != 2) {
						String Porttosend = String.valueOf((Integer.parseInt(qwerty) * 2));

						Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
								Integer.parseInt(Porttosend));
						String m = "RECOVER ME" + "!" + msgToSend;
						//OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
						//outputStreamWriter.write(m);
						//outputStreamWriter.flush();

						DataOutputStream d = new DataOutputStream(socket.getOutputStream());
						d.writeBytes(m + "\n");
						d.flush();
                        /*byte[]  b;
                        b = m.getBytes();
                        OutputStream outs = socket.getOutputStream();
                        outs.write(b);
                        outs.flush();
                        outs.close();*/
						Log.e("done 1", i + "");

						InputStreamReader isr = new InputStreamReader(socket.getInputStream());
						BufferedReader br = new BufferedReader(isr);
						// Log.e("value pfresult2222222",result);
						if ((msg = br.readLine()) != null) {
							result += msg;

						}
						//String h[ ]=msg.split("!");
						// Log.e(TAG,"QWERTY:"+h[1]);
						// Log.e(TAG,"RESULT"+h[0]);
						qwerty = suc;

						Log.e("value pfresult", result);

						i++;

					}
					// b.put(result);

					String results = result;
					Log.e("value of results", results + "");
					if (results.equals("none!!none!!") || results.contains("null") || results.contains("none!!")) {
						results = "none!!";
					}
					int place = 0;
					if (!results.contains("none!!")) {
						String ans[] = results.split("\\|");
						for (String s : ans) {
							Log.e("String s", s);

							try {
								String[] k = s.split(" ");
								String hash = genHash(k[0]);
								for (int z = 0; z < 5; z++) {

									if (z == 0) {
										if ((hash.compareTo(genHash(order.get(z))) <= 0) && (hash.compareTo(genHash(order.get(4))) <= 0) && (genHash(order.get(4)).compareTo(genHash(order.get(z))) > 0)) {
											Log.e("here it is", order.get(z));

											place = z;
											Log.e("place0", place + "");
											break;

										} else if ((hash.compareTo(genHash(order.get(z))) > 0) && (hash.compareTo(genHash(order.get(4))) > 0) && (genHash(order.get(4)).compareTo(genHash(order.get(z))) > 0)) {
											Log.e("here it is2", order.get(z));
											place = z;
											Log.e("place0", place + "");
											break;

										} else if ((hash.compareTo(genHash(order.get(4))) > 0) && (hash.compareTo(genHash(order.get(z))) <= 0)) {
											Log.e("here it is33", order.get(z));
											place = z;
											Log.e("place0", place + "");
											break;
										}
									} else {
										if ((hash.compareTo(genHash(order.get(z))) <= 0) && (hash.compareTo(genHash(order.get(z - 1))) <= 0) && (genHash(order.get(z - 1)).compareTo(genHash(order.get(z))) > 0)) {
											Log.e("here it is", order.get(i));
											place = z;
											Log.e("place", place + "");
											break;

										} else if ((hash.compareTo(genHash(order.get(z))) > 0) && (hash.compareTo(genHash(order.get(z - 1))) > 0) && (genHash(order.get(z - 1)).compareTo(genHash(order.get(z))) > 0)) {
											Log.e("here it is2", order.get(z));
											place = z;
											Log.e("place", place + "");
											break;

										} else if ((hash.compareTo(genHash(order.get(z - 1))) > 0) && (hash.compareTo(genHash(order.get(z))) <= 0)) {
											Log.e("here it is33", order.get(z));
											place = z;
											Log.e("place", place + "");
											break;
										}


									}


								}
								if (myplace == 0) {
									if (place == myplace) {
										FileOutputStream outputStream;
										Keystoshow.add(k[0]);
										outputStream = getContext().openFileOutput(k[0], MODE_PRIVATE);
										outputStream.write(k[1].getBytes());
										outputStream.close();
									} else if (place == 4) {
										FileOutputStream outputStream;
										Keystoshow.add(k[0]);
										outputStream = getContext().openFileOutput(k[0], MODE_PRIVATE);
										outputStream.write(k[1].getBytes());
										outputStream.close();
									} else if (place == 3) {
										FileOutputStream outputStream;
										Keystoshow.add(k[0]);
										outputStream = getContext().openFileOutput(k[0], MODE_PRIVATE);
										outputStream.write(k[1].getBytes());
										outputStream.close();
									}
								} else if (myplace == 1) {
									if (place == myplace) {
										FileOutputStream outputStream;
										Keystoshow.add(k[0]);
										outputStream = getContext().openFileOutput(k[0], MODE_PRIVATE);
										outputStream.write(k[1].getBytes());
										outputStream.close();
									} else if (place == 0) {
										FileOutputStream outputStream;
										Keystoshow.add(k[0]);
										outputStream = getContext().openFileOutput(k[0], MODE_PRIVATE);
										outputStream.write(k[1].getBytes());
										outputStream.close();
									} else if (place == 4) {
										FileOutputStream outputStream;
										Keystoshow.add(k[0]);
										outputStream = getContext().openFileOutput(k[0], MODE_PRIVATE);
										outputStream.write(k[1].getBytes());
										outputStream.close();
									}

								} else {
									if (place == myplace) {
										FileOutputStream outputStream;
										Keystoshow.add(k[0]);
										outputStream = getContext().openFileOutput(k[0], MODE_PRIVATE);
										outputStream.write(k[1].getBytes());
										outputStream.close();
									} else if (place == myplace - 1) {
										FileOutputStream outputStream;
										Keystoshow.add(k[0]);
										outputStream = getContext().openFileOutput(k[0], MODE_PRIVATE);
										outputStream.write(k[1].getBytes());
										outputStream.close();
									} else if (place == myplace - 2) {
										FileOutputStream outputStream;
										Keystoshow.add(k[0]);
										outputStream = getContext().openFileOutput(k[0], MODE_PRIVATE);
										outputStream.write(k[1].getBytes());
										outputStream.close();
									}
								}
							} catch (NoSuchAlgorithmException e) {

							} catch (IOException e) {

							}
						}
					}


				}  else if (msgToSend.contains("query=")) {
					synchronized (obj) {
						/*
						try{
							sleep(20);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}*/
						String[] msg = msgToSend.split(" ");
						Log.e("YesWhat=25", msgToSend);
						Log.e("Yeshere", msg[0]);
						String msg1 = "";
						String Porttosend = "";

						//String m2;
						//String m3;
						String success;
						String success2;
						// String msg []= msgToSend.split(" ");
						System.out.println("Checking" + msg.length);
						for (int k = 0; k < msg.length; k++) {
							System.out.println(msg[k]);
						}

						if (msg[2].equals("4")) {
							success = order.get(0);
							success2 = order.get(1);
						} else if (msg[2].equals("3")) {
							success = order.get(4);
							success2 = order.get(0);
						} else {
							if (msg[2].contains("-")) {
								if (msg[2].split("-")[2].equals("3")) {
									success = order.get((Integer.parseInt(msg[2].split("-")[2]) + 1));
									success2 = order.get(0);
								} else if (msg[2].split("-")[2].equals("4")) {
									success = order.get(0);
									success2 = order.get(1);
								} else {
									success = order.get((Integer.parseInt(msg[2].split("-")[2]) + 1));
									success2 = order.get((Integer.parseInt(msg[2].split("-")[2]) + 2));
								}
							} else {
								success = order.get((Integer.parseInt(msg[2]) + 1));
								success2 = order.get((Integer.parseInt(msg[2]) + 2));
							}
						}
						int i = 0;
						String m1 = "";
						while (i < 3) {
							try {
								if (i == 0) {
									Porttosend = String.valueOf((Integer.parseInt(msg[1]) * 2));
								} else if (i == 1) {
									Porttosend = String.valueOf((Integer.parseInt(success) * 2));
								} else if (i == 2) {
									Porttosend = String.valueOf((Integer.parseInt(success2) * 2));
								}
								Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
										Integer.parseInt(Porttosend));
								socket.setSoTimeout(1500);


								DataOutputStream d = new DataOutputStream(socket.getOutputStream());
								d.writeBytes(msg[0] + "\n");
								d.flush();

								Log.e(TAG, "got value yay");

								InputStreamReader isr = new InputStreamReader(socket.getInputStream());
								BufferedReader br = new BufferedReader(isr);
								if ((msg1 = br.readLine()) != null) {
									// msg1 = br.readLine();
									m1 += msg1;
								}
								Log.e("value of m1", m1);
								socket.close();
							} catch (IOException e) {
								//Log.e("tag", e.getMessage());
							}


							//socket.close();
							i++;
						}

						String[] k = m1.split("\\|");
						for (int q = 0; q < k.length; q++) {
							Log.e("k inside:", "yeh:" + k[q]);
						}
						Log.e("k length:", "" + k.length);
						long temp = 0L;
						String key = null;
						String value = null;
						for (String s : k) {
							Log.e("S::::", s);
							String[] l = s.split(" ");
							if (l.length == 3) {
								System.out.println("Checking" + l.length);
								for (int r = 0; r < l.length; r++) {
									System.out.println(l[r]);
								}
								if (Long.parseLong(l[2]) > temp) {
									temp = Long.parseLong(l[2]);
									key = l[0];
									value = l[1];
								}
							}
						}
						String res = key + " " + value;
						//if(k[0].equals(""))
						//return k[1];
						// b.put(msg1);
						// else

                   /* for(String s:k){
                        if(!s.equals("")){
                            return s;
                        }
                    }*/
						return res;
						//Log.e("b.put",msg1);
						// socket.close();
					}
				} else if (msgToSend.contains("mine:")) {

					String[] k = msgToSend.split(":");
					String msg = "replica:" + k[1];

					Log.e("here", "what= 5" + msg);
					// String msg []= msgToSend.split(" ");
					String Porttosend = String.valueOf((Integer.parseInt(suc1) * 2));

					Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
							Integer.parseInt(Porttosend));
					byte[] b;
					b = msg.getBytes();
					OutputStream outs = socket.getOutputStream();
					outs.write(b);
					outs.close();

					socket.close();
					String Porttosend2 = String.valueOf((Integer.parseInt(suc2) * 2));

					Socket socket2 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
							Integer.parseInt(Porttosend2));
					byte[] b2;
					b2 = msg.getBytes();
					OutputStream outs2 = socket2.getOutputStream();
					outs2.write(b2);
					outs2.close();

					socket2.close();
					return null;
				} else if (what == 5) {


					Log.e("here", "what= 5");
					// String msg []= msgToSend.split(" ");
					String Porttosend = String.valueOf((Integer.parseInt(suc) * 2));

					Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
							Integer.parseInt(Porttosend));
					byte[] b;
					b = msgToSend.getBytes();
					OutputStream outs = socket.getOutputStream();
					outs.write(b);
					outs.close();

					socket.close();
				} else if (what == -1) {
					String msg[] = msgToSend.split(":");
					String Porttosend = String.valueOf((Integer.parseInt(msg[0]) * 2));
					Log.e("in client task", "sending");
					Log.e("TAG", Porttosend);
					Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
							Integer.parseInt(Porttosend));

					Log.e("in client task", "sending");

					/*System.out.println(b);*/
					/*
					 * TODO: Fill in your client code that sends out a message.
					 */
					byte[] b;
					b = msgToSend.getBytes();
					OutputStream outs = socket.getOutputStream();
					outs.write(b);
					outs.close();

					socket.close();
				} else if (what == 2) {
					//it does not belong to me..send to successor for insert
					String Porttosend = String.valueOf((Integer.parseInt(suc) * 2));

					Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
							Integer.parseInt(Porttosend));


					Log.e(TAG, "WHAT=2");
					/*System.out.println(b);*/
					/*
					 * TODO: Fill in your client code that sends out a message.
					 */
					String m = "msg-" + msgToSend;
					byte[] b;
					b = m.getBytes();
					OutputStream outs = socket.getOutputStream();
					outs.write(b);
					outs.close();

					socket.close();
				} else if (what == 100) {
					String[] send = msgToSend.split(" ");
					String Porttosend = String.valueOf((Integer.parseInt(send[1]) * 2));

					Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
							Integer.parseInt(Porttosend));


					Log.e(TAG, "WHAT=2");
					/*System.out.println(b);*/
					/*
					 * TODO: Fill in your client code that sends out a message.
					 */
					String m = msgToSend;
					byte[] b;
					b = m.getBytes();
					OutputStream outs = socket.getOutputStream();
					outs.write(b);
					outs.close();
					socket.close();

				} else if (what == 10) {
					//of no use
					String[] send = msgToSend.split(" ");
					String Porttosend = String.valueOf((Integer.parseInt(suc) * 2));

					Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
							Integer.parseInt(Porttosend));


					Log.e(TAG, "WHAT=2");
					/*System.out.println(b);*/
					/*
					 * TODO: Fill in your client code that sends out a message.
					 */
					String m = msgToSend;
					byte[] b;
					b = m.getBytes();
					OutputStream outs = socket.getOutputStream();
					outs.write(b);
					outs.close();
					socket.close();
				} else if (msgToSend.contains("you:")) {
					//of no use
					String k[] = msgToSend.split(":");
					String nextport;
					String nexttonext;
					Log.e("what=20", msgToSend);
					String[] send = k[1].split("-");
					System.out.println("sending" + send.length);
					for (int a = 0; a < send.length; a++) {
						System.out.println("sending" + send[a]);
					}
					String Porttosend = String.valueOf((Integer.parseInt(send[1]) * 2));
					Log.e("tag", "inside insert");
					if (send[2].equals("4")) {
						nextport = order.get(0);
						nexttonext = order.get(1);
					} else if (send[2].equals("3")) {
						nextport = order.get(Integer.parseInt(send[2]) + 1);
						nexttonext = order.get(0);
					} else {
						nextport = order.get(Integer.parseInt(send[2]) + 1);
						nexttonext = order.get(Integer.parseInt(send[2]) + 2);

					}

					Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
							Integer.parseInt(Porttosend));

					//	String p=  "finished"+ ":"+ msgToSend.split("#")[1];
					Log.e(TAG, "WHAT=20");
					/*System.out.println(b);*/
					/*
					 * TODO: Fill in your client code that sends out a message.
					 */
					String m = "my" + "#" + send[0];
					byte[] b;
					b = m.getBytes();
					OutputStream outs = socket.getOutputStream();
					outs.write(b);
					outs.close();

					//socket.close();
					String Porttosend2 = String.valueOf((Integer.parseInt(nextport) * 2));
					//String m1 = "my" + "#" + send[0];
					String m1 = "replica:" + send[0];
					byte[] b1;
					b1 = m1.getBytes();
					Socket socket2 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
							Integer.parseInt(Porttosend2));
					OutputStream outs1 = socket2.getOutputStream();
					outs1.write(b1);
					outs1.close();

					// socket2.close();
					String Porttosend3 = String.valueOf((Integer.parseInt(nexttonext) * 2));
					// String m2 = "my" + "#" + send[0];
					String m2 = "replica:" + send[0];
					byte[] b2;
					b2 = m2.getBytes();
					Socket socket3 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
							Integer.parseInt(Porttosend3));
					OutputStream outs2 = socket3.getOutputStream();
					outs2.write(b2);
					outs2.close();

				} else if (what == 12) {
					//works forquery *

					String qport = portStr;
					Log.e(TAG, "qport:::::::" + qport);
					String result = "";
					String qwerty = suc;
					int mine = myplace;
					if (mine == 4) {
						mine = 0;
					} else {
						mine = mine + 1;
					}

					while (!qwerty.equals(qport)) {
						String msg = "";
						try {
							String Porttosend = String.valueOf((Integer.parseInt(qwerty) * 2));

							Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
									Integer.parseInt(Porttosend));
							socket.setSoTimeout(1500);
							String m = "finished" + ":" + msgToSend;
							//OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
							//outputStreamWriter.write(m);
							//outputStreamWriter.flush();

							DataOutputStream d = new DataOutputStream(socket.getOutputStream());
							d.writeBytes(m + "\n");
							d.flush();
                        /*byte[]  b;
                        b = m.getBytes();
                        OutputStream outs = socket.getOutputStream();
                        outs.write(b);
                        outs.flush();
                        outs.close();*/
							Log.e(TAG, "Sent to Server of Successor");

							InputStreamReader isr = new InputStreamReader(socket.getInputStream());
							BufferedReader br = new BufferedReader(isr);
							if ((msg = br.readLine()) != null) {
								result += msg;
							}

							// String h[ ]=msg.split("!");
							// Log.e(TAG,"QWERTY:"+h[1]);
							// Log.e(TAG,"RESULT"+h[0]);
							if (mine == 4) {
								Log.e("mine", "" + "mine" + 3);
								qwerty = order.get(0);
								mine = 0;
							} else if (mine == 3) {
								Log.e("mine", "" + "mine" + 4);
								qwerty = order.get(4);
								mine++;
							} else if (mine == 1) {
								Log.e("mine", "" + "mine" + "elsewhere");
								//if(mine==4)
								qwerty = order.get(2);
								mine++;
							} else if (mine == 2) {
								Log.e("mine", "" + "mine" + "elsewhere");
								//if(mine==4)
								qwerty = order.get(3);
								mine++;
							} else if (mine == 0) {
								Log.e("mine", "" + "mine" + "elsewhere");
								//if(mine==4)
								qwerty = order.get(1);
								mine++;
							}


							//qwerty =h[1];

							socket.close();
						} catch (IOException e) {

						}

					}
					Log.e("result!!!!!!!!", result);
					w.put(msgToSend + result);

				} else if (msgToSend.contains("delete")) {

					String msg =  msgToSend;
					String Porttosend = String.valueOf((Integer.parseInt(suc1) * 2));

					Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
							Integer.parseInt(Porttosend));
					byte[] b;
					b = msg.getBytes();
					OutputStream outs = socket.getOutputStream();
					outs.write(b);
					outs.close();

					socket.close();
					String Porttosend2 = String.valueOf((Integer.parseInt(suc2) * 2));

					Socket socket2 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
							Integer.parseInt(Porttosend2));
					byte[] b2;
					b2 = msg.getBytes();
					OutputStream outs2 = socket2.getOutputStream();
					outs2.write(b2);
					outs2.close();

					socket2.close();
				}


			} catch (UnknownHostException e) {
				Log.e(TAG, e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Log.e("IT isnot working", "null");
			return null;
		}
	}

	private Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}

	int checkkey(String key) {

		try {
			if (genHash(key).compareTo(hashkeys.get(0)) < 0) {
				return 0;
			} else if (genHash(key).compareTo(hashkeys.get(4)) > 0) {
				return 0;
			}
			for (int i = 0; i < 4; i++) {
				if (genHash(key).compareTo(hashkeys.get(i)) > 0 && genHash(key).compareTo(hashkeys.get(i + 1)) < 0) {
					return i + 1;
				}
			}

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return -1;
	}

	private class Recovernow extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... msgs) {
			//for recovering
			//TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
			// String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
			// final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
			int pred11 = 0;
			int succ22 = 0;
			if (myplace == 4) {
				pred11 = myplace - 1;
				succ22 = 0;
			}
			if (myplace==1) {
				pred11 = myplace - 1;
				succ22 = 2;
			}
			if (myplace == 0) {
				pred11 = 4;
				succ22 = 1;
			}
			if(myplace==2){
				pred11 = 1;
				succ22=3;
			}
			if(myplace==3){
				pred11=2;
				succ22=4;
			}

			String[] remotePort = new String[5];
			remotePort[0] = REMOTE_PORT0;
			remotePort[1] = REMOTE_PORT1;
			remotePort[2] = REMOTE_PORT2;
			remotePort[3] = REMOTE_PORT3;
			remotePort[4] = REMOTE_PORT4;
			String msgToSend = msgs[0]; //Recover Me
			String port = msgs[1];

			Log.e("RECOVERY STARTING ON", port);
			String result = "";
			//String qwerty = order.get(myplace);
			String msg = null;
			String Portpred = String.valueOf((Integer.parseInt(order.get(pred11)) * 2));
			String Portsucc = String.valueOf((Integer.parseInt(order.get(succ22)) * 2));
			try {
				String test = "";
				Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
						Integer.parseInt(Portpred));
				DataOutputStream d = new DataOutputStream(socket.getOutputStream());
				d.writeBytes(msgToSend + "\n");
				d.flush();
				//Log.e("ASK FOR RECOVERY TO:", remotePort[i]);

				InputStreamReader isr = new InputStreamReader(socket.getInputStream());
				BufferedReader br = new BufferedReader(isr);
				if ((msg = br.readLine()) != null) {
					result += msg;
				}

				//Log.e("GOT RECOVERY FROM:", remotePort[i]);
				Log.e("Recover Data:", test += msg);

				socket.close();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}


			try {
				 msg = "";
				Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
						Integer.parseInt(Portsucc));
				DataOutputStream d = new DataOutputStream(socket1.getOutputStream());
				d.writeBytes(msgToSend + "\n");
				d.flush();
				//Log.e("ASK FOR RECOVERY TO:", remotePort[i]);

				InputStreamReader isr = new InputStreamReader(socket1.getInputStream());
				BufferedReader br = new BufferedReader(isr);
				if ((msg = br.readLine()) != null) {
					result += msg;
				}

				//Log.e("GOT RECOVERY FROM:", remotePort[i]);
				//Log.e("Recover Data:", test += msg);


			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			/*
			for (int i = 0; i < 5; i++) {
				if (!port.equals(remotePort[i])) {
					try {
						String test = "";
						Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
								Integer.parseInt(remotePort[i]));
						DataOutputStream d = new DataOutputStream(socket.getOutputStream());
						d.writeBytes(msgToSend + "\n");
						d.flush();
						Log.e("ASK FOR RECOVERY TO:", remotePort[i]);

						InputStreamReader isr = new InputStreamReader(socket.getInputStream());
						BufferedReader br = new BufferedReader(isr);
						if ((msg = br.readLine()) != null) {
							result += msg;
						}

						Log.e("GOT RECOVERY FROM:", remotePort[i]);
						Log.e("Recover Data:", test += msg);


					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}*/

			//Got values in result as Key Value Time|Key Value Time
			HashMap<String, String> map = new HashMap<String, String>();
			String alldata[] = result.split("\\|");
			Log.e("Got total key value:", "" + alldata.length);
			Log.e("MYPLACE IN ORDER ISSSS:", "" + myplace);
			if (alldata.length > 1) {
				for (int i = 0; i < alldata.length; i++) {
					String key = alldata[i].split(" ")[0];

					String val = alldata[i].split(" ")[1];
					String time = alldata[i].split(" ")[2];
					Log.e("alldata key value", key + ":" + val);
					int pred1 = 0;
					int pred2 = 0;
					if (myplace >= 2) {
						pred1 = myplace - 1;
						pred2 = myplace - 2;
					}
					if (myplace == 1) {
						pred1 = myplace - 1;
						pred2 = 4;
					}
					if (myplace == 0) {
						pred1 = 4;
						pred2 = 3;
					}

					int r = checkkey(key);
					Log.e("MYPLACE IN ORDER IS:", "" + myplace);
					if (r == myplace || r == pred1 || r == pred2) {

						map.put(key, val + " " + time);

					}
				}
				synchronized (obj) {
					for (String k : map.keySet()) {
						Log.e("Selected for insertion:", k + ":" + map.get(k));
						ContentValues values = new ContentValues();
						values.put("key", "my#" + k);
						values.put("value", map.get(k));
						insert(mUri, values);
					}

				}

			}
			recoverystatus = true;

			return null;

		}
	}
}
