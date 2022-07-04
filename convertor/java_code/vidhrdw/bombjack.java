/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class bombjack
{
	
	static int background_image;
	
	static struct tilemap *fg_tilemap, *bg_tilemap;
	
	public static WriteHandlerPtr bombjack_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(fg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr bombjack_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (colorram.read(offset)!= data)
		{
			colorram.write(offset,data);
			tilemap_mark_tile_dirty(fg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr bombjack_background_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (background_image != data)
		{
			background_image = data;
			tilemap_mark_all_tiles_dirty(bg_tilemap);
		}
	} };
	
	public static WriteHandlerPtr bombjack_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (flip_screen != (data & 0x01))
		{
			flip_screen_set(data & 0x01);
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	} };
	
	static void get_bg_tile_info(int tile_index)
	{
		UINT8 *tilerom = memory_region(REGION_GFX4);
	
		int offs = (background_image & 0x07) * 0x200 + tile_index;
		int code = (background_image & 0x10) ? tilerom[offs] : 0;
		int attr = tilerom[offs + 0x100];
		int color = attr & 0x0f;
		int flags = (attr & 0x80) ? TILE_FLIPY : 0;
	
		SET_TILE_INFO(1, code, color, flags)
	}
	
	static void get_fg_tile_info(int tile_index)
	{
		int code = videoram.read(tile_index)+ 16 * (colorram.read(tile_index)& 0x10);
		int color = colorram.read(tile_index)& 0x0f;
	
		SET_TILE_INFO(0, code, color, 0)
	}
	
	VIDEO_START( bombjack )
	{
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows, 
			TILEMAP_OPAQUE, 16, 16, 16, 16);
	
		if (bg_tilemap == 0)
			return 1;
	
		fg_tilemap = tilemap_create(get_fg_tile_info, tilemap_scan_rows, 
			TILEMAP_TRANSPARENT, 8, 8, 32, 32);
	
		if (fg_tilemap == 0)
			return 1;
	
		tilemap_set_transparent_pen(fg_tilemap, 0);
	
		return 0;
	}
	
	static void bombjack_draw_sprites( struct mame_bitmap *bitmap )
	{
		int offs;
	
		for (offs = spriteram_size - 4; offs >= 0; offs -= 4)
		{
	
	/*
	 abbbbbbb cdefgggg hhhhhhhh iiiiiiii
	
	 a        use big sprites (32x32 instead of 16x16)
	 bbbbbbb  sprite code
	 c        x flip
	 d        y flip (used only in death sequence?)
	 e        ? (set when big sprites are selected)
	 f        ? (set only when the bonus (B) materializes?)
	 gggg     color
	 hhhhhhhh x position
	 iiiiiiii y position
	*/
			int sx,sy,flipx,flipy;
	
	
			sx = spriteram.read(offs+3);
			if (spriteram.read(offs)& 0x80)
				sy = 225-spriteram.read(offs+2);
			else
				sy = 241-spriteram.read(offs+2);
			flipx = spriteram.read(offs+1)& 0x40;
			flipy =	spriteram.read(offs+1)& 0x80;
			if (flip_screen)
			{
				if (spriteram.read(offs+1)& 0x20)
				{
					sx = 224 - sx;
					sy = 224 - sy;
				}
				else
				{
					sx = 240 - sx;
					sy = 240 - sy;
				}
				flipx = !flipx;
				flipy = !flipy;
			}
	
			drawgfx(bitmap,Machine->gfx[(spriteram.read(offs)& 0x80) ? 3 : 2],
					spriteram.read(offs)& 0x7f,
					spriteram.read(offs+1)& 0x0f,
					flipx,flipy,
					sx,sy,
					&Machine->visible_area,TRANSPARENCY_PEN,0);
		}
	}
	
	VIDEO_UPDATE( bombjack )
	{
		tilemap_draw(bitmap, &Machine->visible_area, bg_tilemap, 0, 0);
		tilemap_draw(bitmap, &Machine->visible_area, fg_tilemap, 0, 0);
		bombjack_draw_sprites(bitmap);
	}
}
