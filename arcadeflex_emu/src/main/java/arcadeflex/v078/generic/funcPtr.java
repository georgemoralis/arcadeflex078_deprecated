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

    public static abstract interface InterruptHandlerPtr {

        public abstract void handler();
    }

    /**
     * memory related
     */
    public static abstract interface OpbaseHandlerPtr {

        public abstract int handler(int address);
    }

    public static abstract interface SetOpbaseHandlerPtr {

        public abstract void handler(int pc);
    }

    /**
     * cpu interface related
     */
    public static abstract interface BurnHandlerPtr {

        public abstract void handler(int cycles);
    }

    public static abstract interface IrqCallbackHandlerPtr {

        public abstract int handler(int irqline);
    }
}
