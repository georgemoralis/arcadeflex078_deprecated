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
}
