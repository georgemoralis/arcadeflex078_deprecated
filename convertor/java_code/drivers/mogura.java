/* Mogura Desse */

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class mogura
{
	
	data8_t *mogura_tileram;
	data8_t* mogura_gfxram;
	static struct tilemap *mogura_tilemap;
	
	PALETTE_INIT( mogura )
	{
		int i,j;
		#define COLOR(gfxn,offs) (colortable[Machine->drv->gfxdecodeinfo[gfxn].color_codes_start + offs])
	
		j = 0;
		for (i = 0;i < 0x20;i++)
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
	
			palette_set_color(j,r,g,b);
			j+=4;
			if (j>31) j-=31;
		}
	}
	
	
	static void get_mogura_tile_info(int tile_index)
	{
		int code = mogura_tileram[tile_index];
		int attr = mogura_tileram[tile_index+0x800];
	
		SET_TILE_INFO(
				0,
				code,
				(attr>>1)&7,
				0)
	}
	
	
	VIDEO_START( mogura )
	{
		mogura_tilemap = tilemap_create(get_mogura_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,64, 32);
		return 0;
	}
	
	VIDEO_UPDATE( mogura )
	{
		/* tilemap layout is a bit strange ... */
		struct rectangle clip;
		clip.min_x = Machine->visible_area.min_x;
		clip.max_x = 256-1;
		clip.min_y = Machine->visible_area.min_y;
		clip.max_y = Machine->visible_area.max_y;
		tilemap_set_scrollx(mogura_tilemap,0, 256);
		tilemap_draw(bitmap,&clip,mogura_tilemap,0,0);
	
		clip.min_x = 256;
		clip.max_x = 512-1;
		clip.min_y = Machine->visible_area.min_y;
		clip.max_y = Machine->visible_area.max_y;
		tilemap_set_scrollx(mogura_tilemap,0, -128);
		tilemap_draw(bitmap,&clip,mogura_tilemap,0,0);
	
	}
	
	public static WriteHandlerPtr mogura_tileram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		mogura_tileram[offset] = data;
		tilemap_mark_tile_dirty(mogura_tilemap,offset&0x7ff);
	} };
	
	
	public static IO_ReadPort readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x08, 0x08, input_port_0_r ),
		new IO_ReadPort( 0x0c, 0x0c, input_port_1_r ),
		new IO_ReadPort( 0x0d, 0x0d, input_port_2_r ),
		new IO_ReadPort( 0x0e, 0x0e, input_port_3_r ),
		new IO_ReadPort( 0x0f, 0x0f, input_port_4_r ),
		new IO_ReadPort( 0x10, 0x10, input_port_5_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	
	static WRITE_HANDLER(dac_w)
	{
		DAC_0_data_w(0, data & 0xf0 );	/* left */
		DAC_1_data_w(0, (data & 0x0f)<<4 );	/* right */
	}
	
	
	public static IO_WritePort writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0x00, MWA_NOP ), // ??
		new IO_WritePort( 0x14, 0x14, dac_w ),	/* 4 bit DAC x 2. MSB = left, LSB = right */
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	
	WRITE_HANDLER ( mogura_gfxram_w )
	{
		mogura_gfxram[offset] = data ;
	
		decodechar(Machine->gfx[0], offset/16, mogura_gfxram, Machine->drv->gfxdecodeinfo[0].gfxlayout);
	
		tilemap_mark_all_tiles_dirty(mogura_tilemap);
	}
	
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0xc000, 0xdfff, MRA_RAM ), // main ram
		new Memory_ReadAddress( 0xe000, 0xefff, MRA_RAM ), // ram based characters
		new Memory_ReadAddress( 0xf000, 0xffff, MRA_RAM ), // tilemap
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0xc000, 0xdfff, MWA_RAM ),
		new Memory_WriteAddress( 0xe000, 0xefff, mogura_gfxram_w, mogura_gfxram ),
		new Memory_WriteAddress( 0xf000, 0xffff, mogura_tileram_w, mogura_tileram ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	static InputPortPtr input_ports_mogura = new InputPortPtr(){ public void handler() { 
		PORT_START(); 		/* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN4 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_SERVICE2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_SERVICE3 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_SERVICE4 );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1);
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1);
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1);
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START1 );
	
		PORT_START();       /* IN2 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2);
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
		PORT_START();       /* IN3 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3);
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3);
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER3);
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START3 );
	
		PORT_START();       /* IN4 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER4 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4);
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4);
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER4);
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START4 );
	
		PORT_START(); 
		PORT_BITX( 0x01, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
		PORT_BIT ( 0xfe, IP_ACTIVE_LOW, IPT_UNUSED );
	INPUT_PORTS_END(); }}; 
	
	
	
	static GfxLayout tiles8x8_layout = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,1),
		2,
		new int[] { 0, 1 },
		new int[] { 0, 2, 4, 6, 8, 10, 12, 14 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		16*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, tiles8x8_layout, 0, 8 ),
		new GfxDecodeInfo( -1 )
	};
	
	static DACinterface dac_interface = new DACinterface
	(
		2,
		new int[] { MIXER(50, MIXER_PAN_LEFT), MIXER(50, MIXER_PAN_RIGHT) }
	);
	
	static MACHINE_DRIVER_START( mogura )
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80,3000000)		 /* 3 MHz */
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_PORTS(readport,writeport)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_FRAMES_PER_SECOND(60) // ?
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		MDRV_GFXDECODE(gfxdecodeinfo)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(512, 512)
		MDRV_VISIBLE_AREA(0, 320-1, 0, 256-1)
		MDRV_PALETTE_LENGTH(32)
	
		MDRV_PALETTE_INIT(mogura)
		MDRV_VIDEO_START(mogura)
		MDRV_VIDEO_UPDATE(mogura)
	
		/* sound hardware */
		MDRV_SOUND_ATTRIBUTES(SOUND_SUPPORTS_STEREO)
		MDRV_SOUND_ADD(DAC, dac_interface)
	MACHINE_DRIVER_END
	
	static RomLoadPtr rom_mogura = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );
		ROM_LOAD( "gx141.5n", 0x00000, 0x08000, CRC(98e6120d),SHA1(45cdb2d78224a7c44fff8cd3487f33c57669a06c)  )
	
		ROM_REGION( 0x1000, REGION_GFX1, 0 );
		/* allocate ram here for ram based characters */
	
		ROM_REGION( 0x20, REGION_PROMS, 0 );
		ROM_LOAD( "gx141.7j", 0x00, 0x20,  CRC(b21c5d5f),SHA1(6913c840dd69a7d4687f4c4cbe3ff12300f62bc2) )
	ROM_END(); }}; 
	
	public static GameDriver driver_mogura	   = new GameDriver("1991"	,"mogura"	,"mogura.java"	,rom_mogura,null	,machine_driver_mogura	,input_ports_mogura	,null	,ROT0	,	"Konami", "Mogura Desse" )
}
