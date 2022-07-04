/***************************************************************************

	PSX SPU

	preliminary version by smf.

***************************************************************************/

#if !defined( PSX_SPU_H )

READ32_HANDLER( psx_spu_r );
READ32_HANDLER( psx_spu_delay_r );
WRITE32_HANDLER( psx_spu_w );

struct PSXSPUinterface
{
	int num;
	int baseclock;
};

#define PSX_SPU_H ( 1 )
#endif
