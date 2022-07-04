/*********************************************************/
/*    ricoh RF5C68(or clone) PCM controller              */
/*********************************************************/
#ifndef __RF5C68_H__
#define __RF5C68_H__

struct RF5C68interface
{
	int clock;
	int volume;
};

int RF5C68_sh_start( const struct MachineSound *msound );


#endif
