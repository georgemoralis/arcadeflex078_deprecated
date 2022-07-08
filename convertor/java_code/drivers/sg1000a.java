/*********************************************************
Sega hardware based on their SG-1000 console
Driver by Tomasz Slanina  dox@space.pl


Supported games :
- Champion Boxing
- Champion Pro Wrestling

Memory map :
0x0000 - 0xBFFF ROM
0xC000 - 0xC3FF RAM

CPU:
Z80 A  			 	           3.57954 MHz (Champion Boxing)
315-5114 (encrypted Z80) 3.57954 MHz (Champion Pro Wrestling)

8255 for I/O port work
3 Eproms for program and graphics
TMM2064 for program RAM
TMS9928 for graphics ( 3.57954 MHz? )
8 8118 dynamic RAMs for the graphics
74LS139 and 74LS32 for logic gating
ULN2003 for coin counter output
76489 for music
7808 voltage regulator to a transistorized circuit for TV output
secondary crystal, numbers unknown for the TMS9928

******************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class sg1000a
{
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
	    new Memory_ReadAddress( 0x0000, 0xbFFF, MRA_ROM ),
	    new Memory_ReadAddress( 0xc000, 0xc3ff, MRA_RAM ),
	
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
	    new Memory_WriteAddress( 0x0000, 0xbFFF, MWA_ROM ),
	    new Memory_WriteAddress( 0xc000, 0xc3ff, MWA_RAM ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	static PORT_READ_START ( readport )
	    { 0xBE, 0xBE, TMS9928A_vram_r },
	    { 0xBF, 0xBF, TMS9928A_register_r },
	    { 0xDC, 0xDC, input_port_0_r},
	    { 0xDD, 0xDD, input_port_1_r},
	    { 0xDE, 0xDE, input_port_2_r},
	PORT_END
	
	static PORT_WRITE_START ( writeport )
	    { 0xBE, 0xBE, TMS9928A_vram_w },
	    { 0xBF, 0xBF, TMS9928A_register_w },
	    { 0xDF, 0xDF, MWA_NOP },  //? 8255 ?
	    { 0x7f, 0x7F, SN76496_0_w },
	PORT_END
	
	static InputPortPtr input_ports_chwrestl = new InputPortPtr(){ public void handler() { 
	    PORT_START(); 
	    PORT_BIT ( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP |IPF_PLAYER2 );
	    PORT_BIT ( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN |IPF_PLAYER2 );
	    PORT_BIT ( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT |IPF_PLAYER2 );
	    PORT_BIT ( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT |IPF_PLAYER2 );
	    PORT_BIT ( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 |IPF_PLAYER2 );
	    PORT_BIT ( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 |IPF_PLAYER2 );
	    PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_START1 );
		  PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
	    PORT_START(); 
	    PORT_BIT ( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP );
	    PORT_BIT ( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN );
	    PORT_BIT ( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT );
	    PORT_BIT ( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT );
	    PORT_BIT ( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
	    PORT_BIT ( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
	    PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );
		  PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );
	
	    PORT_START(); 
	    PORT_DIPNAME( 0x80, 0x80, "Language" );
			PORT_DIPSETTING(    0x00, "Japanese" );
			PORT_DIPSETTING(    0x80, "English" );
			PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Demo_Sounds") );
			PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
			PORT_DIPSETTING(    0x00, DEF_STR( "On") );
			PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_B") );
			PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
			PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
			PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
			PORT_DIPSETTING(    0x10, DEF_STR( "1C_3C") );
	
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_chboxing = new InputPortPtr(){ public void handler() { 
	    PORT_START(); 
	    PORT_BIT ( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP );
	    PORT_BIT ( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN );
	    PORT_BIT ( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT );
	    PORT_BIT ( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT );
	    PORT_BIT ( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 );
	    PORT_BIT ( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 );
	    PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_START1 );
		  PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_START2 );
	
	    PORT_START(); 
	    PORT_BIT ( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP |IPF_PLAYER2 );
	    PORT_BIT ( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN |IPF_PLAYER2 );
	    PORT_BIT ( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT |IPF_PLAYER2 );
	    PORT_BIT ( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT |IPF_PLAYER2 );
	    PORT_BIT ( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 |IPF_PLAYER2 );
	    PORT_BIT ( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 |IPF_PLAYER2 );
	    PORT_BIT ( 0x40, IP_ACTIVE_LOW, IPT_COIN1 );
		  PORT_BIT ( 0x80, IP_ACTIVE_LOW, IPT_COIN2 );
	
	    PORT_START(); 
	    PORT_DIPNAME( 0x80, 0x00, "Language" );
			PORT_DIPSETTING(    0x80, "Japanese" );
			PORT_DIPSETTING(    0x00, "English" );
			PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Demo_Sounds") );
			PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
			PORT_DIPSETTING(    0x00, DEF_STR( "On") );
			PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Coin_B") );
			PORT_DIPSETTING(    0x00, DEF_STR( "2C_1C") );
			PORT_DIPSETTING(    0x30, DEF_STR( "1C_1C") );
			PORT_DIPSETTING(    0x20, DEF_STR( "1C_2C") );
			PORT_DIPSETTING(    0x10, DEF_STR( "1C_3C") );
	INPUT_PORTS_END(); }}; 
	
	
	static SN76496interface sn76496_interface = new SN76496interface
	(
	    1,  		/* 1 chip 		*/
	    new int[] {3579545},  /* 3.579545 MHz */
	    new int[] { 100 }
	);
	
	static INTERRUPT_GEN( sg100a_interrupt )
	{
	    TMS9928A_interrupt();
	}
	
	static void vdp_interrupt (int state)
	{
		cpu_set_irq_line(0,0, HOLD_LINE);
	}
	
	static const TMS9928a_interface tms9928a_interface =
	{
		TMS99x8A,
		0x4000,
		vdp_interrupt
	};
	
	static MACHINE_DRIVER_START( sg1000a )
		MDRV_CPU_ADD(Z80, 3579545)       /* 3.579545 Mhz */
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_PORTS(readport,writeport)
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_CPU_VBLANK_INT(sg100a_interrupt,1)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
		MDRV_TMS9928A( &tms9928a_interface )
		MDRV_SOUND_ADD(SN76496, sn76496_interface)
	MACHINE_DRIVER_END
	
	
	static RomLoadPtr rom_chwrestl = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 2*0x10000, REGION_CPU1, 0 );
		ROM_LOAD( "5732",	0x0000, 0x4000, CRC(a4e44370),SHA1(a9dbf60e77327dd2bec6816f3142b42ad9ca4d09) ) /* encrypted */
		ROM_LOAD( "5733",	0x4000, 0x4000, CRC(4f493538),SHA1(467862fe9337497e3cdebb29bf28f6cfe3066ccd) ) /* encrypted */
		ROM_LOAD( "5734",	0x8000, 0x4000, CRC(d99b6301),SHA1(5e762ed45cde08d5223828c6b1d3569b2240462c) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_chboxing = new RomLoadPtr(){ public void handler(){ 
	  ROM_REGION( 0x10000, REGION_CPU1, 0 );
	  ROM_LOAD( "cb6105.bin",	0x0000, 0x4000, CRC(43516f2e),SHA1(e3a9bbe914b5bfdcd1f85ca5fae922c4cae3c106) )
		ROM_LOAD( "cb6106.bin",	0x4000, 0x4000, CRC(65e2c750),SHA1(843466b8d6baebb4d5e434fbdafe3ae8fed03475) )
		ROM_LOAD( "cb6107.bin",	0x8000, 0x2000, CRC(c2f8e522),SHA1(932276e7ad33aa9efbb4cd10bc3071d88cb082cb) )
	ROM_END(); }}; 
	
	
	DRIVER_INIT(chwrestl)
	{
		regulus_decode();
	}
	
	public static GameDriver driver_chboxing	   = new GameDriver("1984"	,"chboxing"	,"sg1000a.java"	,rom_chboxing,null	,machine_driver_sg1000a	,input_ports_chboxing	,null	,ROT0	,	"Sega", "Champion Boxing")
	public static GameDriver driver_chwrestl	   = new GameDriver("1985"	,"chwrestl"	,"sg1000a.java"	,rom_chwrestl,null	,machine_driver_sg1000a	,input_ports_chwrestl	,init_chwrestl	,ROT0	,	"Sega", "Champion Pro Wrestling")
	
}
