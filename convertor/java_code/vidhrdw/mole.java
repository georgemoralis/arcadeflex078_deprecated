/***************************************************************************
  vidhrdw/mole.c
  Functions to emulate the video hardware of Mole Attack!.
  Mole Attack's Video hardware is essentially two banks of 512 characters.
  The program uses a single byte to indicate which character goes in each location,
  and uses a control location (0x8400) to select the character sets
***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class mole
{
	
	static int tile_bank;
	static UINT16 *tile_data;
	static struct tilemap *bg_tilemap;
	
	#define TILE_SIZE	8
	#define NUM_ROWS	25
	#define NUM_COLS	40
	#define NUM_TILES	(NUM_ROWS * NUM_COLS)
	
	PALETTE_INIT( moleattack )
	{
		int i;
		int r, g, b;
	
		for (i = 0; i < 8; i++) {
			r = (i & 1) ? 0xff : 0x00;
			g = (i & 4) ? 0xff : 0x00;
			b = (i & 2) ? 0xff : 0x00;
			palette_set_color(i, r, g, b);
		}
	}
	
	static void get_bg_tile_info(int tile_index)
	{
		UINT16 code = tile_data[tile_index];
		SET_TILE_INFO((code & 0x200) ? 1 : 0, code & 0x1ff, 0, 0)
	}
	
	VIDEO_START( moleattack )
	{
		tile_data = (UINT16 *)auto_malloc(NUM_TILES * sizeof(UINT16));
	
		if (tile_data == 0)
			return 1;
	
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows, 
			TILEMAP_OPAQUE, TILE_SIZE, TILE_SIZE, NUM_COLS, NUM_ROWS);
	
		if (bg_tilemap == 0)
			return 1;
	
		return 0;
	}
	
	public static WriteHandlerPtr moleattack_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset < NUM_TILES) {
			if (tile_data[offset] != data) {
				tile_data[offset] = data | (tile_bank << 8);
				tilemap_mark_tile_dirty(bg_tilemap, offset);
			}
		}
	} };
	
	public static WriteHandlerPtr moleattack_tilesetselector_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		tile_bank = data;
		tilemap_mark_all_tiles_dirty(bg_tilemap);
	} };
	
	public static WriteHandlerPtr moleattack_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		flip_screen_set(data);
	} };
	
	VIDEO_UPDATE( moleattack )
	{
		tilemap_draw(bitmap, &Machine->visible_area, bg_tilemap, 0, 0);
	}
}
