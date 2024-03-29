/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package arcadeflex.v078.drivers;

import static arcadeflex.v078.mame.commonH.REGION_CPU1;
import static arcadeflex.v078.mame.commonH.ROM_END;
import static arcadeflex.v078.mame.commonH.ROM_LOAD;
import static arcadeflex.v078.mame.commonH.ROM_REGION;
import static arcadeflex.v078.mame.driverH.*;
import static common.FuncPtr.*;

public class minivadr
{
/*TODO*///	
/*TODO*///	
/*TODO*///	VIDEO_UPDATE( minivadr );
/*TODO*///	PALETTE_INIT( minivadr );
/*TODO*///	
/*TODO*///	
/*TODO*///	public static Memory_ReadAddress readmem[]={
/*TODO*///		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
/*TODO*///		new Memory_ReadAddress( 0x0000, 0x1fff, MRA_ROM ),
/*TODO*///		new Memory_ReadAddress( 0xa000, 0xbfff, MRA_RAM ),
/*TODO*///		new Memory_ReadAddress( 0xe008, 0xe008, input_port_0_r ),
/*TODO*///		new Memory_ReadAddress(MEMPORT_MARKER, 0)
/*TODO*///	};
/*TODO*///	
/*TODO*///	public static Memory_WriteAddress writemem[]={
/*TODO*///		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
/*TODO*///		new Memory_WriteAddress( 0x0000, 0x1fff, MWA_ROM ),
/*TODO*///		new Memory_WriteAddress( 0xa000, 0xbfff, minivadr_videoram_w, videoram, videoram_size ),
/*TODO*///		new Memory_WriteAddress( 0xe008, 0xe008, MWA_NOP ),		// ???
/*TODO*///		new Memory_WriteAddress(MEMPORT_MARKER, 0)
/*TODO*///	};
	

	static InputPortPtr input_ports_minivadr = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	INPUT_PORTS_END(); 
        }}; 


        public static MachinePtr machine_driver_minivadr = new MachinePtr() {
            @Override
            public void handler(InternalMachineDriver machine) {
                //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
                /*TODO*///	static MACHINE_DRIVER_START( minivadr )
/*TODO*///	
/*TODO*///		/* basic machine hardware */
/*TODO*///		MDRV_CPU_ADD(Z80,24000000 / 6)		 /* 4 MHz ? */
/*TODO*///		MDRV_CPU_MEMORY(readmem,writemem)
/*TODO*///		MDRV_CPU_VBLANK_INT(irq0_line_hold,1)
	
		MDRV_FRAMES_PER_SECOND(60);
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION);
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER);
		MDRV_SCREEN_SIZE(256, 256);
		MDRV_VISIBLE_AREA(0, 256-1, 16, 240-1);
		MDRV_PALETTE_LENGTH(2);
	
		MDRV_PALETTE_INIT("minivadr");
/*TODO*///		MDRV_VIDEO_START(generic)
/*TODO*///		MDRV_VIDEO_UPDATE(minivadr)
/*TODO*///	
/*TODO*///		/* sound hardware */
/*TODO*///	MACHINE_DRIVER_END
            }
        };
                

	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_minivadr = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 );/* 64k for code */
		ROM_LOAD( "d26-01.bin",	0x0000, 0x2000, "CRC(a96c823d),SHA1(aa9969ff80e94b0fff0f3530863f6b300510162e)");
	ROM_END(); 
    }}; 
	
	
	public static GameDriver driver_minivadr	   = new GameDriver("1990"	,"minivadr"	,"minivadr.java"	,rom_minivadr,null	,machine_driver_minivadr	,input_ports_minivadr	,null	,ROT0	,	"Taito Corporation", "Minivader" );
}
