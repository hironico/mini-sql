package com.jenkov.cliargs;

import java.util.HashMap;
import java.util.TreeSet;

import java.lang.reflect.Field;

/**
 * Utility class for parsing command line arguments.
 * Provides methods to handle switches and their values from command line input.
 */
public class CliArgs {

    private String[] args = null;

    private final HashMap<String, Integer> switchIndexes = new HashMap<String, Integer>();
    private final TreeSet<Integer> takenIndexes = new TreeSet<Integer>();

    /**
     * Constructs a CliArgs instance and parses the given arguments.
     * @param args the command line arguments array to parse
     */
    public CliArgs(String[] args) {
        parse(args);
    }

    /**
     * Parses the given arguments array.
     * @param arguments the arguments to parse
     */
    public void parse(String[] arguments) {
        this.args = arguments;
        //locate switches.
        switchIndexes.clear();
        takenIndexes.clear();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                switchIndexes.put(args[i], i);
                takenIndexes.add(i);
            }
        }
    }

    /**
     * Returns the original arguments array.
     * @return the arguments array
     */
    public String[] args() {
        return args;
    }

    /**
     * Retrieves the argument at the specified index.
     * @param index the index of the argument
     * @return the argument at the index
     */
    public String arg(int index) {
        return args[index];
    }

    /**
     * Checks if the specified switch is present in the arguments.
     * @param switchName the name of the switch
     * @return true if the switch is present, false otherwise
     */
    public boolean switchPresent(String switchName) {
        return switchIndexes.containsKey(switchName);
    }

    /**
     * Retrieves the value of a switch.
     * @param switchName the name of the switch
     * @return the switch value or null if not found
     */
    public String switchValue(String switchName) {
        return switchValue(switchName, null);
    }

    /**
     * Retrieves the value of a switch, with a default value if not found.
     * @param switchName the name of the switch
     * @param defaultValue the default value to return if switch is not found
     * @return the switch value or the default value
     */
    public String switchValue(String switchName, String defaultValue) {
        if (!switchIndexes.containsKey(switchName))
            return defaultValue;

        int switchIndex = switchIndexes.get(switchName);
        if (switchIndex + 1 < args.length) {
            takenIndexes.add(switchIndex + 1);
            return args[switchIndex + 1];
        }
        return defaultValue;
    }

    /**
     * Retrieves the long value of a switch.
     * @param switchName the name of the switch
     * @return the switch value as Long or null
     */
    public Long switchLongValue(String switchName) {
        return switchLongValue(switchName, null);
    }

    /**
     * Retrieves the long value of a switch, with a default value.
     * @param switchName the name of the switch
     * @param defaultValue the default value to return if switch is not found or parsing fails
     * @return the switch value as Long or the default value
     */
    public Long switchLongValue(String switchName, Long defaultValue) {
        String switchValue = switchValue(switchName, null);

        if (switchValue == null)
            return defaultValue;
        return Long.parseLong(switchValue);
    }

    /**
     * Retrieves the double value of a switch.
     * @param switchName the name of the switch
     * @return the switch value as Double or null
     */
    public Double switchDoubleValue(String switchName) {
        return switchDoubleValue(switchName, null);
    }

    /**
     * Retrieves the double value of a switch, with a default value.
     * @param switchName the name of the switch
     * @param defaultValue the default value to return if switch is not found or parsing fails
     * @return the switch value as Double or the default value
     */
    public Double switchDoubleValue(String switchName, Double defaultValue) {
        String switchValue = switchValue(switchName, null);

        if (switchValue == null)
            return defaultValue;
        return Double.parseDouble(switchValue);
    }

    /**
     * Retrieves the array of values for a switch.
     * @param switchName the name of the switch
     * @return the array of switch values, or empty array if not found
     */
    public String[] switchValues(String switchName) {
        if (!switchIndexes.containsKey(switchName))
            return new String[0];

        int switchIndex = switchIndexes.get(switchName);

        int nextArgIndex = switchIndex + 1;
        while (nextArgIndex < args.length && !args[nextArgIndex].startsWith("-")) {
            takenIndexes.add(nextArgIndex);
            nextArgIndex++;
        }

        String[] values = new String[nextArgIndex - switchIndex - 1];
        for (int j = 0; j < values.length; j++) {
            values[j] = args[switchIndex + j + 1];
        }
        return values;
    }

    /**
     * Creates a POJO instance and fills it with switch values based on its fields.
     * @param <T> the type of the POJO
     * @param pojoClass the class of the POJO to create
     * @return the instantiated POJO with fields set from switches
     * @throws RuntimeException if instantiation or field access fails
     */
    public <T> T switchPojo(Class<T> pojoClass) {
        try {
            T pojo = pojoClass.getDeclaredConstructor().newInstance();

            Field[] fields = pojoClass.getFields();
            for (Field field : fields) {
                Class<?> fieldType = field.getType();
                String fieldName = "-" + field.getName().replace('_', '-');

                if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
                    field.set(pojo, switchPresent(fieldName));
                } else if (fieldType.equals(String.class)) {
                    if (switchValue(fieldName) != null) {
                        field.set(pojo, switchValue(fieldName));
                    }
                } else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
                    if (switchLongValue(fieldName) != null) {
                        field.set(pojo, switchLongValue(fieldName));
                    }
                } else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
                    if (switchLongValue(fieldName) != null) {
                        field.set(pojo, switchLongValue(fieldName).intValue());
                    }
                } else if (fieldType.equals(Short.class) || fieldType.equals(short.class)) {
                    if (switchLongValue(fieldName) != null) {
                        field.set(pojo, switchLongValue(fieldName).shortValue());
                    }
                } else if (fieldType.equals(Byte.class) || fieldType.equals(byte.class)) {
                    if (switchLongValue(fieldName) != null) {
                        field.set(pojo, switchLongValue(fieldName).byteValue());
                    }
                } else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
                    if (switchDoubleValue(fieldName) != null) {
                        field.set(pojo, switchDoubleValue(fieldName));
                    }
                } else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
                    if (switchDoubleValue(fieldName) != null) {
                        field.set(pojo, switchDoubleValue(fieldName).floatValue());
                    }
                } else if (fieldType.equals(String[].class)) {
                    String[] values = switchValues(fieldName);
                    if (values.length != 0) {
                        field.set(pojo, values);
                    }
                }
            }

            return pojo;
        } catch (Exception e) {
            throw new RuntimeException("Error creating switch POJO", e);
        }
    }

    /**
     * Returns the array of targets (non-switch arguments).
     * @return the array of target arguments
     */
    public String[] targets() {
        String[] targetArray = new String[args.length - takenIndexes.size()];
        int targetIndex = 0;
        for (int i = 0; i < args.length; i++) {
            if (!takenIndexes.contains(i)) {
                targetArray[targetIndex++] = args[i];
            }
        }

        return targetArray;
    }

}
