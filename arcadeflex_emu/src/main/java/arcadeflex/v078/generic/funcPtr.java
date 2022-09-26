/**
 * ported to v0.78
 */
package arcadeflex.v078.generic;

public class funcPtr {

    /**
     * common functions
     */
    public abstract static interface ReadHandlerPtr {

        public abstract int handler(int offset);
    }

    public abstract static interface WriteHandlerPtr {

        public abstract void handler(int offset, int data);
    }

    /**
     * memory related
     */
    public static abstract interface opbase_handlerPtr {

        public abstract int handler(int address);
    }

    public static abstract interface setopbase {

        public abstract void handler(int pc);
    }
}
