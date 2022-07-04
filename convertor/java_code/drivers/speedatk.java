/*****************************************************************************************

Speed Attack! (c) 1984 Seta Kikaku Corp.

driver by Pierpaolo Prazzoli & Angelo Salese, based on early work by David Haywood

TODO:
 - Coinage Dip Switch doesn't match the readme? Also there are wrong coin insertions even
   with the bit impulse macro. There are chances that there's a circuitry which controls
   both...

How to play:
 - A to D selects a card.
 - Turn takes one or more cards into your hand (depends on how many cards you
   putted on the stacks).
 - Left & right puts a card on one of the two stacks.

Notes:
 - According to the text gfx rom, there are also a Taito and a KKK versions out there.

------------------------------------------------------------------------------------------
SPEED ATTACK!
(c)SETA

CPU	:Z80 x 1
SOUND	:AY-3-8910 x 1
XTAL	:12MHZ

SETA CUSTOM ?
AC-002 , AC-003

CB1-1   :1C
CB0-2   :1D
CB1-3   :1F
CB0-4   :1H
CB0-5   :7C
CB0-6   :7D
CB0-7   :7E

CB1.BPR :7L TBP18S030
CB2.BPR :6K 82S129

----------------------------------------------------------

DIP SWITCH 8BIT (Default: ALL ON)

SW 1,2 : COIN CREDIT   LL:1-1 HL:1-2 LH:1-5 HH:1-10
SW 3,4 : LEVEL LL:EASY -> LH -> HL -> HH:HARD
SW 5,6 : NOT USE
SW 7   : FLIP SCREEN H:FLIP
SW 8   : TEST MODE H:TEST

   PARTS SIDE | SOLDIER SIDE
  ----------------------------
      GND   | 1|    GND
      GND   | 2|    GND
      +5V   | 3|    +5V
            | 4|
     +12V   | 5|   +12V
  SPEAKER(+)| 6|  SPEAKER(-)
     SYNC   | 7| COIN COUNTER
       B    | 8|  SERVICE
       G    | 9|  COIN SW
       R    |10|
     PD 6   |11|   PS 6 (NOT USE)
     PD 5   |12|   PS 5 (NOT USE)
     PD 4   |13|   PS 4
     PD 3   |14|   PS 3
     PD 1   |15|   PS 1
     PD 2   |16|   PS 2
            |17|
            |18|

PS / PD :  key matrix
*****************************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class speedatk
{
	
	static UINT8 mux_data;
	
	extern extern extern extern PALETTE_INIT( speedatk );
	extern VIDEO_START( speedatk );
	extern VIDEO_UPDATE( speedatk );
	
	/* This "key matrix" device maps some buttons with multiple bit activations,for        *
	 * example pressing button A + button B it causes an output of button C.               *
	 * This function converts the bit inputs of this game into the usual way MAME does,and *
	 * it handles the multiplexer device between player one and two.                       */
	public static ReadHandlerPtr key_matrix_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch(mux_data)
		{
			case 0x02:
			{
				switch(readinputport(1))
				{
					case 0x002: return 0x02;
					case 0x001: return 0x01;
					case 0x004: return 0x03;
					case 0x008: return 0x04;
					case 0x010: return 0x07;
					case 0x020: return 0x08;
					case 0x040: return 0x09;
					case 0x080: return 0x0a;
					case 0x100: return 0x10;
					case 0x200: return 0x20;
					case 0x400: return 0x40;
					case 0x800: return 0x80;
					default:	return 0x00;
				}
			}
			case 0x04:
			{
				switch(readinputport(2))
				{
					case 0x002: return 0x02;
					case 0x001: return 0x01;
			 		case 0x004: return 0x03;
			 		case 0x008: return 0x04;
					case 0x010: return 0x07;
					case 0x020: return 0x08;
					case 0x040: return 0x09;
					case 0x080: return 0x0a;
					case 0x100: return 0x10;
					case 0x200: return 0x20;
					case 0x400: return 0x40;
					case 0x800: return 0x80;
					default:	return 0x00;
				}
			}
			default: logerror("Input reads with mux_data = %x\n",mux_data);
		}
	
		return 0x00;
	} };
	
	public static WriteHandlerPtr key_matrix_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		mux_data = data;
	} };
	
	public static ReadHandlerPtr read_8001  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return 1;
	} };
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0x8000, key_matrix_r ),
		new Memory_ReadAddress( 0x8001, 0x8001, read_8001 ),
		new Memory_ReadAddress( 0x8588, 0x858f, MRA_RAM ),
		new Memory_ReadAddress( 0x8800, 0x8bff, MRA_RAM ),
		new Memory_ReadAddress( 0x8c00, 0x8fff, MRA_RAM ),
		new Memory_ReadAddress( 0xa000, 0xa3ff, MRA_RAM ),
		new Memory_ReadAddress( 0xb000, 0xb3ff, MRA_RAM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0x8000, 0x8000, key_matrix_w ),
		new Memory_WriteAddress( 0x8588, 0x858f, MWA_RAM ),
		new Memory_WriteAddress( 0x8800, 0x8bff, MWA_RAM ),
		new Memory_WriteAddress( 0x8c00, 0x8fff, MWA_RAM ),
		new Memory_WriteAddress( 0xa000, 0xa3ff, speedatk_videoram_w, videoram ),
		new Memory_WriteAddress( 0xb000, 0xb3ff, speedatk_colorram_w ,colorram ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	public static IO_ReadPort readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x40, 0x40, input_port_0_r ),
		/* are these not used? after they're read it sets bit 7 */
		new IO_ReadPort( 0x60, 0x60, MRA_NOP ),
		new IO_ReadPort( 0x61, 0x61, MRA_NOP ),
		new IO_ReadPort( 0x68, 0x68, MRA_NOP ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	public static IO_WritePort writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x01, 0x01, speedatk_flip_screen_w ),
		new IO_WritePort( 0x40, 0x40, AY8910_control_port_0_w ),
		new IO_WritePort( 0x41, 0x41, AY8910_write_port_0_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	static InputPortPtr input_ports_speedatk = new InputPortPtr(){ public void handler() { 
		PORT_START(); 
		PORT_SERVICE( 0x01, IP_ACTIVE_HIGH );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x00, "Easy" );
		PORT_DIPSETTING(    0x20, "Normal" );
		PORT_DIPSETTING(    0x10, "Hard" );
		PORT_DIPSETTING(    0x30, "Hardest" );
		/* Doesn't work? */
		PORT_DIPNAME( 0xc0, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x40, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x80, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(    0xc0, "1 Coin/10 Credits" );
	
		PORT_START(); 
		PORT_BIT( 0x0002, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );//P1 A
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );//P1 B
		PORT_BIT( 0x0004, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER1 );//P1 C
		PORT_BIT( 0x0008, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_PLAYER1 );//P1 D
		PORT_BIT( 0x0010, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x0040, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0080, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT( 0x0100, IP_ACTIVE_HIGH, IPT_BUTTON5 | IPF_PLAYER1 );//P1 Turn
		PORT_BIT( 0x0200, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x0400, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT_IMPULSE( 0x800, IP_ACTIVE_HIGH, IPT_COIN1, 2 );
	
		PORT_START(); 
		PORT_BIT( 0x0002, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );//P2 A
		PORT_BIT( 0x0001, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );//P2 B
		PORT_BIT( 0x0004, IP_ACTIVE_HIGH, IPT_BUTTON3 | IPF_PLAYER2 );//P2 C
		PORT_BIT( 0x0008, IP_ACTIVE_HIGH, IPT_BUTTON4 | IPF_PLAYER2 );//P2 D
		PORT_BIT( 0x0010, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_2WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0020, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x0040, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_2WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0080, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x0100, IP_ACTIVE_HIGH, IPT_BUTTON5 | IPF_PLAYER2 );//P2 Turn
		PORT_BIT( 0x0200, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT( 0x0400, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT_IMPULSE( 0x0800, IP_ACTIVE_HIGH, IPT_COIN2, 2 );
	INPUT_PORTS_END(); }}; 
	
	static GfxLayout charlayout_1bpp = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,1),
		3,
		new int[] { 0, 0, 0 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8
	);
	
	static GfxLayout charlayout_3bpp = new GfxLayout
	(
		8,8,
		RGN_FRAC(1,3),
		3,
		new int[] { RGN_FRAC(2,3), RGN_FRAC(1,3), RGN_FRAC(0,3) },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
		8*8
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, charlayout_1bpp,   0, 32 ),
		new GfxDecodeInfo( REGION_GFX2, 0, charlayout_3bpp,   0, 32 ),
		new GfxDecodeInfo( -1 )
	};
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
		1,			/* 1 chip */
		4000000,    /* ?? */
		new int[] { 100 },
		new ReadHandlerPtr[] { 0 },
		new ReadHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 },
		new WriteHandlerPtr[] { 0 }
	);
	
	static MACHINE_DRIVER_START( speedatk )
		MDRV_CPU_ADD(Z80,12000000/2)
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_PORTS(readport,writeport)
		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(320, 256)
		MDRV_VISIBLE_AREA(0*8, 34*8-1, 0*8, 29*8-1)
	
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(16)
		MDRV_COLORTABLE_LENGTH(0x100)
		MDRV_PALETTE_INIT(speedatk)
	
		MDRV_VIDEO_START(speedatk)
		MDRV_VIDEO_UPDATE(speedatk)
	
		/* sound hardware */
		MDRV_SOUND_ADD(AY8910, ay8910_interface)
	MACHINE_DRIVER_END
	
	static RomLoadPtr rom_speedatk = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );
		ROM_LOAD( "cb1-1",        0x0000, 0x2000, CRC(df988e05);SHA1(0ec91c5f2e1adf952a4fe7aede591e763773a75b) )
		ROM_LOAD( "cb0-2",        0x2000, 0x2000, CRC(be949154);SHA1(8a594a7ebdc8456290919163f7ea4ccb0d1f4edb) )
		ROM_LOAD( "cb1-3",        0x4000, 0x2000, CRC(741a5949);SHA1(7f7bebd4fb73fef9aa28549d100f632c442ac9b3) )
		ROM_LOAD( "cb0-4",        0x6000, 0x2000, CRC(53a9c0c8);SHA1(cd0fd94411dabf09828c1f629891158c40794127) )
	
		ROM_REGION( 0x2000, REGION_GFX1, ROMREGION_DISPOSE );
		ROM_LOAD( "cb0-7",        0x0000, 0x2000, CRC(a86007b5);SHA1(8e5cab76c37a8d53e1355000cd1a0a85ffae0e8c) )
	
		ROM_REGION( 0x6000, REGION_GFX2, ROMREGION_DISPOSE );
		ROM_LOAD( "cb0-5",        0x0000, 0x2000, CRC(47a966e7);SHA1(fdaa0f88656afc431bae367679ce6298fa962e0f) )
		ROM_LOAD( "cb0-6",        0x2000, 0x2000, CRC(cc1da937);SHA1(1697bb008bfa5c33a282bd470ac39c324eea7509) )
		ROM_COPY( REGION_GFX2,    0x0000, 0x4000, 0x1000 );/* Fill the blank space with cards gfx */
		ROM_COPY( REGION_GFX1,    0x1000, 0x5000, 0x1000 );/* Gfx from cb0-7 */
	
		ROM_REGION( 0x0120, REGION_PROMS, 0 );
		ROM_LOAD( "cb1.bpr",      0x0000, 0x0020, CRC(a0176c23);SHA1(133fb9eef8a6595cac2dcd7edce4789899a59e84) ) /* color PROM */
		ROM_LOAD( "cb2.bpr",      0x0020, 0x0100, CRC(a604cf96);SHA1(a4ef6e77dcd3abe4c27e8e636222a5ee711a51f5) ) /* lookup table */
	ROM_END(); }}; 
	
	public static GameDriver driver_speedatk	   = new GameDriver("1984"	,"speedatk"	,"speedatk.java"	,rom_speedatk,null	,machine_driver_speedatk	,input_ports_speedatk	,null	,ROT0	,	"Seta Kikaku Corp.", "Speed Attack!" )
}
