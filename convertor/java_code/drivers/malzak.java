/*

  Malzak

  Driver by Reip, Barry Rodewald
  SAA 5050 display code "borrowed" from the Philips 2000T/2000M driver in MESS

  Basic memory map

	0x0000 - 0x0fff | S2650 code [malzak.5, malzak.4, malzak.2 (data only)]
	0x1000 - 0x13ff | Work RAM
	0x1400 - 0x14ff | S2636 #1 video
	0x1500 - 0x15ff | S2636 #2 video
	0x1600 - 0x16ff | Playfield gfx
	0x1700 - 0x17ff | Work RAM - contains hiscore table, coin count
	0x1800 - 0x1fff | SAA 5050 video RAM

  TODO - I/O ports (0x00 for sprite->background collisions)
         sound (2x SN76477)
		 playfield graphics may be banked, tiles above 0x1f are incorrect
		 sprite->sprite collision aren't quite perfect
		   (you can often fly through flying missiles)

*/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class malzak
{
	
	#define SAA5050_VBLANK 2500
	
	
	
	extern unsigned char* saa5050_vidram;  /* Video RAM for SAA 5050 */
	extern unsigned char* s2636_1_ram;  /* Video RAM for S2636 #1 */
	extern unsigned char* s2636_2_ram;  /* Video RAM for S2636 #2 */
	extern unsigned char s2636_1_dirty[4];
	extern unsigned char s2636_2_dirty[4];
	
	// in vidhrdw/malzak.c
	VIDEO_START( malzak );
	VIDEO_UPDATE( malzak );
	
	public static ReadHandlerPtr malzak_s2636_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return s2636_1_ram[offset];
	} };
	
	public static ReadHandlerPtr malzak_s2636_2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return s2636_2_ram[offset];
	} };
	
	public static WriteHandlerPtr malzak_s2636_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		s2636_w(s2636_1_ram,offset,data,s2636_1_dirty);
	} };
	
	public static WriteHandlerPtr malzak_s2636_2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		s2636_w(s2636_2_ram,offset,data,s2636_2_dirty);
	} };
	
	
	public static ReadHandlerPtr saa5050_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return saa5050_vidram[offset];
	} };
	
	public static WriteHandlerPtr saa5050_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		saa5050_vidram[offset] = data;
	} };
	
	public static ReadHandlerPtr fake_VRLE_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (s2636_1_ram[0xcb] & 0x3f) + (cpu_getvblank()*0x40);
	} };
	
	public static ReadHandlerPtr ram_mirror_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return cpu_readmem16(0x1000+offset);
	} };
	
	public static WriteHandlerPtr ram_mirror_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		cpu_writemem16(0x1000+offset,data);
	} };
	
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x0fff, MRA_ROM ),
		new Memory_ReadAddress( 0x1000, 0x10ff, MRA_RAM ),
		new Memory_ReadAddress( 0x1100, 0x11ff, MRA_RAM ),
		new Memory_ReadAddress( 0x1200, 0x12ff, MRA_RAM ),
		new Memory_ReadAddress( 0x1300, 0x13ff, MRA_RAM ),
		new Memory_ReadAddress( 0x14cb, 0x14cb, fake_VRLE_r ),
		new Memory_ReadAddress( 0x1400, 0x14ff, malzak_s2636_1_r ),
		new Memory_ReadAddress( 0x1500, 0x15ff, malzak_s2636_2_r ),
		new Memory_ReadAddress( 0x1600, 0x16ff, MRA_RAM ),
		new Memory_ReadAddress( 0x1700, 0x17ff, MRA_RAM ),
		new Memory_ReadAddress( 0x1800, 0x1fff, saa5050_r ),  // SAA 5050 video RAM
		new Memory_ReadAddress( 0x2000, 0x2fff, MRA_ROM ),
		new Memory_ReadAddress( 0x3000, 0x3fff, ram_mirror_r ),
		new Memory_ReadAddress( 0x4000, 0x4fff, MRA_ROM ),
		new Memory_ReadAddress( 0x5000, 0x5fff, ram_mirror_r ),
	
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x0fff, MWA_ROM ),
		new Memory_WriteAddress( 0x1000, 0x10ff, MWA_RAM ),
		new Memory_WriteAddress( 0x1100, 0x11ff, MWA_RAM ),
		new Memory_WriteAddress( 0x1200, 0x12ff, MWA_RAM ),
		new Memory_WriteAddress( 0x1300, 0x13ff, MWA_RAM ),
		new Memory_WriteAddress( 0x1400, 0x14ff, malzak_s2636_1_w ), // S2636 offset $CB bit 40 tested as collision ?
		new Memory_WriteAddress( 0x1500, 0x15ff, malzak_s2636_2_w ),
		new Memory_WriteAddress( 0x1600, 0x16ff, playfield_w ),
		new Memory_WriteAddress( 0x1600, 0x16ff, MWA_RAM ),
		new Memory_WriteAddress( 0x1700, 0x17ff, MWA_RAM ),
		new Memory_WriteAddress( 0x1800, 0x1fff, saa5050_w ),  // SAA 5050 video RAM
		new Memory_WriteAddress( 0x2000, 0x2fff, MWA_ROM ),
		new Memory_WriteAddress( 0x3000, 0x3fff, ram_mirror_w ),
		new Memory_WriteAddress( 0x4000, 0x4fff, MWA_ROM ),
		new Memory_WriteAddress( 0x5000, 0x5dff, ram_mirror_w ),
	
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static ReadHandlerPtr s2650_data_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		usrintf_showmessage("S2650 data port read");
		return 0xff;
	} };
	
	public static WriteHandlerPtr port40_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	//	usrintf_showmessage("S2650 [0x%04x]: port 0x40 write: 0x%02x",cpunum_get_pc_byte(0),data);
	//	if(data & 0x01)
	//		irqenable = 1;
	//	else
	//		irqenable = 0;
	} };
	
	public static WriteHandlerPtr port60_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		temp_x = data;
	} };
	
	public static WriteHandlerPtr portc0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		temp_y = data;
	} };
	
	public static ReadHandlerPtr collision_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		// High 4 bits seem to refer to the row affected.
		static int counter;
	
		if(++counter > 15)
			counter = 0;
		return 0xd0 + counter;
	} };
	
	public static IO_ReadPort readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x00, 0x00, collision_r ), // returns where a collision can occur.
	    new IO_ReadPort( 0x80, 0x80, input_port_0_r ),  //controls
		new IO_ReadPort( S2650_DATA_PORT, S2650_DATA_PORT, s2650_data_r ),  // read upon death
	    new IO_ReadPort( S2650_SENSE_PORT, S2650_SENSE_PORT, input_port_3_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x40, 0x40, port40_w ),  // possibly sound codes for dual SN76477s
		new IO_WritePort( 0x60, 0x60, port60_w ),  // possibly playfield scroll X offset
		new IO_WritePort( 0xa0, 0xa0, MWA_NOP ),  // echoes I/O port read from port 0x80
		new IO_WritePort( 0xc0, 0xc0, portc0_w ),  // possibly playfield scroll Y offset
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	static InputPortPtr input_ports_malzak = new InputPortPtr(){ public void handler() { 
	
		/* Malzak has a stick (not sure if it's 4-way or 8-way),
		   and only one button (firing and bomb dropping on the same button) */
	
		PORT_START();  /* I/O port 0x80 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT  );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    );
	
	    PORT_START(); 
	//	PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Lives") );
	//	PORT_DIPSETTING(    0x00, "3" );
	//	PORT_DIPSETTING(    0x01, "4" );
	//    PORT_DIPNAME( 0x02, 0x00, "Lightning Speed" );
	//	PORT_DIPSETTING(    0x00, "Slow" );
	//	PORT_DIPSETTING(    0x02, "Fast" );
	//	PORT_DIPNAME( 0x1C, 0x04, DEF_STR( "Coinage") );
	//	PORT_DIPSETTING(	0x00, DEF_STR( "2C_1C") );
	//	PORT_DIPSETTING(	0x04, DEF_STR( "1C_1C") );
	//	PORT_DIPSETTING(	0x08, DEF_STR( "1C_2C") );
	//	PORT_DIPSETTING(	0x0C, DEF_STR( "1C_3C") );
	//	PORT_DIPSETTING(	0x10, DEF_STR( "1C_4C") );
	//	PORT_DIPSETTING(	0x14, DEF_STR( "1C_5C") );
	//	PORT_DIPSETTING(	0x18, DEF_STR( "1C_6C") );
	//	PORT_DIPSETTING(	0x1C, DEF_STR( "1C_7C") );
	//	PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Bonus_Life") );
	//	PORT_DIPSETTING(    0x00, "1000" );
	//	PORT_DIPSETTING(    0x20, "1500" );
	  //  PORT_DIPNAME( 0x40, 0x00, "Extended Play" );
	//	PORT_DIPSETTING(    0x00, DEF_STR( "No") );
	//	PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
	
		PORT_START(); 
	//	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN3 );
	//	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
	//	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START1 );
	//	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );
	//	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_4WAY );
	//	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_4WAY );
	//	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_4WAY );
	//	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_UP  | IPF_4WAY );
	
		PORT_START(); 	/* SENSE */
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_VBLANK );
	
	INPUT_PORTS_END(); }}; 
	
	static GfxLayout charlayout = new GfxLayout
	(
		16,16,
		RGN_FRAC(1,1),
		1,
		new int[] { 0 },
		new int[] { 7, 6, 5, 4, 3, 2, 1, 0, 15, 14, 13, 12, 11, 10, 9, 8 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
		  8*16, 9*16, 10*16, 11*16, 12*16, 13*16, 14*16, 15*16 },
	//	{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		16*16
	//	8*8
	);
	
	static GfxLayout s2636_character10 = new GfxLayout
	(
		8,10,
		5,
		1,
		new int[] { 0 },
		new int[] { 0,1,2,3,4,5,6,7 },
	   	new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8, 8*8, 9*8 },
		8*16
	);
	
	static GfxLayout saa5050_charlayout = new GfxLayout
	(
		6, 10,
		256,
		1,
		new int[] { 0 },
		new int[] { 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8,
		  5*8, 6*8, 7*8, 8*8, 9*8 },
		8 * 10
	);
	
	static GfxLayout saa5050_hilayout = new GfxLayout
	(
		6, 10,
		256,
		1,
		new int[] { 0 },
		new int[] { 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 0*8, 1*8, 1*8, 2*8,
		  2*8, 3*8, 3*8, 4*8, 4*8 },
		8 * 10
	);
	
	static GfxLayout saa5050_lolayout = new GfxLayout
	(
		6, 10,
		256,
		1,
		new int[] { 0 },
		new int[] { 2, 3, 4, 5, 6, 7 },
		new int[] { 5*8, 5*8, 6*8, 6*8, 7*8,
		  7*8, 8*8, 8*8, 9*8, 9*8 },
		8 * 10
	);
	
	static unsigned char saa5050_palette[8 * 3] =
	{
		0x00, 0x00, 0x00,	/* black */
		0xff, 0x00, 0x00,	/* red */
		0x00, 0xff, 0x00,	/* green */
		0xff, 0xff, 0x00,	/* yellow */
		0x00, 0x00, 0xff,	/* blue */
		0xff, 0x00, 0xff,	/* magenta */
		0x00, 0xff, 0xff,	/* cyan */
		0xff, 0xff, 0xff	/* white */
	};
	
	/* bgnd, fgnd */
	static	unsigned	short	saa5050_colortable[64 * 2] =
	{
		0,1, 0,1, 0,2, 0,3, 0,4, 0,5, 0,6, 0,7,
		1,0, 1,1, 1,2, 1,3, 1,4, 1,5, 1,6, 1,7,
		2,0, 2,1, 2,2, 2,3, 2,4, 2,5, 2,6, 2,7,
		3,0, 3,1, 3,2, 3,3, 3,4, 3,5, 3,6, 3,7,
		4,0, 4,1, 4,2, 4,3, 4,4, 4,5, 4,6, 4,7,
		5,0, 5,1, 5,2, 5,3, 5,4, 5,5, 5,6, 5,7,
		6,0, 6,1, 6,2, 6,3, 6,4, 6,5, 6,6, 6,7,
		7,0, 7,1, 7,2, 7,3, 7,4, 7,5, 7,6, 7,7
	};
	
	//add s2636 decodes here (i.e. from zac2650) and maybe re-arrange them
	static GfxDecodeInfo malzak_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout,  0, 16 ),
	  	new GfxDecodeInfo( REGION_CPU1, 0x0000, s2636_character10, 0, 8 ),	/* s2636 #1  */
		new GfxDecodeInfo( REGION_CPU1, 0x0000, s2636_character10, 0, 8 ),	/* s2636 #2  */
		new GfxDecodeInfo( REGION_GFX2, 0x0000, saa5050_charlayout, 0, 128),
		new GfxDecodeInfo( REGION_GFX2, 0x0000, saa5050_hilayout, 0, 128),
		new GfxDecodeInfo( REGION_GFX2, 0x0000, saa5050_lolayout, 0, 128),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static int val = -1;
	
	static INTERRUPT_GEN( malzak_interrupt )
	{
	//	if(irqenable != 0)
	//		cpu_set_irq_line_and_vector(0,0,HOLD_LINE,0x0300);
	}
	
	static PALETTE_INIT( malzak )
	{
		palette_set_colors(0, saa5050_palette, sizeof(saa5050_palette) / 3);
		memcpy(colortable, saa5050_colortable, sizeof (saa5050_colortable));
	}
	
	
	MACHINE_INIT(malzak)
	{
		val++;
		printf("val = %X\n",val);
	}
	
	static struct SN76477interface sn76477_intf =
	{  /* probably not correct */
		2,	/* 2 chips */
		{ 25,25 },  /* mixing level   pin description		 */
		{ 0,0	/* N/C */},		/*	4  noise_res		 */
		{ 0,0	/* N/C */},		/*	5  filter_res		 */
		{ 0,0	/* N/C */},		/*	6  filter_cap		 */
		{ 0,0	/* N/C */},		/*	7  decay_res		 */
		{ 0,0	/* N/C */},		/*	8  attack_decay_cap  */
		{ RES_K(100),RES_K(100) },		/* 10  attack_res		 */
		{ RES_K(56), RES_K(56)  },		/* 11  amplitude_res	 */
		{ RES_K(10), RES_K(10)  },		/* 12  feedback_res 	 */
		{ 0,0	/* N/C */},		/* 16  vco_voltage		 */
		{ CAP_U(0.1),CAP_U(0.1) },		/* 17  vco_cap			 */
		{ RES_K(8.2),RES_K(8.2) },		/* 18  vco_res			 */
		{ 5.0, 5.0		 },		/* 19  pitch_voltage	 */
		{ RES_K(120), RES_K(120) },		/* 20  slf_res			 */
		{ CAP_U(1.0),CAP_U(1.0) },		/* 21  slf_cap			 */
		{ 0,0	/* N/C */},		/* 23  oneshot_cap		 */
		{ 0,0	/* N/C */}		/* 24  oneshot_res		 */
	};
	
	
	static MACHINE_DRIVER_START( malzak )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(S2650, 3800000/4/3)
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_PORTS(readport,writeport)
	//	MDRV_CPU_VBLANK_INT(malzak_interrupt,1)
	
		MDRV_FRAMES_PER_SECOND(50)
		MDRV_VBLANK_DURATION(SAA5050_VBLANK)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(240, 240)
		MDRV_VISIBLE_AREA(0, 239, 0, 239)
		MDRV_GFXDECODE(malzak_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(16)
		MDRV_PALETTE_INIT(malzak)
		MDRV_COLORTABLE_LENGTH(128)
	
		MDRV_MACHINE_INIT(malzak)
	
		MDRV_VIDEO_START(malzak)
		MDRV_VIDEO_UPDATE(malzak)
	
		/* sound hardware */
		MDRV_SOUND_ADD(SN76477,sn76477_intf);
	
	MACHINE_DRIVER_END
	
	static RomLoadPtr rom_malzak = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x8000, REGION_CPU1, 0 );
		ROM_LOAD( "malzak.5",     0x0000, 0x0800, CRC(75355c98);SHA1(7036ed5d9ee38585b1a6bc204d410d5fb5ddd81f) )
		ROM_CONTINUE( 0x2000, 0x0800 );
		ROM_LOAD( "malzak.4",     0x0800, 0x0400, CRC(744c81e3);SHA1(c08d6df3cf2808a5f99d8247fc19a59be88121a9) )
		ROM_CONTINUE( 0x4000, 0x0c00 );
		ROM_LOAD( "malzak.2",     0x0c00, 0x0800, CRC(2a12ad67);SHA1(f89a50b62311a170004c061abd8dedc3ebd84748) )
		ROM_LOAD( "malzak.3",     0x4400, 0x0800, CRC(b947229e);SHA1(37b88b5aa91a483fcfe60a9bdd67a66f6378c487) )
	
		ROM_REGION(0x0800, REGION_USER1, 0);
	
		ROM_REGION( 0x0800, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "malzak.1",     0x0000, 0x0800, CRC(74d5ff7b);SHA1(cae326370dc83b86542f9d070e2dc91b1b833356) )
	
		ROM_REGION(0x01000, REGION_GFX2,0);// internal character set?
		ROM_LOAD("p2000.chr", 0x0140, 0x08c0, BAD_DUMP CRC(78c17e3e);
	
	ROM_END(); }}; 
	
	public static GameDriver driver_malzak	   = new GameDriver("19??"	,"malzak"	,"malzak.java"	,rom_malzak,null	,machine_driver_malzak	,input_ports_malzak	,null	,ROT0	,	"Kitronix", "Malzak", GAME_NOT_WORKING | GAME_NO_SOUND )
}
