/***************************************************************************

	vidhrdw.c

	Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class tagteam
{
	
	static int palettebank;
	
	static struct tilemap *bg_tilemap;
	
	PALETTE_INIT( tagteam )
	{
		int i;
	
		for (i = 0;i < Machine->drv->total_colors;i++)
		{
			int bit0,bit1,bit2,r,g,b;
	
	
			/* red component */
			bit0 = (*color_prom >> 0) & 0x01;
			bit1 = (*color_prom >> 1) & 0x01;
			bit2 = (*color_prom >> 2) & 0x01;
			r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* green component */
			bit0 = (*color_prom >> 3) & 0x01;
			bit1 = (*color_prom >> 4) & 0x01;
			bit2 = (*color_prom >> 5) & 0x01;
			g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* blue component */
			bit0 = 0;
			bit1 = (*color_prom >> 6) & 0x01;
			bit2 = (*color_prom >> 7) & 0x01;
			b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
			palette_set_color(i,r,g,b);
			color_prom++;
		}
	}
	
	public static WriteHandlerPtr tagteam_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr tagteam_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (colorram.read(offset)!= data)
		{
			colorram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static ReadHandlerPtr tagteam_mirrorvideoram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int x,y;
	
		/* swap x and y coordinates */
		x = offset / 32;
		y = offset % 32;
		offset = 32 * y + x;
	
		return videoram_r(offset);
	} };
	
	public static ReadHandlerPtr tagteam_mirrorcolorram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int x,y;
	
		/* swap x and y coordinates */
		x = offset / 32;
		y = offset % 32;
		offset = 32 * y + x;
	
		return colorram_r(offset);
	} };
	
	public static WriteHandlerPtr tagteam_mirrorvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int x,y;
	
		/* swap x and y coordinates */
		x = offset / 32;
		y = offset % 32;
		offset = 32 * y + x;
	
		tagteam_videoram_w(offset,data);
	} };
	
	public static WriteHandlerPtr tagteam_mirrorcolorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int x,y;
	
		/* swap x and y coordinates */
		x = offset / 32;
		y = offset % 32;
		offset = 32 * y + x;
	
		tagteam_colorram_w(offset,data);
	} };
	
	public static WriteHandlerPtr tagteam_control_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	logerror("%04x: control = %02x\n",activecpu_get_pc(),data);
	
		/* bit 7 is the palette bank */
		palettebank = (data & 0x80) >> 7;
	} };
	
	public static WriteHandlerPtr tagteam_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (flip_screen != (data &0x01))
		{
			flip_screen_set(data & 0x01);
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	} };
	
	static void get_bg_tile_info(int tile_index)
	{
		int code = videoram.read(tile_index)+ 256 * colorram.read(tile_index);
		int color = palettebank * 2; // GUESS
	
		SET_TILE_INFO(0, code, color, 0)
	}
	
	VIDEO_START( tagteam )
	{
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows_flip_x,
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if (bg_tilemap == 0)
			return 1;
	
		return 0;
	}
	
	static void tagteam_draw_sprites( struct mame_bitmap *bitmap )
	{
		int offs;
	
		for (offs = 0; offs < 0x20; offs += 4)
		{
			int spritebank = (videoram.read(offs)& 0x30) << 4;
			int code = videoram.read(offs + 1)+ 256 * spritebank;
			int color = 1 + 2 * palettebank; // GUESS
			int flipx = videoram.read(offs)& 0x04;
			int flipy = videoram.read(offs)& 0x02;
			int sx = 240 - videoram.read(offs + 3);
			int sy = 240 - videoram.read(offs + 2);
	
			if (!(videoram.read(offs)& 0x01)) continue;
	
			if (flip_screen)
			{
				sx = 240 - sx;
				sy = 240 - sy;
				flipx = !flipx;
				flipy = !flipy;
			}
	
			drawgfx(bitmap, Machine->gfx[1],
				code, color,
				flipx, flipy,
				sx, sy,
				&Machine->visible_area,
				TRANSPARENCY_PEN, 0);
	
			/* Wrap around */
	
			code = videoram.read(offs + 0x20)+ 256 * spritebank;
			color = palettebank;
			sy += (flip_screen ? -256 : 256);
	
			drawgfx(bitmap, Machine->gfx[1],
				code, color,
				flipx, flipy,
				sx, sy,
				&Machine->visible_area,
				TRANSPARENCY_PEN, 0);
		}
	}
	
	VIDEO_UPDATE( tagteam )
	{
		tilemap_draw(bitmap, &Machine->visible_area, bg_tilemap, 0, 0);
		tagteam_draw_sprites(bitmap);
	}
}
