package otld.otld.jvm;

import org.objectweb.asm.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;
import otld.otld.intermediate.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class JVMCompiler {
    /** The default base class for programs. */
    private static final String BASE_CLASS = "BaseProgram.class";

    /** The local variable for the program. */
    private static final int LV_PROGRAM = 0;

    /** The class writer for a single class. */
    private ClassWriter writer;

    /** THe class visitor for a single class. */
    private ClassVisitor visitor;

    /** End label for every block. */
    private Map<Block, Label> endLabels;

    /** Stack of nested blocks. */
    private Stack<Block> blocks;

    /** Class name. */
    private String className;

    public JVMCompiler() {
        this(false);
    }

    public JVMCompiler(final boolean checkCalls) {
        this.endLabels = new HashMap<>();
        this.blocks = new Stack<>();

        try {
            final ClassReader reader = new ClassReader(JVMCompiler.class.getResourceAsStream(BASE_CLASS));
            this.writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
            this.visitor = checkCalls ? new CheckClassAdapter(this.writer) : this.writer;
            this.className = reader.getClassName();
        } catch (IOException e) {
            throw new RuntimeException("Base class file not found.");
        }
    }

    /**
     * Generates Java byte code from the given program.
     * @param program The program to compile.
     */
    public byte[] compile(final Program program) {
        this.changeClass(program);

        // Add fields for variables
        for (Variable variable : program.getVariables()) {
            this.addField(variable);
        }

        // Add methods for functions
        for (Function function : program.getFunctions()) {
            this.addMethod(function);
        }

        this.addMainMethod(program);

        return this.writer.toByteArray();
    }

    protected void changeClass(final Program program) {
        // Set class name
        this.className = program.getId();
        this.visitor.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, this.className, null, "Object", null);
    }

    protected void addMainMethod(final Program program) {
        // Add static main method
        final MethodVisitor mainVisitor = this.visitor.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC, "main", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String[].class)), null, null);
        mainVisitor.visitCode();

        // Put new instance in local variable and run main
        mainVisitor.visitTypeInsn(Opcodes.NEW, this.className);
        mainVisitor.visitInsn(Opcodes.DUP);
        mainVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "Object", "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
        mainVisitor.visitInsn(Opcodes.DUP);
        mainVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.className, "main", Type.getMethodDescriptor(Type.VOID_TYPE), false);
        mainVisitor.visitInsn(Opcodes.RETURN);

        mainVisitor.visitMaxs(10, 10); // TODO Make this reasonable
        mainVisitor.visitEnd();

        // Add main instance method
        final MethodVisitor visitor = this.visitor.visitMethod(Opcodes.ACC_PUBLIC, "main", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
        visitor.visitCode();

        for (Operation op : program.getBody()) {
            this.addOperation(visitor, op);
        }

        visitor.visitInsn(Opcodes.RETURN);

        visitor.visitMaxs(10, 10); // TODO Make this reasonable
        visitor.visitEnd();
    }

    /**
     * Adds a field to the current class.
     * @param variable The variable to add a field for.
     */
    protected void addField(final Variable variable) {
        this.visitor.visitField(Opcodes.ACC_PUBLIC, variable.getId(), ASM.getASMType(variable.getType()).getDescriptor(), null, this.constantValue(variable.getInitialValue()));
    }

    /**
     * Adds a method to the current class.
     * @param function The function to add as method.
     */
    protected void addMethod(final Function function) {
        MethodVisitor visitor = this.visitor.visitMethod(Opcodes.ACC_PUBLIC, function.getId(), ASM.getASMMethodType(function.getType(), function.getArgs()).getDescriptor(), null, null);

        // Start method code
        visitor.visitCode();

        for (Operation op : function.getBody()) {
            this.addOperation(visitor, op);
        }

        // End method code
        visitor.visitMaxs(10, 10); // TODO Make this reasonable
        visitor.visitEnd();
    }

    protected void addOperation(final MethodVisitor visitor, final Operation operation) {
        if (operation instanceof Application) {
            this.addApplicationOperation(visitor, (Application) operation);
        } else if (operation instanceof Assignment) {
            this.addAssignmentOperation(visitor, (Assignment) operation);
        } else if (operation instanceof Break) {
            this.addBreakOperation(visitor, (Break) operation);
        } else if (operation instanceof Block) {
            this.addBlockOperation(visitor, (Block) operation);
        } else if (operation instanceof Call) {
            this.addCallOperation(visitor, (Call) operation);
        } else if (operation instanceof Return) {
            this.addReturnOperation(visitor, (Return) operation);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    protected void addApplicationOperation(final MethodVisitor visitor, final Application application) {
        Variable[] args = application.getArgs();

        for (int i = args.length - 1; i >= 0; i--) {
            this.loadVariable(visitor, args[i]);
        }

        // Calculate value using appropriate operation
        switch (application.getOperator()) {
            case ADDITION:
                visitor.visitInsn(Opcodes.IADD);
                break;
            case SUBTRACTION:
                visitor.visitInsn(Opcodes.ISUB);
                break;
            case MULTIPLICATION:
                visitor.visitInsn(Opcodes.IMUL);
                break;
            case DIVISION:
                visitor.visitInsn(Opcodes.IDIV);
                break;
            case MODULUS:
                visitor.visitInsn(Opcodes.IREM);
                break;
            case AND:
            case LAND:
                visitor.visitInsn(Opcodes.IAND);
                break;
            case OR:
            case LOR:
                visitor.visitInsn(Opcodes.IOR);
                break;
            case LXOR:
                visitor.visitInsn(Opcodes.IXOR);
                break;
            case NOT:
                visitor.visitInsn(Opcodes.INEG);
                break;
            case COMPLTE:
            case COMPGTE:
            case COMPLT:
            case COMPGT:
            case EQUALS:
            case NEQUALS:
                // Set labels for jumps
                final Label labelTrue = new Label();
                final Label labelEnd = new Label();

                // Subtract values for comparison with 0
                visitor.visitInsn(Opcodes.ISUB);

                // Compare using appropriate operation
                switch (application.getOperator()) {
                    case COMPLTE:
                        visitor.visitJumpInsn(Opcodes.IFLE, labelTrue);
                        break;
                    case COMPGTE:
                        visitor.visitJumpInsn(Opcodes.IFGE, labelTrue);
                        break;
                    case COMPLT:
                        visitor.visitJumpInsn(Opcodes.IFLT, labelTrue);
                        break;
                    case COMPGT:
                        visitor.visitJumpInsn(Opcodes.IFGT, labelTrue);
                        break;
                    case EQUALS:
                        visitor.visitJumpInsn(Opcodes.IFEQ, labelTrue);
                        break;
                    case NEQUALS:
                        visitor.visitJumpInsn(Opcodes.IFNE, labelTrue);
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }

                // Put 0 on stack for false
                visitor.visitInsn(Opcodes.ICONST_0);
                visitor.visitJumpInsn(Opcodes.GOTO, labelEnd);

                // Put 1 on stack for true
                visitor.visitLabel(labelTrue);
                visitor.visitInsn(Opcodes.ICONST_1);
                visitor.visitLabel(labelEnd);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        this.storeVariable(visitor, application.getVariable());
    }

    protected void addAssignmentOperation(final MethodVisitor visitor, final Assignment assignment) {
        if (assignment instanceof ValueAssignment) {
            final ValueAssignment valueAssignment = (ValueAssignment) assignment;

            // Calculate value
            final int intValue = (int) valueAssignment.getValue();
            final int bytes = intValue / 256;
            final int extra = intValue % 256;

            // Put small number on stack
            visitor.visitIntInsn(Opcodes.BIPUSH, extra);

            // Add a number of full bytes to get the desired value
            if (bytes > 0) {
                visitor.visitIntInsn(Opcodes.BIPUSH, 256);
                visitor.visitIntInsn(Opcodes.BIPUSH, bytes);
                visitor.visitInsn(Opcodes.IMUL);
                visitor.visitInsn(Opcodes.IADD);
            }
        } else if (assignment instanceof VariableAssignment) {
            final VariableAssignment variableAssignment = (VariableAssignment) assignment;

            this.loadVariable(visitor, variableAssignment.getSource());
        }

        this.storeVariable(visitor, assignment.getDestination());
    }

    protected void addBreakOperation(final MethodVisitor visitor, final Break breaks) {
        if (this.blocks.empty()) {
            // No blocks, so we are done, call System.exit()
            visitor.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(System.class), "exit", Type.getMethodDescriptor(Type.VOID_TYPE), false);
        } else {
            // Jump to the end label of the block
            visitor.visitJumpInsn(Opcodes.GOTO, this.endLabels.get(this.blocks.peek()));
        }
    }

    protected void addBlockOperation(final MethodVisitor visitor, final Block block) {
        // Enter block
        this.blocks.push(block);

        // Create end label
        final Label endLabel = new Label();
        this.endLabels.put(block, endLabel);

        // Do regular block operations
        if (block instanceof Conditional) {
            final Conditional conditional = (Conditional) block;

            // Create label for when condition is false
            final Label falseLabel = new Label();

            // Switch on boolean condition
            visitor.visitJumpInsn(Opcodes.IFEQ, falseLabel);

            // Add operations for true body
            for (Operation op : conditional.getBodyFalse()) {
                this.addOperation(visitor, op);
            }

            // Jump to end
            visitor.visitJumpInsn(Opcodes.GOTO, endLabel);

            // Label for false condition
            visitor.visitLabel(falseLabel);

            // Add operations for false body
            for (Operation op : conditional.getBodyFalse()) {
                this.addOperation(visitor, op);
            }
        } else if (block instanceof Loop) {
            final Loop loop = (Loop) block;

            // Create label to jump back to condition
            final Label condLabel = new Label();

            // Add condition label
            visitor.visitLabel(condLabel);

            // Switch on boolean condition
            visitor.visitJumpInsn(Opcodes.IFNE, endLabel);

            // Add operations for body
            for (Operation op : loop.getBody()) {
                this.addOperation(visitor, op);
            }

            // Jump back to condition
            visitor.visitJumpInsn(Opcodes.GOTO, condLabel);
        } else {
            throw new UnsupportedOperationException();
        }

        // Set end label for breaks
        visitor.visitLabel(endLabel);

        // Exit block
        blocks.pop();
    }

    protected void addCallOperation(final MethodVisitor visitor, final Call call) {
        final Variable[] args = call.getArgs();

        // Put argument fields on stack
        for (int i = args.length - 1; i >= 0; i--) {
            visitor.visitVarInsn(Opcodes.ALOAD, LV_PROGRAM);
            visitor.visitFieldInsn(Opcodes.GETFIELD, this.className, args[i].getId(), ASM.getASMType(args[i].getType()).getDescriptor());
        }

        // Put object reference for field on stack
        visitor.visitVarInsn(Opcodes.ALOAD, LV_PROGRAM);
        visitor.visitInsn(Opcodes.SWAP);

        // Execute method and put result on stack
        visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.className, call.getFunction().getId(), ASM.getASMMethodType(call.getFunction().getType(), call.getFunction().getArgs()).getDescriptor(), false);

        this.storeVariable(visitor, call.getVariable());
    }

    protected void addReturnOperation(final MethodVisitor visitor, final Return returns) {
        this.loadVariable(visitor, returns.getSource());

        switch (returns.getSource().getType()) {
            case INT:
            case BOOL:
            case CHAR:
                visitor.visitInsn(Opcodes.IRETURN);
                break;
            case INTARR:
            case BOOLARR:
            case CHARARR:
                visitor.visitInsn(Opcodes.ARETURN);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected void loadVariable(final MethodVisitor visitor, final Variable variable) {
        if (variable.isFunctionArg()) {
            // Load from local variable
            visitor.visitVarInsn(Opcodes.ILOAD, Integer.parseInt(variable.getId()) + 1);
        } else {
            // Put object reference for field on stack
            visitor.visitVarInsn(Opcodes.ALOAD, LV_PROGRAM);

            // Put source field value on stack
            visitor.visitFieldInsn(Opcodes.GETFIELD, this.className, variable.getId(), ASM.getASMType(variable.getType()).getDescriptor());
        }
    }

    protected void storeVariable(final MethodVisitor visitor, final Variable variable) {
        if (variable.isFunctionArg()) {
            // Save to local variable
            visitor.visitVarInsn(Opcodes.ISTORE, Integer.parseInt(variable.getId()) + 1);
        } else {
            // Put object reference for field on stack
            visitor.visitVarInsn(Opcodes.ALOAD, LV_PROGRAM);
            visitor.visitInsn(Opcodes.SWAP);

            // Write result to field
            visitor.visitFieldInsn(Opcodes.PUTFIELD, this.className, variable.getId(), ASM.getASMType(variable.getType()).getDescriptor());
        }
    }

    protected Object constantValue(Object obj) {
        if (obj instanceof Integer) {
            return obj;
        } else if (obj instanceof Boolean) {
            return (boolean) obj ? 1 : 0;
        } else if (obj instanceof Character) {
            return (int) (char) obj;
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
