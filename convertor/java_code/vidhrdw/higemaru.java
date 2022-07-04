/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class higemaru
{
	
	static struct tilemap *bg_tilemap;
	
	public static WriteHandlerPtr higemaru_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr higemaru_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (colorram.read(offset)!= data)
		{
			colorram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	***************************************************************************/
	PALETTE_INIT( higemaru )
	{
		int i;
		#define TOTAL_COLORS(gfxn) (Machine->gfx[gfxn]->total_colors * Machine->gfx[gfxn]->color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine->drv->gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
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
	
		/* color_prom now points to the beginning of the lookup table */
	
		/* characters use colors 0-15 */
		for (i = 0;i < TOTAL_COLORS(0);i++)
			COLOR(0,i) = *(color_prom++) & 0x0f;
	
		color_prom += 128;	/* the bottom half of the PROM doesn't seem to be used */
	
		/* sprites use colors 16-31 */
		for (i = 0;i < TOTAL_COLORS(1);i++)
			COLOR(1,i) = (*(color_prom++) & 0x0f) + 0x10;
	}
	
	public static WriteHandlerPtr higemaru_c800_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (data & 0x7c) logerror("c800 = %02x\n",data);
	
		/* bits 0 and 1 are coin counters */
		coin_counter_w(0,data & 2);
		coin_counter_w(1,data & 1);
	
		/* bit 7 flips screen */
		if (flip_screen != (data & 0x80))
		{
			flip_screen_set(data & 0x80);
			tilemap_mark_all_tiles_dirty(bg_tilemap);
		}
	} };
	
	static void get_bg_tile_info(int tile_index)
	{
		int code = videoram.read(tile_index)+ ((colorram.read(tile_index)& 0x80) << 1);
		int color = colorram.read(tile_index)& 0x1f;
	
		SET_TILE_INFO(0, code, color, 0)
	}
	
	VIDEO_START( higemaru )
	{
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows, 
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if (bg_tilemap == 0)
			return 1;
	
		return 0;
	}
	
	static void higemaru_draw_sprites( struct mame_bitmap *bitmap )
	{
		int offs;
	
		for (offs = spriteram_size - 16;offs >= 0;offs -= 16)
		{
			int code,col,sx,sy,flipx,flipy;
	
			code = spriteram.read(offs)& 0x7f;
			col = spriteram.read(offs + 4)& 0x0f;
			sx = spriteram.read(offs + 12);
			sy = spriteram.read(offs + 8);
			flipx = spriteram.read(offs + 4)& 0x10;
			flipy = spriteram.read(offs + 4)& 0x20;
			if (flip_screen)
			{
				sx = 240 - sx;
				sy = 240 - sy;
				flipx = !flipx;
				flipy = !flipy;
			}
	
			drawgfx(bitmap,Machine->gfx[1],
					code,
					col,
					flipx,flipy,
					sx,sy,
					&Machine->visible_area,TRANSPARENCY_PEN,15);
	
			/* draw again with wraparound */
			drawgfx(bitmap,Machine->gfx[1],
					code,
					col,
					flipx,flipy,
					sx - 256,sy,
					&Machine->visible_area,TRANSPARENCY_PEN,15);
		}
	}
	
	VIDEO_UPDATE( higemaru )
	{
		tilemap_draw(bitmap, &Machine->visible_area, bg_tilemap, 0, 0);
		higemaru_draw_sprites(bitmap);
	}
}
