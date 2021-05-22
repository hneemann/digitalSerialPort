package de.neemann.digital.plugin;

import de.neemann.digital.core.NodeException;
import com.fazecast.jSerialComm.*;
import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class JSerialComPort implements COMInterface, SerialPortDataListener {
    private final SerialPort serialPort;
    private final OutputStream outputStream;
    private final InputStream inputStream;
    private final Buffer cache;
    private final List<DataAvailInterface> datasubs;

    public JSerialComPort(String portName, int baudRate) throws NodeException {
        cache = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(1024));

        try {
            serialPort = SerialPort.getCommPort(portName);
        } catch (SerialPortInvalidPortException e) {
            throw new NodeException("Port " + portName + " is in invalid!");
        }

        if (!serialPort.setComPortParameters(baudRate,
                8,
                SerialPort.ONE_STOP_BIT,
                SerialPort.NO_PARITY)) {
            serialPort.closePort();
            throw new NodeException("Invalid parameter for port " + portName + "!");
        }

        if (!serialPort.openPort()) {
            throw new NodeException("Cannot open port " + portName + "!");
        }

        inputStream = serialPort.getInputStream();
        outputStream = serialPort.getOutputStream();
        serialPort.addDataListener(this);
        datasubs = new ArrayList<>();
    }

    private NodeException createNodeException(String message, Exception e) {
        String m = e.getMessage();
        if (m == null || m.length() == 0)
            return new NodeException(message + ": " + e.getClass().getSimpleName(), e);
        else
            return new NodeException(message, e);
    }

    @Override
    public void write(int data) {
        if (outputStream != null) {
            try {
                outputStream.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int readData() {
        if (!cache.isEmpty()) {
            return (int) cache.remove();
        }
        return -1;
    }

    @Override
    public void close() {
        if (serialPort != null) {
            datasubs.clear();
            serialPort.removeDataListener();
            serialPort.closePort();
        }
    }

    @Override
    public boolean available() {
        return !cache.isEmpty();
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;

        for (int i=0; i<serialPort.bytesAvailable(); i++) {
            try {
                int datum = inputStream.read();
                if (datum >= 0) {
                    cache.add(datum);
                }
            } catch (IOException e) {
                return;
            }
        }

        if (!cache.isEmpty()) {
            for (DataAvailInterface sub : datasubs) {
                sub.onDataAvailable();
            }
        }
    }

    @Override
    public void subscribeToDataAvail(DataAvailInterface sub) {
        datasubs.add(sub);
    }

    @Override
    public void unSubscribeToDataAvail(DataAvailInterface sub) {
        datasubs.remove(sub);
    }
}
