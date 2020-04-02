package de.neemann.digital.plugin;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.*;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A simple ComPort
 */
public class ComPort extends Node implements Element {

    private static final Key<String> COM_PORT =
            new Key<>("comPort", "/dev/ttyUSB0")
                    .setName("Com Port")
                    .setDescription("The COM port to use!");
    private static final Key<Integer> BAUD_RATE =
            new Key.KeyInteger("baudRate", 9600)
                    .setMin(300)
                    .setComboBoxValues(300, 1200, 2400, 4800, 9600, 14400, 19200, 28800, 38400, 57600, 115200)
                    .setName("Baud Rate")
                    .setDescription("The baud rate to use.");

    /**
     * The description of the component
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(ComPort.class,
            input("D_w", "Data to write to the COM port."),
            input("wr", "the write enable"),
            input("rd", "the read enable"),
            input("C", "the clock").setClock()
    ) {
        @Override
        public String getDescription(ElementAttributes elementAttributes) {
            return "A simple COM port which allows to access serial hardware.";
        }
    }
            .addAttribute(Keys.LABEL)
            .addAttribute(COM_PORT)
            .addAttribute(BAUD_RATE)
            .addAttribute(Keys.ROTATE);  // allows to rotate the new component


    private final ObservableValue d_out;
    private final ObservableValue avail;
    private final String portName;
    private final int baudRate;
    private ObservableValue d_in;
    private ObservableValue wrVal;
    private ObservableValue rdVal;
    private ObservableValue clockVal;
    private boolean lastClock;
    private COMInterface comInterface;
    private int comData;

    /**
     * Creates a new COM port
     *
     * @param attr the attributes which are editable in the components properties dialog
     */
    public ComPort(ElementAttributes attr) {
        d_out = new ObservableValue("D_r", 8).setDescription("Data read from the physical port.");
        avail = new ObservableValue("av", 1).setDescription("If set to one there was data available. Valid only if rd=1 and c is rising.");
        portName = attr.get(COM_PORT);
        baudRate = attr.get(BAUD_RATE);
        comData = -1;
    }

    @Override
    public void readInputs() {
        boolean clock = clockVal.getBool();
        if (clock & !lastClock) {
            boolean rd = rdVal.getBool();
            if (rd && comInterface != null) {
                comData = comInterface.readData();
            } else {
                comData = -1;
            }

            boolean wr = wrVal.getBool();
            if (wr && comInterface != null) {
                long data = d_in.getValue();
                comInterface.write((int) data);
            }
        }
        lastClock = clock;
    }

    @Override
    public void writeOutputs() {
        if (comData < 0) {
            d_out.setToHighZ();
            avail.setBool(false);
        } else {
            d_out.setValue(comData);
            avail.setBool(true);
        }
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        d_in = inputs.get(0).checkBits(8, this);
        wrVal = inputs.get(1).checkBits(1, this);
        rdVal = inputs.get(2).checkBits(1, this);
        clockVal = inputs.get(3).addObserverToValue(this).checkBits(1, this);
    }

    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(d_out, avail);
    }

    @Override
    public void init(Model model) throws NodeException {
        comInterface = new RxTxComPort(portName, baudRate);
        model.addObserver(modelEvent -> {
            if (modelEvent.equals(ModelEvent.STOPPED))
                comInterface.close();
        }, ModelEvent.STOPPED);
    }
}
