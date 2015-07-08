package otld.otld.jvm;

import org.objectweb.asm.*;
import org.objectweb.asm.Type;
import otld.otld.intermediate.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.Arrays;

public class JVMBackend {
    /** The class writer for a single class. */
    private ClassWriter writer;

    /** The default base class for programs. */
    private static final String BASE_CLASS = "Program.class";

    /** The local variable for the program. */
    private static final int LV_PROGRAM = 0;

    public JVMBackend() {
        try {
            this.writer = new ClassWriter(new ClassReader(JVMBackend.class.getResourceAsStream(BASE_CLASS)), ClassWriter.COMPUTE_FRAMES);
        } catch (IOException e) {
            throw new RuntimeException("Base class file not found.");
        }
    }

    /**
     * Generates a Java class file from the given program.
     * @param program The program.
     */
    public void generateProgram(final Program program) {
        for (Variable variable : program.getVariables()) {
            this.addField(variable);
        }

        for (Function function : program.getFunctions()) {
            this.addMethod(function);
        }
    }

    /**
     * Adds a field to the current class.
     * @param variable The variable to add a field for.
     */
    protected void addField(final Variable variable) {
        this.writer.visitField(Opcodes.ACC_PUBLIC, variable.getId(), getASMType(variable.getType()).getDescriptor(), null, null);
    }

    /**
     * Adds a method to the current class.
     * @param function The function to add as method.
     */
    protected void addMethod(final Function function) {
        MethodVisitor visitor = this.writer.visitMethod(Opcodes.ACC_PUBLIC, function.getId(), getASMMethodType(function.getType(), function.getArgs()).getDescriptor(), null, null);

        for (Operation op : function.getBody()) {
            this.addOperation(visitor, op);
        }
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
            throw new NotImplementedException();
        }
    }

    protected void addApplicationOperation(final MethodVisitor visitor, final Application application) {
        throw new NotImplementedException();
    }

    protected void addAssignmentOperation(final MethodVisitor visitor, final Assignment assignment) {
        if (assignment instanceof ValueAssignment) {
            final ValueAssignment valueAssignment = (ValueAssignment) assignment;

            // Put constant on stack
            visitor.visitLdcInsn(valueAssignment.getValue());
        } else if (assignment instanceof VariableAssignment) {
            final VariableAssignment variableAssignment = (VariableAssignment) assignment;

            // Put object reference for field on stack
            visitor.visitVarInsn(Opcodes.ALOAD, LV_PROGRAM);

            // Put source field value on stack
            visitor.visitFieldInsn(Opcodes.GETFIELD, "Program", variableAssignment.getSource().getId(), getASMType(variableAssignment.getSource().getType()).getDescriptor());
        }

        // Put object reference for field on stack
        visitor.visitVarInsn(Opcodes.ALOAD, LV_PROGRAM);

        // Write value to field
        visitor.visitFieldInsn(Opcodes.PUTFIELD, "Program", assignment.getDestination().getId(), getASMType(assignment.getDestination().getType()).getDescriptor());
    }

    protected void addBreakOperation(final MethodVisitor visitor, final Break breaks) {
        throw new NotImplementedException();
    }

    protected void addBlockOperation(final MethodVisitor visitor, final Block block) {
        throw new NotImplementedException();
    }

    protected void addCallOperation(final MethodVisitor visitor, final Call call) {
        // Put object reference for field on stack
        visitor.visitVarInsn(Opcodes.ALOAD, LV_PROGRAM);

        // Execute method and put result on stack.
        visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "Program", call.getFunction().getId(), getASMMethodType(call.getFunction().getType()).getDescriptor(), false);

        // Put object reference for field on stack
        visitor.visitVarInsn(Opcodes.ALOAD, LV_PROGRAM);

        // Write result to field
        visitor.visitFieldInsn(Opcodes.PUTFIELD, "Program", call.getVariable().getId(), getASMType(call.getVariable().getType()).getDescriptor());
    }

    protected void addReturnOperation(final MethodVisitor visitor, final Return returns) {
        throw new NotImplementedException();
    }

    /**
     * Returns the ASM method type for the given types.
     * @param type The return type of the method.
     * @param args The argument types of the method.
     * @return The ASM method type.
     */
    private static Type getASMMethodType(final otld.otld.intermediate.Type type, final otld.otld.intermediate.Type ... args) {
        final Type[] asmTypes = new Type[args.length];

        for (int i = 0; i < args.length; i++) {
            asmTypes[i] = getASMType(args[i]);
        }

        return Type.getMethodType(getASMType(type), asmTypes);
    }

    /**
     * Returns the ASM type for the given type.
     * @param type The type (of a variable).
     * @return The ASM type.
     */
    private static Type getASMType(final otld.otld.intermediate.Type type) {
        switch (type) {
            case INT:
                return Type.INT_TYPE;
            case BOOL:
                return Type.BOOLEAN_TYPE;
            case CHAR:
                return Type.CHAR_TYPE;
            case BOOLARR:
                return Type.getType(boolean[].class);
            case INTARR:
                return Type.getType(int[].class);
            case CHARARR:
                return Type.getType(char[].class);
            case ANY:
            default:
                return Type.getType(Object.class);
        }
    }
}
