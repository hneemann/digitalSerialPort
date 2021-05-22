package de.neemann.digital.plugin;

/**
 * Abstraction of a COM interface
 */
public interface COMInterface {
    /**
     * Write a byte to the com port
     *
     * @param data the data to write
     */
    void write(int data);

    /**
     * Reads a byte from the comm interface
     *
     * @return the byte or -1 if no data is available
     */
    int readData();

    /**
     * Closes the port.
     */
    void close();

    /**
     * Checks if there is new data in the receive buffer
     *
     * @return true if new data is available
     */
    boolean available();

    /**
     * Subscribes a DataAvailInterface object which will be notified when new data is received from the
     * serial port
     *
     * @param sub the observer object
     */
    void subscribeToDataAvail(DataAvailInterface sub);

    /**
     * Unsubscribes a previously subscribed DataAvailInterface
     *
     * @param sub the observer object
     */
    void unSubscribeToDataAvail(DataAvailInterface sub);
}
