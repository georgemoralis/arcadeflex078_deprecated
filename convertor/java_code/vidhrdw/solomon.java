/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class solomon
{
	
	UINT8 *solomon_videoram2;
	UINT8 *solomon_colorram2;
	
	static struct tilemap *bg_tilemap, *fg_tilemap;
	
	public static WriteHandlerPtr solomon_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(fg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr solomon_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (colorram.read(offset)!= data)
		{
			colorram.write(offset,data);
			tilemap_mark_tile_dirty(fg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr solomon_videoram2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (solomon_videoram2[offset] != data)
		{
			solomon_videoram2[offset] = data;
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr solomon_colorram2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (solomon_colorram2[offset] != data)
		{
			solomon_colorram2[offset] = data;
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr solomon_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (flip_screen != (data & 0x01))
		{
			flip_screen_set(data & 0x01);
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	} };
	
	static void get_bg_tile_info(int tile_index)
	{
		int attr = solomon_colorram2[tile_index];
		int code = solomon_videoram2[tile_index] + 256 * (attr & 0x07);
		int color = ((attr & 0x70) >> 4);
		int flags = ((attr & 0x80) ? TILE_FLIPX : 0) | ((attr & 0x08) ? TILE_FLIPY : 0);
	
		SET_TILE_INFO(1, code, color, flags)
	}
	
	static void get_fg_tile_info(int tile_index)
	{
		int attr = colorram.read(tile_index);
		int code = videoram.read(tile_index)+ 256 * (attr & 0x07);
		int color = (attr & 0x70) >> 4;
	
		SET_TILE_INFO(0, code, color, 0)
	}
	
	VIDEO_START( solomon )
	{
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows, 
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if (bg_tilemap == 0)
			return 1;
	
		fg_tilemap = tilemap_create(get_fg_tile_info, tilemap_scan_rows, 
			TILEMAP_TRANSPARENT, 8, 8, 32, 32);
	
		if (fg_tilemap == 0)
			return 1;
	
		tilemap_set_transparent_pen(fg_tilemap, 0);
	
		return 0;
	}
	
	static void solomon_draw_sprites( struct mame_bitmap *bitmap )
	{
		int offs;
	
		for (offs = spriteram_size - 4; offs >= 0; offs -= 4)
		{
			int code = spriteram.read(offs)+ 16 * (spriteram.read(offs + 1)& 0x10);
			int color = (spriteram.read(offs + 1)& 0x0e) >> 1;
			int flipx = spriteram.read(offs + 1)& 0x40;
			int flipy =	spriteram.read(offs + 1)& 0x80;
			int sx = spriteram.read(offs + 3);
			int sy = 241 - spriteram.read(offs + 2);
	
			if (flip_screen)
			{
				sx = 240 - sx;
				sy = 242 - sy;
				flipx = !flipx;
				flipy = !flipy;
			}
	
			drawgfx(bitmap, Machine->gfx[2],
				code, color,
				flipx, flipy,
				sx, sy,
				&Machine->visible_area,
				TRANSPARENCY_PEN, 0);
		}
	}
	
	VIDEO_UPDATE( solomon )
	{
		tilemap_draw(bitmap, &Machine->visible_area, bg_tilemap, 0, 0);
		tilemap_draw(bitmap, &Machine->visible_area, fg_tilemap, 0, 0);
		solomon_draw_sprites(bitmap);
	}
}
