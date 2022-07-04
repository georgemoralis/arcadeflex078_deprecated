/*****************************************************************************************

 Speed Attack video hardware emulation

*****************************************************************************************/
/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class speedatk
{
	
	static struct tilemap *tilemap;
	
	/*
	
	Color prom dump(only 0x00-0x10 range has valid data)
	0:---- ---- 0x00 Black
	1:---- -x-- 0x04
	2:---- -xxx 0x07
	3:x-x- -xxx 0xa7
	4:--x- x--- 0x28
	5:xxxx x--- 0xf8
	6:--xx xxxx 0x3f
	7:xxxx xxxx 0xff White
	8:x--- -x-- 0x84
	9:x-x- xx-x 0xad
	a:--x- -x-x 0x25
	b:-xxx xxx- 0x7e
	c:--x- xxxx 0x2f
	d:xx-- ---- 0xc0
	e:--xx -xx- 0x36
	f:xxx- x--- 0xe8
	
	*/
	
	PALETTE_INIT( speedatk )
	{
		int i;
	
		for (i = 0;i < 0x10;i++)
		{
			int bit0,bit1,bit2,r,g,b;
	
			/* red component */
			bit0 = (color_prom.read(i)>> 0) & 0x01;
			bit1 = (color_prom.read(i)>> 1) & 0x01;
			bit2 = (color_prom.read(i)>> 2) & 0x01;
			r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* green component */
			bit0 = (color_prom.read(i)>> 3) & 0x01;
			bit1 = (color_prom.read(i)>> 4) & 0x01;
			bit2 = (color_prom.read(i)>> 5) & 0x01;
			g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* blue component */
			bit0 = 0;
			bit1 = (color_prom.read(i)>> 6) & 0x01;
			bit2 = (color_prom.read(i)>> 7) & 0x01;
			b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
			palette_set_color(i,r,g,b);
		}
	
		color_prom += 0x10;
	
		/* Colortable entry */
		for(i = 0; i < 0x100; i++)
			colortable[i] = color_prom.read(i);	
	}
	
	public static WriteHandlerPtr speedatk_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr speedatk_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (colorram.read(offset)!= data)
		{
			colorram.write(offset,data);
			tilemap_mark_tile_dirty(tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr speedatk_flip_screen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		flip_screen_set(data);
	} };
	
	static void get_tile_info(int tile_index)
	{
		int code, color, region;
	
		code = videoram.read(tile_index)+ ((colorram.read(tile_index)& 0xe0) << 3);
		color = colorram.read(tile_index)& 0x0f;
		region = (colorram.read(tile_index)& 0x10) >> 4;
	
		color += 2;
		if(region)
			color += 0x10;
	
		SET_TILE_INFO(region, code, color, 0)
	}
	
	VIDEO_START( speedatk )
	{
		tilemap = tilemap_create(get_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,34,32);
	
		if (tilemap == 0)
			return 1;
	
		return 0;
	}
	
	VIDEO_UPDATE( speedatk )
	{
		tilemap_draw(bitmap,cliprect,tilemap,0,0);
	}
}
