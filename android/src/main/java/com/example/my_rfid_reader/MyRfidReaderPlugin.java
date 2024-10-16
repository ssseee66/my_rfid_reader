package com.example.my_rfid_reader;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.gg.reader.api.dal.GClient;
import com.gg.reader.api.dal.HandlerDebugLog;
import com.gg.reader.api.protocol.gx.EnumG;
import com.gg.reader.api.protocol.gx.MsgBaseInventoryEpc;
import com.gg.reader.api.protocol.gx.MsgBaseStop;
import com.gg.reader.api.protocol.gx.MsgAppGetReaderInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.StandardMessageCodec;

/** MyRfidReaderPlugin */
public class MyRfidReaderPlugin implements FlutterPlugin{
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private static final String FLUTTER_TO_ANDROID_CHANNEL = "flutter_rfid_android";
    private BasicMessageChannel<Object> flutter_channel;
    private Context applicationContext;
    private GClient client = new GClient();
    private boolean CONNECT_SUCCESS = false;
    private Map<String, Object> message_map = new HashMap<>();
    private boolean POWER_ON = false;
    private List<String> epc_message = new LinkedList<>();
    private boolean APPEAR_OVER = true;
    
    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        Log.e("onAttachedToEngine", "onAttachedToEngine");
        applicationContext = flutterPluginBinding.getApplicationContext();
        
        flutter_channel = new BasicMessageChannel<>(
                flutterPluginBinding.getBinaryMessenger(),
                FLUTTER_TO_ANDROID_CHANNEL,
                StandardMessageCodec.INSTANCE
        );
        
        subscriberHandler();     // 订阅标签事件
        
        flutter_channel.setMessageHandler((message, reply) -> {
            Map<String, Object> arguments = (Map<String, Object>) message;
            if (arguments != null) {
                if (arguments.containsKey("startConnect")) {
                    if ((boolean) arguments.get("startConnect")) {
                        
                        CONNECT_SUCCESS = client.openHdSerial("13:115200", 1000);
                        Log.e("连接", "连接中。。。。。");
                        if (CONNECT_SUCCESS) {
                            Log.e("连接", "连接成功");
                            message_map.clear();
                            message_map.put("connectMessage", "连接成功");
                            flutter_channel.send(message_map);
                        } else {
                            message_map.clear();
                            message_map.put("connectMessage", "连接失败");
                            flutter_channel.send(message_map);
                        }
                    }
                } else if (arguments.containsKey("turnOnPower")) {
                    if ((boolean) arguments.get("turnOnPower")) {
                        if (CONNECT_SUCCESS) {
                            Log.e("上电", "上电成功");
                            client.hdPowerOn();
                            MsgAppGetReaderInfo msgAppGetReaderInfo = new MsgAppGetReaderInfo();
                            client.sendSynMsg(msgAppGetReaderInfo);
                            String serial_number = "";
                            if (msgAppGetReaderInfo.getRtCode() == 0) {
                                serial_number = msgAppGetReaderInfo.getReaderSerialNumber();
                                Log.e("serial_number", serial_number);
                                message_map.clear();
                                message_map.put("powerMessage", "上电成功#" + serial_number);
                                flutter_channel.send(message_map);
                                POWER_ON = true;
                            } else {
                                Log.e("serial_number", msgAppGetReaderInfo.getRtCode() + "");
                                message_map.clear();
                                message_map.put("powerMessage", "未查询到流水号");
                                flutter_channel.send(message_map);
                            }
                            
                        } else {
                            Log.e("上电", "上电失败");
                            message_map.clear();
                            message_map.put("powerMessage", "上电失败，未建立连接，请先建立连接");
                            flutter_channel.send(message_map);
                            POWER_ON = false;
                        }
                    }
                } else if (arguments.containsKey("turnOffPower")) {
                    if ((boolean) arguments.get("turnOffPower")) {
                        if (CONNECT_SUCCESS) {
                            Log.e("下电", "下电成功");
                            client.hdPowerOff();
                            message_map.clear();
                            message_map.put("powerMessage", "下电成功");
                            flutter_channel.send(message_map);
                            POWER_ON = false;
                        } else {
                            Log.e("下电", "下电失败");
                            message_map.clear();
                            message_map.put("powerMessage", "下电失败，未建立连接，请先建立连接");
                            flutter_channel.send(message_map);
                        }
                    }
                } else if (arguments.containsKey("startReader")) {
                    if ((boolean) arguments.get("startReader")) {
                        if (POWER_ON) {
                            MsgBaseInventoryEpc msgBaseInventoryEpc = new MsgBaseInventoryEpc();
                            msgBaseInventoryEpc.setAntennaEnable(EnumG.AntennaNo_1);
                            msgBaseInventoryEpc.setInventoryMode(EnumG.InventoryMode_Single);
                            client.sendSynMsg(msgBaseInventoryEpc);
                            boolean operationSuccess = false;
                            if (0x00 == msgBaseInventoryEpc.getRtCode()) {
                                // Log.e("读卡", "操作成功");
                                Log.e("读卡", "操作成功");
                                operationSuccess = true;
                            } else {
                                // Log.e("读卡", "操作失败");
                                message_map.clear();
                                message_map.put("readerOperationMessage",
                                        "读卡操作失败：" + msgBaseInventoryEpc.getRtCode() + msgBaseInventoryEpc.getRtMsg());
                                flutter_channel.send(message_map);
                                Log.e("读卡", "操作失败");
                            }
                            // 搞不懂为什么要在外层进行通讯才行，在里面发送的话会发送不了
                            // 并且通讯方法只能在主线程中调用，无法通过创建新线程处理
                            if (operationSuccess) {
                                Log.e("读卡操作", "读卡操作成功");
                                message_map.clear();
                                message_map.put("readerOperationMessage", "读卡操作成功");
                                flutter_channel.send(message_map);
                            }
                        } else {
                            message_map.clear();
                            message_map.put("readerOperationMessage", "未上电，请先进行上电操作");
                            flutter_channel.send(message_map);
                        }
                    }
                } else if (arguments.containsKey("startReaderEpc")) {
                    if ((boolean) arguments.get("startReaderEpc")) {
                        Log.e("start_reader_epc", "开始读数据");
                        if (APPEAR_OVER) {
                            Log.e("start_reader_epc", "读数据");
                            message_map.clear();
                            epc_message.add("OK");   // 当未读取到数据时，flet端会收取不到信息，添加一条读取数据完成的标识
                            message_map.put("epcMessages", epc_message);
                            flutter_channel.send(message_map);
                            epc_message.clear();
                            APPEAR_OVER = false;
                        } else {
                            message_map.clear();
                            List<String> message_list = new LinkedList<>();
                            message_list.add("未上报结束");
                            message_map.put("epcMessages", message_list);
                            flutter_channel.send(message_map);
                            Log.e("appear_over_not", "未上报结束");
                        }
                    }
                }
                // else if (arguments.containsKey("closeConnect")) {
                //     if ((boolean) arguments.get("closeConnect")) {
                //         client.close();
                //         CONNECT_SUCCESS = false;
                //         message_map.clear();
                //         message_map.put("connectMessage", "连接已关闭");
                //         flutter_channel.send(message_map);
                //     }
                // }
            }
        });
    }
    private void subscriberHandler() {
        client.onTagEpcLog = (s, logBaseEpcInfo) -> {
            if (logBaseEpcInfo.getResult() == 0) {
                Log.e("readerEPC", logBaseEpcInfo.getEpc());
                epc_message.add(logBaseEpcInfo.getEpc());
            }
        };
        client.onTagEpcOver = (s, logBaseEpcOver) -> {
            Log.e("HandlerTagEpcOver", logBaseEpcOver.getRtMsg());
            // send();
            Log.e("epcAppearOver", epc_message.toString());
            APPEAR_OVER = true;
        };
        
        client.debugLog = new HandlerDebugLog() {
            public void sendDebugLog(String msg) {
                Log.e("sendDebugLog",msg);
            }
            public void receiveDebugLog(String msg) {
                Log.e("receiveDebugLog",msg);
            }
            
            @Override
            public void crcErrorLog(String msg) {
                Log.e("crcErrorLog", msg);
            }
        };
    }

//  @Override
//  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
//    if (call.method.equals("getPlatformVersion")) {
//      result.success("Android " + android.os.Build.VERSION.RELEASE);
//    } else {
//      result.notImplemented();
//    }
//  }
    
    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {}
}
