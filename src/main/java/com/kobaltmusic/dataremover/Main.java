package com.kobaltmusic.dataremover;

import com.google.common.base.Stopwatch;
import com.kobaltmusic.dataremover.xml.XMLConverter;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Main {


    public static void main(String[] args) throws Exception {
        try {
            List<String> argumentsList = Arrays.asList(args);

            validateArgument(() -> argumentsList.contains(Argument.INPUT_FILE_PATH.getParam()), "\nWrong arguments.\n\n" + USAGE_STRING);

            Map<Argument, String> argumentsMap = parseArguments(argumentsList);

            String inputFile = argumentsMap.get(Argument.INPUT_FILE_PATH);
            checkNotNull(inputFile, "Input file cannot be null");
            Path inputFilePath = Paths.get(inputFile);
            checkArgument(Files.exists(inputFilePath), "Input file doesn't exist");

            String outputFile = Optional.ofNullable(argumentsMap.get(Argument.OUTPUT_FILE_PATH)).orElse(inputFile + " " + ".converted.xml");

            String configurationFile = Optional.ofNullable(argumentsMap.get(Argument.CONFIGURATION_FILE_PATH)).orElse("config.json");
            Path configurationFilePath = Paths.get(configurationFile);

            validateArgument(() -> Files.exists(configurationFilePath), "Configuration file not found. Please specify the configuration file location through the \"-c\" param, or place a configuration file in the jar folder.");

            System.out.println("Starting conversion");

            Stopwatch stopwatch = Stopwatch.createStarted();
            new XMLConverter(inputFile, outputFile, configurationFile).convert(argumentsMap.containsKey(Argument.ESCAPE));

            System.out.println("Conversion completed in " + stopwatch.stop());

        } catch (Exception e) {
            LoggerFactory.getLogger(Main.class).error("Error executing Transformator", e);
            throw e;
        }
    }

    private static Map<Argument, String> parseArguments(List<String> argumentsList) {
        Map<Argument, String> argumentsMap = new HashMap<>();
        for (int i = 0; i < argumentsList.size(); ) {
            Argument argument = Argument.getArgumentByParam(argumentsList.get(i));
            validateArgument(() -> argument != null, String.format("Param %s is not valid.\n%s", argumentsList.get(i), USAGE_STRING));
            if (argument.isSingleArgument()) {
                argumentsMap.put(argument, null);
            } else {
                String value = argumentsList.get(++i);
                validateArgument(() -> !value.startsWith("-"), String.format("%s is not a valid value for argument %s.\n%s", value, argument, USAGE_STRING));
                argumentsMap.put(argument, value);
            }
            i++;
        }
        return argumentsMap;
    }

    private static void validateArgument(Supplier<Boolean> condition, String message) {
        if (!condition.get()) {
            System.err.println(message);
            System.exit(-1);
        }
    }

    public static final String USAGE_STRING = "Usage: \"java -jar <library> -i inputFilePath [-o outputFile] [-p propertyFilePath] [-escape]\"";

    private enum Argument {
        OUTPUT_FILE_PATH("-o", false),
        INPUT_FILE_PATH("-i", false),
        CONFIGURATION_FILE_PATH("-c", false),
        ESCAPE("-escape", true);

        private final String param;
        private final boolean singleArgument;

        private static Map<String, Argument> paramMaps = new HashMap<>();

        static {
            for (Argument argument : Argument.values()) {
                paramMaps.put(argument.getParam(), argument);
            }
        }

        Argument(String param, boolean singleArgument) {
            this.param = param;
            this.singleArgument = singleArgument;
        }

        public String getParam() {
            return param;
        }

        public boolean isSingleArgument() {
            return singleArgument;
        }

        public static Argument getArgumentByParam(String param) {
            return paramMaps.get(param);
        }
    }

}
