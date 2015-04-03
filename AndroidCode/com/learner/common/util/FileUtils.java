

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import android.content.Context;
import android.os.Environment;

public class FileUtils {
	/**
	 * @param context
	 *            设备上下文
	 * @param fileName
	 *            文件名
	 * @param content
	 *            文件保存内容
	 * @param append
	 *            是否为追加模式
	 * 
	 * @变更记录 2013-8-10 上午9:55:20 lig
	 * 
	 */
	public static void saveFile(Context context, String fileName, String content, boolean append) {
		FileOutputStream output = null;
		try {
			output = context.openFileOutput(fileName, append ? Context.MODE_APPEND : Context.MODE_PRIVATE);
			output.write(content.getBytes());
			output.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
				}
			}
		}
	}

	//从属性文件中读数据
	 public String readData(String key,String fileName) {  
         Properties props = new Properties();  
         try {  
             InputStream in = new BufferedInputStream(new FileInputStream(  
                     new File(fileName)));  
             props.load(in);  
             in.close();  
             String value = props.getProperty(key);  
             return value;  
         } catch (Exception e) {  
             e.printStackTrace();  
             return null;  
         }  
     }   
	
	 
	//向属性文件中写入数据
	  public void writeData(String fileName,String key, String value) {  
	        Properties prop = new Properties();  
	        try {  
	            File file = new File(fileName);  
	            if (!file.exists())  
	                file.createNewFile();  
	            InputStream fis = new FileInputStream(file);  
	            prop.load(fis);  
	            fis.close();//一定要在修改值之前关闭fis  
	            OutputStream fos = new FileOutputStream(fileName);  
	            prop.setProperty(key, value);  
	            prop.store(fos, "Update '" + key + "' value");  
	            prop.store(fos, "just for test");  
	           
	            fos.close();  
	        } catch (IOException e) {  
	            e.printStackTrace();
	        }
	    }
	  
	  
	public static void saveFile(String fileName, String content,boolean append){  
		try {  
		//打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件  
		FileWriter writer = new FileWriter(fileName, append);  
		writer.write(content); 
		writer.flush();
		writer.close();  
		} catch (IOException e) {  
		e.printStackTrace();  
		}  
	} 
	/**
	 * @param context
	 * @param fileName
	 * @return 文件内容 或null
	 * 
	 * @变更记录 2013-8-10 上午10:01:57 lig
	 * 
	 */
	public static String readFile(Context context, String fileName) {
		FileInputStream input = null;
		try {
			input = context.openFileInput(fileName);
			StringBuffer buffer = new StringBuffer();
			byte[] data = new byte[2048];
			int length = 0;
			while ((length = input.read(data)) != -1) {
				buffer.append(new String(data, 0, length));
			}
			return buffer.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}
	public static String assertFile(Context context, String fileName) {
		InputStream input = null;
		try {
			input = context.getAssets().open(fileName);
			StringBuffer buffer = new StringBuffer();
			byte[] data = new byte[2048];
			int length = 0;
			while ((length = input.read(data)) != -1) {
				buffer.append(new String(data, 0, length));
			}
			return buffer.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}
	public static String readFile(File fileName) {
		FileInputStream input = null;
		try {
			input = new FileInputStream(fileName);
			StringBuffer buffer = new StringBuffer();
			byte[] data = new byte[2048];
			int length = 0;
			while ((length = input.read(data)) != -1) {
				buffer.append(new String(data, 0, length));
			}
			return buffer.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	/**文件存储路径
	 * @param context
	 * @return /sdcard/certusnet/packagename/  OR  /data/data/packagename/files/certusnet/
	 */
	public static File getStorePath(Context context) {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		if (mExternalStorageAvailable && mExternalStorageWriteable) {
			File f = Environment.getExternalStoragePublicDirectory("certusnet");
			File app= new File(f, context.getPackageName());
			if (!app.exists())
				app.mkdirs();
			return app;
		}
		return context.getDir("certusnet", Context.MODE_PRIVATE);
	}
}
