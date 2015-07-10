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
        return Type.getType(typeAsClass(type));
    }

    /**
     * Returns the equivalent Java class for the given intermediate type.
     * @param type The intermediate type to get the class for.
     * @return The Java class for the given intermediate type.
     */
    protected static Class typeAsClass(final otld.otld.intermediate.Type type) {
        switch (type) {
            case INT:
                return int.class;
            case BOOL:
                return boolean.class;
            case CHAR:
                return char.class;
            case BOOLARR:
                return boolean[].class;
            case INTARR:
                return int[].class;
            case CHARARR:
                return char[].class;
            case ANY:
            default:
                // Fall back to integer primitive. Might go wrong, but this should not happen.
                return int.class;
        }
    }
}
