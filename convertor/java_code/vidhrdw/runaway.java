/***************************************************************************

	Atari Runaway video emulation

****************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class runaway
{
	
	
	static struct tilemap *tilemap;
	
	UINT8* runaway_video_ram;
	UINT8* runaway_sprite_ram;
	
	static int tile_bank = 0;
	
	
	
	public static WriteHandlerPtr runaway_paletteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int R =
			0x21 * ((~data >> 2) & 1) +
			0x47 * ((~data >> 3) & 1) +
			0x97 * ((~data >> 4) & 1);
	
		int G =
			0x21 * ((~data >> 5) & 1) +
			0x47 * ((~data >> 6) & 1) +
			0x97 * ((~data >> 7) & 1);
	
		int B =
			0x21 * 0 +
			0x47 * ((~data >> 0) & 1) +
			0x97 * ((~data >> 1) & 1);
	
		palette_set_color(offset, R, G, B);
	} };
	
	
	
	public static WriteHandlerPtr runaway_video_ram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (data != runaway_video_ram[offset])
		{
			tilemap_mark_tile_dirty(tilemap, offset);
		}
	
		runaway_video_ram[offset] = data;
	} };
	
	
	
	public static WriteHandlerPtr runaway_tile_bank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 1) != tile_bank)
		{
			tilemap_mark_all_tiles_dirty(tilemap);
		}
	
		tile_bank = data & 1;
	} };
	
	
	static void runaway_get_tile_info(int tile_index)
	{
		UINT8 code = runaway_video_ram[tile_index];
	
		SET_TILE_INFO(0, ((code & 0x3f) << 1) | ((code & 0x40) >> 6) | (tile_bank << 7), 0, (code & 0x80) ? TILE_FLIPY : 0);
	}
	
	
	static void qwak_get_tile_info(int tile_index)
	{
		UINT8 code = runaway_video_ram[tile_index];
	
		SET_TILE_INFO(0, ((code & 0x7f) << 1) | ((code & 0x80) >> 7), 0, 0);
	}
	
	
	
	VIDEO_START( runaway )
	{
		tilemap = tilemap_create(runaway_get_tile_info, tilemap_scan_rows, TILEMAP_OPAQUE, 8, 8, 32, 30);
	
		return tilemap == NULL;
	}
	
	
	VIDEO_START( qwak )
	{
		tilemap = tilemap_create(qwak_get_tile_info, tilemap_scan_rows, TILEMAP_OPAQUE, 8, 8, 32, 30);
	
		return tilemap == NULL;
	}
	
	
	
	VIDEO_UPDATE( runaway )
	{
		int i;
	
		tilemap_draw(bitmap, cliprect, tilemap, 0, 0);
	
		for (i = 0; i < 16; i++)
		{
			unsigned code = runaway_sprite_ram[i] & 0x3f;
	
			int x = runaway_sprite_ram[i + 0x20];
			int y = runaway_sprite_ram[i + 0x10];
	
			int flipx = runaway_sprite_ram[i] & 0x40;
			int flipy = runaway_sprite_ram[i] & 0x80;
	
			code |= (runaway_sprite_ram[i + 0x30] << 2) & 0x1c0;
	
			drawgfx(bitmap, Machine->gfx[1],
				code,
				0,
				flipx, flipy,
				x, 240 - y,
				cliprect, TRANSPARENCY_PEN, 0);
	
			drawgfx(bitmap, Machine->gfx[1],
				code,
				0,
				flipx, flipy,
				x - 256, 240 - y,
				cliprect, TRANSPARENCY_PEN, 0);
		}
	}
	
	
	VIDEO_UPDATE( qwak )
	{
		int i;
	
		tilemap_draw(bitmap, cliprect, tilemap, 0, 0);
	
		for (i = 0; i < 16; i++)
		{
			unsigned code = runaway_sprite_ram[i] & 0x7f;
	
			int x = runaway_sprite_ram[i + 0x20];
			int y = runaway_sprite_ram[i + 0x10];
	
			int flipx = 0;
			int flipy = runaway_sprite_ram[i] & 0x80;
	
			code |= (runaway_sprite_ram[i + 0x30] << 2) & 0x1c0;
	
			drawgfx(bitmap, Machine->gfx[1],
				code,
				0,
				flipx, flipy,
				x, 240 - y,
				cliprect, TRANSPARENCY_PEN, 0);
	
			drawgfx(bitmap, Machine->gfx[1],
				code,
				0,
				flipx, flipy,
				x - 256, 240 - y,
				cliprect, TRANSPARENCY_PEN, 0);
		}
	}
}
