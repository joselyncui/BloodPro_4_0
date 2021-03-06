package com.wm.tools;

import java.util.Locale;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;

/**
 * 负责扫描的工具类
 * @author Like
 *
 */
public class DeviceScanner {
	
	public final static int STATE_BEGIN_SCAN = 0;
	public final static int STATE_END_SCAN = 1;

	// 扫描时间，超过这个时间没有扫描到设备，认定扫描失败
	private final static int SCAN_PERIOD = 8000;
	private static DeviceScanner mDeviceScanner = null;
	// 存放扫描到的设备
	private BluetoothDevice mDevice = null;
	private Handler mHandler = null;
	private BluetoothAdapter mBluetoothAdapter = null;
	private boolean mScanning = false;
	private ScanCallback mCallback = null;

	private DeviceScanner(BluetoothAdapter bluetoothAdapter,
			ScanCallback callback) {
		this.mHandler = new Handler();
		this.mBluetoothAdapter = bluetoothAdapter;
		this.mCallback = callback;
	}

	public static DeviceScanner getInstance(BluetoothAdapter bluetoothAdapter,
			ScanCallback callback) {
		if (mDeviceScanner == null)
			mDeviceScanner = new DeviceScanner(bluetoothAdapter, callback);
		return mDeviceScanner;
	}

	public interface ScanCallback {
		
		void onScanStateChange(int scanState);

		void onScanSuccess(BluetoothDevice device);

		void onScanFailed();

	}

	/**
	 * 控制扫描的开始与结束
	 * 
	 * @param enable
	 *            如果为true开始扫描，false结束扫描
	 */
	@SuppressWarnings("deprecation")
	public void scanLeDevice(final boolean enable) {
		if (enable) {
			if(mScanning)
				return;
			mDevice = null;
			mCallback.onScanStateChange(STATE_BEGIN_SCAN);
			// 在指定时间之后停止扫描
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					scanLeDevice(false);
				}
			}, SCAN_PERIOD);
			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mCallback.onScanStateChange(STATE_END_SCAN);
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			if(mDevice == null) 
				mCallback.onScanFailed();
			else
				mCallback.onScanSuccess(mDevice);
		}
	}

	private LeScanCallback mLeScanCallback = new LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			if (isBloodPressure(device)) {
				mDevice = device;
				scanLeDevice(false);
			}
		}
	};

	/**
	 * 用于判断扫描到的设备是否是血压计
	 * 根据蓝牙名称判断是否为血压计
	 * 
	 * @param device
	 *            扫描到的设备
	 * @return 如果是血压计返回true， 否则返回false
	 */
	public boolean isBloodPressure(BluetoothDevice device) {
//		if (device.getName().toLowerCase(Locale.getDefault())
//				.contains("ble")) {
//			return true;
//		}
//		return false;
		return true;
	}
	
	/**
	 * 用于判断是否处于正在扫描状态
	 * @return 如果正在扫描返回true，否在返回false
	 */
	public boolean isScanning() {
		return mScanning;
	}

}
