/*************************************************************************

	Sega Pengo

**************************************************************************/

/*----------- defined in vidhrdw/pengo.c -----------*/

PALETTE_INIT( pacman );
PALETTE_INIT( pengo );

VIDEO_START( pacman );
VIDEO_START( pengo );


VIDEO_UPDATE( pengo );

VIDEO_UPDATE( vanvan );

extern data8_t *sprite_bank, *tiles_bankram;
VIDEO_START( s2650games );
VIDEO_UPDATE( s2650games );
