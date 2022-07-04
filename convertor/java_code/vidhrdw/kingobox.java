/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class kingobox
{
	
	UINT8 *kingofb_videoram2;
	UINT8 *kingofb_colorram2;
	UINT8 *kingofb_scroll_y;
	
	
	static int palette_bank;
	
	static struct tilemap *bg_tilemap, *fg_tilemap;
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  King of Boxer has three 256x4 palette PROMs, connected to the RGB output
	  this way:
	
	  bit 3 -- 180 ohm resistor  -- RED/GREEN/BLUE
	        -- 360 ohm resistor  -- RED/GREEN/BLUE
	        -- 750 ohm resistor  -- RED/GREEN/BLUE
	  bit 0 -- 1.5kohm resistor  -- RED/GREEN/BLUE
	
	  The foreground color code directly goes to the RGB output, this way:
	
	  bit 5 --  51 ohm resistor  -- RED
	  bit 4 --  51 ohm resistor  -- GREEN
	  bit 3 --  51 ohm resistor  -- BLUE
	
	***************************************************************************/
	PALETTE_INIT( kingofb )
	{
		int i;
		#define TOTAL_COLORS(gfxn) (Machine->gfx[gfxn]->total_colors * Machine->gfx[gfxn]->color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine->drv->gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < 256;i++)
		{
			int bit0,bit1,bit2,bit3,r,g,b;
	
	
			/* red component */
			bit0 = (color_prom.read(0)>> 0) & 0x01;
			bit1 = (color_prom.read(0)>> 1) & 0x01;
			bit2 = (color_prom.read(0)>> 2) & 0x01;
			bit3 = (color_prom.read(0)>> 3) & 0x01;
			r = 0x10 * bit0 + 0x21 * bit1 + 0x45 * bit2 + 0x89 * bit3;
			/* green component */
			bit0 = (color_prom.read(256)>> 0) & 0x01;
			bit1 = (color_prom.read(256)>> 1) & 0x01;
			bit2 = (color_prom.read(256)>> 2) & 0x01;
			bit3 = (color_prom.read(256)>> 3) & 0x01;
			g = 0x10 * bit0 + 0x21 * bit1 + 0x45 * bit2 + 0x89 * bit3;
			/* blue component */
			bit0 = (color_prom.read(2*256)>> 0) & 0x01;
			bit1 = (color_prom.read(2*256)>> 1) & 0x01;
			bit2 = (color_prom.read(2*256)>> 2) & 0x01;
			bit3 = (color_prom.read(2*256)>> 3) & 0x01;
			b = 0x10 * bit0 + 0x21 * bit1 + 0x45 * bit2 + 0x89 * bit3;
	
			palette_set_color(i,r,g,b);
			color_prom++;
		}
	
	
		/* the foreground chars directly map to primary colors */
		for (i = 0;i < 8;i++)
		{
			/* red component */
			int r = 0xff * ((i >> 2) & 0x01);
			/* green component */
			int g = 0xff * ((i >> 1) & 0x01);
			/* blue component */
			int b = 0xff * ((i >> 0) & 0x01);
			palette_set_color(i+256,r,g,b);
		}
	
		for (i = 0;i < TOTAL_COLORS(0)/2;i++)
		{
			COLOR(0,2*i+0) = 0;	/* transparent */
			COLOR(0,2*i+1) = 256 + i;
		}
	}
	
	
	/* Ring King has one 256x8 PROM instead of two 256x4 */
	PALETTE_INIT( ringking )
	{
		int i;
		#define TOTAL_COLORS(gfxn) (Machine->gfx[gfxn]->total_colors * Machine->gfx[gfxn]->color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine->drv->gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < 256;i++)
		{
			int bit0,bit1,bit2,bit3,r,g,b;
	
	
			/* red component */
			bit0 = (color_prom.read(0)>> 4) & 0x01;
			bit1 = (color_prom.read(0)>> 5) & 0x01;
			bit2 = (color_prom.read(0)>> 6) & 0x01;
			bit3 = (color_prom.read(0)>> 7) & 0x01;
			r = 0x10 * bit0 + 0x21 * bit1 + 0x45 * bit2 + 0x89 * bit3;
			/* green component */
			bit0 = (color_prom.read(0)>> 0) & 0x01;
			bit1 = (color_prom.read(0)>> 1) & 0x01;
			bit2 = (color_prom.read(0)>> 2) & 0x01;
			bit3 = (color_prom.read(0)>> 3) & 0x01;
			g = 0x10 * bit0 + 0x21 * bit1 + 0x45 * bit2 + 0x89 * bit3;
			/* blue component */
			bit0 = (color_prom.read(256)>> 0) & 0x01;
			bit1 = (color_prom.read(256)>> 1) & 0x01;
			bit2 = (color_prom.read(256)>> 2) & 0x01;
			bit3 = (color_prom.read(256)>> 3) & 0x01;
			b = 0x10 * bit0 + 0x21 * bit1 + 0x45 * bit2 + 0x89 * bit3;
	
			palette_set_color(i,r,g,b);
			color_prom++;
		}
	
	
		/* the foreground chars directly map to primary colors */
		for (i = 0;i < 8;i++)
		{
			/* red component */
			int r = 0xff * ((i >> 2) & 0x01);
			/* green component */
			int g = 0xff * ((i >> 1) & 0x01);
			/* blue component */
			int b = 0xff * ((i >> 0) & 0x01);
			palette_set_color(i+256,r,g,b);
		}
	
		for (i = 0;i < TOTAL_COLORS(0)/2;i++)
		{
			COLOR(0,2*i+0) = 0;	/* transparent */
			COLOR(0,2*i+1) = 256 + i;
		}
	}
	
	public static WriteHandlerPtr kingofb_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr kingofb_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (colorram.read(offset)!= data)
		{
			colorram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr kingofb_videoram2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (kingofb_videoram2[offset] != data)
		{
			kingofb_videoram2[offset] = data;
			tilemap_mark_tile_dirty(fg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr kingofb_colorram2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (kingofb_colorram2[offset] != data)
		{
			kingofb_colorram2[offset] = data;
			tilemap_mark_tile_dirty(fg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr kingofb_f800_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		kingofb_nmi_enable = data & 0x20;
	
		if (palette_bank != ((data & 0x18) >> 3))
		{
			palette_bank = (data & 0x18) >> 3;
			tilemap_mark_all_tiles_dirty(bg_tilemap);
		}
	
		if (flip_screen != (data & 0x80))
		{
			flip_screen_set(data & 0x80);
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	} };
	
	static void get_bg_tile_info(int tile_index)
	{
		int attr = colorram.read(tile_index);
		int bank = ((attr & 0x04) >> 2) + 2;
		int code = (tile_index / 16) ? videoram.read(tile_index)+ ((attr & 0x03) << 8) : 0;
		int color = ((attr & 0x70) >> 4) + 8 * palette_bank;
	
		SET_TILE_INFO(bank, code, color, 0)
	}
	
	static void get_fg_tile_info(int tile_index)
	{
		int attr = kingofb_colorram2[tile_index];
		int bank = (attr & 0x02) >> 1;
		int code = kingofb_videoram2[tile_index] + ((attr & 0x01) << 8);
		int color = (attr & 0x38) >> 3;
	
		SET_TILE_INFO(bank, code, color, 0)
	}
	
	VIDEO_START( kingofb )
	{
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_cols_flip_y, 
			TILEMAP_OPAQUE, 16, 16, 16, 16);
	
		if (bg_tilemap == 0)
			return 1;
	
		fg_tilemap = tilemap_create(get_fg_tile_info, tilemap_scan_cols_flip_y, 
			TILEMAP_TRANSPARENT, 8, 8, 32, 32);
	
		if (fg_tilemap == 0)
			return 1;
	
		tilemap_set_transparent_pen(fg_tilemap, 0);
	
		return 0;
	}
	
	static void kingofb_draw_sprites( struct mame_bitmap *bitmap )
	{
		int offs;
	
		for (offs = spriteram_size - 4; offs >= 0; offs -= 4)
		{
			int bank = (spriteram.read(offs + 3)& 0x04) >> 2;
			int code = spriteram.read(offs + 2)+ ((spriteram.read(offs + 3)& 0x03) << 8);
			int color = ((spriteram.read(offs + 3)& 0x70) >> 4) + 8 * palette_bank;
			int flipx = 0;		
			int flipy = spriteram.read(offs + 3)& 0x80;
			int sx = spriteram.read(offs+1);
			int sy = spriteram.read(offs);
	
			if (flip_screen)
			{
				sx = 240 - sx;
				sy = 240 - sy;
				flipx = !flipx;
				flipy = !flipy;
			}
	
			drawgfx(bitmap, Machine->gfx[2 + bank],
				code, color,
				flipx, flipy,
				sx, sy,
				0, TRANSPARENCY_PEN, 0);
		}
	}
	
	VIDEO_UPDATE( kingofb )
	{
		tilemap_set_scrolly(bg_tilemap, 0, -(*kingofb_scroll_y));
		tilemap_draw(bitmap, &Machine->visible_area, bg_tilemap, 0, 0);
		kingofb_draw_sprites(bitmap);
		tilemap_draw(bitmap, &Machine->visible_area, fg_tilemap, 0, 0);
	}
	
	/* Ring King */
	
	static void ringking_get_bg_tile_info(int tile_index)
	{
		int code = (tile_index / 16) ? videoram.read(tile_index): 0;
		int color = ((colorram.read(tile_index)& 0x70) >> 4) + 8 * palette_bank;
	
		SET_TILE_INFO(4, code, color, 0)
	}
	
	VIDEO_START( ringking )
	{
		bg_tilemap = tilemap_create(ringking_get_bg_tile_info, tilemap_scan_cols_flip_y, 
			TILEMAP_OPAQUE, 16, 16, 16, 16);
	
		if (bg_tilemap == 0)
			return 1;
	
		fg_tilemap = tilemap_create(get_fg_tile_info, tilemap_scan_cols_flip_y, 
			TILEMAP_TRANSPARENT, 8, 8, 32, 32);
	
		if (fg_tilemap == 0)
			return 1;
	
		tilemap_set_transparent_pen(fg_tilemap, 0);
	
		return 0;
	}
	
	static void ringking_draw_sprites( struct mame_bitmap *bitmap )
	{
		int offs;
	
		for (offs = 0; offs < spriteram_size; offs += 4)
		{
			int bank = (spriteram.read(offs + 1)& 0x04) >> 2;
			int code = spriteram.read(offs + 3)+ ((spriteram.read(offs + 1)& 0x03) << 8);
			int color = ((spriteram.read(offs + 1)& 0x70) >> 4) + 8 * palette_bank;
			int flipx = 0;
			int flipy = ( spriteram.read(offs + 1)& 0x80 ) ? 0 : 1;
			int sx = spriteram.read(offs+2);
			int sy = spriteram.read(offs);
	
			if (flip_screen)
			{
				sx = 240 - sx;
				sy = 240 - sy;
				flipx = !flipx;
				flipy = !flipy;
			}
	
			drawgfx(bitmap, Machine->gfx[2 + bank],
				code, color,
				flipx, flipy,
				sx, sy,
				0, TRANSPARENCY_PEN, 0);
		}
	}
	
	VIDEO_UPDATE( ringking )
	{
		tilemap_set_scrolly(bg_tilemap, 0, -(*kingofb_scroll_y));
		tilemap_draw(bitmap, &Machine->visible_area, bg_tilemap, 0, 0);
		ringking_draw_sprites(bitmap);
		tilemap_draw(bitmap, &Machine->visible_area, fg_tilemap, 0, 0);
	}
}
