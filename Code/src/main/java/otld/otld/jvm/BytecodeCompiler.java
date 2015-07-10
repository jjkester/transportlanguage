package otld.otld.jvm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;
import otld.otld.Compiler;
import otld.otld.intermediate.*;

import java.util.*;

/**
 * Compiler for compiling an intermediate Program to Java bytecode.
 *
 * This compiler targets Java version 1.7. Compiled programs can be run on any JRE that supports this language level,
 * which will most likely be JRE 7 or higher.
 *
 * Use {@code compileDebug()} instead of {@code compile()} to test ASM visitor calls.
 */
public class BytecodeCompiler extends Compiler {
    /** The compilation result. */
    private byte[] result;

    /** The actual class writer. */
    private ClassWriter writer;

    /** The visitor for writing a class. Will contain the class writer, possibly with adapters. */
    private ClassVisitor visitor;

    /** Visitor for the current method. Can be {@code null}. */
    private MethodVisitor methodVisitor;

    /** The storage location of variables. A negative value indicates that the variable is a field. */
    private Map<Variable, Integer> localStorage;

    /** The local variable containing the class instance. */
    private int objectLocation;

    /** The targets for the break operation. */
    private Stack<Label> breakTargets;

    /** Variables with an initial value. */
    private Set<Variable> initialVariables;

    /** The number of local variables used. */
    private int maxLocal = 16; // Sane default

    /** The number of stack positions used. */
    private int maxStack = 16; // Sane default

    /**
     * Creates a new compiler for Java bytecode.
     * @param program The program to compile.
     */
    public BytecodeCompiler(final Program program) {
        super(program);
        this.result = new byte[0];
        this.writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        this.visitor = this.writer;
        this.methodVisitor = null;
        this.localStorage = new HashMap<>();
        this.objectLocation = 0;
        this.breakTargets = new Stack<>();
        this.initialVariables = new HashSet<>();
    }

    @Override
    public void compile() {
        this.visitProgram(this.program);
        this.result = this.writer.toByteArray();
        this.compiled = true;
    }

    /**
     * Compiles the program while checking the ASM visitor calls made by the compiler.
     * This method is for debug purposes only and should not be used in production.
     */
    public void compileDebug() {
        // Set debug visitor
        this.visitor = new CheckClassAdapter(this.writer);

        // Call compile as normal
        this.compile();
    }

    @Override
    public byte[] asByteArray() {
        return this.result;
    }

    /**
     * Returns the location of the variable.
     *
     * A postitive number indicates the local storage slot the variable is in.
     * A negative number indicates that the variable is a field.
     * Zero is a special value and indicates the local class instance ({@code this}).
     *
     * @param variable The variable to get the location for.
     * @return The location of the variable.
     */
    protected int getVariableLocation(final Variable variable) {
        // Defaults to -1, which indicates the variable is a field, as most variables are fields.
        return this.localStorage.getOrDefault(variable, -1);
    }

    /**
     * Sets the location of a variable.
     *
     * A postitive number indicates the local storage slot the variable is in.
     * A negative number indicates that the variable is a field.
     * Zero is a special value and indicates the local class instance ({@code this}).
     *
     * @param variable The variable to set the location for.
     * @param location The location of the variable.
     */
    protected void setVariableLocation(final Variable variable, final int location) {
        this.localStorage.put(variable, location);
    }

    /**
     * Visitor method for when a variable value is needed on the stack.
     * Writes instructions that push the value of the variable to the top of the stack.
     * @param variable The variable to load.
     */
    protected void visitLoadVariable(final Variable variable) {
        final int location = this.getVariableLocation(variable);

        if (location < 0) {
            // Put object reference on stack
            this.methodVisitor.visitVarInsn(Opcodes.ALOAD, this.objectLocation);

            // Put field value on stack
            this.methodVisitor.visitFieldInsn(Opcodes.GETFIELD, this.program.getId(), variable.getId(), ASM.getASMType(variable.getType()).getDescriptor());
        } else {
            // Put value on stack
            switch (variable.getType()) {
                case BOOL:
                case INT:
                case CHAR:
                    this.methodVisitor.visitVarInsn(Opcodes.ILOAD, location);
                    break;
                default:
                    this.unsupported(variable);
            }
        }
    }

    /**
     * Visitor method for when a value must be stored in a variable.
     * Writes instructions that push the value of the variable to the top of the stack.
     * @param variable The variable to write.
     */
    protected void visitStoreVariable(final Variable variable) {
        final int location = this.getVariableLocation(variable);

        if (location < 0) {
            // Put object reference on stack
            this.methodVisitor.visitVarInsn(Opcodes.ALOAD, this.objectLocation);
            this.methodVisitor.visitInsn(Opcodes.SWAP);

            // Put field value on stack
            this.methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, this.program.getId(), variable.getId(), ASM.getASMType(variable.getType()).getDescriptor());
        } else {
            // Store value in local variable
            switch (variable.getType()) {
                case BOOL:
                case INT:
                case CHAR:
                    this.methodVisitor.visitVarInsn(Opcodes.ISTORE, location);
                    break;
                default:
                    this.unsupported(variable);
            }
        }
    }

    @Override
    protected void visitApplication(final Application application) {
        // Load argument values onto stack
        for (Variable arg : application.getArgs()) {
            this.visitLoadVariable(arg);
        }

        // Do operation and put result on stack
        this.visitOperator(application.getOperator());

        // Store result
        this.visitStoreVariable(application.getVariable());
    }

    @Override
    protected void visitAssignment(final Assignment assignment) {
        // Forward call to specialized visitor
        if (assignment instanceof ValueAssignment) {
            this.visitValueAssignment((ValueAssignment) assignment);
        } else if (assignment instanceof VariableAssignment) {
            this.visitVariableAssignment((VariableAssignment) assignment);
        } else {
            this.unsupported(assignment);
        }
    }

    @Override
    protected void visitBlock(final Block block) {
        // Forward call to specialized visitor
        if (block instanceof Conditional) {
            this.visitConditional((Conditional) block);
        } else if (block instanceof Loop) {
            this.visitLoop((Loop) block);
        } else {
            this.unsupported(block);
        }
    }

    @Override
    protected void visitBreak(Break brake) {
        // Check if there is a block to break out of
        if (this.breakTargets.empty()) {
            // If not, "break" out of the program with a void return
            this.methodVisitor.visitInsn(Opcodes.RETURN);
        } else {
            // Jump to the break target of the block
            this.methodVisitor.visitJumpInsn(Opcodes.GOTO, this.breakTargets.peek());
        }
    }

    @Override
    protected void visitCall(Call call) {
        // Put object reference for field on stack
        this.methodVisitor.visitVarInsn(Opcodes.ALOAD, this.objectLocation);

        // Put argument values on stack.
        for (Variable arg : call.getArgs()) {
            this.visitLoadVariable(arg);
        }

        // Execute method and put result on stack
        this.methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.program.getId(), call.getFunction().getId(), ASM.getASMMethodType(call.getFunction().getType(), call.getFunction().getArgs()).getDescriptor(), false);

        // Store result in variable
        this.visitStoreVariable(call.getVariable());
    }

    @Override
    protected void visitConditional(Conditional conditional) {
        // Create labels for jumps
        final Label labelFalse = new Label();
        final Label labelEnd = new Label();

        // Set break target
        this.breakTargets.push(labelEnd);

        // Put condition onto stack
        this.visitLoadVariable(conditional.getCondition());

        // Jump if condition is false
        this.methodVisitor.visitJumpInsn(Opcodes.IFEQ, labelFalse);

        // Add 'true' body
        this.visitOperationSequence(conditional.getBodyTrue());

        // Jump to end
        this.methodVisitor.visitJumpInsn(Opcodes.GOTO, labelEnd);

        // Place 'false' label
        this.methodVisitor.visitLabel(labelFalse);

        // Add 'false' body
        this.visitOperationSequence(conditional.getBodyFalse());

        // Place 'end' label
        this.methodVisitor.visitLabel(labelEnd);

        // Unset break target
        this.breakTargets.pop();
    }

    @Override
    protected void visitFunction(Function function) {
        // Set variable locations
        final Variable[] variables = function.getVariables();

        for (int i = 0; i < variables.length; i++) {
            this.setVariableLocation(variables[i], i + 1);
        }

        // Create new method
        this.methodVisitor = this.visitor.visitMethod(Opcodes.ACC_PUBLIC, function.getId(), ASM.getASMMethodType(function.getType(), function.getArgs()).getDescriptor(), null, null);

        // Start method body
        this.methodVisitor.visitCode();

        // Add method instructions
        this.visitOperationSequence(function.getBody());

        // End method body
        this.methodVisitor.visitMaxs(this.maxStack, this.maxLocal);
        this.methodVisitor.visitEnd();
    }

    @Override
    protected void visitInput(Input input) {
        // Create new scanner
        this.methodVisitor.visitTypeInsn(Opcodes.NEW, "java/util/Scanner");
        this.methodVisitor.visitInsn(Opcodes.DUP);

        // Put reference to System.in on stack
        this.methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");

        // Call constructor with argument
        this.methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/Scanner", "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType("Ljava/io/InputStream;")), false);

        // Print query
        this.methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        this.methodVisitor.visitLdcInsn(input.getQuery());
        this.methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class)), false);

        // Consume scanner and put input value on stack
        switch (input.getDestination().getType()) {
            case BOOL:
                this.methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextBoolean", Type.getMethodDescriptor(Type.BOOLEAN_TYPE), false);
                break;
            case INT:
                this.methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextInt", Type.getMethodDescriptor(Type.INT_TYPE), false);
                break;
            case CHAR:
                // Put regex for a single char on the stack
                this.methodVisitor.visitLdcInsn(".");
                this.methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "next", Type.getMethodDescriptor(Type.getType(String.class)), false);

                // Consume string result and put the first char on the stack
                this.methodVisitor.visitIntInsn(Opcodes.SIPUSH, 0);
                this.methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", Type.getMethodDescriptor(Type.CHAR_TYPE, Type.INT_TYPE), false);
                break;
            default:
                this.unsupported(input);
        }

        // Store input in variable
        this.visitStoreVariable(input.getDestination());
    }

    @Override
    protected void visitLoop(Loop loop) {
        // Create labels for jumps
        final Label labelCond = new Label();
        final Label labelEnd = new Label();

        // Set break target
        this.breakTargets.push(labelEnd);

        // Place condition label
        this.methodVisitor.visitLabel(labelCond);

        // Load condition onto stack
        this.visitLoadVariable(loop.getCondition());

        // Jump to end if condition is false
        this.methodVisitor.visitJumpInsn(Opcodes.IFEQ, labelEnd);

        // Add loop instructions
        this.visitOperationSequence(loop.getBody());

        // Jump back to condition
        this.methodVisitor.visitJumpInsn(Opcodes.GOTO, labelCond);

        // Place end label
        this.methodVisitor.visitLabel(labelEnd);

        // Unset break target
        this.breakTargets.pop();
    }

    @Override
    protected void visitOperation(Operation operation) {
        // Forward call to specialized visitor
        if (operation instanceof Application) {
            this.visitApplication((Application) operation);
        } else if (operation instanceof Assignment) {
            this.visitAssignment((Assignment) operation);
        } else if (operation instanceof Break) {
            this.visitBreak((Break) operation);
        } else if (operation instanceof Block) {
            this.visitBlock((Block) operation);
        } else if (operation instanceof Call) {
            this.visitCall((Call) operation);
        } else if (operation instanceof Input) {
            this.visitInput((Input) operation);
        } else if (operation instanceof Output) {
            this.visitOutput((Output) operation);
        } else if (operation instanceof Return) {
            this.visitReturn((Return) operation);
        } else {
            this.unsupported(operation);
        }
    }

    @Override
    protected void visitOperationSequence(OperationSequence sequence) {
        // Visit all operations in the sequence.
        for (Operation operation : sequence) {
            this.visitOperation(operation);
        }
    }

    @Override
    protected void visitOperator(Operator operator) {
        // Write the correct opcode for the operator
        switch (operator) {
            case ADDITION:
                this.methodVisitor.visitInsn(Opcodes.IADD);
                break;
            case SUBTRACTION:
                this.methodVisitor.visitInsn(Opcodes.ISUB);
                break;
            case MULTIPLICATION:
                this.methodVisitor.visitInsn(Opcodes.IMUL);
                break;
            case DIVISION:
                this.methodVisitor.visitInsn(Opcodes.IDIV);
                break;
            case MODULUS:
                this.methodVisitor.visitInsn(Opcodes.IREM);
                break;
            case AND:
            case LAND:
                this.methodVisitor.visitInsn(Opcodes.IAND);
                break;
            case OR:
            case LOR:
                this.methodVisitor.visitInsn(Opcodes.IOR);
                break;
            case LXOR:
                this.methodVisitor.visitInsn(Opcodes.IXOR);
                break;
            case NOT:
                this.methodVisitor.visitIntInsn(Opcodes.SIPUSH, 1);
                this.methodVisitor.visitInsn(Opcodes.IADD);
                this.methodVisitor.visitIntInsn(Opcodes.SIPUSH, 2);
                this.methodVisitor.visitInsn(Opcodes.IREM);
                break;
            case COMPLTE:
            case COMPGTE:
            case COMPLT:
            case COMPGT:
            case EQUALS:
            case NEQUALS:
                // Create labels for jumps in comparison operators
                final Label labelTrue = new Label();
                final Label labelEnd = new Label();

                // Subtract values for comparison with 0
                this.methodVisitor.visitInsn(Opcodes.ISUB);

                // Compare using appropriate operation
                switch (operator) {
                    case COMPLTE:
                        this.methodVisitor.visitJumpInsn(Opcodes.IFLE, labelTrue);
                        break;
                    case COMPGTE:
                        this.methodVisitor.visitJumpInsn(Opcodes.IFGE, labelTrue);
                        break;
                    case COMPLT:
                        this.methodVisitor.visitJumpInsn(Opcodes.IFLT, labelTrue);
                        break;
                    case COMPGT:
                        this.methodVisitor.visitJumpInsn(Opcodes.IFGT, labelTrue);
                        break;
                    case EQUALS:
                        this.methodVisitor.visitJumpInsn(Opcodes.IFEQ, labelTrue);
                        break;
                    case NEQUALS:
                        this.methodVisitor.visitJumpInsn(Opcodes.IFNE, labelTrue);
                        break;
                    default:
                        this.unsupported(operator);
                }

                // Put 0 on stack for false
                this.methodVisitor.visitInsn(Opcodes.ICONST_0);
                this.methodVisitor.visitJumpInsn(Opcodes.GOTO, labelEnd);

                // Put 1 on stack for true
                this.methodVisitor.visitLabel(labelTrue);
                this.methodVisitor.visitInsn(Opcodes.ICONST_1);

                // Add end label
                this.methodVisitor.visitLabel(labelEnd);
                break;
            default:
                this.unsupported(operator);
        }
    }

    @Override
    protected void visitOutput(Output output) {
        // Put references to System.out on stack
        this.methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        this.methodVisitor.visitInsn(Opcodes.DUP);

        // Put string constant on stack
        this.methodVisitor.visitLdcInsn(output.getDescription());

        // Call print
        this.methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "print", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class)), false);

        // Put value on stack
        this.visitLoadVariable(output.getSource());

        // Call println
        this.methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", Type.getMethodDescriptor(Type.VOID_TYPE, ASM.getASMType(output.getSource().getType())), false);
    }

    @Override
    protected void visitProgram(Program program) {
        // Create new class
        this.visitor.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, program.getId(), null, "java/lang/Object", null);

        // Declare variables as fields
        for (Variable variable : program.getVariables()) {
            this.visitVariable(variable);
        }

        // Declare functions as methods
        for (Function function : program.getFunctions()) {
            this.visitFunction(function);
        }

        // Build constructor
        this.visitProgramConstructor(program);

        // Build main method
        this.visitProgramMain(program);
    }

    protected void visitProgramConstructor(final Program program) {
        // Add class constructor
        this.methodVisitor = this.visitor.visitMethod(Opcodes.ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);

        // Set body of constructor
        this.methodVisitor.visitCode();

        // Call superclass constructor
        this.methodVisitor.visitVarInsn(Opcodes.ALOAD, this.objectLocation);
        this.methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);

        // Set initial values for variables
        for (Variable variable : this.initialVariables) {
            this.visitValueAssignment(variable.createValueAssignment(variable.getInitialValue()));
        }

        // Void return
        this.methodVisitor.visitInsn(Opcodes.RETURN);

        this.methodVisitor.visitMaxs(this.maxStack, this.maxLocal);
        this.methodVisitor.visitEnd();
    }

    protected void visitProgramMain(final Program program) {
        // Store current object location
        final int oldObjectLocation = this.objectLocation;

        // Set object location for main method.
        this.objectLocation = 1;

        // Add main method
        this.methodVisitor = this.visitor.visitMethod(Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, "main", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String[].class)), null, null);

        // Begin main method code
        this.methodVisitor.visitCode();

        // Create new instance
        this.methodVisitor.visitTypeInsn(Opcodes.NEW, program.getId());
        this.methodVisitor.visitInsn(Opcodes.DUP);
        this.methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, program.getId(), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);

        // Store instance in local storage
        this.methodVisitor.visitVarInsn(Opcodes.ASTORE, this.objectLocation);

        // Add method instructions
        this.visitOperationSequence(program.getBody());

        // Add void return
        this.methodVisitor.visitInsn(Opcodes.RETURN);

        // End main method code
        this.methodVisitor.visitMaxs(this.maxStack, this.maxLocal);
        this.methodVisitor.visitEnd();

        // Restore object location
        this.objectLocation = oldObjectLocation;
    }

    @Override
    protected void visitReturn(Return returm) {
        // Load value onto stack
        this.visitLoadVariable(returm.getSource());

        // Return value with correct operation for each type
        switch (returm.getSource().getType()) {
            case BOOL:
            case INT:
            case CHAR:
                this.methodVisitor.visitInsn(Opcodes.IRETURN);
                break;
            default:
                this.unsupported(returm);
        }
    }

    @Override
    protected void visitValueAssignment(ValueAssignment assignment) {
        // Put value on stack for different types
        switch (assignment.getDestination().getType()) {
            case BOOL:
                this.visitIntConst(((boolean) assignment.getValue()) ? 1 : 0); // True = 1, False = 0
                break;
            case INT:
                this.visitIntConst((int) assignment.getValue());
                break;
            case CHAR:
                this.visitIntConst((int) (char) assignment.getValue()); // Integer value of character
                break;
            default:
                this.unsupported(assignment);
        }

        // Store value to variable
        this.visitStoreVariable(assignment.getDestination());
    }

    @Override
    protected void visitVariable(Variable variable) {
        // Add field for the variable
        this.visitor.visitField(Opcodes.ACC_PUBLIC, variable.getId(), ASM.getASMType(variable.getType()).getDescriptor(), null, null);

        // Queue initial value
        if (variable.getInitialValue() != null) {
            this.initialVariables.add(variable);
        }
    }

    @Override
    protected void visitVariableAssignment(VariableAssignment assignment) {
        // Load value of source variable
        this.visitLoadVariable(assignment.getSource());

        // Store value to destination variable
        this.visitStoreVariable(assignment.getDestination());
    }

    /**
     * Visitor for an integer constant.
     * Writes code which puts the constant to the stack.
     * @param value The value to put on the stack.
     */
    protected void visitIntConst(int value) {
        int mult = value / Byte.MAX_VALUE;
        int rest = value % Byte.MAX_VALUE;

        // Put rest on stack.
        this.methodVisitor.visitIntInsn(Opcodes.BIPUSH, rest);

        // Put multiples of maximum value on stack until desired value is reached.
        while (mult > 0) {
            // Put maximum on stack
            this.methodVisitor.visitIntInsn(Opcodes.BIPUSH, Byte.MAX_VALUE);

            // Multiply desired number of times (if mult is too large, take maximum value)
            this.methodVisitor.visitIntInsn(Opcodes.BIPUSH, Math.min(mult, Byte.MAX_VALUE));
            this.methodVisitor.visitInsn(Opcodes.IMUL);

            // Add multiplied value to rest
            this.methodVisitor.visitInsn(Opcodes.IADD);

            // Decrease mult with maximum value to get remaining mults, gets below zero (ends loop) when finished
            mult -= Byte.MAX_VALUE;
        }
    }
}
