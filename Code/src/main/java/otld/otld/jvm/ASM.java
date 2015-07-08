package otld.otld.jvm;

import org.objectweb.asm.Type;

/**
 * Class containing utility methods for ASM.
 */
public class ASM {
    /**
     * Returns the ASM method type for the given types.
     * @param type The return type of the method.
     * @param args The argument types of the method.
     * @return The ASM method type.
     */
    public static Type getASMMethodType(final otld.otld.intermediate.Type type, final otld.otld.intermediate.Type... args) {
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
    public static Type getASMType(final otld.otld.intermediate.Type type) {
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
