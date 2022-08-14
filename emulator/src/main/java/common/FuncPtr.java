package common;

import static arcadeflex.v078.mame.commonH.*;
import static arcadeflex.v078.mame.drawgfxH.*;
import static common.PtrLib.*;

public class FuncPtr {

    /**
     * common
     */
    public static abstract interface ReadHandlerPtr {

        public abstract int handler(int offset);
    }

    public static abstract interface WriteHandlerPtr {

        public abstract void handler(int offset, int data);
    }

    /**
     * driver
     */
    public static abstract interface RomLoadPtr {

        public abstract void handler();
    }

    public static abstract interface InputPortPtr {

        public abstract void handler();
    }

    public static abstract interface InitDriverPtr {

        public abstract void handler();
    }

    /**
     * vidhrdw
     */
    public static abstract interface VhPaletteInitPtr {

        public abstract void handler(char[] colortable, UBytePtr color_prom);
    }

    public static abstract interface VhStartPtr {

        public abstract int handler();
    }

    public static abstract interface VhUpdatePtr {

        public abstract void handler(mame_bitmap bitmap, rectangle cliprect);
    }
}
