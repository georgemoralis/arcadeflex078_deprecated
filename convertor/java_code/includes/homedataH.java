extern data8_t *homedata_vreg;
extern data8_t reikaids_which;


PALETTE_INIT( mrokumei );
PALETTE_INIT( reikaids );
PALETTE_INIT( pteacher );

VIDEO_START( mrokumei );
VIDEO_START( reikaids );
VIDEO_START( pteacher );
VIDEO_START( lemnangl );
VIDEO_UPDATE( mrokumei );
VIDEO_UPDATE( reikaids );
VIDEO_UPDATE( pteacher );
VIDEO_EOF( homedata );
