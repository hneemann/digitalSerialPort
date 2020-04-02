package de.neemann.digital.plugin;

import de.neemann.digital.core.NodeException;
import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RxTxComPort implements COMInterface {

    private final RXTXPort serialPort;
    private final OutputStream outputStream;
    private final InputThread thread;
    private final InputStream inputStream;

    public RxTxComPort(String portName, int baudRate) throws NodeException {
        try {
            CommPortIdentifier port = CommPortIdentifier.getPortIdentifier(portName);

            try {
                serialPort = port.open("Digital", 2000);
            } catch (PortInUseException e) {
                throw new NodeException("Port " + portName + " is in use!");
            }

            try {
                serialPort.setSerialPortParams(baudRate,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                serialPort.enableReceiveTimeout(1000);

                outputStream = serialPort.getOutputStream();
                inputStream = serialPort.getInputStream();
                thread = new InputThread(inputStream);
                thread.setDaemon(true);
                thread.start();
            } catch (UnsupportedCommOperationException e) {
                serialPort.close();
                throw createNodeException("invalid port parameter", e);
            }

        } catch (NoSuchPortException e) {
            throw createNodeException("error initializing comm port", e);
        }
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
        if (thread != null)
            return thread.getData();
        return -1;
    }

    @Override
    public void close() {
        if (thread != null)
            thread.interrupt();

        if (serialPort != null)
            serialPort.close();
    }

    private static final class InputThread extends Thread {
        private static final int BUFSIZE = 1024;
        private final InputStream inputStream;

        private final int[] buffer = new int[BUFSIZE];
        private int newest;
        private int oldest;
        private int inBuffer;

        private InputThread(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    int data = inputStream.read();
                    if (data >= 0)
                        synchronized (buffer) {
                            if (inBuffer < BUFSIZE) {
                                buffer[newest] = data;
                                newest = inc(newest);
                                inBuffer++;
                            }
                        }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private int getData() {
            synchronized (buffer) {
                if (inBuffer > 0) {
                    int r = buffer[oldest];
                    inBuffer--;
                    oldest = inc(oldest);
                    return r;
                } else
                    return -1;
            }
        }

        private static int inc(int p) {
            p++;
            if (p == BUFSIZE)
                p = 0;
            return p;
        }
    }
}
