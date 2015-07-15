package otld.otld;

import otld.otld.intermediate.Program;
import otld.otld.jvm.BytecodeCompiler;
import otld.otld.parsing.*;
import otld.otld.parsing.Error;

import java.io.*;
import java.util.Collection;
import java.util.List;

/**
 * Main class of the application.
 *
 * Ties the different parts together.
 */
public class Main {
    public static final String VERSION = "v0.1";
    public static final String EXT = "tldr";

    /**
     * Main method of the compiler suite.
     */
    public static void main(final String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "compile":
                    handleCompile(args);
                    break;
                case "help":
                    printHelp();
                    break;
                default:
                    System.err.println("Unregcognized command.");
            }
        } else {
            printHelp();
        }
    }

    /**
     * Handler for the compile command.
     *
     * @param args The original arguments of the program.
     */
    public static void handleCompile(final String[] args) {
        String[] filenames = new String[args.length - 1];

        // Collect all file names
        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                if (!args[i].matches("[a-zA-Z][a-zA-Z0-9]*")) {
                    System.err.println("Invalid program name: " + args[i]);
                    System.exit(1);
                }
                filenames[i - 1] = args[i];
            }
        }

        // Compile all file names
        for (String filename : filenames) {
            try {
                File file = new File(filename + "." + EXT);
                compile(file);
            } catch (IOException e) {
                System.err.println(String.format("Error with file: %s (%s)", filename, e.getMessage()));
            }
        }
    }

    /**
     * Compiles the given file.
     *
     * @param file The source file to compile.
     * @throws IOException There was an error reading the given file or writing the output file.
     */
    public static void compile(final File file) throws IOException {
        FileInputStream in = new FileInputStream(file);

        otldRailroad parser = otldRailroad.parseFile(in);

        if (!parser.getErrors().isEmpty()) {
            handleCompileErrors(parser.getErrors());
            System.exit(1);
        }

        Program program = parser.getProgram();

        // Compile program to bytecode
        Compiler compiler = new BytecodeCompiler(program);
        compiler.compile();

        // Get output path
        String folderPath = file.getParent();
        String outPath = (folderPath == null ? program.getId() : folderPath) + ".class";
        FileOutputStream out = new FileOutputStream(outPath);

        // Write program bytecode
        out.write(compiler.asByteArray());
        out.close();
    }

    /**
     * Prints every error to stderr.
     * @param errors The errors.
     */
    public static void handleCompileErrors(final List<Error> errors) {
        for (Error error : errors) {
            System.err.println(error.getError());
        }
    }

    /**
     * Prints the help information to the standard output.
     */
    public static void printHelp() {
        StringBuilder out = new StringBuilder();
        String sep = System.lineSeparator();

        out.append("Open Transport Language Deluxe ").append(VERSION).append(sep);
        out.append("Usage: java -jar otld.jar <program-name>").append(sep).append(sep);

        out.append("Please check the following:").append(sep);
        out.append("- The command should be run from the directory containing the OTLD program file.").append(sep);
        out.append("- The name of the file should be equal to the program name and only contain letters (a-z) and numbers, but not start with a number.").append(sep);
        out.append("- The file name should end with '.tldr', which is the extension for OTLD program files.").append(sep).append(sep);

        out.append("After compilation the program is saved to the file <program-name>.class.").append(sep);
        out.append("The program can be run like a normal Java program.").append(sep);
        out.append("Command: java <program-name>").append(sep);

        System.out.println(out.toString());
    }
}
