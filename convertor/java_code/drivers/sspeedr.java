/***************************************************************************

Taito Super Speed Race driver

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class sspeedr
{
	
	extern extern extern extern 
	extern extern extern extern 
	extern extern extern extern 
	extern VIDEO_START( sspeedr );
	extern VIDEO_UPDATE( sspeedr );
	extern VIDEO_EOF( sspeedr );
	
	static UINT8 led_TIME[2];
	static UINT8 led_SCORE[24];
	
	
	static PALETTE_INIT( sspeedr )
	{
		int i;
	
		for (i = 0; i < 16; i++)
		{
			int r = (i & 1) ? 0xb0 : 0x20;
			int g = (i & 2) ? 0xb0 : 0x20;
			int b = (i & 4) ? 0xb0 : 0x20;
	
			if (i & 8)
			{
				r += 0x4f;
				g += 0x4f;
				b += 0x4f;
			}
	
			palette_set_color(i, r, g, b);
		}
	}
	
	
	public static ReadHandlerPtr sspeedr_steering_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT8 val = readinputport(0);
	
		return 0x3f ^ (val >> 2) ^ (val >> 3);
	} };
	
	
	public static WriteHandlerPtr sspeedr_int_ack_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		cpu_set_irq_line(0, 0, CLEAR_LINE);
	} };
	
	
	public static WriteHandlerPtr sspeedr_lamp_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		artwork_show("lampGO",
			data & 1);
		artwork_show("lampEP",
			data & 2);
	
		coin_counter_w(0, data & 8);
	} };
	
	
	public static WriteHandlerPtr sspeedr_time_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UINT8 prev = led_TIME[offset];
	
		char buf_old[8];
		char buf_new[8];
	
		data = data & 15;
	
		sprintf(buf_old, "LEDT%d-%c", offset, prev >= 10 ? 'X' : '0' + prev);
		sprintf(buf_new, "LEDT%d-%c", offset, data >= 10 ? 'X' : '0' + data);
	
		artwork_show(buf_old, 0);
		artwork_show(buf_new, 1);
	
		led_TIME[offset] = data;
	} };
	
	
	public static WriteHandlerPtr sspeedr_score_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UINT8 prev = led_SCORE[offset];
	
		char buf_old[8];
		char buf_new[8];
	
		data = ~data & 15;
	
		sprintf(buf_old, "LED%02d-%c", offset, prev >= 10 ? 'X' : '0' + prev);
		sprintf(buf_new, "LED%02d-%c", offset, data >= 10 ? 'X' : '0' + data);
	
		artwork_show(buf_old, 0);
		artwork_show(buf_new, 1);
	
		led_SCORE[offset] = data;
	} };
	
	
	public static WriteHandlerPtr sspeedr_sound_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* not implemented */
	} };
	
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x0fff, MRA_ROM ),
		new Memory_ReadAddress( 0x2000, 0x21ff, MRA_RAM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x0fff, MWA_ROM ),
		new Memory_WriteAddress( 0x2000, 0x21ff, MWA_RAM ),
		new Memory_WriteAddress( 0x7f00, 0x7f17, sspeedr_score_w ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static IO_ReadPort readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x00, 0x00, sspeedr_steering_r ),
		new IO_ReadPort( 0x01, 0x01, input_port_1_r ),
		new IO_ReadPort( 0x03, 0x03, input_port_2_r ),
		new IO_ReadPort( 0x04, 0x04, input_port_3_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	
	public static IO_WritePort writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0x01, sspeedr_sound_w ),
		new IO_WritePort( 0x02, 0x02, sspeedr_lamp_w ),
		new IO_WritePort( 0x04, 0x05, sspeedr_time_w ),
		new IO_WritePort( 0x06, 0x06, watchdog_reset_w ),
		new IO_WritePort( 0x10, 0x10, sspeedr_driver_horz_w ),
		new IO_WritePort( 0x11, 0x11, sspeedr_driver_pic_w ),
		new IO_WritePort( 0x12, 0x12, sspeedr_driver_horz_2_w ),
		new IO_WritePort( 0x13, 0x13, sspeedr_drones_horz_w ),
		new IO_WritePort( 0x14, 0x14, sspeedr_drones_horz_2_w ),
		new IO_WritePort( 0x15, 0x15, sspeedr_drones_mask_w ),
		new IO_WritePort( 0x16, 0x16, sspeedr_driver_vert_w ),
		new IO_WritePort( 0x17, 0x18, sspeedr_track_vert_w ),
		new IO_WritePort( 0x19, 0x19, sspeedr_track_horz_w ),
		new IO_WritePort( 0x1a, 0x1a, sspeedr_track_horz_2_w ),
		new IO_WritePort( 0x1b, 0x1b, sspeedr_track_ice_w ),
		new IO_WritePort( 0x1c, 0x1e, sspeedr_drones_vert_w ),
		new IO_WritePort( 0x1f, 0x1f, sspeedr_int_ack_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	
	static InputPortPtr input_ports_sspeedr = new InputPortPtr(){ public void handler() { 
	
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x80, IPT_DIAL, 25, 10, 0x00, 0xff );
	
		PORT_START(); 
		PORT_ANALOG( 0x1f, 0x00, IPT_PEDAL | IPF_REVERSE, 25, 20, 0x00, 0x1f );
	
		PORT_START(); 
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "2C_3C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x0C, 0x08, "Play Time" );
		PORT_DIPSETTING(    0x00, "60 seconds");
		PORT_DIPSETTING(    0x04, "70 seconds");
		PORT_DIPSETTING(    0x08, "80 seconds");
		PORT_DIPSETTING(    0x0C, "90 seconds");
		PORT_DIPNAME( 0x10, 0x00, "Extended Play" );
		PORT_DIPSETTING(    0x00, "20 seconds" );
		PORT_DIPSETTING(    0x10, "30 seconds" );
		PORT_DIPNAME( 0xE0, 0x20, DEF_STR( "Service_Mode") );
		PORT_DIPSETTING(    0x20, "Play Mode" );
		PORT_DIPSETTING(    0xA0, "RAM/ROM Test" );
		PORT_DIPSETTING(    0xE0, "Accelerator Adjustment" );
	
		PORT_START(); 
		PORT_BIT ( 0x01, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x02, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT ( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_TOGGLE );/* gear shift lever */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW, IPT_COIN1 );
	
	INPUT_PORTS_END(); }}; 
	
	
	static GfxLayout car_layout = new GfxLayout
	(
		32, 16,
		16,
		4,
		new int[] { 0, 1, 2, 3 },
		new int[] {
			0x04, 0x04, 0x00, 0x00, 0x0c, 0x0c, 0x08, 0x08,
			0x14, 0x14, 0x10, 0x10, 0x1c, 0x1c, 0x18, 0x18,
			0x24, 0x24, 0x20, 0x20, 0x2c, 0x2c, 0x28, 0x28,
			0x34, 0x34, 0x30, 0x30, 0x3c, 0x3c, 0x38, 0x38
		},
		new int[] {
			0x000, 0x040, 0x080, 0x0c0, 0x100, 0x140, 0x180, 0x1c0,
			0x200, 0x240, 0x280, 0x2c0, 0x300, 0x340, 0x380, 0x3c0
		},
		0x400
	);
	
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, car_layout, 0, 1 ),
		new GfxDecodeInfo( REGION_GFX2, 0, car_layout, 0, 1 ),
		new GfxDecodeInfo( -1 )
	};
	
	
	static MACHINE_DRIVER_START( sspeedr )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80, 19968000 / 8)
		MDRV_CPU_MEMORY(readmem, writemem)
		MDRV_CPU_PORTS(readport, writeport)
		MDRV_CPU_VBLANK_INT(irq0_line_assert, 1)
	
		MDRV_FRAMES_PER_SECOND(59.39)
		MDRV_VBLANK_DURATION(16 * 1000000 / 15680)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(376, 256)
		MDRV_VISIBLE_AREA(0, 375, 0, 247)
		MDRV_GFXDECODE(gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(16)
	
		MDRV_PALETTE_INIT(sspeedr)
		MDRV_VIDEO_START(sspeedr)
		MDRV_VIDEO_UPDATE(sspeedr)
		MDRV_VIDEO_EOF(sspeedr)
	
		/* sound hardware */
	MACHINE_DRIVER_END
	
	
	static RomLoadPtr rom_sspeedr = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );
		ROM_LOAD( "ssr0000.pgm", 0x0000, 0x0800, CRC(bfc7069a);SHA1(2f7aa3d3c7cfd804ba4b625c6a8338534a204855) )
		ROM_LOAD( "ssr0800.pgm", 0x0800, 0x0800, CRC(ec46b59a);SHA1(d5727efecb32ad3d034b885e4a57d7373368ca9e) )
	
		ROM_REGION( 0x0800, REGION_GFX1, ROMREGION_DISPOSE );/* driver */
		ROM_LOAD( "ssrm762a.f3", 0x0000, 0x0800, CRC(de4653a9);SHA1(a6bbffb7eb60581eee43c74d20ca00b50c9a6e07) )
	
		ROM_REGION( 0x0800, REGION_GFX2, ROMREGION_DISPOSE );/* drone */
		ROM_LOAD( "ssrm762b.j3", 0x0000, 0x0800, CRC(ef6a1cd6);SHA1(77c31f14783e5ba90849bdc930b099c8360aeba7) )
	
		ROM_REGION( 0x0800, REGION_GFX3, 0 );/* track */
		ROM_LOAD( "ssrm762c.l3", 0x0000, 0x0800, CRC(ebaad3ee);SHA1(54ac994b505d20c75cf07a4f68da12360ee00153) )
	ROM_END(); }}; 
	
	
	public static GameDriver driver_sspeedr	   = new GameDriver("1979"	,"sspeedr"	,"sspeedr.java"	,rom_sspeedr,null	,machine_driver_sspeedr	,input_ports_sspeedr	,null	,ROT270	,	"Midway", "Super Speed Race", GAME_NO_SOUND )
}
