/*
 * ported to v0.78
 * ported to v0.67
 */
package arcadeflex.v078.vidhrdw;

//common imports
import static common.FuncPtr.*;
import static common.PtrLib.*;
//mame imports
import static arcadeflex.v078.mame.mame.*;
import static arcadeflex.v078.mame.commonH.*;
import static arcadeflex.v078.mame.drawgfxH.*;
//vidhrdw imports
import static arcadeflex.v078.vidhrdw.generic.*;

public class minivadr {

    /**
     * *****************************************************************
     *
     * Palette Setting.
     *
     ******************************************************************
     */
    public static VhPaletteInitPtr palette_init_minivadr = new VhPaletteInitPtr() {
        public void handler(char[] colortable, UBytePtr color_prom) {
            /*TODO*///            palette_set_color(0, 0x00, 0x00, 0x00);
/*TODO*///            palette_set_color(1, 0xff, 0xff, 0xff);
        }
    };

    /**
     * *****************************************************************
     *
     * Draw Pixel.
     *
     ******************************************************************
     */
    public static WriteHandlerPtr minivadr_videoram_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int i;
            int x, y;
            int color;

            videoram.write(offset, data);

            x = (offset % 32) * 8;
            y = (offset / 32);

            /*TODO*///            if (x >= Machine.visible_area.min_x
/*TODO*///                    && x <= Machine.visible_area.max_x
/*TODO*///                    && y >= Machine.visible_area.min_y
/*TODO*///                    && y <= Machine.visible_area.max_y) {
/*TODO*///                for (i = 0; i < 8; i++) {
/*TODO*///                    color = Machine.pens.read(((data >> i) & 0x01));
/*TODO*///
/*TODO*///                    plot_pixel(tmpbitmap, x + (7 - i), y, color);
/*TODO*///                }
/*TODO*///            }
        }
    };

    public static VhUpdatePtr video_update_minivadr = new VhUpdatePtr() {
        public void handler(mame_bitmap bitmap, rectangle cliprect) {
            if (get_vh_global_attribute_changed() != 0) {
                int offs;

                /* redraw bitmap */
                for (offs = 0; offs < videoram_size[0]; offs++) {
                    minivadr_videoram_w.handler(offs, videoram.read(offs));
                }
            }
            /*TODO*///            copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, Machine.visible_area, TRANSPARENCY_NONE, 0);
        }
    };
}
