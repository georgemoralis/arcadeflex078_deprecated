/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class pbaction
{
	
	UINT8 *pbaction_videoram2, *pbaction_colorram2;
	
	static int scroll;
	
	static struct tilemap *bg_tilemap, *fg_tilemap;
	
	public static WriteHandlerPtr pbaction_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr pbaction_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (colorram.read(offset)!= data)
		{
			colorram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr pbaction_videoram2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (pbaction_videoram2[offset] != data)
		{
			pbaction_videoram2[offset] = data;
			tilemap_mark_tile_dirty(fg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr pbaction_colorram2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (pbaction_colorram2[offset] != data)
		{
			pbaction_colorram2[offset] = data;
			tilemap_mark_tile_dirty(fg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr pbaction_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		scroll = data - 3;
		if (flip_screen) scroll = -scroll;
		tilemap_set_scrollx(bg_tilemap, 0, scroll);
		tilemap_set_scrollx(fg_tilemap, 0, scroll);
	} };
	
	public static WriteHandlerPtr pbaction_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		flip_screen_set(data & 0x01);
	} };
	
	static void get_bg_tile_info(int tile_index)
	{
		int attr = colorram.read(tile_index);
		int code = videoram.read(tile_index)+ 0x10 * (attr & 0x70);
		int color = attr & 0x0f;
		int flags = (attr & 0x80) ? TILE_FLIPY : 0;
	
		SET_TILE_INFO(1, code, color, flags)
	}
	
	static void get_fg_tile_info(int tile_index)
	{
		int attr = pbaction_colorram2[tile_index];
		int code = pbaction_videoram2[tile_index] + 0x10 * (attr & 0x30);
		int color = attr & 0x0f;
		int flags = ((attr & 0x40) ? TILE_FLIPX : 0) | ((attr & 0x80) ? TILE_FLIPY : 0);
	
		SET_TILE_INFO(0, code, color, flags)
	}
	
	VIDEO_START( pbaction )
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
	
	static void pbaction_draw_sprites( struct mame_bitmap *bitmap )
	{
		int offs;
	
		for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
		{
			int sx,sy,flipx,flipy;
	
			/* if next sprite is double size, skip this one */
			if (offs > 0 && spriteram.read(offs - 4)& 0x80) continue;
	
			sx = spriteram.read(offs+3);
			if (spriteram.read(offs)& 0x80)
				sy = 225-spriteram.read(offs+2);
			else
				sy = 241-spriteram.read(offs+2);
			flipx = spriteram.read(offs+1)& 0x40;
			flipy =	spriteram.read(offs+1)& 0x80;
			if (flip_screen)
			{
				if (spriteram.read(offs)& 0x80)
				{
					sx = 224 - sx;
					sy = 225 - sy;
				}
				else
				{
					sx = 240 - sx;
					sy = 241 - sy;
				}
				flipx = !flipx;
				flipy = !flipy;
			}
	
			drawgfx(bitmap,Machine->gfx[(spriteram.read(offs)& 0x80) ? 3 : 2],	/* normal or double size */
					spriteram.read(offs),
					spriteram.read(offs + 1)& 0x0f,
					flipx,flipy,
					sx + (flip_screen ? scroll : -scroll), sy,
					&Machine->visible_area,TRANSPARENCY_PEN,0);
		}
	}
	
	VIDEO_UPDATE( pbaction )
	{
		tilemap_draw(bitmap, &Machine->visible_area, bg_tilemap, 0, 0);
		pbaction_draw_sprites(bitmap);
		tilemap_draw(bitmap, &Machine->visible_area, fg_tilemap, 0, 0);
	}
}
