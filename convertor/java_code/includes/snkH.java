/*************************************************************************

	various SNK triple Z80 games

*************************************************************************/

/*----------- defined in drivers/snk.c -----------*/

extern INTERRUPT_GEN( snk_irq_AB );
extern INTERRUPT_GEN( snk_irq_BA );

extern READ_HANDLER ( snk_cpuA_nmi_trigger_r );
extern 
extern READ_HANDLER ( snk_cpuB_nmi_trigger_r );
extern 


/*----------- defined in vidhrdw/snk.c -----------*/

extern PALETTE_INIT( snk_3bpp_shadow );
extern PALETTE_INIT( snk_4bpp_shadow );

extern VIDEO_START( snk );
extern VIDEO_START( sgladiat );

extern VIDEO_UPDATE( tnk3 );
extern VIDEO_UPDATE( ikari );
extern VIDEO_UPDATE( tdfever );
extern VIDEO_UPDATE( gwar );
extern VIDEO_UPDATE( sgladiat );



// note: compare tdfever which does blinking in software with tdfeverj which does it in hardware


/*----------- defined in drivers/hal21.c -----------*/

extern PALETTE_INIT( aso );
