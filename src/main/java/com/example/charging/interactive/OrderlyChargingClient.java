package com.example.charging.interactive;

import com.example.charging.entity.EVData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

// 用户端，可以通过此class输入必要的各类参数
public class OrderlyChargingClient {
    private boolean isConnected = false;
    private static final Logger logger = LogManager.getLogger(OrderlyChargingClient.class);

    public OrderlyChargingClient() {}

    public void sendEVData(EVData ev) {
        while (!isConnected) {
            try{
                Socket socket = new Socket("localHost", 8080);
                isConnected = true;
                logger.info("Connected");
                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                outputStream.writeObject(ev);
                logger.info("Object to be written: " + ev.getUuid());
            } catch (IOException se) {se.printStackTrace();}
        }
    }
}
