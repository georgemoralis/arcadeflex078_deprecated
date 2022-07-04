/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class hexa
{
	
	static int charbank;
	static struct tilemap *bg_tilemap;
	
	public static WriteHandlerPtr hexa_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset / 2);
		}
	} };
	
	public static WriteHandlerPtr hexa_d008_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		unsigned char *RAM = memory_region(REGION_CPU1);
		int bankaddress;
	
		/* bit 0 = flipx (or y?) */
		if (flip_screen_x != (data & 0x01))
		{
			flip_screen_x_set(data & 0x01);
			tilemap_mark_all_tiles_dirty(bg_tilemap);
		}
	
		/* bit 1 = flipy (or x?) */
		if (flip_screen_y != (data & 0x02))
		{
			flip_screen_y_set(data & 0x02);
			tilemap_mark_all_tiles_dirty(bg_tilemap);
		}
	
		/* bit 2 - 3 unknown */
	
		/* bit 4 could be the ROM bank selector for 8000-bfff (not sure) */
		bankaddress = 0x10000 + ((data & 0x10) >> 4) * 0x4000;
		cpu_setbank(1, &RAM[bankaddress]);
	
		/* bit 5 = char bank */
		if (charbank != ((data & 0x20) >> 5))
		{
			charbank = (data & 0x20) >> 5;
			tilemap_mark_all_tiles_dirty(bg_tilemap);
		}
	
		/* bit 6 - 7 unknown */
	} };
	
	static void get_bg_tile_info(int tile_index)
	{
		int offs = tile_index * 2;
		int tile = videoram.read(offs + 1)+ ((videoram.read(offs)& 0x07) << 8) + (charbank << 11);
		int color = (videoram.read(offs)& 0xf8) >> 3;
	
		SET_TILE_INFO(0, tile, color, 0)
	}
	
	VIDEO_START( hexa )
	{
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows, 
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if (bg_tilemap == 0)
			return 1;
	
		return 0;
	}
	
	/***************************************************************************
	
	  Draw the game screen in the given mame_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	VIDEO_UPDATE( hexa )
	{
		tilemap_draw(bitmap, &Machine->visible_area, bg_tilemap, 0, 0);
	}
}
