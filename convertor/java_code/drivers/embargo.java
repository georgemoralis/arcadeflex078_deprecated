/***************************************************************************

Cinematronics Embargo driver

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package drivers;

public class embargo
{
	
	static int dial_enable_1;
	static int dial_enable_2;
	
	static int input_select;
	
	
	
	static VIDEO_UPDATE( embargo )
	{
		copybitmap(bitmap, tmpbitmap, 0, 0, 0, 0, cliprect, TRANSPARENCY_NONE, 0);
	}
	
	
	public static WriteHandlerPtr embargo_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int col = offset % 32;
		int row = offset / 32;
	
		int i;
	
		for (i = 0; i < 8; i++)
		{
			plot_pixel(tmpbitmap, 8 * col + i, row, (data >> i) & 1);
		}
	
		videoram.write(offset,data);
	} };
	
	
	public static ReadHandlerPtr embargo_input_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return (readinputport(1) << (7 - input_select)) & 0x80;
	} };
	
	
	public static ReadHandlerPtr embargo_dial_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UINT8 lo = 0;
		UINT8 hi = 0;
	
		UINT8 mapped_lo = 0;
		UINT8 mapped_hi = 0;
	
		int i;
	
		/* game reads 4 bits per dial and maps them onto clock directions */
	
		static const UINT8 map[] =
		{
			0x0, 0xB, 0x1, 0x2, 0x4, 0x4, 0x2, 0x3,
			0x9, 0xA, 0x8, 0x9, 0x8, 0x5, 0x7, 0x6
		};
	
		if (dial_enable_1 && !dial_enable_2)
		{
			lo = readinputport(3);
			hi = readinputport(4);
		}
		if (dial_enable_2 && !dial_enable_1)
		{
			lo = readinputport(5);
			hi = readinputport(6);
		}
	
		lo = 12 * lo / 256;
		hi = 12 * hi / 256;
	
		for (i = 0; i < 16; i++)
		{
			if (map[i] == lo)
			{
				mapped_lo = i;
			}
			if (map[i] == hi)
			{
				mapped_hi = i;
			}
		}
	
		return 16 * mapped_hi + mapped_lo;
	} };
	
	
	public static WriteHandlerPtr embargo_port1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		dial_enable_1 = data & 1; /* other bits unknown */
	} };
	public static WriteHandlerPtr embargo_port2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		dial_enable_2 = data & 1; /* other bits unknown */
	} };
	
	
	public static WriteHandlerPtr embargo_input_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		input_select = data & 7;
	} };
	
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x0fff, MRA_ROM ),
		new Memory_ReadAddress( 0x1e00, 0x3dff, MRA_RAM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x0fff, MWA_ROM ),
		new Memory_WriteAddress( 0x1e00, 0x1fff, MWA_RAM ),
		new Memory_WriteAddress( 0x2000, 0x3dff, embargo_videoram_w, videoram, videoram_size ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static IO_ReadPort readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x01, 0x01, input_port_0_r ),
		new IO_ReadPort( 0x02, 0x02, embargo_dial_r ),
		new IO_ReadPort( S2650_DATA_PORT, S2650_DATA_PORT, input_port_2_r ),
		new IO_ReadPort( S2650_CTRL_PORT, S2650_CTRL_PORT, embargo_input_r ),
		new IO_ReadPort(MEMPORT_MARKER, 0)
	};
	
	
	public static IO_WritePort writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x01, 0x01, embargo_port1_w ),
		new IO_WritePort( 0x02, 0x02, embargo_port2_w ),
		new IO_WritePort( 0x03, 0x03, IOWP_NOP ), /* always 0xFE */
		new IO_WritePort( S2650_CTRL_PORT, S2650_CTRL_PORT, embargo_input_w ),
		new IO_WritePort(MEMPORT_MARKER, 0)
	};
	
	
	static InputPortPtr input_ports_embargo = new InputPortPtr(){ public void handler() { 
	
		PORT_START();  /* port 0x01 */
		PORT_DIPNAME( 0x03, 0x00, "Rounds" );
		PORT_DIPSETTING(    0x00, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPSETTING(    0x02, "5" );
		PORT_DIPSETTING(    0x03, "6" );
	
		PORT_START();  /* S2650_CONTROL_PORT */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_START3 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_START4 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );
	
		PORT_START();  /* S2650_DATA_PORT */
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_COIN1 );
	
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x80, IPT_DIAL | IPF_PLAYER1, 50, 8, 0, 0 );
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x80, IPT_DIAL | IPF_PLAYER2, 50, 8, 0, 0 );
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x80, IPT_DIAL | IPF_PLAYER3, 50, 8, 0, 0 );
		PORT_START(); 
		PORT_ANALOG( 0xff, 0x80, IPT_DIAL | IPF_PLAYER4, 50, 8, 0, 0 );
	
	INPUT_PORTS_END(); }}; 
	
	
	static MACHINE_DRIVER_START( embargo )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(S2650, 625000)
		MDRV_CPU_MEMORY(readmem, writemem)
		MDRV_CPU_PORTS(readport, writeport)
	
		MDRV_FRAMES_PER_SECOND(60)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(0, 255, 0, 239)
		MDRV_PALETTE_LENGTH(2)
	
		MDRV_PALETTE_INIT(black_and_white)
		MDRV_VIDEO_START(generic_bitmapped)
		MDRV_VIDEO_UPDATE(embargo)
	
		/* sound hardware */
	MACHINE_DRIVER_END
	
	
	static RomLoadPtr rom_embargo = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x8000, REGION_CPU1, 0 );
		ROM_LOAD( "emb1", 0x0000, 0x0200, CRC(00dcbc24),SHA1(67018a20d7694618123499640f041fb518ea29fa) )
		ROM_LOAD( "emb2", 0x0200, 0x0200, CRC(e7069b11),SHA1(b933095087cd4fe10f12fd244606aaaed1c31bca) )
		ROM_LOAD( "emb3", 0x0400, 0x0200, CRC(1af7a966),SHA1(a8f6d1063927106f44c43f64c26b52c07c5450df) )
		ROM_LOAD( "emb4", 0x0600, 0x0200, CRC(d9c75da0),SHA1(895784ec543f1c73ced5f37751a26cb3305030f3) )
		ROM_LOAD( "emb5", 0x0800, 0x0200, CRC(15960b58),SHA1(2e6c196b240cef92799f83deef2b1c501c01f9c9) )
		ROM_LOAD( "emb6", 0x0a00, 0x0200, CRC(7ba23058),SHA1(ad3736ec7617ecb902ea686055e55203be1ea5fd) )
		ROM_LOAD( "emb7", 0x0c00, 0x0200, CRC(6d46a593),SHA1(5432ae1c167e774c47f06ffd0e8acf801891dee1) )
		ROM_LOAD( "emb8", 0x0e00, 0x0200, CRC(f0b00634),SHA1(317aacc9022596a2af0f3b399fe119fe9c8c1679) )
	ROM_END(); }}; 
	
	
	public static GameDriver driver_embargo	   = new GameDriver("1977"	,"embargo"	,"embargo.java"	,rom_embargo,null	,machine_driver_embargo	,input_ports_embargo	,null	,ROT0	,	"Cinematronics", "Embargo", GAME_NO_SOUND )
}
