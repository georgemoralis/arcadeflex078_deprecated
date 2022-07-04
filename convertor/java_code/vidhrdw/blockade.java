/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class blockade
{
	
	static struct tilemap *bg_tilemap;
	
	public static WriteHandlerPtr blockade_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	
		if (input_port_3_r(0) & 0x80)
		{
			logerror("blockade_videoram_w: scanline %d\n", cpu_getscanline());
			cpu_spinuntil_int();
		}
	} };
	
	static void get_bg_tile_info(int tile_index)
	{
		int code = videoram.read(tile_index);
	
		SET_TILE_INFO(0, code, 0, 0)
	}
	
	VIDEO_START( blockade )
	{
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows,
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if (bg_tilemap == 0)
			return 1;
	
		return 0;
	}
	
	VIDEO_UPDATE( blockade )
	{
		tilemap_draw(bitmap, &Machine->visible_area, bg_tilemap, 0, 0);
	}
}
