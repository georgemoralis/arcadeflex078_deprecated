/**
 * @file memdbg.h
 */
#ifndef MEMDBG_H
#define MEMDBG_H

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package mame;

public class memdbgH
{
	
	/* protypes for MALLOC, FREE macros.
	 *
	 * code using memdbg.c for tracing should always use MALLOC, FREE instead of direct calls
	 * to malloc, free.
	 */
	void *MALLOC( size_t lSize );
	void FREE( void *pMem );
	
	/**
	 * dumps a list of all current allocations, with module, line number, and allocation size
	 */
	
	#if defined(USE_MEMDBG)
	/* memory tracking is enabled; reroute the MALLOC, FREE macros to the appropriate functions
	 * in memdbg.c
	 */
				#define MALLOC( _SIZE ) memdbg_Alloc( (_SIZE) ,__FILE__, __LINE__)
		#define FREE( _PTR ) memdbg_Free( (_PTR) )
	#else
	/* memory tracking is not enabled; just route MALLOC, FREE macros directly to the core
	 * malloc, free routines.
	 */
		#define MALLOC( _SIZE ) malloc( (_SIZE) )
		#define FREE( _PTR ) free( (_PTR) )
	#endif /* defined(MEMDBG) */
	
	#endif /* MEMDBG_H */
}
