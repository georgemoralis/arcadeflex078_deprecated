/* Space Bugger */

/* is the rom mapping ok, are the roms good? it jumps to the 0x800 region and I don't have a rom for there,
the only remaining rom has fixed bits, then the game crashes .. */

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class sbugger
{
	
	/* readme info
	
	Space Bugger
	Game-A-Tron 1981
	
	
	                       U20 U21 U22
	                   U32 U33 U34 U35
	             6MHz
	
	          8085   8156
	                       2114 2114 2114 2114
	                       2114 2114 2114 2114
	
	Main
	
	-------
	
	
	            GFX
	
	            2114 2114 2114 2114
	
	12.440MHz
	
	Graphics PCB
	
	-------
	
	
	
	                         76489  76489
	
	
	Sound PCB
	
	-------
	
	*/
	
	
	
	
	data8_t* sbugger_videoram, *sbugger_videoram_attr;
	
	PALETTE_INIT(sbugger);
	VIDEO_UPDATE(sbugger);
	VIDEO_START(sbugger);
	
	/* memory maps */
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x37ff, MRA_ROM ),
	
		new Memory_ReadAddress( 0xc800, 0xcfff, MRA_RAM ), /* video ram */
	
		new Memory_ReadAddress( 0xe000, 0xe0ff, MRA_RAM ), /* sp is set to e0ff */
	
		new Memory_ReadAddress( 0xf400, 0xffff, MRA_RAM ),
	
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x37ff, MWA_ROM ),
	
		new Memory_WriteAddress( 0xc800, 0xcbff, sbugger_videoram_attr_w, sbugger_videoram_attr ),
		new Memory_WriteAddress( 0xcc00, 0xcfff, sbugger_videoram_w, sbugger_videoram ),
	
		new Memory_WriteAddress( 0xe000, 0xe0ff, MWA_RAM ), /* sp is set to e0ff */
	
		new Memory_WriteAddress( 0xf400, 0xffff, MWA_RAM ),
	
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_ReadPort readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0xe1, 0xe1, input_port_0_r ),
		new IO_ReadPort( 0xe2, 0xe2, input_port_1_r ),
		new IO_ReadPort( 0xe3, 0xe3, input_port_2_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	/* there are some port writes */
	
	/* gfx decode */
	
	static GfxLayout char16layout = new GfxLayout
	(
		8,16,
		RGN_FRAC(1,1),
		1,
		new int[] { 0 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
			8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8
		},
		16*8
	);
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, char16layout,   0, 1  ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	/* input ports */
	
	static InputPortPtr input_ports_sbugger = new InputPortPtr(){ public void handler() { 
		PORT_START(); 	/* 8-bit */
		PORT_DIPNAME( 0x01, 0x01, "E1" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START1 );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 	/* 8-bit */
		PORT_DIPNAME( 0x01, 0x01, "E2" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_COIN1 );
	
		PORT_START(); 	/* 8-bit */
		PORT_DIPNAME( 0x01, 0x01, "E3" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	/* sound stuff */
	
	/* machine driver */
	
	static MACHINE_DRIVER_START( sbugger )
		MDRV_CPU_ADD(8085A, 6000000/2)        /* 3.00 MHz??? */
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_PORTS(readport,0)
	
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		MDRV_GFXDECODE(gfxdecodeinfo)
	
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(64*8, 32*8)
		MDRV_VISIBLE_AREA(0*8, 64*8-1, 0*8, 32*8-1)
		MDRV_PALETTE_LENGTH(512)
	
		MDRV_PALETTE_INIT(sbugger)
		MDRV_VIDEO_START(sbugger)
		MDRV_VIDEO_UPDATE(sbugger)
	
		/* sound hardware */
	MACHINE_DRIVER_END
	
	/* rom loading */
	
	static RomLoadPtr rom_sbugger = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );/* 8085 Code */
	
		ROM_LOAD( "spbugger.u35", 0x0000, 0x0800, CRC(7c2000a1);SHA1(01a60745ea8e9a70de37d1a785fad1d17eafc812) ) // seems to map at 0
		ROM_LOAD( "spbugger.u22", 0x0800, 0x0800, BAD_DUMP CRC(66e00c53);SHA1(49ca567a98978308306cdb8455c61c022668693b) ) // FIXED BITS (xxxx1111)  it jumps here .... bad rom?
		ROM_LOAD( "spbugger.u34", 0x1000, 0x0800, CRC(db357dde);SHA1(363392b971f48e9d99f4167aa17f0c885b0865ee) ) // seems to map at 1000
		ROM_LOAD( "spbugger.u21", 0x1800, 0x0800, CRC(618a5b2a);SHA1(aa7a40b1944f09c396f675d7dd3a8c3c35bf01f1) ) // seems to map at 1800
		ROM_LOAD( "spbugger.u20", 0x2000, 0x0800, CRC(8957563c);SHA1(b33a75fcf375d2a1a766105f87dd8e4d42db3d76) ) // seems to map at 2000
		ROM_LOAD( "spbugger.u33", 0x2800, 0x0800, CRC(f6cb1399);SHA1(53cb67e29a238c5ac20c6be9423d850e004212c1) ) // seems to map at 2800
		ROM_LOAD( "spbugger.u32", 0x3000, 0x0800, CRC(f49af2b3);SHA1(1519ee4786b78546767827d3a9508e7ddb646765) ) // seems to map at 3000
	
		ROM_REGION( 0x1000, REGION_GFX1, ROMREGION_DISPOSE );/* GFX */
		ROM_LOAD( "spbugger.gfx", 0x0000, 0x1000, CRC(d3f345b5);SHA1(a5082ffc3043352e9b731af95770bdd62fb928bf) )
	ROM_END(); }}; 
	
	/* game drivers */
	
	public static GameDriver driver_sbugger	   = new GameDriver("1981"	,"sbugger"	,"sbugger.java"	,rom_sbugger,null	,machine_driver_sbugger	,input_ports_sbugger	,null	,ROT270	,	"Game-A-Tron", "Space Bugger", GAME_NO_SOUND | GAME_NOT_WORKING )
	
}
