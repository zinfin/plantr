package com.esri.android.plantr;

import com.esri.android.plantr.ble.BleManager;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* Copyright 2016 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For additional information, contact:
 * Environmental Systems Research Institute, Inc.
 * Attn: Contracts Dept
 * 380 New York Street
 * Redlands, California, USA 92373
 *
 * email: contracts@esri.com
 *
 */


/**
 * Created by chri8702 on 12/15/2016.
 */

public class MLUSensor implements BleManager.BleManagerListener
{
  public interface MLUSensorCallback
  {
    void onConnecting();
    void onConnected();
    void onDisconnected();
    void onNewValue(int value);
  }

  // Service Constants
  private static final String UUID_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
  private static final String UUID_RX = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
  private static final String UUID_TX = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
  private static final String UUID_DFU = "00001530-1212-EFDE-1523-785FEABCD123";

  private Context _context = null;
  private BleManager _bleManager = null;
  private MLUSensorCallback _callback = null;
  private boolean _isConnected = false;

  private int _value = -1;

  private Pattern _pattern = null;
  private Matcher _matcher = null;

  public MLUSensor(Context context)
  {
    _context = context;
    _bleManager = BleManager.getInstance(_context);
    _bleManager.setBleListener(this);
    startConnection();
  }

  private void startConnection()
  {
    _bleManager.connect(_context, "D4:81:68:08:B0:E8");
  }

  public void setCallback(MLUSensorCallback callback)
  {
    _callback = callback;
  }

  public boolean isConnected()
  {
    return _isConnected;
  }

  public int getValue()
  {
    synchronized (_context)
    {
      return _value;
    }
  }

  private void parseData(String data)
  {
    if (_pattern == null)
    {
      try
      {
        _pattern = Pattern.compile("(\\d{1,2})( - Sum: )?(?:\\d*)?(?: - Avg: )?(?:\\d*)? \\\\");
      }
      catch (Exception e)
      {
        _pattern = null;
        return;
      }
    }

    Matcher matcher = _pattern.matcher(data);
    if (matcher.find())
    {
      try
      {
        String v = matcher.group(1);
        int value = Integer.valueOf(v);

        synchronized (_context)
        {
          _value = value;
        }

        if (_callback != null)
        {
          _callback.onNewValue(value);
        }
      }
      catch (Exception e)
      {
      }
    }
  }

  @Override
  public void onDataAvailable(BluetoothGattCharacteristic characteristic)
  {
    if (characteristic.getUuid().toString().equalsIgnoreCase(UUID_RX))
    {
      String data = new String(characteristic.getValue(), Charset.forName("UTF-8"));
      parseData(data);
    }
  }

  @Override
  public void onConnecting()
  {
  }

  @Override
  public void onConnected()
  {
    // Treat the "Connected" event as the start of the connection process, since
    // there's still the need to negotiate services and UART endpoints; once that's
    // complete, tell the client we're all connected and ready
    if (_callback != null)
    {
      _callback.onConnecting();
    }
  }

  @Override
  public void onDisconnected()
  {
    _isConnected = false;
    if (_callback != null)
    {
      _callback.onDisconnected();
    }

    startConnection();
  }

  @Override
  public void onServicesDiscovered()
  {
    BluetoothGattService uartService = _bleManager.getGattService(UUID_SERVICE);
    _bleManager.enableNotification(uartService, UUID_RX, true);

    // Now that services are set up for notifications, we should shortly begin receiving
    // data; tell the client we're ready to go
    _isConnected = true;
    if (_callback != null)
    {
      _callback.onConnected();
    }
  }

  @Override
  public void onDataAvailable(BluetoothGattDescriptor descriptor)
  {
  }

  @Override
  public void onReadRemoteRssi(int rssi)
  {
  }
}

